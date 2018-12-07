package io.ona.kujaku.test.shadows;

import android.content.Context;
import android.opengl.GLSurfaceView;

import com.mapbox.mapboxsdk.maps.renderer.glsurfaceview.GLSurfaceViewMapRenderer;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 06/12/2018
 */
@Implements(GLSurfaceViewMapRenderer.class)
public class ShadowGLSurfaceViewMapRenderer extends ShadowMapRenderer {

    @Implementation
    public void __constructor__(Context context, GLSurfaceView glSurfaceView, String localIdeographFontFamily) {
        // Nothing should happen in the constructor
    }
}
