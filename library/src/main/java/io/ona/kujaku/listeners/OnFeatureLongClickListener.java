package io.ona.kujaku.listeners;

import androidx.annotation.NonNull;

import com.mapbox.geojson.Feature;

import java.util.List;

/**
 * Created by Richard Kareko on 5/18/20.
 */

public interface OnFeatureLongClickListener {

    /**
     * Called when a features(s) is long clicked on the map and adheres to params passed in
     * {@link io.ona.kujaku.views.KujakuMapView#setOnFeatureLongClickListener(OnFeatureLongClickListener, String...)}
     * or {@link io.ona.kujaku.views.KujakuMapView#setOnFeatureLongClickListener(OnFeatureLongClickListener, com.mapbox.mapboxsdk.style.expressions.Expression, String...)}
     *
     * @param features
     */
    void onFeatureLongClick(@NonNull List<Feature> features);
}
