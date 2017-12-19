package utils;

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
        if (positionInQuestion.getLatitude() <= myMapBounds.getLatNorth()
                && positionInQuestion.getLatitude() >= myMapBounds.getLatSouth()
                && positionInQuestion.getLongitude() <= myMapBounds.getLonEast()
                && positionInQuestion.getLongitude() >= myMapBounds.getLonWest()) {
            return true;
        }

        return false;
    }
}
