package io.ona.kujaku.listeners;

import com.mapbox.geojson.Feature;

import java.util.List;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 29/11/2018
 */

public interface OnFeatureClickListener {

    /**
     * Called when a features(s) is clicked on the map and adheres to params passed in
     * {@link io.ona.kujaku.views.KujakuMapView#setOnFeatureClickListener(OnFeatureClickListener, String...)}
     * or {@link io.ona.kujaku.views.KujakuMapView#setOnFeatureClickListener(OnFeatureClickListener, com.mapbox.mapboxsdk.style.expressions.Expression, String...)}
     *
     * @param features
     */
    void onFeatureClick(List<Feature> features);
}
