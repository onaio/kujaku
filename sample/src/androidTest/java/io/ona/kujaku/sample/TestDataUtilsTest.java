package io.ona.kujaku.sample;

import com.mapbox.geojson.Feature;

import org.json.JSONArray;
import org.junit.Test;

import java.util.List;

import static io.ona.kujaku.sample.utils.TestDataUtils.createFeatureJsonArray;
import static io.ona.kujaku.sample.utils.TestDataUtils.createFeatureList;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

/**
 * @author Vincent Karuri
 */
public class TestDataUtilsTest {

    @Test
    public void testCreateFeatureListShouldNotBeEmptyList() {
        List<Feature> features = null;
        try {
            features = createFeatureList(5, 0, 31.03823, -1.214348, "test_property", "Point", false, new String[]{"property1", "property2", "property3"}, 0.001);
        } catch (Exception e) {

        }
        assertNotNull(features);
        assertEquals(features.size(), 5);
    }

    @Test
    public void testCreateFeatureJsonArrayShouldNotBeEmpty() {
        JSONArray features = null;
        try {
            features = createFeatureJsonArray(5, 31.03823, -1.214348, "test_property", new String[]{"property1", "property2", "property3"});
        } catch (Exception e) {

        }
        assertNotNull(features);
        assertEquals(features.length(), 5);
    }
}
