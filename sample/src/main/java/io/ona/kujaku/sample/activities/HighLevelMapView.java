package io.ona.kujaku.sample.activities;

import android.os.Bundle;
import androidx.annotation.NonNull;
import android.util.Log;

import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;

import org.json.JSONObject;

import io.ona.kujaku.callbacks.AddPointCallback;
import io.ona.kujaku.sample.R;
import io.ona.kujaku.views.KujakuMapView;

public class HighLevelMapView extends BaseNavigationDrawerActivity {

    private static final String TAG = HighLevelMapView.class.getName();

    private KujakuMapView kujakuMapView;
    private boolean canAddPoint = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        kujakuMapView = findViewById(R.id.kmv_highLevelMapView_mapView);
        kujakuMapView.onCreate(savedInstanceState);
        kujakuMapView.addPoint(false, new AddPointCallback() {
            @Override
            public void onPointAdd(JSONObject jsonObject) {
                Log.d(TAG, jsonObject.toString());
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "User cancelled adding points");
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
        return R.layout.activity_high_level_map_view;
    }

    @Override
    protected int getSelectedNavigationItem() {
        return R.id.nav_high_level_add_point;
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
