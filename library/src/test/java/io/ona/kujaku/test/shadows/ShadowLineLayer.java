package io.ona.kujaku.test.shadows;

import com.mapbox.mapboxsdk.style.layers.LineLayer;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 11/02/2019
 */

@Implements(LineLayer.class)
public class ShadowLineLayer extends ShadowLayer {

    @Implementation
    protected void initialize(String layerId, String sourceId) {
        // Do nothing here
    }
}
