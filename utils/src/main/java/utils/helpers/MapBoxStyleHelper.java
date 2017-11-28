package utils.helpers;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.mapbox.services.commons.utils.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import utils.exceptions.InvalidMapBoxStyleException;

/**
 * Helps manipulate the contents of a MapBox Style object.
 * <p>
 * See:
 * https://www.mapbox.com/mapbox-gl-js/style-spec/
 * <p>
 * Created by Jason Rogena - jrogena@ona.io on 11/7/17.
 * Created by Ephraim Kigamba - ekigamba@ona.io on 11/7/17/
 */

public class MapBoxStyleHelper {
    private final JSONObject styleObject;

    public MapBoxStyleHelper(JSONObject styleObject) {
        this.styleObject = styleObject;
    }

    /**
     * Adds Kujaku's configuration to the MapBox style's metadata object
     * 
     * @param kujakuConfig
     * @return
     * @throws JSONException
     */
    public boolean insertKujakuConfig(@NonNull JSONObject kujakuConfig) throws JSONException {
        if (styleObject != null) {
            if (!styleObject.has("metadata")) {
                styleObject.put("metadata", new JSONObject());
            }

            styleObject.getJSONObject("metadata").put("kujaku", kujakuConfig);

            return true;
        }

        return false;
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

    /**
     * Links MapBox data-source to a pre-existing MapBox layer
     *
     * @param sourceName    Name of the MapBox datasource
     * @param layerId       Id of the MapBox layer
     * @param sourceLayer   Layer on the vector tile source to use
     * @return  {@code TRUE} if able to associate the data-source with the layer
     * @throws JSONException                If unable to parse the style object
     * @throws InvalidMapBoxStyleException  If the style object is missing required components
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
            throws JSONException, InvalidMapBoxStyleException{
        if (layerIds == null || layerIds.length < 1 || styleObject == null) {
            return false;
        }

        ArrayList<String> layerIdsList = new ArrayList<>(Arrays.asList(layerIds));

        if (styleObject.has("layers")) {
            JSONArray layers = styleObject.getJSONArray("layers");
            for (int i = 0; i < layers.length(); i++) {
                JSONObject currentLayer = layers.getJSONObject(i);
                if (currentLayer.has("id") && !currentLayer.getString("id").isEmpty()) {
                    for (Iterator<String> layerIdIterator = layerIdsList.iterator(); layerIdIterator.hasNext();) {
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

    public JSONObject getStyleObject() {
        return styleObject;
    }
}
