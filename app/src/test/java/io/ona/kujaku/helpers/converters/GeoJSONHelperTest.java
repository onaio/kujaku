package io.ona.kujaku.helpers.converters;

import com.mapbox.mapboxsdk.BuildConfig;
import com.mapbox.mapboxsdk.geometry.LatLng;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;

/**
 * Created by Ephraim Kigamba on 06/11/2017.
 */

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, manifest = Config.NONE)
public class GeoJSONHelperTest {

    @Test
    public void getFeatureShouldReturnPointFeatureWithoutProperties() {
        // Might fail bcoz of Feature collection positioning
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

            //todo - Make this better
            assertEquals(expectedJsonObject.toString(), actualJsonObject.toString());

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void getFeatureShouldReturnMultiPointFeatureWithoutProperties() {

    }


    @Test
    public void getFeatureShouldReturnPointFeatureWithProperties() {}

    @Test
    public void getFeatureShouldReturnMultiPointFeatuerWithProperties() {}
}
