package io.ona.kujaku.manager;

import android.support.annotation.NonNull;

import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.annotation.CircleManager;
import com.mapbox.mapboxsdk.plugins.annotation.FillManager;
import com.mapbox.mapboxsdk.plugins.annotation.LineManager;

import io.ona.kujaku.views.KujakuMapView;

public class AnnotationRepositoryManager {

    private static FillManager fillManager;
    private static LineManager lineManager;
    private static CircleManager circleManager;

    public static FillManager getFillManagerInstance(@NonNull KujakuMapView mapView, @NonNull MapboxMap mapboxMap, @NonNull Style style) {
        if (fillManager == null) {
            fillManager = new FillManager(mapView, mapboxMap, style);
        }

        return fillManager;
    }

    public static LineManager getLineManagerInstance(@NonNull KujakuMapView mapView, @NonNull MapboxMap mapboxMap, @NonNull Style style) {
        if (lineManager == null) {
            lineManager = new LineManager(mapView, mapboxMap, style);
        }

        return lineManager;
    }

    public static CircleManager getCircleManagerInstance(@NonNull KujakuMapView mapView, @NonNull MapboxMap mapboxMap, @NonNull Style style) {
        if (circleManager == null) {
            circleManager = new CircleManager(mapView, mapboxMap, style);
        }

        return circleManager;
    }

    public static void onStop() {
        AnnotationRepositoryManager.fillManager = null;
        AnnotationRepositoryManager.lineManager = null;
        AnnotationRepositoryManager.circleManager = null;
    }
}
