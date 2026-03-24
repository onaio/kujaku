package io.ona.kujaku.listeners;

import androidx.annotation.NonNull;

import com.mapbox.geojson.Point;

public interface OnSplittingLongClickListener {

    /**
     * Called when map is long clicked
     */
    void onSplittingLongClick(@NonNull Point latLng);
}
