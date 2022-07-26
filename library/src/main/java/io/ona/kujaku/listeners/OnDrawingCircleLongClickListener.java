package io.ona.kujaku.listeners;

import androidx.annotation.NonNull;

import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.plugins.annotation.Circle;

public interface OnDrawingCircleLongClickListener {

    /**
     * Called when an annotation has been long clicked
     *
     * @param circle the circle clicked.
     */
    void onCircleLongClick(@NonNull Circle circle);

    /**
     * Called when no circle was clicked
     *
     */
    void onCircleNotLongClick(@NonNull LatLng latLng);
}
