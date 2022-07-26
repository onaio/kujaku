package io.ona.kujaku.sample.activities;

import android.os.Bundle;
import androidx.annotation.NonNull;
import android.util.Log;

import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;

import io.ona.kujaku.exceptions.WmtsCapabilitiesException;
import io.ona.kujaku.listeners.WmtsCapabilitiesListener;
import io.ona.kujaku.sample.BuildConfig;
import io.ona.kujaku.sample.R;
import io.ona.kujaku.services.WmtsCapabilitiesService;
import io.ona.kujaku.wmts.model.WmtsCapabilities;
import io.ona.kujaku.views.KujakuMapView;

public class WmtsActivity extends BaseNavigationDrawerActivity {

    private static final String TAG = WmtsActivity.class.getName();

    private KujakuMapView kujakuMapView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, BuildConfig.MAPBOX_SDK_ACCESS_TOKEN);

        kujakuMapView = findViewById(R.id.wmts_mapView);

        WmtsCapabilitiesService wmtsService = new WmtsCapabilitiesService(getString(R.string.wmts_capabilities_url));
        wmtsService.requestData();
        wmtsService.setListener(new WmtsCapabilitiesListener() {
            @Override
            public void onCapabilitiesReceived(WmtsCapabilities capabilities) {
                try {
                    kujakuMapView.addWmtsLayer(capabilities);
                }
                catch (WmtsCapabilitiesException ex) {
                    Log.e(TAG, "A WmtsCapabilitiesException occurs", ex);
                }
            }

            @Override
            public void onCapabilitiesError(Exception ex) {
                Log.e(TAG,"Capabilities Exception", ex);
            }
        });

        kujakuMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull MapboxMap mapboxMap) {
                mapboxMap.setStyle(Style.MAPBOX_STREETS);
                mapboxMap.easeCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(30.96088179449847
                        , -99.1250589414584)
                        , 5.441840862122009)
                );
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (kujakuMapView != null) kujakuMapView.onResume();
    }

    @Override
    protected int getContentView() {
        return R.layout.wmts_map_view;
    }

    @Override
    protected int getSelectedNavigationItem() {
        return R.id.nav_wmts_activity;
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
