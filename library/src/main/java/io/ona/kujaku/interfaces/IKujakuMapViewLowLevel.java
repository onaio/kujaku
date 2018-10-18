package io.ona.kujaku.interfaces;

import android.support.annotation.Nullable;

import com.mapbox.mapboxsdk.geometry.LatLng;

import org.json.JSONObject;

import io.ona.kujaku.listeners.OnLocationChanged;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 26/09/2018
 */

public interface IKujakuMapViewLowLevel {

    /**
     * Enables/disables the marker layout so that the user can scroll to their preferred location
     *
     * @param canAddPoint
     */
    void enableAddPoint(boolean canAddPoint);

    /**
     * Call this to enable/disable adding a new point using GPS. In this layout the user cannot
     * scroll since the marker position is guided by the GPS updates. Disabling it disables the marker
     * layout and GPS querying.
     *
     * @param canAddPoint
     * @param onLocationChanged
     */
    void enableAddPoint(boolean canAddPoint, @Nullable OnLocationChanged onLocationChanged);

    /**
     * This should be called after calling {@link #enableAddPoint(boolean)} with {@code true} thus
     * disabling the marker layout, adding a point at the current marker position & returns a geoJSON
     * feature of the current marker position.
     *
     * @return
     */
    @Nullable JSONObject dropPoint();

    /**
     * This should be called after calling {@link #enableAddPoint(boolean, OnLocationChanged)} with {@code true} thus
     * disabling the marker layout, disabling GPS location updates, adding a point at @param latLng & returns a geoJSON
     * feature at @param latLng
     *
     * @param latLng
     * @return
     */
    @Nullable JSONObject dropPoint(@Nullable LatLng latLng);
}
