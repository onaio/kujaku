package io.ona.kujaku.helpers.converters;

import com.cocoahero.android.geojson.GeoJSON;
import com.mapbox.mapboxsdk.BuildConfig;
import com.mapbox.mapboxsdk.constants.GeometryConstants;
import com.mapbox.mapboxsdk.geometry.LatLng;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.skyscreamer.jsonassert.JSONAssert;

import java.util.ArrayList;
import java.util.UUID;

import io.ona.kujaku.utils.helpers.converters.GeoJSONFeature;
import io.ona.kujaku.utils.helpers.converters.GeoJSONHelper;

import static org.junit.Assert.assertEquals;

/**
 * Created by Ephraim Kigamba on 06/11/2017.
 */

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class GeoJSONHelperTest {

    @Test
    public void getFeatureShouldReturnPointFeatureWithoutProperties() {
        String geoJsonFeatureCollection = "{\n" +
                "  \"features\": [\n" +
                "      {\n" +
                "        \"type\": \"Feature\",\n" +
                "        \"properties\": {},\n" +
                "        \"geometry\": {\n" +
                "          \"coordinates\": [\n" +
                "            36.791183,\n" +
                "            -1.293522,\n" +
                "            0\n" +
                "          ],\n" +
                "          \"type\": \"Point\"\n" +
                "        },\n" +
                "        \"id\": \"b3369aa6b5022be641198d898bd20e47\"\n" +
                "      }\n" +
                "    ],\n" +
                "  \"type\": \"FeatureCollection\"\n" +
                "}";

        GeoJSONFeature geoJSONFeature = new GeoJSONFeature();
        geoJSONFeature.addPoint(new LatLng(-1.293522, 36.791183));
        geoJSONFeature.setId("b3369aa6b5022be641198d898bd20e47");

        GeoJSONHelper geoJSONHelper = new GeoJSONHelper(geoJSONFeature);

        try {
            JSONObject expectedJsonObject = new JSONObject(geoJsonFeatureCollection);
            JSONObject actualJsonObject = new JSONObject(geoJSONHelper.getJsonFeatureCollection());

            JSONAssert.assertEquals(expectedJsonObject.toString(), actualJsonObject.toString(), true);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void getGeoJsonShouldReturnGeoJsonWithPointFeatureWithoutProperties() {
        String geoJson = "{\n" +
                "  \"type\": \"geojson\",\n" +
                "  \"data\": {\n" +
                "    \"features\": [\n" +
                "        {\n" +
                "          \"type\": \"Feature\",\n" +
                "          \"properties\": {},\n" +
                "          \"geometry\": {\n" +
                "            \"coordinates\": [\n" +
                "              36.791183,\n" +
                "              -1.293522,\n" +
                "              0\n" +
                "            ],\n" +
                "            \"type\": \"Point\"\n" +
                "          },\n" +
                "          \"id\": \"b3369aa6b5022be641198d898bd20e47\"\n" +
                "        }\n" +
                "      ],\n" +
                "    \"type\": \"FeatureCollection\"\n" +
                "  }\n" +
                "}";

        GeoJSONFeature geoJSONFeature = new GeoJSONFeature();
        geoJSONFeature.addPoint(new LatLng(-1.293522, 36.791183));
        geoJSONFeature.setId("b3369aa6b5022be641198d898bd20e47");

        GeoJSONHelper geoJSONHelper = new GeoJSONHelper(geoJSONFeature);

        try {
            JSONObject expectedJsonObject = new JSONObject(geoJson);
            JSONObject actualJsonObject = new JSONObject(geoJSONHelper.getGeoJsonData());

            JSONAssert.assertEquals(expectedJsonObject.toString(), actualJsonObject.toString(), true);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void getFeatureShouldReturnMultiPointFeatureWithoutProperties() throws JSONException {
        int pointsLen = 5;
        ArrayList<LatLng> multiPoints = new ArrayList<>();

        for(int i = 0; i < pointsLen; i++) {
            multiPoints.add(generateLatLng());
        }

        GeoJSONFeature geoJSONFeature = new GeoJSONFeature(multiPoints);
        GeoJSONHelper geoJSONHelper = new GeoJSONHelper(geoJSONFeature);

        JSONObject jsonObject = new JSONObject(geoJSONHelper.getJsonFeatureCollection());


        assertEquals(GeoJSON.TYPE_FEATURE_COLLECTION, jsonObject.getString("type"));
        assertEquals(1, jsonObject.getJSONArray("features").length());
        assertEquals(GeoJSON.TYPE_MULTI_POINT, jsonObject.getJSONArray("features").getJSONObject(0).getJSONObject("geometry").getString("type"));
        assertEquals(0, jsonObject.getJSONArray("features").getJSONObject(0).getJSONObject("properties").length());
        assertEquals(pointsLen, jsonObject.getJSONArray("features").getJSONObject(0).getJSONObject("geometry").getJSONArray("coordinates").length());
    }

    @Test
    public void getFeatureShouldReturnPointFeatureWithProperties() throws JSONException {
        int propertiesLen = 5;
        ArrayList<LatLng> multiPoints = new ArrayList<>();
        multiPoints.add(generateLatLng());

        ArrayList<GeoJSONFeature.Property> multiProperties = new ArrayList<>();

        for(int i = 0; i < propertiesLen; i++) {
            multiProperties.add(new GeoJSONFeature.Property(generateRandomPropertyName(), generateRandomPropertyValue()));
        }

        GeoJSONFeature geoJSONFeature = new GeoJSONFeature(multiPoints, multiProperties);
        GeoJSONHelper geoJSONHelper = new GeoJSONHelper(geoJSONFeature);

        JSONObject jsonObject = new JSONObject(geoJSONHelper.getJsonFeatureCollection());


        assertEquals(GeoJSON.TYPE_FEATURE_COLLECTION, jsonObject.getString("type"));
        assertEquals(1, jsonObject.getJSONArray("features").length());
        assertEquals(GeoJSON.TYPE_POINT, jsonObject.getJSONArray("features").getJSONObject(0).getJSONObject("geometry").getString("type"));
        assertEquals(propertiesLen, jsonObject.getJSONArray("features").getJSONObject(0).getJSONObject("properties").length());
        assertEquals(3, jsonObject.getJSONArray("features").getJSONObject(0).getJSONObject("geometry").getJSONArray("coordinates").length());
    }

    @Test
    public void getFeatureShouldReturnMultiPointFeatureWithProperties() throws JSONException {
        int propertiesLen = 5;
        int pointsLen = 8;
        ArrayList<LatLng> multiPoints = new ArrayList<>();

        ArrayList<GeoJSONFeature.Property> multiProperties = new ArrayList<>();

        for(int i = 0; i < propertiesLen; i++) {
            multiProperties.add(new GeoJSONFeature.Property(generateRandomPropertyName(), generateRandomPropertyValue()));
        }

        for (int j = 0; j < pointsLen; j++) {
            multiPoints.add(generateLatLng());
        }

        GeoJSONFeature geoJSONFeature = new GeoJSONFeature(multiPoints, multiProperties);
        GeoJSONHelper geoJSONHelper = new GeoJSONHelper(geoJSONFeature);

        JSONObject jsonObject = new JSONObject(geoJSONHelper.getJsonFeatureCollection());


        assertEquals(GeoJSON.TYPE_FEATURE_COLLECTION, jsonObject.getString("type"));
        assertEquals(1, jsonObject.getJSONArray("features").length());
        assertEquals(GeoJSON.TYPE_MULTI_POINT, jsonObject.getJSONArray("features").getJSONObject(0).getJSONObject("geometry").getString("type"));
        assertEquals(propertiesLen, jsonObject.getJSONArray("features").getJSONObject(0).getJSONObject("properties").length());
        assertEquals(pointsLen, jsonObject.getJSONArray("features").getJSONObject(0).getJSONObject("geometry").getJSONArray("coordinates").length());
    }

    private LatLng generateLatLng() {
        return new LatLng(
                (Math.random() * (GeometryConstants.MAX_LATITUDE - GeometryConstants.MIN_LATITUDE)) + GeometryConstants.MIN_LATITUDE,
                (Math.random() * 340d) + GeometryConstants.MIN_LONGITUDE
        );
    }

    private String generateRandomString() {
        return UUID.randomUUID().toString();
    }

    private String generateRandomPropertyName() {
        return generateRandomString();
    }

    private String generateRandomPropertyValue() {
        return generateRandomString();
    }
}
