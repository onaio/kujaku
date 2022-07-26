package io.ona.kujaku.utils.helpers;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.TextUtils;

import com.mapbox.mapboxsdk.geometry.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import io.ona.kujaku.utils.config.KujakuConfig;
import io.ona.kujaku.utils.exceptions.InvalidMapBoxStyleException;

/**
 * Helps manipulate the contents of a MapBox Style object.
 * <p>
 * See:
 * https://www.mapbox.com/mapbox-gl-js/style-spec/
 * <p>
 * Created by Jason Rogena - jrogena@ona.io on 11/7/17.
 * Created by Ephraim Kigamba - ekigamba@ona.io on 11/7/17.
 */

public class MapBoxStyleHelper {

    public static final String TAG = MapBoxStyleHelper.class.getName();

    public static final String KEY_KUJAKU = "kujaku";
    private final JSONObject styleObject;
    private final KujakuConfig kujakuConfig;
    public static final String KEY_MAP_CENTER = "center";
    public static final String KEY_ROOT_ZOOM = "zoom";
    public static final String KEY_METADATA = "metadata";

    public MapBoxStyleHelper(JSONObject styleObject) throws JSONException, InvalidMapBoxStyleException {
        this.styleObject = styleObject;
        if (this.styleObject.has(KEY_METADATA)
                && this.styleObject.getJSONObject(KEY_METADATA).has(KEY_KUJAKU)) {
            this.kujakuConfig = new KujakuConfig(this.styleObject.getJSONObject(KEY_METADATA).getJSONObject(KEY_KUJAKU));
        }
        else {
            this.kujakuConfig = new KujakuConfig();
        }
    }

    public JSONObject build() throws InvalidMapBoxStyleException, JSONException {
        if (kujakuConfig.isValid()) {
            if (!styleObject.has(KEY_METADATA)) {
                styleObject.put(KEY_METADATA, new JSONObject());
            }
            styleObject.getJSONObject(KEY_METADATA).put(KEY_KUJAKU, kujakuConfig.toJsonObject());
        } else {
            throw new InvalidMapBoxStyleException("The Kujaku configuraiton in the MapBox style is incomplete");
        }

        return styleObject;
    }

    public boolean insertGeoJsonDataSource(@NonNull String sourceName, JSONObject geoJson, @NonNull String layerId)
            throws JSONException, InvalidMapBoxStyleException {
        if (styleObject != null && !TextUtils.isEmpty(sourceName)) {
            JSONObject styleSources = new JSONObject();
            if (styleObject.has("sources")) {
                styleSources = styleObject.getJSONObject("sources");
            }
            styleSources.put(sourceName, geoJson);
            styleObject.put("sources", styleSources);

            return linkDataSourceToLayer(sourceName, layerId, null);
        }

        return false;
    }

    public KujakuConfig getKujakuConfig() {
        return kujakuConfig;
    }

    public boolean isKujakuConfigPresent() throws JSONException {
        return this.styleObject.has(KEY_METADATA) && this.styleObject.getJSONObject(KEY_METADATA).has(KEY_KUJAKU);
    }

    /**
     * Links MapBox data-source to a pre-existing MapBox layer
     *
     * @param sourceName  Name of the MapBox datasource
     * @param layerId     Id of the MapBox layer
     * @param sourceLayer Layer on the vector tile source to use
     * @return {@code TRUE} if able to associate the data-source with the layer
     * @throws JSONException               If unable to parse the style object
     * @throws InvalidMapBoxStyleException If the style object is missing required components
     */
    public boolean linkDataSourceToLayer(@NonNull String sourceName, @NonNull String layerId, @Nullable String sourceLayer)
            throws JSONException, InvalidMapBoxStyleException {
        if (!TextUtils.isEmpty(sourceName) && !TextUtils.isEmpty(layerId) && styleObject != null) {
            if (styleObject.has("layers")) {
                JSONArray layers = styleObject.getJSONArray("layers");
                for (int i = 0; i < layers.length(); i++) {
                    JSONObject currentLayer = layers.getJSONObject(i);
                    if (currentLayer.has("id") && !currentLayer.getString("id").isEmpty()) {
                        if (layerId.equals(currentLayer.getString("id"))) {
                            currentLayer.put("source", sourceName);
                            if (TextUtils.isEmpty(sourceLayer) && currentLayer.has("source-layer")) {
                                currentLayer.remove("source-layer");
                            } else if (!TextUtils.isEmpty(sourceLayer)) {
                                currentLayer.put("source-layer", sourceLayer);
                            }

                            return true;
                        }
                    } else {
                        throw new InvalidMapBoxStyleException("Layer with no Id");
                    }
                }
            } else {
                throw new InvalidMapBoxStyleException("Provided style does not have layers");
            }
        }

        return false;
    }

    /**
     * Makes layers invisible by changing the visibility property of the layer. This is useful
     * where layers in a style have dummy data which is not going to be replaced
     *
     * @param layerIds String array of <a href="https://www.mapbox.com/mapbox-gl-js/style-spec/#layer-id">layerIds</a>
     * @return
     * @throws JSONException
     * @throws InvalidMapBoxStyleException If the style object is missing required components
     */
    public boolean disableLayers(@Nullable String[] layerIds)
            throws JSONException, InvalidMapBoxStyleException {
        if (layerIds == null || layerIds.length < 1 || styleObject == null) {
            return false;
        }

        ArrayList<String> layerIdsList = new ArrayList<>(Arrays.asList(layerIds));

        if (styleObject.has("layers")) {
            JSONArray layers = styleObject.getJSONArray("layers");
            for (int i = 0; i < layers.length(); i++) {
                JSONObject currentLayer = layers.getJSONObject(i);
                if (currentLayer.has("id") && !currentLayer.getString("id").isEmpty()) {
                    for (Iterator<String> layerIdIterator = layerIdsList.iterator(); layerIdIterator.hasNext(); ) {
                        String layerId = layerIdIterator.next();
                        if (layerId.equals(currentLayer.getString("id"))) {
                            JSONObject layout = new JSONObject();
                            layout.put("visibility", "none");
                            currentLayer.put("layout", layout);

                            layerIdIterator.remove();
                            if (layerIdsList.isEmpty()) {
                                return true;
                            }
                        }
                    }
                } else {
                    throw new InvalidMapBoxStyleException("Layer with no Id");
                }
            }
        }

        return true;
    }

    /**
     * Sets the <a href="https://www.mapbox.com/mapbox-gl-js/style-spec/#root-center">root center</a>
     * property of a map to the center of the bounds given
     *
     * @see MapBoxStyleHelper#setMapCenter(LatLng)
     *
     * @param topLeft
     * @param bottomRight
     * @throws JSONException
     */
    public void setMapCenter(@NonNull LatLng topLeft, @NonNull LatLng bottomRight) throws JSONException {
        setMapCenter(getCenterFromBounds(topLeft, bottomRight));
    }

    /**
     * Sets the <a href="https://www.mapbox.com/mapbox-gl-js/style-spec/#root-center">root center</a>
     * property of a map to the center of the {@link LatLng} given
     *
     * @see MapBoxStyleHelper#setMapCenter(LatLng, LatLng)
     *
     * @param mapCenter
     * @throws JSONException
     */
    public void setMapCenter(@NonNull LatLng mapCenter) throws JSONException {
        JSONArray jsonArray = new JSONArray();
        jsonArray.put(mapCenter.getLongitude());
        jsonArray.put(mapCenter.getLatitude());

        styleObject.put(MapBoxStyleHelper.KEY_MAP_CENTER, jsonArray);
    }

    /**
     * Removes the <a href="https://www.mapbox.com/mapbox-gl-js/style-spec/#root-center">root center</a>
     * property of a map from the Mapbox Style JSON if it exists
     */
    public void removeMapCenter() {
        styleObject.remove(KEY_MAP_CENTER);
    }

    /*
     * Updates or adds the root zoom property to the Mapbox Style
     *
     * @param zoom
     * @throws JSONException
     */
    public void setRootZoom(double zoom) throws JSONException {
        styleObject.put(KEY_ROOT_ZOOM, zoom);
    }

    public JSONObject getStyleObject() {
        return styleObject;
    }

    public static LatLng getCenterFromBounds(LatLng topLeft, LatLng bottomRight) {
        return new LatLng(
                (topLeft.getLatitude() + bottomRight.getLatitude())/2,
                (bottomRight.getLongitude() + topLeft.getLongitude())/2
        );
    }
}
