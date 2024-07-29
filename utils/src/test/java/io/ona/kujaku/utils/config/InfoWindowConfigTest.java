package io.ona.kujaku.utils.config;

import junit.framework.Assert;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import io.ona.kujaku.utils.exceptions.InvalidMapBoxStyleException;

/**
 * Created by Jason Rogena - jrogena@ona.io on 1/3/18.
 */

@RunWith(RobolectricTestRunner.class)
@org.robolectric.annotation.Config(manifest = org.robolectric.annotation.Config.NONE)
public class InfoWindowConfigTest {

    @Test
    public void testConstructor() throws JSONException, InvalidMapBoxStyleException {
        // Test invalid visible property JSONObject
        try {
            JSONObject visProperties = new JSONObject();
            visProperties.put(InfoWindowConfig.KEY_VP_LABEL, "");
            visProperties.put(InfoWindowConfig.KEY_VP_ID, "id");
            JSONArray array1 = new JSONArray();
            array1.put(visProperties);
            JSONObject object1 = new JSONObject();
            object1.put(InfoWindowConfig.KEY_VISIBLE_PROPERTIES, array1);
            new InfoWindowConfig(object1);
            Assert.fail("InvalidMapBoxStyleException not thrown for an incorrect visible property");
        } catch (InvalidMapBoxStyleException e) {

        }

        // Test constructor using JSONObject
        JSONObject visProperties = new JSONObject();
        String label = "label1";
        String id = "id1";
        visProperties.put(InfoWindowConfig.KEY_VP_LABEL, label);
        visProperties.put(InfoWindowConfig.KEY_VP_ID, id);
        JSONArray array2 = new JSONArray();
        array2.put(visProperties);
        JSONObject object2 = new JSONObject();
        object2.put(InfoWindowConfig.KEY_VISIBLE_PROPERTIES, array2);
        InfoWindowConfig config1 = new InfoWindowConfig(object2);
        Assert.assertEquals(config1.getVisibleProperties().length(), 1);
        Assert.assertEquals(config1.getVisibleProperties()
                .getJSONObject(0).getString(InfoWindowConfig.KEY_VP_ID), id);
        Assert.assertEquals(config1.getVisibleProperties()
                .getJSONObject(0).getString(InfoWindowConfig.KEY_VP_LABEL), label);
    }

    @Test
    public void testSetVisibleProperty() throws JSONException, InvalidMapBoxStyleException {
        // Test invalid visible property
        try {
            InfoWindowConfig config = new InfoWindowConfig();
            config.addVisibleProperty(null, null);
            Assert.fail("InvalidMapBoxStyleException not thrown for an incorrect visible property");
        } catch (InvalidMapBoxStyleException e) {

        }

        try {
            InfoWindowConfig config = new InfoWindowConfig();
            config.addVisibleProperty("test", null);
            Assert.fail("InvalidMapBoxStyleException not thrown for an incorrect visible property");
        } catch (InvalidMapBoxStyleException e) {

        }

        try {
            InfoWindowConfig config = new InfoWindowConfig();
            config.addVisibleProperty(null, "test");
            Assert.fail("InvalidMapBoxStyleException not thrown for an incorrect visible property");
        } catch (InvalidMapBoxStyleException e) {

        }

        try {
            InfoWindowConfig config = new InfoWindowConfig();
            config.addVisibleProperty("test", "");
            Assert.fail("InvalidMapBoxStyleException not thrown for an incorrect visible property");
        } catch (InvalidMapBoxStyleException e) {

        }

        try {
            InfoWindowConfig config = new InfoWindowConfig();
            config.addVisibleProperty("", "test");
            Assert.fail("InvalidMapBoxStyleException not thrown for an incorrect visible property");
        } catch (InvalidMapBoxStyleException e) {

        }

        InfoWindowConfig config = new InfoWindowConfig();
        String id = "id1";
        String label = "label1";
        config.addVisibleProperty(id, label);
        Assert.assertEquals(id, config.getVisibleProperties()
                .getJSONObject(0).getString(InfoWindowConfig.KEY_VP_ID));
        Assert.assertEquals(label, config.getVisibleProperties()
                .getJSONObject(0).getString(InfoWindowConfig.KEY_VP_LABEL));
    }

    @Test
    public void isValid() throws InvalidMapBoxStyleException, JSONException {
        // Test config with no visible properties
        InfoWindowConfig config1 = new InfoWindowConfig();
        Assert.assertFalse(config1.isValid());

        // Test a valid config
        InfoWindowConfig config2 = new InfoWindowConfig();
        config2.addVisibleProperty("test", "test");
        Assert.assertTrue(config2.isValid());
    }

    @Test
    public void toJsonObject() throws JSONException, InvalidMapBoxStyleException {
        // Test when values set using constructor with JSONObject
        int size = 4;
        JSONArray array1 = new JSONArray();
        for (int i = 0; i < size; i++) {
            JSONObject visProperties = new JSONObject();
            visProperties.put(InfoWindowConfig.KEY_VP_LABEL, "label" + String.valueOf(i));
            visProperties.put(InfoWindowConfig.KEY_VP_ID, "id" + String.valueOf(i));
            array1.put(visProperties);
        }
        JSONObject object1 = new JSONObject();
        object1.put(InfoWindowConfig.KEY_VISIBLE_PROPERTIES, array1);
        InfoWindowConfig config1 = new InfoWindowConfig(object1);
        Assert.assertEquals(config1.toJsonObject().getJSONArray(
                InfoWindowConfig.KEY_VISIBLE_PROPERTIES).length(), size);
        for (int i = 0; i < size; i++) {
            Assert.assertEquals(config1.toJsonObject().getJSONArray(
                    InfoWindowConfig.KEY_VISIBLE_PROPERTIES).getJSONObject(i)
                            .getString(InfoWindowConfig.KEY_VP_ID), "id" + String.valueOf(i));
            Assert.assertEquals(config1.toJsonObject().getJSONArray(
                    InfoWindowConfig.KEY_VISIBLE_PROPERTIES).getJSONObject(i)
                            .getString(InfoWindowConfig.KEY_VP_LABEL), "label" + String.valueOf(i));
        }
        Assert.assertNotSame(config1.toJsonObject(), object1);
    }

}