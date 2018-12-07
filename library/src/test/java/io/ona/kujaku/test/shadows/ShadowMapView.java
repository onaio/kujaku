package io.ona.kujaku.test.shadows;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;

import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

import org.robolectric.Robolectric;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.shadows.ShadowView;
import org.robolectric.shadows.ShadowViewGroup;
import org.robolectric.util.ReflectionHelpers;

import io.ona.kujaku.R;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 06/12/2018
 */
@Implements(MapView.class)
public class ShadowMapView extends ShadowViewGroup {

    @Implementation
    public void __constructor__(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.mapbox_mapview_internal, null);
    }

    @Implementation
    public void getMapAsync(final @NonNull OnMapReadyCallback callback) {

    }
}
