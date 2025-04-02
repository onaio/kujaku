package io.ona.kujaku.helpers.converters;

import org.junit.Test;
import java.util.ArrayList;
import io.ona.kujaku.utils.helpers.converters.GeoJSONFeature;

import static org.junit.Assert.*;

import com.mapbox.geojson.Point;

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
        assertTrue("Samsung".equals(geoJSONFeature.getFeatureProperties().get(0).getValue()));


        assertTrue("name".equals(geoJSONFeature.getFeatureProperties().get(1).getName()));
        assertTrue("Mt. Kenya".equals(geoJSONFeature.getFeatureProperties().get(1).getValue()));

        assertTrue("discovered".equals(geoJSONFeature.getFeatureProperties().get(2).getName()));
        assertTrue("Apparently 1849".equals(geoJSONFeature.getFeatureProperties().get(2).getValue()));
    }

    @Test
    public void addPointShouldIncreasePoints() {

        GeoJSONFeature geoJSONFeature = new GeoJSONFeature();
        geoJSONFeature.addProperty("name", "Some boundary");
        geoJSONFeature.addPoint(Point.fromLngLat(4,3));
        geoJSONFeature.addPoint(Point.fromLngLat(12,9));

        int count = 10;
        for(int i = 0; i < count; i++) {
            geoJSONFeature.addPoint(Point.fromLngLat(getRandomLatOrLong(), getRandomLatOrLong()));
            assertEquals(geoJSONFeature.getFeaturePoints().size(), i + 3);
        }

    }

    @Test
    public void constructorShouldCreatePointFeature() {
        GeoJSONFeature geoJSONFeature = new GeoJSONFeature();
        geoJSONFeature.addProperty("monitor name", "Samsung");
        geoJSONFeature.addPoint(Point.fromLngLat(4,3));

        assertEquals(GeoJSONFeature.Type.POINT, geoJSONFeature.getFeatureType());

        ArrayList<Point> myPoints = new ArrayList<>();
        myPoints.add(Point.fromLngLat(30,-5));
        GeoJSONFeature geoJSONFeature2 = new GeoJSONFeature(myPoints);

        assertEquals(GeoJSONFeature.Type.POINT, geoJSONFeature2.getFeatureType());

    }

    private double getRandomLatOrLong() {
        double myRandom = Math.random();

        return (myRandom * 180) - 90;
    }

    @Test
    public void constructorShouldCreateMultiPointFeature() {

        GeoJSONFeature geoJSONFeature = new GeoJSONFeature();
        geoJSONFeature.addProperty("monitor name", "Samsung");
        geoJSONFeature.addPoint(Point.fromLngLat(4,3));
        geoJSONFeature.addPoint(Point.fromLngLat(12,9));

        assertEquals(GeoJSONFeature.Type.MULTI_POINT, geoJSONFeature.getFeatureType());

        ArrayList<Point> myPoints = new ArrayList<>();
        myPoints.add(Point.fromLngLat(30,-5));
        myPoints.add(Point.fromLngLat(5.29093,-1.2923));
        GeoJSONFeature geoJSONFeature2 = new GeoJSONFeature(myPoints);

        assertEquals(GeoJSONFeature.Type.MULTI_POINT, geoJSONFeature2.getFeatureType());
    }
}
