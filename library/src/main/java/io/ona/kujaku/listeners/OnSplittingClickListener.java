package io.ona.kujaku.listeners;

import androidx.annotation.NonNull;

import com.mapbox.geojson.Point;


public interface OnSplittingClickListener {

    /**
     * Called when map is clicked
     */
    void onSplittingClick(@NonNull Point latLng);
}
