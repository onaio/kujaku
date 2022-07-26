package io.ona.kujaku.sample.activities;

import android.location.Location;
import android.os.Bundle;
import androidx.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.gson.Gson;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;

import org.json.JSONObject;

import es.dmoral.toasty.Toasty;
import io.ona.kujaku.callbacks.OnLocationServicesEnabledCallBack;
import io.ona.kujaku.listeners.OnLocationChanged;
import io.ona.kujaku.sample.R;
import io.ona.kujaku.views.KujakuMapView;

public class LowLevelLocationAddPointMapView extends BaseNavigationDrawerActivity {

    private static final String TAG = LowLevelLocationAddPointMapView.class.getName();
    private KujakuMapView kujakuMapView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        kujakuMapView = findViewById(R.id.kmv_lowLevelLocationAddPointMapView_mapView);
        kujakuMapView.onCreate(savedInstanceState);

        Button doneBtn = findViewById(R.id.btn_lowLevelLocationAddPointMapView_doneBtn);
        Button goToMyLocationBtn = findViewById(R.id.btn_lowLevelLocationAddPointMapView_myLocationBtn);

        doneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!kujakuMapView.isCanAddPoint()) {
                    Toasty.info(getApplicationContext(), getString(R.string.click_go_to_my_location_msg), Toast.LENGTH_LONG, true).show();
                }

                if (kujakuMapView.isCanAddPoint()) {
                    JSONObject featurePoint = kujakuMapView.dropPoint();
                    if (featurePoint != null) {
                        Log.e("FEATURE POINT", featurePoint.toString());
                    }
                }
            }
        });

        goToMyLocationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // callback
                OnLocationServicesEnabledCallBack onLocationServicesEnabledCallBack = new OnLocationServicesEnabledCallBack() {
                    @Override
                    public void onSuccess() {
                        kujakuMapView.enableAddPoint(true, new OnLocationChanged() {
                            @Override
                            public void onLocationChanged(Location location) {
                                Log.d(TAG, new Gson().toJson(location));
                            }
                        });
                    }
                };
                // warm gps
                kujakuMapView.setWarmGps(true,
                        "Location disabled",
                        "The add point features will not work. We cannot drop points without your location.",
                        onLocationServicesEnabledCallBack);
            }
        });

        kujakuMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull MapboxMap mapboxMap) {
                mapboxMap.setStyle(Style.MAPBOX_STREETS);
            }
        });
    }

    @Override
    protected int getContentView() {
        return R.layout.activity_low_level_location_add_point_map_view;
    }

    @Override
    protected int getSelectedNavigationItem() {
        return R.id.nav_low_level_location_add_point;
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