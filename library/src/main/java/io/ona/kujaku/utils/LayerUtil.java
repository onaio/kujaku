package io.ona.kujaku.utils;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.mapbox.mapboxsdk.style.expressions.Expression;
import com.mapbox.mapboxsdk.style.layers.BackgroundLayer;
import com.mapbox.mapboxsdk.style.layers.CircleLayer;
import com.mapbox.mapboxsdk.style.layers.ExposedLayoutPropertyValue;
import com.mapbox.mapboxsdk.style.layers.ExposedPaintPropertyValue;
import com.mapbox.mapboxsdk.style.layers.FillExtrusionLayer;
import com.mapbox.mapboxsdk.style.layers.FillLayer;
import com.mapbox.mapboxsdk.style.layers.HeatmapLayer;
import com.mapbox.mapboxsdk.style.layers.HillshadeLayer;
import com.mapbox.mapboxsdk.style.layers.Layer;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.layers.PropertyValue;
import com.mapbox.mapboxsdk.style.layers.RasterLayer;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.Source;
import com.mapbox.mapboxsdk.style.sources.VectorSource;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

import timber.log.Timber;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 2019-05-20
 */

public class LayerUtil {

    public static final String TYPE = "type";
    public static final String ID = "id";
    public static final String SOURCE = "source";
    public static final String SOURCE_LAYER = "source-layer";
    public static final String MINZOOM = "minzoom";
    public static final String MAXZOOM = "maxzoom";
    public static final String FILTER = "filter";
    public static final String LAYOUT = "layout";
    public static final String PAINT = "paint";

    public static final String URL = "url";

    public static final class LAYER_TYPE {
        public static final String FILL = "fill";
        public static final String LINE = "line";
        public static final String SYMBOL = "symbol";
        public static final String CIRCLE = "circle";
        public static final String HEATMAP = "heatmap";
        public static final String FILL_EXTRUSION = "fill-extrusion";
        public static final String RASTER = "raster";
        public static final String HILLSHADE = "hillshade";
        public static final String BACKGROUND = "background";
    }

    public static final class SOURCE_TYPE {
        public static final String VECTOR = "vector";
        public static final String RASTER = "raster";
        public static final String RASTER_DEM = "raster-dem";
        public static final String GEOJSON = "geojson";
        public static final String IMAGE = "image";
        public static final String VIDEO = "video";
    }

    @Nullable
    public Layer getLayer(@NonNull String layerJSON) {
        Layer layer = null;
        try {
            JSONObject jsonObject = new JSONObject(layerJSON);
            String layerType = jsonObject.getString(TYPE);
            String layerId = jsonObject.getString(ID);
            String source = jsonObject.optString(SOURCE);
            String sourceLayer = jsonObject.optString(SOURCE_LAYER);
            String filter = jsonObject.optString(FILTER);
            Expression filterExpression = null;
            ArrayList<PropertyValue> propertyValues = new ArrayList<>();

            if (!jsonObject.optString(LAYOUT).equals("")) {
                JSONObject layoutObject = jsonObject.optJSONObject(LAYOUT);

                Iterator<String> keys = layoutObject.keys();
                while (keys.hasNext()) {
                    String key = keys.next();
                    Object value = layoutObject.opt(key);

                    if (value instanceof JSONArray) {
                        // Check if this is an array of numbers
                        JSONArray jsonArray = (JSONArray) value;

                        if (isJSONArrayOfNumbers(jsonArray)) {
                            if (key.equals("text-offset")) {
                                Float[] lineDashArrayValue = new Float[jsonArray.length()];

                                for (int i = 0; i < jsonArray.length(); i++) {
                                    lineDashArrayValue[i] = Double.valueOf(jsonArray.getDouble(i)).floatValue();
                                }

                                value = lineDashArrayValue;
                            }
                        } else {
                            value = Expression.raw(value.toString());
                        }
                    } else if (value instanceof JSONObject) {
                        value = value.toString();
                    }

                    if (value != null) {
                        propertyValues.add(new ExposedLayoutPropertyValue<>(key, value));
                    }

                }
            }

            if (!jsonObject.optString(PAINT).equals("")) {
                JSONObject paintObject = jsonObject.optJSONObject(PAINT);

                Iterator<String> keys = paintObject.keys();
                while (keys.hasNext()) {
                    String key = keys.next();
                    Object value = paintObject.opt(key);

                    if (value instanceof JSONArray) {
                        // Check if this is an array of numbers
                        JSONArray jsonArray = (JSONArray) value;

                        if (isJSONArrayOfNumbers(jsonArray)) {
                            if (key.equals("line-dasharray") || key.equals("line-translate") || key.equals("text-translate")) {
                                Float[] lineDashArrayValue = new Float[jsonArray.length()];

                                for (int i = 0; i < jsonArray.length(); i++) {
                                    lineDashArrayValue[i] = Double.valueOf(jsonArray.getDouble(i)).floatValue();
                                }

                                value = lineDashArrayValue;
                            }
                        } else {
                            value = Expression.raw(value.toString());
                        }
                    } else if (value instanceof JSONObject) {
                        value = value.toString();
                    }

                    if (value != null) {
                        propertyValues.add(new ExposedPaintPropertyValue<>(key, value));
                    }

                }
            }

            if (!TextUtils.isEmpty(filter)) {
                filterExpression = Expression.raw(filter);
            }

            if (layerType.equals(LAYER_TYPE.RASTER)) {
                layer = new RasterLayer(layerId, source);
                if (!TextUtils.isEmpty(sourceLayer)) {
                    ((RasterLayer) layer).setSourceLayer(sourceLayer);
                }
            } else if (layerType.equals(LAYER_TYPE.FILL)) {
                layer = new FillLayer(layerId, source);
                if (!TextUtils.isEmpty(sourceLayer)) {
                    ((FillLayer) layer).setSourceLayer(sourceLayer);
                }

                if (filterExpression != null) {
                    ((FillLayer) layer).setFilter(filterExpression);
                }
            } else if (layerType.equals(LAYER_TYPE.LINE)) {
                layer = new LineLayer(layerId, source);
                if (!TextUtils.isEmpty(sourceLayer)) {
                    ((LineLayer) layer).setSourceLayer(sourceLayer);
                }

                if (filterExpression != null) {
                    ((LineLayer) layer).setFilter(filterExpression);
                }
            } else if (layerType.equals(LAYER_TYPE.SYMBOL)) {
                layer = new SymbolLayer(layerId, source);
                if (!TextUtils.isEmpty(sourceLayer)) {
                    ((SymbolLayer) layer).setSourceLayer(sourceLayer);
                }

                if (filterExpression != null) {
                    ((SymbolLayer) layer).setFilter(filterExpression);
                }
            } else if (layerType.equals(LAYER_TYPE.CIRCLE)) {
                layer = new CircleLayer(layerId, source);
                if (!TextUtils.isEmpty(sourceLayer)) {
                    ((CircleLayer) layer).setSourceLayer(sourceLayer);
                }

                if (filterExpression != null) {
                    ((CircleLayer) layer).setFilter(filterExpression);
                }
            } else if (layerType.equals(LAYER_TYPE.HEATMAP)) {
                layer = new HeatmapLayer(layerId, source);
                if (!TextUtils.isEmpty(sourceLayer)) {
                    ((HeatmapLayer) layer).setSourceLayer(sourceLayer);
                }

                if (filterExpression != null) {
                    ((HeatmapLayer) layer).setFilter(filterExpression);
                }
            } else if (layerType.equals(LAYER_TYPE.FILL_EXTRUSION)) {
                layer = new FillExtrusionLayer(layerId, source);
                if (!TextUtils.isEmpty(sourceLayer)) {
                    ((FillExtrusionLayer) layer).setSourceLayer(sourceLayer);
                }

                if (filterExpression != null) {
                    ((FillExtrusionLayer) layer).setFilter(filterExpression);
                }
            } else if (layerType.equals(LAYER_TYPE.HILLSHADE)) {
                layer = new HillshadeLayer(layerId, source);
                if (!TextUtils.isEmpty(sourceLayer)) {
                    ((HillshadeLayer) layer).setSourceLayer(sourceLayer);
                }
            } else if (layerType.equals(LAYER_TYPE.BACKGROUND)) {
                layer = new BackgroundLayer(layerId);
            }

            if (layer != null) {
                layer.setProperties(propertyValues.toArray(new PropertyValue[0]));

                if (jsonObject.has(MINZOOM)) {
                    layer.setMinZoom(((Double) jsonObject.getDouble(MINZOOM)).floatValue());
                }

                if (jsonObject.has(MAXZOOM)) {
                    layer.setMaxZoom(((Double) jsonObject.getDouble(MAXZOOM)).floatValue());
                }
            }
        } catch (JSONException e) {
            Timber.e(e);
            return null;
        }

        return layer;
    }

    private boolean isJSONArrayOfNumbers(@NonNull JSONArray jsonArray) {
        if (jsonArray.length() > 1) {
            return !Double.isNaN(jsonArray.optDouble(0)) && !Double.isNaN(jsonArray.optDouble(1));
        } else {
            return !Double.isNaN(jsonArray.optDouble(0));
        }
    }

    @Nullable
    public Source getSource(@NonNull String sourceId, @NonNull String sourceJson) {
        Source source = null;
        try {
            JSONObject jsonObject = new JSONObject(sourceJson);
            String sourceType = jsonObject.getString(TYPE);
            String url = jsonObject.optString(URL);

            if (sourceType.equals(SOURCE_TYPE.VECTOR)) {
                VectorSource vectorSource;
                //TileSet tileSet = new TileSet()
                if (url != null) {
                    vectorSource = new VectorSource(sourceId, url);
                } else {

                }
            }

        } catch (JSONException e) {
            Timber.e(e);
            return null;
        }

        return source;
    }

    protected Object convertJsonArray(@NonNull JsonArray jsonArray) {

        /*for (int i = 1; i < jsonArray.size(); i++) {
            JsonElement jsonElement = jsonArray.get(i);
            if (jsonElement instanceof JsonArray) {
                JsonArray nestedArray = (JsonArray) jsonElement;
                Object[] array = new Object[nestedArray.size()];
                for (int j = 0; j < nestedArray.size(); j++) {
                    JsonElement element = nestedArray.get(j);
                    if (element instanceof JsonPrimitive) {
                        array[j] = convertToValue((JsonPrimitive) element);
                    } else {
                        throw new IllegalArgumentException("Nested literal arrays are not supported.");
                    }
                }

                arguments.add(new Expression.ExpressionLiteralArray(array));
            } else {
                arguments.add(convert(jsonElement));
            }
        }*/
        return null;
    }

    @NonNull
    protected Object convertToValue(@NonNull JsonPrimitive jsonPrimitive) {
        if (jsonPrimitive.isBoolean()) {
            return jsonPrimitive.getAsBoolean();
        } else if (jsonPrimitive.isNumber()) {
            return jsonPrimitive.getAsFloat();
        } else if (jsonPrimitive.isString()) {
            return jsonPrimitive.getAsString();
        } else {
            return jsonPrimitive;
        }
    }
}
