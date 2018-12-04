package io.ona.kujaku.utils;

import com.mapbox.mapboxsdk.geometry.LatLng;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 04/12/2018
 */

public class CoordinateUtilsTest {

    @Test
    public void isLocationInBounds() {
    }

    @Test
    public void isLocationInBounds1() {
    }

    @Test
    public void getBoundsShouldReturnNull() {
        assertNull(CoordinateUtils.getBounds(new LatLng[]{}));
    }

    @Test
    public void getBoundsShouldCreateBoundsFromSinglePoint() {
        double lat = 11.345435;
        double lng = 34.29379234;

        LatLng[] expectedBounds = new LatLng[]{
                new LatLng(lat + CoordinateUtils.DEFAULT_BOUND_DIFFERENCE/2, lng + CoordinateUtils.DEFAULT_BOUND_DIFFERENCE/2),
                new LatLng(lat  - CoordinateUtils.DEFAULT_BOUND_DIFFERENCE/2, lng - CoordinateUtils.DEFAULT_BOUND_DIFFERENCE/2),
        };

        assertArrayEquals(expectedBounds, CoordinateUtils.getBounds(new LatLng[]{new LatLng(lat, lng)}));
    }

    @Test
    public void isLatLngHigherShouldReturnFalse() {
        double lat = 23.10293423;
        double lng = 12.324234323;
        assertFalse(CoordinateUtils.isLatLngHigher(
                new LatLng(lat, lng),
                new LatLng(lat + CoordinateUtils.DEFAULT_BOUND_DIFFERENCE, lng + CoordinateUtils.DEFAULT_BOUND_DIFFERENCE)
        ));

        assertFalse(CoordinateUtils.isLatLngHigher(
                new LatLng(lat, lng),
                new LatLng(lat + CoordinateUtils.DEFAULT_BOUND_DIFFERENCE, lng - CoordinateUtils.DEFAULT_BOUND_DIFFERENCE)
        ));
        assertFalse(CoordinateUtils.isLatLngHigher(
                new LatLng(lat, lng),
                new LatLng(lat - CoordinateUtils.DEFAULT_BOUND_DIFFERENCE, lng + CoordinateUtils.DEFAULT_BOUND_DIFFERENCE)
        ));

        assertFalse(CoordinateUtils.isLatLngHigher(
                new LatLng(lat, lng),
                new LatLng(lat, lng)
        ));

        assertFalse(CoordinateUtils.isLatLngHigher(
                new LatLng(lat - CoordinateUtils.DEFAULT_BOUND_DIFFERENCE, lng + CoordinateUtils.DEFAULT_BOUND_DIFFERENCE),
                new LatLng(lat, lng)
        ));
    }

    @Test
    public void isLatLngHigherShouldReturnTrue() {
        double lat = 23.10293423;
        double lng = 12.324234323;
        assertTrue(CoordinateUtils.isLatLngHigher(
                new LatLng(lat + CoordinateUtils.DEFAULT_BOUND_DIFFERENCE, lng + CoordinateUtils.DEFAULT_BOUND_DIFFERENCE),
                new LatLng(lat, lng)
        ));

        assertTrue(CoordinateUtils.isLatLngHigher(
                new LatLng(lat + CoordinateUtils.DEFAULT_BOUND_DIFFERENCE, lng),
                new LatLng(lat, lng)
        ));

        assertTrue(CoordinateUtils.isLatLngHigher(
                new LatLng(lat, lng + CoordinateUtils.DEFAULT_BOUND_DIFFERENCE),
                new LatLng(lat, lng)
        ));
    }

    @Test
    public void isLatLngLowerShouldReturnTrue() {
        double lat = 23.10293423;
        double lng = 12.324234323;
        assertTrue(CoordinateUtils.isLatLngLower(
                new LatLng(lat - CoordinateUtils.DEFAULT_BOUND_DIFFERENCE, lng - CoordinateUtils.DEFAULT_BOUND_DIFFERENCE),
                new LatLng(lat, lng)
        ));

        assertTrue(CoordinateUtils.isLatLngLower(
                new LatLng(lat, lng - CoordinateUtils.DEFAULT_BOUND_DIFFERENCE),
                new LatLng(lat, lng)
        ));

        assertTrue(CoordinateUtils.isLatLngLower(
                new LatLng(lat - CoordinateUtils.DEFAULT_BOUND_DIFFERENCE, lng),
                new LatLng(lat, lng)
        ));
    }

    @Test
    public void isLatLngLowerShouldReturnFalse() {
        double lat = 23.10293423;
        double lng = 12.324234323;
        assertFalse(CoordinateUtils.isLatLngLower(
                new LatLng(lat + CoordinateUtils.DEFAULT_BOUND_DIFFERENCE, lng + CoordinateUtils.DEFAULT_BOUND_DIFFERENCE),
                new LatLng(lat, lng)
        ));
        assertFalse(CoordinateUtils.isLatLngLower(
                new LatLng(lat + CoordinateUtils.DEFAULT_BOUND_DIFFERENCE, lng),
                new LatLng(lat, lng)
        ));
        assertFalse(CoordinateUtils.isLatLngLower(
                new LatLng(lat, lng + CoordinateUtils.DEFAULT_BOUND_DIFFERENCE),
                new LatLng(lat, lng)
        ));
        assertFalse(CoordinateUtils.isLatLngLower(
                new LatLng(lat, lng),
                new LatLng(lat, lng)
        ));
        assertFalse(CoordinateUtils.isLatLngLower(
                new LatLng(lat + CoordinateUtils.DEFAULT_BOUND_DIFFERENCE, lng - CoordinateUtils.DEFAULT_BOUND_DIFFERENCE),
                new LatLng(lat, lng)
        ));
        assertFalse(CoordinateUtils.isLatLngLower(
                new LatLng(lat - CoordinateUtils.DEFAULT_BOUND_DIFFERENCE, lng + CoordinateUtils.DEFAULT_BOUND_DIFFERENCE),
                new LatLng(lat, lng)
        ));

    }
}