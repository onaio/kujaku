package io.ona.kujaku.test.shadows;

import com.mapbox.mapboxsdk.style.layers.SymbolLayer;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 11/02/2019
 */

@Implements(SymbolLayer.class)
public class ShadowSymbolLayer extends ShadowLayer {

    @Implementation
    protected  void initialize(String layerId, String sourceId) {
        shadowLayerId = layerId;
    }
}
