package io.ona.kujaku.helpers;

import android.content.Context;

import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapboxMap;

/**
 * @author Vincent Karuri
 */
public class MapboxLocationComponentWrapper {

    private static LocationComponent locationComponent;

    private MapboxLocationComponentWrapper() {}

    @SuppressWarnings( {"MissingPermission"})
    public static void init(MapboxMap mapboxMap, Context context) {
        locationComponent = mapboxMap.getLocationComponent();
        locationComponent.activateLocationComponent(context, (LocationEngine) null);
        locationComponent.setLocationComponentEnabled(true);
        locationComponent.setCameraMode(CameraMode.TRACKING);
        locationComponent.setRenderMode(RenderMode.NORMAL);
    }

    public static LocationComponent getLocationComponent() {
        return locationComponent;
    }
}
