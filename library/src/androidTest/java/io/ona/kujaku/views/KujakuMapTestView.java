package io.ona.kujaku.views;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.AttributeSet;

import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.VisibleRegion;
import com.mapbox.mapboxsdk.maps.MapboxMapOptions;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 05/11/2018
 */

public class KujakuMapTestView extends KujakuMapView {

    public boolean isMapCentered = false;
    public boolean enableAddPointIsCalled = false;

    private VisibleRegion visibleRegion;

    public KujakuMapTestView(@NonNull Context context) {
        super(context);
    }

    public KujakuMapTestView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public KujakuMapTestView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public KujakuMapTestView(@NonNull Context context, @Nullable MapboxMapOptions options) {
        super(context, options);
    }

    @Override
    public void centerMap(@NonNull LatLng point, int animateToNewTargetDuration) {
        isMapCentered = true;
    }

    @Override
    public void centerMap(@NonNull LatLng point, int animateToNewTargetDuration, double newZoom) {
        isMapCentered = true;
    }

    public void setVisibleRegion(VisibleRegion visibleRegion) {
        this.visibleRegion = visibleRegion;
    }

    @Nullable
    @Override
    protected VisibleRegion getCurrentBounds() {
        return visibleRegion;
    }
}
