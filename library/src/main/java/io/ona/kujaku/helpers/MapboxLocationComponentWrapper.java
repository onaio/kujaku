package io.ona.kujaku.helpers;

import android.content.Context;
import android.support.annotation.NonNull;

import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapboxMap;

/**
 * @author Vincent Karuri
 */
public class MapboxLocationComponentWrapper {

    private LocationComponent locationComponent;

    @SuppressWarnings( {"MissingPermission"})
    public void init(@NonNull MapboxMap mapboxMap, @NonNull Context context) {
        locationComponent = mapboxMap.getLocationComponent();
        locationComponent.activateLocationComponent(context, (LocationEngine) null);
        locationComponent.setLocationComponentEnabled(true);
        locationComponent.setCameraMode(CameraMode.NONE);
        locationComponent.setRenderMode(RenderMode.NORMAL);
    }

    public LocationComponent getLocationComponent() {
       return locationComponent;
    }
}
