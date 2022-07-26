package io.ona.kujaku.listeners;

import androidx.annotation.NonNull;

import com.mapbox.mapboxsdk.geometry.LatLng;

public interface OnSplittingClickListener {

    /**
     * Called when map is clicked
     */
    void onSplittingClick(@NonNull LatLng latLng);
}
