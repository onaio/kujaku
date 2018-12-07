package io.ona.kujaku.test.shadows;

import android.content.Context;

import com.mapbox.mapboxsdk.maps.renderer.MapRenderer;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 06/12/2018
 */
@Implements(MapRenderer.class)
public class ShadowMapRenderer {

    @Implementation
    public void __constructor__(Context context, String localIdeographFontFamily) {
        // Nothing should happen in the constructor
    }
}
