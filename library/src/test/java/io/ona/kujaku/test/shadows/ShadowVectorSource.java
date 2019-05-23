package io.ona.kujaku.test.shadows;

import com.mapbox.mapboxsdk.style.sources.VectorSource;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 05/11/2018
 */

@Implements(VectorSource.class)
public class ShadowVectorSource {

    @Implementation
    protected void initialize(String layerId, Object options) {
        // We should do nothing here
    }
}
