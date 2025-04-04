package io.ona.kujaku.listeners;

import android.graphics.Point;

import androidx.annotation.NonNull;

import com.mapbox.maps.plugin.annotation.generated.CircleAnnotation;

public interface OnDrawingCircleLongClickListener {

    /**
     * Called when an annotation has been long clicked
     *
     * @param circle the circle clicked.
     */
    void onCircleLongClick(@NonNull CircleAnnotation circle);

    /**
     * Called when no circle was clicked
     *
     */
    void onCircleNotLongClick(@NonNull Point latLng);
}
