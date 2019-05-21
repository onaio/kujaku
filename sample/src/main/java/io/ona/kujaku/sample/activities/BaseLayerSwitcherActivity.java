package io.ona.kujaku.sample.activities;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;

import io.ona.kujaku.plugin.switcher.BaseLayerSwitcherPlugin;
import io.ona.kujaku.plugin.switcher.layer.SatelliteBaseLayer;
import io.ona.kujaku.plugin.switcher.layer.StreetsBaseLayer;
import io.ona.kujaku.sample.BuildConfig;
import io.ona.kujaku.sample.R;
import io.ona.kujaku.views.KujakuMapView;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 29/05/2019
 */

public class BaseLayerSwitcherActivity extends BaseNavigationDrawerActivity {

    private KujakuMapView kujakuMapView;
    private MapboxMap mapboxMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, BuildConfig.MAPBOX_SDK_ACCESS_TOKEN);

        kujakuMapView = findViewById(R.id.kmv_baseLayerSwitcher_mapView);
        kujakuMapView.onCreate(savedInstanceState);
        kujakuMapView.focusOnUserLocation(true);

        kujakuMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull MapboxMap mapboxMap) {
                BaseLayerSwitcherActivity.this.mapboxMap = mapboxMap;
                mapboxMap.setStyle(new Style.Builder().fromUrl("asset://base-layer-switcher-style.json"));

                mapboxMap.getStyle(new Style.OnStyleLoaded() {
                    @Override
                    public void onStyleLoaded(@NonNull Style style) {
                        BaseLayerSwitcherPlugin baseLayerSwitcherPlugin = new BaseLayerSwitcherPlugin(kujakuMapView, mapboxMap, style);
                        SatelliteBaseLayer satelliteBaseLayer = new SatelliteBaseLayer();
                        StreetsBaseLayer streetsBaseLayer = new StreetsBaseLayer(BaseLayerSwitcherActivity.this);

                        baseLayerSwitcherPlugin.addBaseLayer(satelliteBaseLayer, true);
                        baseLayerSwitcherPlugin.addBaseLayer(streetsBaseLayer, false);

                        baseLayerSwitcherPlugin.show();
                    }
                });
            }
        });
    }

    @Override
    protected int getContentView() {
        return R.layout.activity_base_layer_switcher;
    }

    @Override
    protected int getSelectedNavigationItem() {
        return R.id.nav_base_layer_switcher_plugin;
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
