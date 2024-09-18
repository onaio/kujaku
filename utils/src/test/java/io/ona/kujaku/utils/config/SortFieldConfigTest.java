package io.ona.kujaku.utils.config;

import org.junit.Assert;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import io.ona.kujaku.utils.exceptions.InvalidMapBoxStyleException;
import io.ona.kujaku.utils.helpers.MapBoxStyleHelper;

@RunWith(RobolectricTestRunner.class)
@org.robolectric.annotation.Config(manifest = org.robolectric.annotation.Config.NONE)
public class SortFieldConfigTest {
    @Test
    public void testConstructor() throws JSONException, InvalidMapBoxStyleException {
        // Test incorrect type
        try {
            new SortFieldConfig("wrong", "test");
            Assert.fail("InvalidMapBoxStyleException not thrown with incorrect sort field type");
        } catch (InvalidMapBoxStyleException e) {

        }

        JSONObject object1 = new JSONObject();
        object1.put(SortFieldConfig.KEY_DATA_FIELD, "test");
        object1.put(SortFieldConfig.KEY_TYPE, "wrong_type");
        try {
            new SortFieldConfig(object1);
            Assert.fail("InvalidMapBoxStyleException not thrown with incorrect sort field type");
        } catch (InvalidMapBoxStyleException e) {

        }

        // Test correct construction
        SortFieldConfig.FieldType type1 = SortFieldConfig.FieldType.DATE;
        String dataField1 = "testdatafield";
        SortFieldConfig config1 = new SortFieldConfig(type1, dataField1);
        Assert.assertEquals(config1.getType(), type1);
        Assert.assertEquals(config1.getDataField(), dataField1);

        String type2 = SortFieldConfig.FieldType.DATE.toString();
        SortFieldConfig config2 = new SortFieldConfig(type2, dataField1);
        Assert.assertEquals(config2.getType().toString(), type2);
        Assert.assertEquals(config2.getDataField(), dataField1);

        JSONObject object2 = new JSONObject();
        object2.put(SortFieldConfig.KEY_DATA_FIELD, dataField1);
        object2.put(SortFieldConfig.KEY_TYPE, type2);
        SortFieldConfig config3 = new SortFieldConfig(object2);
        Assert.assertEquals(config3.getType().toString(), type2);
        Assert.assertEquals(config3.getDataField(), dataField1);
    }

    @Test
    public void testSetType() throws InvalidMapBoxStyleException {
        // Test invalid type
        try {
            SortFieldConfig config = new SortFieldConfig();
            config.setType("invalid_type");
            Assert.fail("InvalidMapBoxStyleException didn't get thrown for an invalid type");
        } catch (InvalidMapBoxStyleException e) {

        }

        try {
            SortFieldConfig config = new SortFieldConfig();
            config.setType("");
            Assert.fail("InvalidMapBoxStyleException didn't get thrown for an invalid type");
        } catch (InvalidMapBoxStyleException e) {

        }

        try {
            SortFieldConfig config = new SortFieldConfig();
            config.setType((String) null);
            Assert.fail("InvalidMapBoxStyleException didn't get thrown for an invalid type");
        } catch (InvalidMapBoxStyleException e) {

        }

        SortFieldConfig config1 = new SortFieldConfig();
        config1.setType(SortFieldConfig.FieldType.DATE);
        Assert.assertEquals(SortFieldConfig.FieldType.DATE, config1.getType());

        SortFieldConfig config2 = new SortFieldConfig();
        config2.setType(SortFieldConfig.FieldType.NUMBER);
        Assert.assertEquals(SortFieldConfig.FieldType.NUMBER, config2.getType());

        SortFieldConfig config3 = new SortFieldConfig();
        config3.setType(SortFieldConfig.FieldType.STRING);
        Assert.assertEquals(SortFieldConfig.FieldType.STRING, config3.getType());
    }

    @Test
    public void testIsValidType() {
        Assert.assertFalse(SortFieldConfig.isValidType(""));
        Assert.assertFalse(SortFieldConfig.isValidType(null));
        Assert.assertFalse(SortFieldConfig.isValidType("testinvalid"));
        Assert.assertTrue(SortFieldConfig
                .isValidType(SortFieldConfig.FieldType.DATE.toString().toUpperCase()));
        Assert.assertTrue(SortFieldConfig
                .isValidType(SortFieldConfig.FieldType.DATE.toString().toLowerCase()));
        Assert.assertTrue(SortFieldConfig
                .isValidType(SortFieldConfig.FieldType.NUMBER.toString().toUpperCase()));
        Assert.assertTrue(SortFieldConfig
                .isValidType(SortFieldConfig.FieldType.NUMBER.toString().toLowerCase()));
        Assert.assertTrue(SortFieldConfig
                .isValidType(SortFieldConfig.FieldType.STRING.toString().toUpperCase()));
        Assert.assertTrue(SortFieldConfig
                .isValidType(SortFieldConfig.FieldType.STRING.toString().toLowerCase()));
    }

    public void testIsValid() {
        SortFieldConfig config1 = new SortFieldConfig();
        Assert.assertFalse(config1.isValid());

        SortFieldConfig config2 = new SortFieldConfig(SortFieldConfig.FieldType.DATE, null);
        Assert.assertFalse(config2.isValid());
        SortFieldConfig config3 = new SortFieldConfig(SortFieldConfig.FieldType.DATE, "");
        Assert.assertFalse(config3.isValid());

        SortFieldConfig config4 = new SortFieldConfig();
        config4.setDataField("data");
        Assert.assertFalse(config4.isValid());

        SortFieldConfig config5 = new SortFieldConfig();
        config5.setType(SortFieldConfig.FieldType.DATE);
        Assert.assertFalse(config5.isValid());

        SortFieldConfig config6 = new SortFieldConfig(SortFieldConfig.FieldType.DATE, "test");
        Assert.assertFalse(config6.isValid());

        SortFieldConfig config7 = new SortFieldConfig();
        config7.setType(SortFieldConfig.FieldType.DATE);
        config7.setDataField("dtat");
        Assert.assertFalse(config7.isValid());
    }

    @Test
    public void testToJsonObject() throws JSONException, InvalidMapBoxStyleException {
        String dataField1 = "test";
        SortFieldConfig.FieldType type1 = SortFieldConfig.FieldType.NUMBER;
        SortFieldConfig config1 = new SortFieldConfig(type1, dataField1);
        Assert.assertEquals(config1.toJsonObject()
                .getString(SortFieldConfig.KEY_TYPE).toLowerCase(), type1.toString().toLowerCase());
        Assert.assertEquals(config1.toJsonObject()
                        .getString(SortFieldConfig.KEY_DATA_FIELD).toLowerCase(),
                dataField1.toLowerCase());

        JSONObject object1 = new JSONObject();
        object1.put(SortFieldConfig.KEY_DATA_FIELD, dataField1);
        object1.put(SortFieldConfig.KEY_TYPE, type1.toString());
        SortFieldConfig config2 = new SortFieldConfig(object1);
        Assert.assertEquals(config2.toJsonObject()
                .getString(SortFieldConfig.KEY_TYPE).toLowerCase(), type1.toString().toLowerCase());
        Assert.assertEquals(config2.toJsonObject()
                        .getString(SortFieldConfig.KEY_DATA_FIELD).toLowerCase(),
                dataField1.toLowerCase());
        Assert.assertNotSame(object1, config2.toJsonObject());
    }

    @Test
    public void testExtractSortFieldConfigs() throws InvalidMapBoxStyleException, JSONException {
        int count = 5;
        MapBoxStyleHelper helper1 = new MapBoxStyleHelper(new JSONObject());
        for (int i = 0; i < count; i++) {
            SortFieldConfig config1 = new SortFieldConfig(SortFieldConfig.FieldType.DATE,
                    "test" + String.valueOf(i));
            helper1.getKujakuConfig().addSortFieldConfig(config1);
        }
        helper1.getKujakuConfig().addDataSourceConfig("test");
        helper1.getKujakuConfig().getInfoWindowConfig().addVisibleProperty("test", "test");

        // Assert size
        SortFieldConfig[] configs1 = SortFieldConfig
                .extractSortFieldConfigs(new MapBoxStyleHelper(helper1.build()));
        Assert.assertEquals(configs1.length, count);
        // Assert order
        for (int i = 0; i < count; i++) {
            Assert.assertEquals(configs1[i].getDataField(), "test" + String.valueOf(i));
        }
    }
}
