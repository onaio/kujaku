package io.ona.kujaku.helpers.converters;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Created by Ephraim Kigamba on 06/11/2017.
 */

public class GeoJSONFeatureTest {

    @Test
    public void addPropertyShouldIncreaseProperties() {
        GeoJSONFeature geoJSONFeature = new GeoJSONFeature();
        geoJSONFeature.addProperty("monitor name", "Samsung");

        assertEquals(geoJSONFeature.getFeatureProperties().size(), 1);

        geoJSONFeature.addProperty("name", "Mt. Kenya");
        geoJSONFeature.addProperty("discovered", "Apparently 1849");
        assertEquals(geoJSONFeature.getFeatureProperties().size(), 3);

        //Check the contents
        assertTrue("monitor name".equals(geoJSONFeature.getFeatureProperties().get(0).getName()));
        assertTrue("Samsung".equals(geoJSONFeature.getFeatureProperties().get(0).getValue()));


        assertTrue("name".equals(geoJSONFeature.getFeatureProperties().get(1).getName()));
        assertTrue("Mt. Kenya".equals(geoJSONFeature.getFeatureProperties().get(1).getValue()));

        assertTrue("discovered".equals(geoJSONFeature.getFeatureProperties().get(2).getName()));
        assertTrue("Apparently 1849".equals(geoJSONFeature.getFeatureProperties().get(2).getValue()));
    }

    @Test
    public void addPointShouldIncreasePoints() {

    }

    @Test
    public void constructorShouldCreatePointFeature() {}

    @Test
    public void constructorShouldCreateMultiPointFeature() {}
}
