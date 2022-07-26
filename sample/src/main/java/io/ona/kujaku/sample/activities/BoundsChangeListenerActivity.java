package io.ona.kujaku.sample.activities;

import android.os.Bundle;
import androidx.annotation.NonNull;
import android.widget.TextView;

import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;

import java.text.DecimalFormat;

import io.ona.kujaku.listeners.BoundsChangeListener;
import io.ona.kujaku.sample.R;
import io.ona.kujaku.views.KujakuMapView;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 27/11/2018
 */

public class BoundsChangeListenerActivity extends BaseNavigationDrawerActivity {

    private KujakuMapView kujakuMapView;
    private TextView topRightBound;
    private TextView topLeftBound;
    private TextView bottomRightBound;
    private TextView bottomLeftBound;

    private DecimalFormat sixDForm;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        kujakuMapView = findViewById(R.id.kmv_boundingBoxListener_mapView);
        kujakuMapView.onCreate(savedInstanceState);

        topRightBound = findViewById(R.id.tv_boundingBoxListener_topRightCoordinate);
        topLeftBound = findViewById(R.id.tv_boundingBoxListener_topLeftCoordinate);
        bottomRightBound = findViewById(R.id.tv_boundingBoxListener_bottomRightCoordinate);
        bottomLeftBound = findViewById(R.id.tv_boundingBoxListener_bottomLeftCoordinate);

        sixDForm = new DecimalFormat("0.######");
        String latLongFormat = getString(R.string.lat_long_format);

        kujakuMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull MapboxMap mapboxMap) {
                mapboxMap.setStyle(Style.MAPBOX_STREETS);
            }
        });
        kujakuMapView.setBoundsChangeListener(new BoundsChangeListener() {
            @Override
            public void onBoundsChanged(LatLng topLeft, LatLng topRight, LatLng bottomRight, LatLng bottomLeft) {
                topRightBound.setText(
                        String.format(latLongFormat, topRight.getLatitude(), topRight.getLongitude())
                );
                topLeftBound.setText(
                        String.format(latLongFormat, topLeft.getLatitude(), topLeft.getLongitude())
                );

                bottomLeftBound.setText(
                        String.format(latLongFormat, bottomLeft.getLatitude(), bottomLeft.getLongitude())
                );

                bottomRightBound.setText(
                        String.format(latLongFormat, bottomRight.getLatitude(), bottomRight.getLongitude())
                );
            }
        });
    }

    @Override
    protected int getContentView() {
        return R.layout.activity_bounding_box_listener_activity;
    }

    @Override
    protected int getSelectedNavigationItem() {
        return R.id.nav_bounding_box_listener;
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
