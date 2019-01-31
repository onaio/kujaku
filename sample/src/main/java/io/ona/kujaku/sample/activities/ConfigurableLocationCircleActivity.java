package io.ona.kujaku.sample.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.mapbox.mapboxsdk.Mapbox;

import io.ona.kujaku.sample.BuildConfig;
import io.ona.kujaku.sample.R;
import io.ona.kujaku.views.KujakuMapView;

public class ConfigurableLocationCircleActivity extends BaseNavigationDrawerActivity {

    private KujakuMapView kujakuMapView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, BuildConfig.MAPBOX_SDK_ACCESS_TOKEN);

        kujakuMapView = findViewById(R.id.configurable_circle_activity_map_view);
        kujakuMapView.showCurrentLocationBtn(true);

        Button btnIncreaseCircleRadius = findViewById(R.id.btn_increase_circle_radius);
        btnIncreaseCircleRadius.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                kujakuMapView.focusOnUserLocation(true, 70);
            }
        });

        Button btnDecreaseCircleRadius = findViewById(R.id.btn_decrease_circle_radius);
        btnDecreaseCircleRadius.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                kujakuMapView.focusOnUserLocation(true, 10);
            }
        });
    }


    @Override
    protected int getContentView() {
        return R.layout.activity_configurable_location_circle;
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

    @Override
    protected int getSelectedNavigationItem() {
        return R.id.nav_configurable_circle;
    }
}
