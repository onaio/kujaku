package io.ona.kujaku.utils;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.turf.TurfConstants;
import com.mapbox.turf.TurfMeasurement;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 19/12/2017.
 */

public class CoordinateUtils {

    /**
     * Checks whether the provided coordinate is within the provided Bounds.
     *
     * @param positionInQuestion
     * @param myMapBounds
     * @return
     */
    public static boolean isLocationInBounds(@NonNull LatLng positionInQuestion, @NonNull LatLngBounds myMapBounds) {
        return isLocationInBounds(positionInQuestion, myMapBounds.getLatNorth(), myMapBounds.getLatSouth(), myMapBounds.getLonEast(), myMapBounds.getLonWest());
    }

    /**
     * Checks whether the provided coordinate is within the provided Bounds.
     *
     * @param positionInQuestion
     * @param latNorth
     * @param latSouth
     * @param lonEast
     * @param lonWest
     * @return
     */
    public static boolean isLocationInBounds(@NonNull LatLng positionInQuestion, double latNorth, double latSouth, double lonEast, double lonWest) {
        return (positionInQuestion.getLatitude() <= latNorth
                && positionInQuestion.getLatitude() >= latSouth
                && positionInQuestion.getLongitude() <= lonEast
                && positionInQuestion.getLongitude() >= lonWest);
    }


    /**
     * Returns a padded bbox of in this order {@code [minX, minY, maxX, maxY]} with a padding of
     * the distance in metres passed
     *
     * @param bbox            in this order  {@code [minX, minY, maxX, maxY]}
     * @param paddingInMetres
     * @return
     */
    public static double[] getPaddedBbox(@NonNull double[] bbox, double paddingInMetres) {
        if (bbox.length < 4 || paddingInMetres <= 0) {
            return bbox;
        }

        double minX = TurfMeasurement.destination(Point.fromLngLat(bbox[0], bbox[1])
                , paddingInMetres
                , -90d
                , TurfConstants.UNIT_METRES
                ).longitude();

        double minY = TurfMeasurement.destination(Point.fromLngLat(bbox[0], bbox[1])
                        , paddingInMetres
                        , -180d
                        , TurfConstants.UNIT_METRES
                ).latitude();

        double maxX = TurfMeasurement.destination(Point.fromLngLat(bbox[2], bbox[3])
                , paddingInMetres
                , 90d
                , TurfConstants.UNIT_METRES
                ).longitude();

        double maxY = TurfMeasurement.destination(Point.fromLngLat(bbox[2], bbox[3])
                        , paddingInMetres
                        , 0d
                        , TurfConstants.UNIT_METRES
                ).latitude();

        return new double[]{minX, minY, maxX, maxY};
    }

    /**
     * This generates the 5 coordinates required by the geometry of a polygon to create a 4-sided
     * polygon. This polygon will be used to draw on the map. In case you just need four points,
     * then you can leave the last point.
     *
     * @param bbox
     * @return
     */
    @Nullable
    public static LatLng[] generate5pointsFromBbox(@NonNull double[] bbox) {
        if (bbox.length < 4) {
            return null;
        }

        return new LatLng[]{
                new LatLng(bbox[1], bbox[0]),
                new LatLng(bbox[3], bbox[0]),
                new LatLng(bbox[3], bbox[2]),
                new LatLng(bbox[1], bbox[2]),
                new LatLng(bbox[1], bbox[0])
        };
    }

}
