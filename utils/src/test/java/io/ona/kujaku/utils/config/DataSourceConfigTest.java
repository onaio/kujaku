package io.ona.kujaku.utils.config;

import junit.framework.Assert;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.ArrayList;

import io.ona.kujaku.utils.exceptions.InvalidMapBoxStyleException;

@RunWith(RobolectricTestRunner.class)
@org.robolectric.annotation.Config(manifest = org.robolectric.annotation.Config.NONE)
public class DataSourceConfigTest {
    @Test
    public void testConstructor() throws JSONException, InvalidMapBoxStyleException {
        try {
            String name1 = null;
            new DataSourceConfig(name1);
            Assert.fail("InvalidMapBoxStyleException not thrown when initialized using null name");
        } catch (InvalidMapBoxStyleException e) {

        }

        try {
            new DataSourceConfig("");
            Assert.fail("InvalidMapBoxStyleException not thrown when initialized using empty name");
        } catch (InvalidMapBoxStyleException e) {

        }

        try {
            JSONObject object1 = new JSONObject();
            object1.put(DataSourceConfig.KEY_NAME, "");
            new DataSourceConfig(object1);
            Assert.fail("InvalidMapBoxStyleException not thrown when initialized using JSONObject with an empty name");
        } catch (InvalidMapBoxStyleException e) {

        }

        try {
            new DataSourceConfig(new JSONObject());
            Assert.fail("InvalidMapBoxStyleException not thrown when initialized using JSONObject with no name key");
        } catch (InvalidMapBoxStyleException e) {

        } catch (JSONException e) {

        }

        String name2 = "test";
        DataSourceConfig config1 = new DataSourceConfig(name2);
        Assert.assertEquals(name2, config1.getName());

        JSONObject object2 = new JSONObject();
        object2.put(DataSourceConfig.KEY_NAME, name2);
        DataSourceConfig  config2 = new DataSourceConfig(object2);
        Assert.assertEquals(name2, config2.getName());
    }

    @Test
    public void testIsValid() throws InvalidMapBoxStyleException, JSONException {
        DataSourceConfig config1 = new DataSourceConfig();
        Assert.assertFalse(config1.isValid());

        DataSourceConfig config2 = new DataSourceConfig("test");
        Assert.assertTrue(config2.isValid());

        JSONObject object1 = new JSONObject();
        object1.put(DataSourceConfig.KEY_NAME, "test");
        DataSourceConfig config3 = new DataSourceConfig(object1);
        Assert.assertTrue(config3.isValid());
    }

    @Test
    public void testToJsonObject() throws JSONException, InvalidMapBoxStyleException {
        String name1 = "test";
        JSONObject object1 = new JSONObject();
        object1.put(DataSourceConfig.KEY_NAME, name1);
        DataSourceConfig config1 = new DataSourceConfig(object1);
        // Assert that the resultant JSONObject is similar to what was passed in the constructor
        Assert.assertEquals(object1.toString(), config1.toJsonObject().toString());
        // Make sure the churned out JSONObject doesn't refer the same object passed in the constructor
        Assert.assertNotSame(object1, config1.toJsonObject());
    }

    @Test
    public void testExtractDataSourceNames() throws InvalidMapBoxStyleException {
        // Assert that the default size is 0
        Assert.assertEquals(0, DataSourceConfig.extractDataSourceNames(new ArrayList<DataSourceConfig>()).length);

        ArrayList<DataSourceConfig> configs1 = new ArrayList<>();
        int size1 = 6;
        for (int i = 0; i < size1; i++) {
            configs1.add(new DataSourceConfig(String.valueOf(i)));
        }
        // Assert the size of the generated list
        String[] names1 = DataSourceConfig.extractDataSourceNames(configs1);
        Assert.assertEquals(size1, names1.length);
        // Assert the order of names
        for (int i = 0; i < names1.length; i++) {
            Assert.assertEquals(String.valueOf(i), names1[i]);
        }
    }
}