package io.ona.kujaku.utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mapbox.geojson.Point;
import com.mapbox.maps.CoordinateBounds;
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
    public static boolean isLocationInBounds(@NonNull Point positionInQuestion, @NonNull CoordinateBounds myMapBounds) {
        return isLocationInBounds(positionInQuestion, myMapBounds.north(), myMapBounds.south(), myMapBounds.east(), myMapBounds.west());
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
    public static boolean isLocationInBounds(@NonNull Point positionInQuestion, double latNorth, double latSouth, double lonEast, double lonWest) {
        return (positionInQuestion.latitude() <= latNorth
                && positionInQuestion.latitude() >= latSouth
                && positionInQuestion.longitude() <= lonEast
                && positionInQuestion.longitude() >= lonWest);
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
    public static Point[] generate5pointsFromBbox(@NonNull double[] bbox) {
        if (bbox.length < 4) {
            return null;
        }

        return new Point[]{
                Point.fromLngLat(bbox[0],bbox[0]),
                Point.fromLngLat(bbox[0],bbox[3]),
                Point.fromLngLat(bbox[2],bbox[3]),
                Point.fromLngLat(bbox[2],bbox[1]),
                Point.fromLngLat(bbox[0],bbox[1])
        };
    }


}
