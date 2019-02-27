package io.ona.kujaku.layers;

import android.support.annotation.NonNull;

import com.mapbox.mapboxsdk.maps.MapboxMap;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 18/02/2019
 */
public interface KujakuLayer {

    /**
     * Adds the layer to a {@link MapboxMap}
     *
     * @param mapboxMap
     */
    void addLayerToMap(@NonNull MapboxMap mapboxMap);

    /**
     * Enables the layer on the {@link MapboxMap} in case it is not already
     * {@link com.mapbox.mapboxsdk.style.layers.Property#VISIBLE}. If the layer is not on the map,
     * nothing happens
     *
     * @param mapboxMap
     */
    void enableLayerOnMap(@NonNull MapboxMap mapboxMap);

    /**
     * Disables the layer if it is already on the {@link MapboxMap}
     *
     * @param mapboxMap
     */
    void disableLayerOnMap(@NonNull MapboxMap mapboxMap);

    /**
     * Used to check if the layer has been enabled on the {@link MapboxMap}
     *
     * @return {@code true} if the layer is enabled, {@code false} otherwise
     */
    boolean isVisible();
}