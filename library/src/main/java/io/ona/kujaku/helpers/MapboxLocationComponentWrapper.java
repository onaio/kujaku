package io.ona.kujaku.helpers;

import static com.mapbox.maps.plugin.Plugin.MAPBOX_LOCATION_COMPONENT_PLUGIN_ID;

import android.content.Context;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mapbox.maps.MapView;
import com.mapbox.maps.plugin.LocationPuck2D;
import com.mapbox.maps.plugin.locationcomponent.LocationComponentPlugin;

import io.ona.kujaku.callbacks.OnLocationComponentInitializedCallback;

/**
 * @author Vincent Karuri
 */
public class MapboxLocationComponentWrapper {

    private LocationComponentPlugin locationComponent;
    private OnLocationComponentInitializedCallback onLocationComponentInitializedCallback;
    /**
     * Init Location Component Wrapper
     *
     * @param mapView {@link MapView}
     * @param context
     * @param showBearing whether to show bearing indicator
     */
    @SuppressWarnings({"MissingPermission"})
    public void init(@NonNull MapView mapView, @NonNull Context context, boolean showBearing) {
        locationComponent = mapView.getPlugin(MAPBOX_LOCATION_COMPONENT_PLUGIN_ID);

        LocationPuck2D locationPuck = new LocationPuck2D();

        PackageManager pm = context.getPackageManager();
        boolean hasCompass = pm != null && pm.hasSystemFeature(PackageManager.FEATURE_SENSOR_COMPASS);

        locationComponent.setLocationPuck(locationPuck);
        locationComponent.setEnabled(true);

        if (hasCompass && showBearing) {
            locationPuck.setBearingImage(null);
            locationComponent.setPulsingEnabled(true);
        }

        if (onLocationComponentInitializedCallback != null) {
            onLocationComponentInitializedCallback.onLocationComponentInitialized();
        }
    }

    /**
     * Get the location component
     * @return LocationComponentPlugin instance
     */
    @Nullable
    public LocationComponentPlugin getLocationComponent() {
        return locationComponent;
    }

    /**
     * Set callback for when location component is initialized
     * @param callback OnLocationComponentInitializedCallback
     */
    public void setOnLocationComponentInitializedCallback(OnLocationComponentInitializedCallback callback) {
        this.onLocationComponentInitializedCallback = callback;
    }
}