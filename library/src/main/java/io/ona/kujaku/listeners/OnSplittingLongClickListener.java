package io.ona.kujaku.listeners;

import android.support.annotation.NonNull;

import com.mapbox.mapboxsdk.geometry.LatLng;

public interface OnSplittingLongClickListener {

    /**
     * Called when map is long clicked
     */
    void onSplittingLongClick(@NonNull LatLng latLng);
}
