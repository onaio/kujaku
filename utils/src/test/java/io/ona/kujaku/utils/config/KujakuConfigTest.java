package io.ona.kujaku.utils.config;

import junit.framework.Assert;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import io.ona.kujaku.utils.BuildConfig;
import io.ona.kujaku.utils.exceptions.InvalidMapBoxStyleException;
import io.ona.kujaku.utils.helpers.MapBoxStyleHelper;

/**
 * Created by Jason Rogena - jrogena@ona.io on 1/2/18.
 */

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, manifest = Config.NONE)
public class KujakuConfigTest {

    @Test
    public void testValidKujakuConfig() throws JSONException, InvalidMapBoxStyleException {
        MapBoxStyleHelper styleHelper = new MapBoxStyleHelper(new JSONObject());
        styleHelper.getKujakuConfig().addDataSourceConfig("test-source");
        styleHelper.getKujakuConfig().addSortFieldConfig(SortFieldConfig.FieldType.DATE.toString(), "testField");
        styleHelper.getKujakuConfig().getInfoWindowConfig().addVisibleProperty("testId", "Test Label");

        Assert.assertTrue(styleHelper.getKujakuConfig().isValid());
        styleHelper.build();// Will throw an exception if could not build
    }

    @Test
    public void testInvalidKujakuConfig() throws JSONException, InvalidMapBoxStyleException {
        // Config with nothing set
        KujakuConfig config1 = new KujakuConfig();
        Assert.assertFalse(config1.isValid());

        // Config with unset info-window config
        KujakuConfig config2 = new KujakuConfig();
        config2.addSortFieldConfig(SortFieldConfig.FieldType.STRING.toString(), "test");
        config2.addDataSourceConfig("test");
        Assert.assertFalse(config2.isValid());

        // Config with unset sort fields config
        KujakuConfig config3 = new KujakuConfig();
        config3.addDataSourceConfig("test");
        config3.getInfoWindowConfig().addVisibleProperty("test", "test");
        Assert.assertFalse(config3.isValid());

        // Config with unset data-source names
        KujakuConfig config4 = new KujakuConfig();
        config4.addSortFieldConfig(SortFieldConfig.FieldType.STRING.toString(), "test");
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
        helper2.getKujakuConfig().addSortFieldConfig(SortFieldConfig.FieldType.STRING.toString(), "test");
        helper2.getKujakuConfig().addDataSourceConfig("test");
        try {
            helper2.build();
            Assert.fail("Exception not thrown when building style with invalid Kujaku config");
        } catch (InvalidMapBoxStyleException e) {
        }
    }
}