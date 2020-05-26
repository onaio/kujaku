package io.ona.kujaku.utils;

import com.mapbox.mapboxsdk.geometry.LatLng;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import io.ona.kujaku.BaseTest;
import io.ona.kujaku.location.clients.SphericalUtil;

public class SphericalUtilTest extends BaseTest {

    private static final double EARTH_RADIUS = SphericalUtil.EARTH_RADIUS;

    // The vertices of an octahedron, for testing
    private final LatLng up = new LatLng(90, 0);
    private final LatLng down = new LatLng(-90, 0);
    private final LatLng front = new LatLng(0, 0);
    private final LatLng right = new LatLng(0, 90);
    private final LatLng back = new LatLng(0, -180);
    private final LatLng left = new LatLng(0, -90);

    private static void expectEq(Object expected, Object actual) {
        Assert.assertEquals(expected, actual);
    }

    private static void expectNearNumber(double actual, double expected, double epsilon) {
        Assert.assertTrue(String.format("Expected %f to be near %f", actual, expected),
                Math.abs(expected - actual) <= epsilon);
    }

    @Test
    public void testAngles() {
        // Same vertex
        expectNearNumber(SphericalUtil.computeAngleBetween(up, up), 0, 1e-6);
        expectNearNumber(SphericalUtil.computeAngleBetween(down, down), 0, 1e-6);
        expectNearNumber(SphericalUtil.computeAngleBetween(left, left), 0, 1e-6);
        expectNearNumber(SphericalUtil.computeAngleBetween(right, right), 0, 1e-6);
        expectNearNumber(SphericalUtil.computeAngleBetween(front, front), 0, 1e-6);
        expectNearNumber(SphericalUtil.computeAngleBetween(back, back), 0, 1e-6);

        // Adjacent vertices
        expectNearNumber(SphericalUtil.computeAngleBetween(up, front), Math.PI / 2, 1e-6);
        expectNearNumber(SphericalUtil.computeAngleBetween(up, right), Math.PI / 2, 1e-6);
        expectNearNumber(SphericalUtil.computeAngleBetween(up, back), Math.PI / 2, 1e-6);
        expectNearNumber(SphericalUtil.computeAngleBetween(up, left), Math.PI / 2, 1e-6);

        expectNearNumber(SphericalUtil.computeAngleBetween(down, front), Math.PI / 2, 1e-6);
        expectNearNumber(SphericalUtil.computeAngleBetween(down, right), Math.PI / 2, 1e-6);
        expectNearNumber(SphericalUtil.computeAngleBetween(down, back), Math.PI / 2, 1e-6);
        expectNearNumber(SphericalUtil.computeAngleBetween(down, left), Math.PI / 2, 1e-6);

        expectNearNumber(SphericalUtil.computeAngleBetween(back, up), Math.PI / 2, 1e-6);
        expectNearNumber(SphericalUtil.computeAngleBetween(back, right), Math.PI / 2, 1e-6);
        expectNearNumber(SphericalUtil.computeAngleBetween(back, down), Math.PI / 2, 1e-6);
        expectNearNumber(SphericalUtil.computeAngleBetween(back, left), Math.PI / 2, 1e-6);

        // Opposite vertices
        expectNearNumber(SphericalUtil.computeAngleBetween(up, down), Math.PI, 1e-6);
        expectNearNumber(SphericalUtil.computeAngleBetween(front, back), Math.PI, 1e-6);
        expectNearNumber(SphericalUtil.computeAngleBetween(left, right), Math.PI, 1e-6);
    }

    @Test
    public void testDistances() {
        expectNearNumber(SphericalUtil.computeDistanceBetween(up, down),
                Math.PI * EARTH_RADIUS, 1e-6);
    }

    @Test
    public void testComputeLength() {
        List<LatLng> latLngs;

        expectNearNumber(SphericalUtil.computeLength(Collections.emptyList()), 0, 1e-6);
        expectNearNumber(SphericalUtil.computeLength(Arrays.asList(new LatLng(0, 0))), 0, 1e-6);

        latLngs = Arrays.asList(new LatLng(0, 0), new LatLng(0.1, 0.1));
        expectNearNumber(SphericalUtil.computeLength(latLngs),
                Math.toRadians(0.1) * Math.sqrt(2) * EARTH_RADIUS, 1);

        latLngs = Arrays.asList(new LatLng(0, 0), new LatLng(90, 0), new LatLng(0, 90));
        expectNearNumber(SphericalUtil.computeLength(latLngs), Math.PI * EARTH_RADIUS, 1e-6);
    }

    @Test
    public void testComputeArea() {
        expectNearNumber(SphericalUtil.computeArea(Arrays.asList(right, up, front, down, right)),
                Math.PI * EARTH_RADIUS * EARTH_RADIUS, .4);

        expectNearNumber(SphericalUtil.computeArea(Arrays.asList(right, down, front, up, right)),
                Math.PI * EARTH_RADIUS * EARTH_RADIUS, .4);
    }

    @Test
    public void testComputeSignedArea() {
        List<LatLng> path = Arrays.asList(right, up, front, down, right);
        List<LatLng> pathReversed = Arrays.asList(right, down, front, up, right);
        expectEq(-SphericalUtil.computeSignedArea(path), SphericalUtil.computeSignedArea(pathReversed));
    }
}
