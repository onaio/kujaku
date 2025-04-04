package io.ona.kujaku.manager;

import androidx.annotation.NonNull;

import com.mapbox.maps.MapboxMap;
import com.mapbox.maps.Style;
import com.mapbox.maps.plugin.annotation.generated.CircleAnnotationManager;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager;
import com.mapbox.maps.plugin.annotation.generated.PolylineAnnotationManager;

import io.ona.kujaku.views.KujakuMapView;

public class AnnotationRepositoryManager {

    private static PointAnnotationManager fillManager;
    private static PolylineAnnotationManager lineManager;
    private static CircleAnnotationManager circleManager;

    public static PointAnnotationManager getFillManagerInstance(@NonNull KujakuMapView mapView, @NonNull MapboxMap mapboxMap, @NonNull Style style) {
        if (fillManager == null) {
            /* TODO Refactor this
            fillManager = new PointAnnotationManager(mapView, mapboxMap, style);*/
        }

        return fillManager;
    }

    public static PolylineAnnotationManager getLineManagerInstance(@NonNull KujakuMapView mapView, @NonNull MapboxMap mapboxMap, @NonNull Style style) {
        if (lineManager == null) {
            /*TODO Refactor this
            lineManager = new PolylineAnnotationManager(mapView, mapboxMap, style);*/
        }

        return lineManager;
    }

    public static CircleAnnotationManager getCircleManagerInstance(@NonNull KujakuMapView mapView, @NonNull MapboxMap mapboxMap, @NonNull Style style) {
        if (circleManager == null) {
            /*TODO Refactor this
            circleManager = new CircleAnnotationManager(mapView, mapboxMap, style);*/
        }

        return circleManager;
    }

    public static void onStop() {
        AnnotationRepositoryManager.fillManager = null;
        AnnotationRepositoryManager.lineManager = null;
        AnnotationRepositoryManager.circleManager = null;
    }
}
