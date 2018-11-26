package io.ona.kujaku.sample.utils;

import android.util.Log;

import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.style.layers.CircleLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static com.mapbox.mapboxsdk.style.expressions.Expression.exponential;
import static com.mapbox.mapboxsdk.style.expressions.Expression.get;
import static com.mapbox.mapboxsdk.style.expressions.Expression.interpolate;
import static com.mapbox.mapboxsdk.style.expressions.Expression.match;
import static com.mapbox.mapboxsdk.style.expressions.Expression.rgb;
import static com.mapbox.mapboxsdk.style.expressions.Expression.stop;
import static com.mapbox.mapboxsdk.style.expressions.Expression.zoom;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleRadius;

/**
 * @author Vincent Karuri
 */
public class TestDataUtils {

    private static final String TAG = TestDataUtils.class.getName();

    private enum FeatureGroup {White, Black, Hispanic, Asian, Other};
    private final static int FEATURE_GROUP_SIZE = FeatureGroup.values().length;

    public static void setGeoJSONSource(MapboxMap mapboxMap, JSONObject featureCollection) throws JSONException {

        JSONArray featuresArray = new JSONArray();
        Log.i(TAG, "Features array size is: " + featuresArray.length());

        // Create and set GeoJsonSource
        featureCollection.put("type", "FeatureCollection");
        featureCollection.put("features", featuresArray);

        GeoJsonSource geoJsonSource = new GeoJsonSource("ethnicity-source", featureCollection.toString());
        mapboxMap.addSource(geoJsonSource);
    }

    public static void addMapBoxLayer(MapboxMap mapboxMap) {

        CircleLayer circleLayer = new CircleLayer("population", "ethnicity-source");

        circleLayer.setSourceLayer("sf2010");
        circleLayer.withProperties(
                circleRadius(
                        interpolate(
                                exponential(1.75f),
                                zoom(),
                                stop(12, 2f),
                                stop(22, 180f)
                        )),
                circleColor(
                        match(get("ethnicity"), rgb(0, 0, 0),
                                stop("White", rgb(251, 176, 59)),
                                stop("Black", rgb(34, 59, 83)),
                                stop("Hispanic", rgb(229, 94, 94)),
                                stop("Asian", rgb(59, 178, 208)),
                                stop("Other", rgb(204, 204, 204)))));

        mapboxMap.addLayer(circleLayer);
    }

    public static JSONArray createFeatureJsonArray(int numFeatures, double longitude, double latitude) throws JSONException {

        final double LAMBDA = 0.0001;

        double longitudeOffset;
        double latitudeOffset;
        double newLongitude = longitude;
        double newLatitude = latitude;

        int featureNumber = 0;
        int prevFeatureNumber = -1;

        JSONArray featuresArray = new JSONArray();
        while (featureNumber < numFeatures) {
            if (prevFeatureNumber != featureNumber) {
                JSONObject feature = new JSONObject();
                feature.put("id", "feature_" + featureNumber);
                feature.put("type", "Feature");

                int featureIndex = (int) (Math.random() * FEATURE_GROUP_SIZE);
                String featureValue = FeatureGroup.values()[featureIndex].toString();
                JSONObject properties = new JSONObject();
                properties.put("ethnicity", featureValue);
                feature.put("properties", properties);

                JSONObject geometry = new JSONObject();
                geometry.put("type", "Point");
                JSONArray coordinates = new JSONArray();
                coordinates.put(newLongitude);
                coordinates.put(newLatitude);
                geometry.put("coordinates", coordinates);

                feature.put("geometry", geometry);

                featuresArray.put(feature);
            }
            // housekeeping
            longitudeOffset = Math.random();
            latitudeOffset = Math.random();
            if (longitudeOffset >= LAMBDA || latitudeOffset >= LAMBDA) {
                featureNumber++;
                newLongitude += longitudeOffset;
                newLatitude += latitudeOffset;
            }
        }
        return featuresArray;
    }

    public static void alterFeatureJsonProperties(JSONObject featureCollection) throws JSONException {

        if (featureCollection.getJSONArray("features").length() == 0) {
            // initial initialization
            JSONArray featuresArray = createFeatureJsonArray(10000, 36.000000, -1.000000);
            Log.i(TAG, "Features array size is: " + featuresArray.length());
            // Create and set GeoJsonSource
            featureCollection.put("type", "FeatureCollection");
            featureCollection.put("features", featuresArray);
        } else {
            // modify properties
            JSONArray featuresArray = featureCollection.getJSONArray("features");
            int featuresSize = featuresArray.length();
            int featuresSampleSize = featuresSize / 100;
            for (int i = 0; i < featuresSampleSize; i++) {
                int featurePropertyValueIndex = (int) (Math.random() * FEATURE_GROUP_SIZE);
                String featurePropertyValue = FeatureGroup.values()[featurePropertyValueIndex].toString();
                int featureIndex = (int) (Math.random() * featuresSize);
                featuresArray.getJSONObject(featureIndex).getJSONObject("properties").put("ethnicity", featurePropertyValue);
            }
            Log.i(TAG, "Features array size is: " + featuresArray.length());
        }
    }

    public static void addFeaturePoints(int numFeaturePoints, JSONObject featureCollection)  throws JSONException {
        JSONArray featuresArray = createFeatureJsonArray(numFeaturePoints, 36.795538, -1.294638);
        Log.i(TAG, "Features array size is: " + featuresArray.length());

        JSONArray currFeaturesArray = featureCollection.getJSONArray("features");
        featuresArray = concatJSONArray(featuresArray, currFeaturesArray);
        // Create and set GeoJsonSource
        featureCollection.put("type", "FeatureCollection");
        featureCollection.put("features", featuresArray);
    }

    private static JSONArray concatJSONArray(JSONArray array1, JSONArray array2) throws JSONException {
        for (int i = 0; i < array2.length(); i++) {
            array1.put(array2.get(i));
        }
        return array1;
    }
}
