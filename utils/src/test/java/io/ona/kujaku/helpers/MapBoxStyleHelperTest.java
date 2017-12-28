package io.ona.kujaku.helpers;

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
    public void testInsertGeoJsonDataSource() throws JSONException, InvalidMapBoxStyleException {
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
        addValidKujakuConfig(styleHelper);
        styleHelper.insertGeoJsonDataSource(geoJsonSourceName, geoJsonDataSource, layerId);
        JSONObject returnedData = styleHelper.build();

        Assert.assertTrue(returnedData.has("sources"));
        Assert.assertTrue(returnedData.getJSONObject("sources").has(geoJsonSourceName));
        Assert.assertEquals(
                returnedData.getJSONObject("sources").getJSONObject(geoJsonSourceName).toString(),
                getGeoJsonDataSource().toString());
        Assert.assertTrue(returnedData.getJSONArray("layers").getJSONObject(0).has("source"));
        Assert.assertTrue(returnedData.getJSONArray("layers").getJSONObject(0).getString("source")
                .equals(geoJsonSourceName));
    }

    @Test
    public void testValidKujakuConfig() throws JSONException, InvalidMapBoxStyleException {
        MapBoxStyleHelper styleHelper = new MapBoxStyleHelper(new JSONObject());
        styleHelper.getKujakuConfig().addDataSourceName("test-source");
        styleHelper.getKujakuConfig().addSortField("testType", "testField");
        styleHelper.getKujakuConfig().getInfoWindowConfig().addVisibleProperty("testId", "Test Label");

        Assert.assertTrue(styleHelper.getKujakuConfig().isValid());
        styleHelper.build();// Will throw an exception if could not build
    }

    @Test
    public void testInvalidKujakuConfig() throws JSONException, InvalidMapBoxStyleException {
        // Config with nothing set
        MapBoxStyleHelper.KujakuConfig config1 = new MapBoxStyleHelper.KujakuConfig();
        Assert.assertFalse(config1.isValid());

        // Config with unset info-window config
        MapBoxStyleHelper.KujakuConfig config2 = new MapBoxStyleHelper.KujakuConfig();
        config2.addSortField("test", "test");
        config2.addDataSourceName("test");
        Assert.assertFalse(config2.isValid());

        // Config with unset sort fields config
        MapBoxStyleHelper.KujakuConfig config3 = new MapBoxStyleHelper.KujakuConfig();
        config3.addDataSourceName("test");
        config3.getInfoWindowConfig().addVisibleProperty("test", "test");
        Assert.assertFalse(config3.isValid());

        // Config with unset data-source names
        MapBoxStyleHelper.KujakuConfig config4 = new MapBoxStyleHelper.KujakuConfig();
        config4.addSortField("test", "test");
        config4.getInfoWindowConfig().addVisibleProperty("test", "test");
        Assert.assertFalse(config4.isValid());
    }

    @Test
    public void testInvalidKujakuConfigException()
            throws InvalidMapBoxStyleException, JSONException {
        // Test invalid config with none of the required fields set
        MapBoxStyleHelper helper1 = new MapBoxStyleHelper(new JSONObject());
        try {
            helper1.build();
            Assert.fail("Exception not thrown when building style with invalid Kujaku config");
        } catch (InvalidMapBoxStyleException e) {
        }

        // Test invalid config with all but one required fields set
        MapBoxStyleHelper helper2 = new MapBoxStyleHelper(new JSONObject());
        helper2.getKujakuConfig().addSortField("test", "test");
        helper2.getKujakuConfig().addDataSourceName("test");
        try {
            helper2.build();
            Assert.fail("Exception not thrown when building style with invalid Kujaku config");
        } catch (InvalidMapBoxStyleException e) {
        }
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

    private MapBoxStyleHelper addValidKujakuConfig(MapBoxStyleHelper helper)
            throws JSONException, InvalidMapBoxStyleException {
        helper.getKujakuConfig().getInfoWindowConfig().addVisibleProperty("test", "test");
        helper.getKujakuConfig().addDataSourceName("test");
        helper.getKujakuConfig().addSortField("test", "test");
        return helper;
    }
}
