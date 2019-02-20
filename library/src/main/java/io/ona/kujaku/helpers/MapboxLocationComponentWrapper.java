package io.ona.kujaku.helpers;

import android.content.Context;
import android.support.annotation.NonNull;

import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapboxMap;

import io.ona.kujaku.exceptions.LocationComponentInitializationException;

/**
 * @author Vincent Karuri
 */
public class MapboxLocationComponentWrapper {

    private static LocationComponent locationComponent;

    private static MapboxLocationComponentWrapper mapboxLocationComponentWrapper;

    private MapboxLocationComponentWrapper() {}

    @SuppressWarnings( {"MissingPermission"})
    public static void init(@NonNull MapboxMap mapboxMap,@NonNull Context context) {
        mapboxLocationComponentWrapper = new MapboxLocationComponentWrapper();
        locationComponent = mapboxMap.getLocationComponent();
        locationComponent.activateLocationComponent(context, (LocationEngine) null);
        locationComponent.setLocationComponentEnabled(true);
        locationComponent.setCameraMode(CameraMode.NONE);
        locationComponent.setRenderMode(RenderMode.NORMAL);
    }

    public static MapboxLocationComponentWrapper getInstance() {
        if (mapboxLocationComponentWrapper == null) {
            throw new LocationComponentInitializationException("The MapboxLocationComponentWrapper has not been initialized! " +
                    "Please initialize it by calling init before calling the getInstance() method.");
        }
        return mapboxLocationComponentWrapper;
    }

    public LocationComponent getLocationComponent() {
       return locationComponent;
    }
}
