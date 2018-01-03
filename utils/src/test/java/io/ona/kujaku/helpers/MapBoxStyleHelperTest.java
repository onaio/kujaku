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
import io.ona.kujaku.utils.config.SortFieldConfig;
import io.ona.kujaku.utils.exceptions.InvalidMapBoxStyleException;
import io.ona.kujaku.utils.helpers.MapBoxStyleHelper;

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
        helper.getKujakuConfig().addDataSourceConfig("test");
        helper.getKujakuConfig().addSortFieldConfig(SortFieldConfig.FieldType.STRING.toString(), "test");
        return helper;
    }
}
