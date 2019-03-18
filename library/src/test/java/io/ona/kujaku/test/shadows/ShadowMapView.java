package io.ona.kujaku.test.shadows;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

import com.mapbox.mapboxsdk.maps.MapView;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.shadows.ShadowViewGroup;

/**
 * This shadow is used to test some methods that can be invoked directly and do not need any of the ui
 * or other code to be called
 *
 * Created by Ephraim Kigamba - ekigamba@ona.io on 14/03/2019
 */
@Implements(MapView.class)
public class ShadowMapView extends ShadowViewGroup {

    @Implementation
    public void __constructor__(@NonNull Context context, @Nullable AttributeSet attrs) {
        // Do nothing
    }

}
