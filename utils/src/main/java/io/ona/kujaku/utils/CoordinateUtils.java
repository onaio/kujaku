package io.ona.kujaku.utils;

import android.support.annotation.NonNull;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.turf.TurfConstants;
import com.mapbox.turf.TurfMeasurement;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 19/12/2017.
 */

public class CoordinateUtils {

    public static final double DEFAULT_BOUND_DIFFERENCE = 0.021103;

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

        double maxX = TurfMeasurement.destination(Point.fromLngLat(bbox[1], bbox[2])
                , paddingInMetres
                , 90d
                , TurfConstants.UNIT_METRES
                ).longitude();

        double maxY = TurfMeasurement.destination(Point.fromLngLat(bbox[1], bbox[2])
                        , paddingInMetres
                        , 0d
                        , TurfConstants.UNIT_METRES
                ).latitude();

        return new double[]{minX, minY, maxX, maxY};
    }

}
