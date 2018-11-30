package io.ona.kujaku.sample.utils;

import android.util.Log;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.mapboxsdk.style.layers.CircleLayer;
import com.mapbox.mapboxsdk.style.layers.Layer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

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

    public static Layer generateMapBoxLayer(String layerId, String sourceId) {

            CircleLayer circleLayer = new CircleLayer(layerId, sourceId);

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
            return circleLayer;
    }

    public static JSONArray createFeatureJsonArray(int numFeatures, double longitude, double latitude, String propertyName, final String[] featureGroup) throws JSONException {

        final double LAMBDA = 0.0001;

        double longitudeOffset;
        double latitudeOffset;
        double newLongitude = longitude;
        double newLatitude = latitude;

        int featureNumber = 0;
        int prevFeatureNumber = -1;

        JSONArray featuresArray = new JSONArray();
        final int FEATURE_GROUP_SIZE = featureGroup.length;
        while (featureNumber < numFeatures) {
            if (prevFeatureNumber != featureNumber) {
                JSONObject feature = new JSONObject();
                feature.put("id", "feature_" + featureNumber);
                feature.put("type", "Feature");

                int featureIndex = (int) (Math.random() * FEATURE_GROUP_SIZE);
                String featureValue = featureGroup[featureIndex];
                JSONObject properties = new JSONObject();
                properties.put(propertyName, featureValue);
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

    public static List<Feature> createFeatureList(int numFeatures, int startingIndex, double longitude, double latitude, String propertyName, final String[] featureGroup) throws JSONException {

        final double LAMBDA = 0.9;

        double longitudeOffset;
        double latitudeOffset;
        double newLongitude = longitude;
        double newLatitude = latitude;

        int featureNumber = startingIndex;
        int prevFeatureNumber = startingIndex;

        List<Feature> features = new ArrayList<>();
        final int FEATURE_GROUP_SIZE = featureGroup.length;
        while (featureNumber < numFeatures + startingIndex) {
            if (prevFeatureNumber != featureNumber) {
                JSONObject feature = new JSONObject();
                feature.put("id", "feature_" + featureNumber);
                feature.put("type", "Feature");

                int featureIndex = (int) (Math.random() * FEATURE_GROUP_SIZE);
                String featureValue = featureGroup[featureIndex];
                JSONObject properties = new JSONObject();
                properties.put(propertyName, featureValue);
                feature.put("properties", properties);

                JSONObject geometry = new JSONObject();
                geometry.put("type", "Point");
                JSONArray coordinates = new JSONArray();
                coordinates.put(newLongitude);
                coordinates.put(newLatitude);
                geometry.put("coordinates", coordinates);

                feature.put("geometry", geometry);

                features.add(com.mapbox.geojson.Feature.fromJson(feature.toString()));
            }
            // housekeeping
            longitudeOffset = Math.random() * 3;
            latitudeOffset = Math.random() * 3;
            if (longitudeOffset >= LAMBDA && latitudeOffset >= LAMBDA) {
                prevFeatureNumber = featureNumber;
                featureNumber++;
                newLongitude += longitudeOffset;
                newLatitude += latitudeOffset;
            }
        }
        return features;
    }


    public static FeatureCollection alterFeatureJsonProperties(int numFeatures, JSONObject featureCollection, String propertyName, final String[] featureGroup) throws JSONException {
        JSONArray featuresArray;
        final int FEATURE_GROUP_SIZE = featureGroup.length;
        if (featureCollection.getJSONArray("features").length() == 0) {
            // initial initialization
            featuresArray = createFeatureJsonArray(10000, 36.000000, -1.000000, propertyName, featureGroup);
            Log.i(TAG, "Features array size is: " + featuresArray.length());
            // Create and set GeoJsonSource
            featureCollection.put("type", "FeatureCollection");
            featureCollection.put("features", featuresArray);
        } else {
            // modify properties
            featuresArray = featureCollection.getJSONArray("features");
            int featuresSize = featuresArray.length();
            for (int i = 0; i < numFeatures; i++) {
                int featurePropertyValueIndex = (int) (Math.random() * FEATURE_GROUP_SIZE);
                String featurePropertyValue = featureGroup[featurePropertyValueIndex];
                int featureIndex = (int) (Math.random() * featuresSize);
                featuresArray.getJSONObject(featureIndex).getJSONObject("properties").put(propertyName, featurePropertyValue);
            }
            Log.i(TAG, "Features array size is: " + featuresArray.length());
        }
        return FeatureCollection.fromJson(featureCollection.toString());
    }

    public static void addFeaturePoints(int numFeaturePoints, JSONObject featureCollection, String propertyName, final String[] featureGroup)  throws JSONException {
        JSONArray featuresArray = createFeatureJsonArray(numFeaturePoints, 36.795538, -1.294638, propertyName, featureGroup);
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
