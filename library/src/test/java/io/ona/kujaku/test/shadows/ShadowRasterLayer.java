package io.ona.kujaku.test.shadows;

import com.mapbox.mapboxsdk.style.layers.RasterLayer;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 11/02/2019
 */

@Implements(RasterLayer.class)
public class ShadowRasterLayer extends ShadowLayer {

    @Implementation
    protected void initialize(String layerId, String sourceId) {
        shadowLayerId = layerId;
        System.err.println("initialize called");
    }
}
