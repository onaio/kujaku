package io.ona.kujaku.sample.activities;

import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;

import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;

import java.io.File;

import io.ona.kujaku.plugin.switcher.BaseLayerSwitcherPlugin;
import io.ona.kujaku.plugin.switcher.layer.MBTilesLayer;
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
       // kujakuMapView.focusOnUserLocation(true, RenderMode.COMPASS);

        kujakuMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull MapboxMap mapboxMap) {
                BaseLayerSwitcherActivity.this.mapboxMap = mapboxMap;
                mapboxMap.setStyle(new Style.Builder().fromUrl("asset://base-layer-switcher-style.json"));

                mapboxMap.getStyle(new Style.OnStyleLoaded() {
                    @Override
                    public void onStyleLoaded(@NonNull Style style) {
                        BaseLayerSwitcherPlugin baseLayerSwitcherPlugin = new BaseLayerSwitcherPlugin(kujakuMapView, style);
                        SatelliteBaseLayer satelliteBaseLayer = new SatelliteBaseLayer();
                        StreetsBaseLayer streetsBaseLayer = new StreetsBaseLayer(BaseLayerSwitcherActivity.this);
                        String[] paths = { Environment.getExternalStorageDirectory().getPath() + "/Download/katete_2019_mt6.mbtiles",
                                Environment.getExternalStorageDirectory().getPath() + "/Download/Chadiza_east.mbtiles",
                                Environment.getExternalStorageDirectory().getPath() + "/Download/Chadiza_east_1.mbtiles",
                               };


                        baseLayerSwitcherPlugin.addBaseLayer(satelliteBaseLayer, true);
                        baseLayerSwitcherPlugin.addBaseLayer(streetsBaseLayer, false);

                        for (String path : paths) {
                            MBTilesLayer mbTilesLayer = new MBTilesLayer(BaseLayerSwitcherActivity.this, new File(path), kujakuMapView.getMbTilesHelper());
                            baseLayerSwitcherPlugin.addBaseLayer(mbTilesLayer, false);
                        }

                        baseLayerSwitcherPlugin.show();

                        CameraPosition cameraPosition = new CameraPosition.Builder()
                                .target(new LatLng(-14.1666, 32.4794))
                                .zoom(15)
                                .build();

                        mapboxMap.setCameraPosition(cameraPosition);
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
