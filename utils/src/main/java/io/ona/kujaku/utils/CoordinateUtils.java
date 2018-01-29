package io.ona.kujaku.utils;

import android.support.annotation.NonNull;

import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;

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

    /*public static boolean isLocationInBounds(@NonNull LatLng positionInQuestion, @NonNull LatLng topLeft, @NonNull LatLng topRight, @NonNull LatLng bottomRight, @NonNull LatLng bottomLeft) {
        if (positionInQuestion.getLatitude() <= latNorth
                && positionInQuestion.getLatitude() >= latSouth
                && positionInQuestion.getLongitude() <= lonEast
                && positionInQuestion.getLongitude() >= lonWest) {
            return true;
        }

        return false;
    }*/
}
