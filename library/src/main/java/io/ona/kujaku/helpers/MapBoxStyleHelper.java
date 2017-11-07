package io.ona.kujaku.helpers;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import io.ona.kujaku.exceptions.InvalidMapBoxStyleException;

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
     * @throws InvalidMapBoxStyleException  If the style object has missing required components
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

    public JSONObject getStyleObject() {
        return styleObject;
    }
}
