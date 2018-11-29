package io.ona.kujaku.sample.activities;

import android.graphics.PointF;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import java.util.List;

import es.dmoral.toasty.Toasty;
import io.ona.kujaku.listeners.OnFeatureClickListener;
import io.ona.kujaku.sample.BuildConfig;
import io.ona.kujaku.sample.R;
import io.ona.kujaku.views.KujakuMapView;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 29/11/2018
 */

public class FeatureClickListenerActivity extends BaseNavigationDrawerActivity {

    private KujakuMapView kujakuMapView;
    private Toast currentToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, BuildConfig.MAPBOX_SDK_ACCESS_TOKEN);

        kujakuMapView = findViewById(R.id.kmv_featureClickListenerActivity_mapView);
        kujakuMapView.setStyleUrl("asset://basic-feature-select-style.json");

        showToast(Toasty.info(this, getString(R.string.feature_click_listener_activity_instructions)));

        kujakuMapView.setOnFeatureClickListener(new OnFeatureClickListener() {
            @Override
            public void onFeatureClick(List<Feature> features) {
                showToast(Toasty.success(FeatureClickListenerActivity.this, getString(R.string.building_exc)));
            }
        }, "building");
    }

    private void showToast(Toast newToast) {
        if (currentToast != null) {
            currentToast.cancel();
        }

        currentToast = newToast;
        currentToast.show();
    }

    @Override
    protected int getContentView() {
        return R.layout.activity_feature_click_listener;
    }

    @Override
    protected int getSelectedNavigationItem() {
        return R.id.nav_feature_click_listener;
    }
    @Override
    protected void onResume() {
        super.onResume();
        if (kujakuMapView != null) kujakuMapView.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (kujakuMapView != null) kujakuMapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (kujakuMapView != null) kujakuMapView.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (kujakuMapView != null) kujakuMapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (kujakuMapView != null) kujakuMapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (kujakuMapView != null) kujakuMapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (kujakuMapView != null) kujakuMapView.onLowMemory();
    }
}
