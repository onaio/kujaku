package io.ona.kujaku.interfaces;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 26/09/2018
 */

import android.support.annotation.NonNull;

import io.ona.kujaku.callbacks.AddPointCallback;

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
}
