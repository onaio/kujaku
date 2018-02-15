package io.ona.kujaku.utils;

import android.support.annotation.NonNull;
import com.mapbox.mapboxsdk.geometry.LatLng;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 19/12/2017.
 */
public class CoordinateUtils {

    /**
     * Checks whether the provided coordinate is within the provided Bounds.
     *
     * <strong>NOTE: The order in which you provide the bounds does not affect the results</strong>
     *
     * @param positionInQuestion
     * @param topLeft
     * @param topRight
     * @param bottomLeft
     * @param bottomRight
     * @return
     */
    public static boolean isLocationInBounds(@NonNull LatLng positionInQuestion, LatLng topLeft, LatLng topRight, LatLng bottomLeft, LatLng bottomRight) {
        Point[] bounds = new Point[4];
        bounds[0] = new Point(topLeft.getLongitude(), topLeft.getLatitude());
        bounds[1] = new Point(topRight.getLongitude(), topRight.getLatitude());
        bounds[2] = new Point(bottomLeft.getLongitude(), bottomLeft.getLatitude());
        bounds[3] = new Point(bottomRight.getLongitude(), bottomRight.getLatitude());

        return contains(bounds, new Point(positionInQuestion.getLongitude(), positionInQuestion.getLatitude()));
    }

    /**
     * Return true if the given point is contained inside the boundary.
     * See: http://www.ecse.rpi.edu/Homepages/wrf/Research/Short_Notes/pnpoly.html
     * @param needle The point to check
     * @return true if the point is inside the boundary, false otherwise
     *
     */
    public static boolean contains(Point[] bounds, Point needle) {
        int i;
        int j;
        boolean result = false;
        for (i = 0, j = bounds.length - 1; i < bounds.length; j = i++) {
            if ((bounds[i].y > needle.y) != (bounds[j].y > needle.y) &&
                    (needle.x < (bounds[j].x - bounds[i].x) * (needle.y - bounds[i].y) / (bounds[j].y-bounds[i].y) + bounds[i].x)) {
                result = !result;
            }
        }
        return result;
    }

    static class Point {
        public final double x;
        public final double y;

        public Point(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }
}
