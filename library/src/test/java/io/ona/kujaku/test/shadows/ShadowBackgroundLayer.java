package io.ona.kujaku.test.shadows;

import com.mapbox.mapboxsdk.style.layers.BackgroundLayer;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 11/02/2019
 */

@Implements(BackgroundLayer.class)
public class ShadowBackgroundLayer extends ShadowLayer {

    @Implementation
    protected void initialize(String layerId) {
        shadowLayerId = layerId;
    }
}
