package io.ona.kujaku.sample.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;

import com.mapbox.geojson.FeatureCollection;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

import java.io.IOException;

import io.ona.kujaku.layers.BoundaryLayer;
import io.ona.kujaku.sample.R;
import io.ona.kujaku.utils.IOUtil;
import io.ona.kujaku.views.KujakuMapView;

public class FociBoundaryActivity extends BaseNavigationDrawerActivity {

    private static final String TAG = FociBoundaryActivity.class.getName();

    private KujakuMapView kujakuMapView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        kujakuMapView = findViewById(R.id.kmv_fociBoundary_mapView);
        kujakuMapView.onCreate(savedInstanceState);

        try {
            FeatureCollection featureCollection = FeatureCollection.fromJson(
                    IOUtil.readInputStreamAsString(getAssets().open("coast-province-and-mti_18.geojson"))
            );

            BoundaryLayer.Builder builder = new BoundaryLayer.Builder(featureCollection)
                    .setLabelProperty("name")
                    .setLabelTextSize(20f)
                    .setLabelColorInt(Color.RED)
                    .setBoundaryColor(Color.RED)
                    .setBoundaryWidth(6f);

            kujakuMapView.addLayer(builder.build());

            kujakuMapView.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(MapboxMap mapboxMap) {
                    // Zoom to the position
                    mapboxMap.easeCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(-14.105596638939968, 32.60676728350983), 8d));
                }
            });
        } catch (IOException e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
    }

    @Override
    protected int getContentView() {
        return R.layout.activity_foci_boundary;
    }

    @Override
    protected int getSelectedNavigationItem() {
        return R.id.nav_foci_boundary;
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
