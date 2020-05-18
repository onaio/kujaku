package io.ona.kujaku.listeners;

import com.mapbox.geojson.Feature;

import java.util.List;

/**
 * Created by Richard Kareko on 5/18/20.
 */

public interface OnFeatureLongClickListener {

    /**
     * Called when a features(s) is long clicked on the map and adheres to params passed in
     * {@link io.ona.kujaku.views.KujakuMapView#setOnFeatureClickListener(OnFeatureClickListener, String...)}
     * or {@link io.ona.kujaku.views.KujakuMapView#setOnFeatureClickListener(OnFeatureClickListener, com.mapbox.mapboxsdk.style.expressions.Expression, String...)}
     *
     * @param features
     */
    void onFeatureLongClick(List<Feature> features);
}
