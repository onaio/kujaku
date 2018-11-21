package io.ona.kujaku.interfaces;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 26/09/2018
 */

import android.support.annotation.NonNull;

import java.util.List;

import io.ona.kujaku.callbacks.AddPointCallback;
import io.ona.kujaku.domain.Point;

public interface IKujakuMapView extends IKujakuMapViewLowLevel {

    /**
     * Enables adding a point on the map. You can either use GPS by passing a {@code true} on @param useGPS
     * which disables the scrolling and animates the location updates on the map while showing the location accuracy
     * until the user selects a certain position.
     *
     * @param useGPS
     * @param addPointCallback returns the location chosen by user from GPS or calls {@link AddPointCallback#onCancel()}
     *                         if the user cancels the operation
     */
    void addPoint(boolean useGPS, @NonNull AddPointCallback addPointCallback);

    void showCurrentLocationBtn(boolean isVisible);

    void focusOnUserLocation(boolean focusOnMyLocation);

    /**
     * This function updates the list of points displayed in KujakuMapView
     *
     * This is done both in the internal {@link List<Point>} data structure and visually on the map using location markers
     *
     * @param points is a list of {@link Point}
     */
    void updateDroppedPoints(List<Point> points);
}
