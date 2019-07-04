package io.ona.kujaku.listeners;

import android.support.annotation.NonNull;

import io.ona.kujaku.layers.KujakuLayer;

/**
 * Created by Emmanuel Otin - eo@novel-t.ch on 19/06/2019
 */

public interface OnKujakuLayerLongClickListener {

    /**
     * Called when a kujakuLayer is long clicked on the map
     * *
     * @param kujakuLayer
     */
    void onKujakuLayerLongClick(@NonNull KujakuLayer kujakuLayer);
}
