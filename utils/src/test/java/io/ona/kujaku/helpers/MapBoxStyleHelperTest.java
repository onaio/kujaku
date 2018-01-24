package io.ona.kujaku.helpers;

import com.mapbox.mapboxsdk.geometry.LatLng;

import junit.framework.Assert;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import io.ona.kujaku.utils.BuildConfig;
import utils.exceptions.InvalidMapBoxStyleException;
import utils.helpers.MapBoxStyleHelper;

/**
 * Created by Jason Rogena - jrogena@ona.io on 11/7/17.
 */

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, manifest = Config.NONE)
public class MapBoxStyleHelperTest {
    /**
     * Tests best case for adding GeoJson data source to MapBox style
     * 
     * @throws JSONException
     * @throws InvalidMapBoxStyleException
     */
    @Test
    public void insertGeoJsonDataSource() throws JSONException, InvalidMapBoxStyleException {
        JSONObject style = new JSONObject();
        style.put("layers", new JSONArray());
        JSONObject layer0 = new JSONObject();
        String layerId = "layer0";
        layer0.put("id", layerId);
        style.getJSONArray("layers").put(layer0);
        JSONObject layer1 = new JSONObject();
        layer1.put("id", "layer1");
        style.getJSONArray("layers").put(layer1);

        JSONObject geoJsonDataSource = getGeoJsonDataSource();
        String geoJsonSourceName = "geojson0";

        MapBoxStyleHelper styleHelper = new MapBoxStyleHelper(style);
        styleHelper.insertGeoJsonDataSource(geoJsonSourceName, geoJsonDataSource, layerId);
        JSONObject returnedData = styleHelper.getStyleObject();

        Assert.assertTrue(returnedData.has("sources"));
        Assert.assertTrue(returnedData.getJSONObject("sources").has(geoJsonSourceName));
        Assert.assertEquals(returnedData.getJSONObject("sources").getJSONObject(geoJsonSourceName).toString(),
                getGeoJsonDataSource().toString());
        Assert.assertTrue(returnedData.getJSONArray("layers").getJSONObject(0).has("source"));
        Assert.assertTrue(returnedData.getJSONArray("layers").getJSONObject(0).getString("source").equals(geoJsonSourceName));
    }

    @Test
    public void setMapCenterWhenGivenCenterPoint() throws JSONException {
        String zoomKey = MapBoxStyleHelper.KEY_MAP_CENTER;
        LatLng mapCenter = new LatLng(23.89454, 57.909234);
        String sampleStyle = getSampleMapboxStyle();
        JSONObject mapboxStyleJSONObject = new JSONObject(sampleStyle);

        MapBoxStyleHelper mapBoxStyleHelper = new MapBoxStyleHelper(mapboxStyleJSONObject);
        mapBoxStyleHelper.setMapCenter(mapCenter);

        JSONObject finalMapboxStyleJSONObject = mapBoxStyleHelper.getStyleObject();

        Assert.assertEquals(mapCenter.getLongitude(), finalMapboxStyleJSONObject.getJSONArray(zoomKey).getDouble(0), 0);
        Assert.assertEquals(mapCenter.getLatitude(), finalMapboxStyleJSONObject.getJSONArray(zoomKey).getDouble(1), 0);
    }

    @Test
    public void setMapCenterWhenGivenBounds() throws JSONException {
        String zoomKey = MapBoxStyleHelper.KEY_MAP_CENTER;
        LatLng topLeft = new LatLng(20.0, -20.0);
        LatLng bottomRight = new LatLng(-10.0, 10.0);
        LatLng mapCenter = new LatLng(5.0, -5.0);

        String sampleStyle = getSampleMapboxStyle();
        JSONObject mapboxStyleJSONObject = new JSONObject(sampleStyle);

        MapBoxStyleHelper mapBoxStyleHelper = new MapBoxStyleHelper(mapboxStyleJSONObject);
        mapBoxStyleHelper.setMapCenter(topLeft, bottomRight);

        JSONObject finalMapboxStyleJSONObject = mapBoxStyleHelper.getStyleObject();

        Assert.assertEquals(mapCenter.getLongitude(), finalMapboxStyleJSONObject.getJSONArray(zoomKey).getDouble(0), 0);
        Assert.assertEquals(mapCenter.getLatitude(), finalMapboxStyleJSONObject.getJSONArray(zoomKey).getDouble(1), 0);
    }

    private JSONObject getGeoJsonDataSource() throws JSONException {
        return new JSONObject("{\n" +
                "    \"type\": \"geojson\",\n" +
                "    \"data\": {\n" +
                "        \"features\": [\n" +
                "            {\n" +
                "              \"type\": \"Feature\",\n" +
                "              \"properties\": {},\n" +
                "              \"geometry\": {\n" +
                "                \"coordinates\": [\n" +
                "                  36.791183,\n" +
                "                  -1.293522\n" +
                "                ],\n" +
                "                \"type\": \"Point\"\n" +
                "              },\n" +
                "              \"id\": \"b3369aa6b5022be641198d898bd20e47\"\n" +
                "            }\n" +
                "          ],\n" +
                "        \"type\": \"FeatureCollection\"\n" +
                "  }\n" +
                "}");
    }

    private String getSampleMapboxStyle() {
        return "{\n" +
                "    \"version\": 8,\n" +
                "    \"name\": \"sample-test-map\",\n" +
                "    \"metadata\": {\n" +
                "        \"mapbox:autocomposite\": true,\n" +
                "        \"mapbox:type\": \"template\"\n" +
                "    },\n" +
                "    \"center\": [\n" +
                "        36.791765548891135,\n" +
                "        -1.2931451332433141\n" +
                "    ],\n" +
                "    \"zoom\": 17.739661716263807,\n" +
                "    \"bearing\": 0,\n" +
                "    \"pitch\": 0,\n" +
                "    \"sources\": {\n" +
                "        \"mapbox://mapbox.mapbox-traffic-v1\": {\n" +
                "            \"url\": \"mapbox://mapbox.mapbox-traffic-v1\",\n" +
                "            \"type\": \"vector\"\n" +
                "        },\n" +
                "        \"composite\": {\n" +
                "            \"url\": \"mapbox://mapbox.mapbox-streets-v7\",\n" +
                "            \"type\": \"vector\"\n" +
                "        },\n" +
                "        \"my-geojson\": {\n" +
                "            \"type\": \"geojson\",\n" +
                "            \"data\": {\n" +
                "                \"features\": [\n" +
                "                    {\n" +
                "                      \"type\": \"Feature\",\n" +
                "                      \"properties\": {},\n" +
                "                      \"geometry\": {\n" +
                "                        \"coordinates\": [\n" +
                "                          36.791183,\n" +
                "                          -1.293522\n" +
                "                        ],\n" +
                "                        \"type\": \"Point\"\n" +
                "                      },\n" +
                "                      \"id\": \"b3369aa6b5022be641198d898bd20e47\"\n" +
                "                    }\n" +
                "                  ],\n" +
                "                \"type\": \"FeatureCollection\"\n" +
                "          }\n" +
                "        }\n" +
                "    },\n" +
                "    \"sprite\": \"mapbox://sprites/john_doe/some_random_id_here\",\n" +
                "    \"glyphs\": \"mapbox://fonts/john_doe/{fontstack}/{range}.pbf\",\n" +
                "    \"layers\": [\n" +
                "        {\n" +
                "            \"id\": \"background\",\n" +
                "            \"type\": \"background\",\n" +
                "            \"paint\": {\n" +
                "                \"background-color\": \"#dedede\"\n" +
                "            }\n" +
                "        },\n" +
                "        {\n" +
                "            \"id\": \"landuse_overlay_national_park\",\n" +
                "            \"type\": \"fill\",\n" +
                "            \"source\": \"composite\",\n" +
                "            \"source-layer\": \"landuse_overlay\",\n" +
                "            \"filter\": [\n" +
                "                \"==\",\n" +
                "                \"class\",\n" +
                "                \"national_park\"\n" +
                "            ],\n" +
                "            \"paint\": {\n" +
                "                \"fill-color\": \"#d2edae\",\n" +
                "                \"fill-opacity\": 0.75\n" +
                "            }\n" +
                "        }\n" +
                "    ],\n" +
                "    \"created\": \"2017-11-02T13:11:06.620Z\",\n" +
                "    \"id\": \"some_id_here\",\n" +
                "    \"modified\": \"2017-11-03T07:00:27.686Z\",\n" +
                "    \"owner\": \"john_doe\",\n" +
                "    \"visibility\": \"private\",\n" +
                "    \"draft\": false\n" +
                "}";
    }
}
