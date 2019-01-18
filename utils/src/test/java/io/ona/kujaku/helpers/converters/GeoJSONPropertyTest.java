package io.ona.kujaku.helpers.converters;

import org.junit.Test;

import io.ona.kujaku.utils.helpers.converters.GeoJSONFeature;

import static org.junit.Assert.*;

/**
 * Created by Ephraim Kigamba on 06/11/2017.
 */

public class GeoJSONPropertyTest {

    @Test
    public void constructorShouldCreatePropertyWithCorrectName() {
        GeoJSONFeature.Property property = new GeoJSONFeature.Property("sample_name", 98);
        assertEquals("sample_name", property.getName());

        GeoJSONFeature.Property property1 = new GeoJSONFeature.Property("sample_name 3", "Kenya");
        assertEquals("sample_name 3", property1.getName());

        GeoJSONFeature.Property propertyProperty = new GeoJSONFeature.Property("fanta", "Fresh new look");
        GeoJSONFeature.Property property2 = new GeoJSONFeature.Property("another property", propertyProperty);
        assertEquals("another property", property2.getName());
    }

    @Test
    public void constructorShouldCreatePropertyWithCorrectValue() {

        GeoJSONFeature.Property property = new GeoJSONFeature.Property("sample_name", 98);
        assertEquals(98, property.getValue());

        GeoJSONFeature.Property property1 = new GeoJSONFeature.Property("sample_name 3", "Kenya");
        assertEquals("Kenya", property1.getValue());

        GeoJSONFeature.Property propertyProperty = new GeoJSONFeature.Property("fanta", "Fresh new look");
        GeoJSONFeature.Property property2 = new GeoJSONFeature.Property("another property", propertyProperty);
        //todo - This might fail --> FIX IT
        assertEquals(propertyProperty, property2.getValue());
    }
}
