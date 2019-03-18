package io.ona.kujaku.test.shadows;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

import io.ona.kujaku.views.KujakuMapView;

/**
 * This shadow is used to test some methods that can be invoked directly and do not need any of the ui
 * or other code to be called
 *
 * Created by Ephraim Kigamba - ekigamba@ona.io on 14/03/2019
 */
@Implements(KujakuMapView.class)
public class ShadowKujakuMapView extends ShadowMapView {

    @Implementation
    public void __constructor__(@NonNull Context context, @Nullable AttributeSet attrs) {
        // Do nothing
    }

}
