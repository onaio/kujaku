package io.ona.kujaku.listeners;

import androidx.annotation.NonNull;

import io.ona.kujaku.layers.KujakuLayer;

/**
 * Created by Emmanuel Otin - eo@novel-t.ch on 19/06/2019
 */

public interface OnKujakuLayerClickListener {

    /**
     * Called when a kujakuLayer is clicked on the map
     * *
     * @param kujakuLayer
     */
    void onKujakuLayerClick(@NonNull KujakuLayer kujakuLayer);
}
