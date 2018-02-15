package io.ona.kujaku.utils;

import com.mapbox.mapboxsdk.geometry.LatLng;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 07/02/2018.
 */
public class CoordinateUtilsTest {

    @Test
    public void isLocationInBounds() {
        LatLng bound1 = new LatLng(1.053254883591756,
                0.339202880859375);
        LatLng bound2 = new LatLng(0.707226913459037,
                -0.11260986328124999);
        LatLng bound3 = new LatLng(0.20324664405209258,
                0.278778076171875);
        LatLng bound4 = new LatLng(0.546561534676349,
                0.98052978515625);
        
        LatLng point1 = new LatLng(0.7218541012920028,
                0.6592279349874681);
        LatLng point2 = new LatLng(0.2881703637723443,
                0.25182564298088916);
        LatLng point3 = new LatLng(0.9615897751356437,
                0.8265933408660401);
        LatLng point4 = new LatLng(0.8249779712917739,
                0.33087073166495085);
        LatLng point5 = new LatLng(0.37328038670170727,
                0.8613979983001541);
        LatLng point6 = new LatLng(0.5828701459957611,
                0.6038155629767493);
        LatLng point7 = new LatLng(0.6336809580943352,
                0.41203384171480817);
        LatLng point8 = new LatLng(0.5391318208772033,
                0.12247511076740225);
        LatLng point9 = new LatLng(0.6852696915452708,
                0.9021573945160707);
        LatLng point10 = new LatLng(0.8761255229096057,
                0.58502197265625);
        LatLng point11 = new LatLng(0.49513234253574634,
                0.8266160519883283);

        assertEquals(true, CoordinateUtils.isLocationInBounds(point1, bound1, bound2, bound3, bound4));
        assertEquals(true, CoordinateUtils.isLocationInBounds(point2, bound1, bound2, bound3, bound4));
        assertEquals(false, CoordinateUtils.isLocationInBounds(point3, bound1, bound2, bound3, bound4));
        assertEquals(true, CoordinateUtils.isLocationInBounds(point4, bound1, bound2, bound3, bound4));
        assertEquals(false, CoordinateUtils.isLocationInBounds(point5, bound1, bound2, bound3, bound4));
        assertEquals(true, CoordinateUtils.isLocationInBounds(point6, bound1, bound2, bound3, bound4));
        assertEquals(true, CoordinateUtils.isLocationInBounds(point7, bound1, bound2, bound3, bound4));
        assertEquals(true, CoordinateUtils.isLocationInBounds(point8, bound1, bound2, bound3, bound4));
        assertEquals(false, CoordinateUtils.isLocationInBounds(point9, bound1, bound2, bound3, bound4));
        assertEquals(false, CoordinateUtils.isLocationInBounds(point10, bound1, bound2, bound3, bound4));
        assertEquals(true, CoordinateUtils.isLocationInBounds(point11, bound1, bound2, bound3, bound4));
    }

    @Test
    public void contains() throws Exception {
        CoordinateUtils.Point[] bounds = new CoordinateUtils.Point[4];
        bounds[0] = new CoordinateUtils.Point(0.339202880859375,
                1.053254883591756);
        bounds[1] = new CoordinateUtils.Point(-0.11260986328124999,
                0.707226913459037);
        bounds[2] = new CoordinateUtils.Point(0.278778076171875,
                0.20324664405209258);
        bounds[3] = new CoordinateUtils.Point(0.98052978515625,
                0.546561534676349);

        CoordinateUtils.Point points[] = new CoordinateUtils.Point[11];

        points[0] = new CoordinateUtils.Point(0.6592279349874681,
                0.7218541012920028);
        points[1] = new CoordinateUtils.Point(0.25182564298088916,
                0.2881703637723443);
        points[2] = new CoordinateUtils.Point(0.8265933408660401,
                0.9615897751356437);
        points[3] = new CoordinateUtils.Point(0.33087073166495085,
                0.8249779712917739);
        points[4] = new CoordinateUtils.Point(0.8613979983001541,
                0.37328038670170727);
        points[5] = new CoordinateUtils.Point(0.6038155629767493,
                0.5828701459957611);
        points[6] = new CoordinateUtils.Point(0.41203384171480817,
                0.6336809580943352);
        points[7] = new CoordinateUtils.Point(0.12247511076740225,
                0.5391318208772033);
        points[8] = new CoordinateUtils.Point(0.9021573945160707,
                0.6852696915452708);
        points[9] = new CoordinateUtils.Point(0.58502197265625,
                0.8761255229096057);
        points[10] = new CoordinateUtils.Point(0.8266160519883283,
                0.49513234253574634);

        boolean[] expectedResults = new boolean[]{true, true, false, true, false, true, true, true, false, false, true};

        for(int i = 0; i < 11; i++) {
            assertEquals(expectedResults[i], CoordinateUtils.contains(bounds, points[i]));
        }
    }

}