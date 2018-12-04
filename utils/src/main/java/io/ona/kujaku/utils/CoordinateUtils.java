package io.ona.kujaku.utils;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;

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

    /*public static boolean isLocationInBounds(@NonNull LatLng positionInQuestion, @NonNull LatLng topLeft, @NonNull LatLng topRight, @NonNull LatLng bottomRight, @NonNull LatLng bottomLeft) {
        if (positionInQuestion.getLatitude() <= latNorth
                && positionInQuestion.getLatitude() >= latSouth
                && positionInQuestion.getLongitude() <= lonEast
                && positionInQuestion.getLongitude() >= lonWest) {
            return true;
        }

        return false;
    }*/

    /**
     * Generate the outermost bounds from an array of {@link LatLng} so that one can easily defined bounds
     * for offline map downloads. The {@link LatLng[]} returned contains the top-right farthest point at
     * index 0 and the bottom left farthest point at index 1
     *
     * @param points
     * @return
     */
    @Nullable
    public static LatLng[] getBounds(@NonNull LatLng[] points) {
        if (points.length < 1) {
            return null;
        }

        LatLng highestPoint = points[0];
        LatLng lowestPoint = points[0];

        if (points.length == 1) {
            // Create a default 0.021103 bound length
            double halfBoundLength = DEFAULT_BOUND_DIFFERENCE/2;
            highestPoint = new LatLng(
                    highestPoint.getLatitude() + halfBoundLength,
                    highestPoint.getLongitude() + halfBoundLength
            );

            lowestPoint = new LatLng(
                    lowestPoint.getLatitude() - halfBoundLength,
                    lowestPoint.getLongitude() - halfBoundLength
            );

            return (new LatLng[]{highestPoint, lowestPoint});
        }

        for(LatLng latLng: points) {
            if (isLatLngHigher(latLng, highestPoint)) {
                highestPoint = new LatLng(latLng.getLatitude(), latLng.getLongitude());
            }

            if (isLatLngLower(latLng, lowestPoint)) {
                lowestPoint = new LatLng(latLng.getLatitude(), latLng.getLongitude());
            }
        }

        // If the bounds are less than 0.021103 apart either longitude or latitude, then increase the bound to a minimum of that
        double defaultBoundDifference = DEFAULT_BOUND_DIFFERENCE;
        double latDifference = highestPoint.getLatitude() - lowestPoint.getLatitude();
        double lngDifference = highestPoint.getLongitude() - lowestPoint.getLongitude();
        if (latDifference < defaultBoundDifference) {
            double increaseDifference = (defaultBoundDifference - latDifference)/2;
            highestPoint.setLatitude(highestPoint.getLatitude() + increaseDifference);
            lowestPoint.setLatitude(lowestPoint.getLatitude() - increaseDifference);
        }

        if (lngDifference < defaultBoundDifference) {
            double increaseDifference = (defaultBoundDifference - lngDifference)/2;
            highestPoint.setLongitude(highestPoint.getLongitude() + increaseDifference);
            lowestPoint.setLongitude(lowestPoint.getLongitude() - increaseDifference);
        }

        return (new LatLng[]{highestPoint, lowestPoint});
    }

    public static LatLng[] getBounds(@NonNull LatLng[] points, int paddingInMetres) {
        LatLng[] bounds = getBounds(points);
        if (bounds != null && paddingInMetres > 0) {
            return bounds;
        } else {
            return bounds;
        }
    }

    public static boolean isLatLngHigher(LatLng firstCoord, LatLng secondCoord) {
        double latDelta = firstCoord.getLatitude() - secondCoord.getLatitude();
        double lngDelta = firstCoord.getLongitude() - secondCoord.getLongitude();

        if ((latDelta >= 0 && lngDelta >= 0) && !(latDelta == 0 && lngDelta == 0)) {
            return true;
        }

        return false;
    }

    public static boolean isLatLngLower(LatLng firstCoord, LatLng secondCoord) {
        double latDelta = secondCoord.getLatitude() - firstCoord.getLatitude();
        double lngDelta = secondCoord.getLongitude() - firstCoord.getLongitude();

        if ((latDelta >= 0 && lngDelta >= 0) && !(latDelta == 0 && lngDelta == 0)) {
            return true;
        }

        return false;
    }

}
