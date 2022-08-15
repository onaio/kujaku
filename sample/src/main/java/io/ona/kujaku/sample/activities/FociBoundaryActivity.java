package io.ona.kujaku.sample.activities;

import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;
import android.util.Log;
import android.view.View;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;

import java.io.IOException;

import io.ona.kujaku.layers.BoundaryLayer;
import io.ona.kujaku.sample.BuildConfig;
import io.ona.kujaku.sample.R;
import io.ona.kujaku.utils.IOUtil;
import io.ona.kujaku.views.KujakuMapView;

public class FociBoundaryActivity extends BaseNavigationDrawerActivity {

    private static final String TAG = FociBoundaryActivity.class.getName();

    private KujakuMapView kujakuMapView;
    private FeatureCollection boundaryFeatureCollection1;
    private FeatureCollection boundaryFeatureCollection2;

    private LatLng focusPoint1;
    private LatLng focusPoint2;

    private BoundaryLayer boundaryLayer;

    private boolean showingBoundary1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, BuildConfig.MAPBOX_SDK_ACCESS_TOKEN);

        kujakuMapView = findViewById(R.id.kmv_fociBoundary_mapView);
        kujakuMapView.onCreate(savedInstanceState);

        try {
            boundaryFeatureCollection1 = FeatureCollection.fromJson(
                    IOUtil.readInputStreamAsString(getAssets().open("coast-province-and-mti_18.geojson"))
            );
            boundaryFeatureCollection2 = FeatureCollection.fromFeature(Feature.fromJson(
                    IOUtil.readInputStreamAsString(getAssets().open("alternative_boundary.geojson"))
            ));
            focusPoint1 = new LatLng(-14.105596638939968, 32.60676728350983);
            focusPoint2 = new LatLng(-14.157028852467521, 32.64508008956909);


            BoundaryLayer.Builder builder = new BoundaryLayer.Builder(boundaryFeatureCollection1)
                    .setLabelProperty("name")
                    .setLabelTextSize(20f)
                    .setLabelColorInt(Color.RED)
                    .setBoundaryColor(Color.RED)
                    .setBoundaryWidth(6f);

            boundaryLayer = builder.build();
            kujakuMapView.addLayer(boundaryLayer);
            showingBoundary1 = true;

            findViewById(R.id.btn_fociBoundary_changeBtn)
                    .setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (showingBoundary1) {
                                showingBoundary1 = false;
                                boundaryLayer.updateFeatures(boundaryFeatureCollection2);
                                changeFocus(focusPoint2);
                            } else {
                                showingBoundary1 = true;
                                boundaryLayer.updateFeatures(boundaryFeatureCollection1);
                                changeFocus(focusPoint1);
                            }
                        }
                    });

            kujakuMapView.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(MapboxMap mapboxMap) {
                    // Zoom to the position
                    mapboxMap.setStyle(Style.MAPBOX_STREETS);
                    mapboxMap.easeCamera(CameraUpdateFactory.newLatLngZoom(focusPoint1, 15d));
                }
            });
        } catch (IOException e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
    }

    private void changeFocus(@NonNull LatLng point) {
        kujakuMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull MapboxMap mapboxMap) {
                mapboxMap.easeCamera(CameraUpdateFactory.newLatLngZoom(point, 15d));
            }
        });
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
