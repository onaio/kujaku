package io.ona.kujaku.listeners;

import androidx.annotation.NonNull;

import com.mapbox.geojson.Point;
import com.mapbox.maps.plugin.annotation.generated.CircleAnnotation;

public interface OnDrawingCircleClickListener {

    /**
     * Called when an annotation has been long clicked
     *
     * @param circle the circle clicked.
     */
    void onCircleClick(@NonNull CircleAnnotation circle);

    /**
     * Called when no circle was clicked
     *
     */
    void onCircleNotClick(@NonNull Point latLng);
}
