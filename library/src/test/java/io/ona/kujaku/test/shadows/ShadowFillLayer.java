package io.ona.kujaku.test.shadows;

import com.mapbox.mapboxsdk.style.layers.FillLayer;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/**
 * Created by Emmanuel OTIN - eo@novel-t.ch on 24/06/2019
 */

@Implements(FillLayer.class)
public class ShadowFillLayer extends ShadowLayer {

    @Implementation
    protected void initialize(String layerId, String sourceId) {
        // Do nothing here
    }
}
