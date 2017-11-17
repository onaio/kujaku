package io.ona.kujaku.activities;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.maps.MapView;

import io.ona.kujaku.R;
import io.ona.kujaku.utils.Permissions;
import utils.Constants;

public class MapActivity extends AppCompatActivity {
    private static final int PERMISSIONS_REQUEST_CODE = 342;
    private MapView mapView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        checkPermissions();

        if (getIntent() != null) {
            Bundle bundle = getIntent().getExtras();
            if (bundle != null){
                initMapBoxSdk(savedInstanceState, bundle.getString(Constants.PARCELABLE_KEY_MAPBOX_ACCESS_TOKEN));
            }
        }
    }

    private void initMapBoxSdk(Bundle savedInstanceState, String mapboxAccessToken) {
        Mapbox.getInstance(this, mapboxAccessToken);
        mapView = (MapView) findViewById(R.id.map_view);
        mapView.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mapView != null) mapView.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mapView != null) mapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mapView != null) mapView.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mapView != null) mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mapView != null) mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mapView != null) mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mapView != null) mapView.onLowMemory();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            checkPermissions();
        }
    }

    private void checkPermissions() {
        String[] unauthorizedPermissions = Permissions.getUnauthorizedCriticalPermissions(this);
        if (unauthorizedPermissions.length > 0) {
            Permissions.request(this, unauthorizedPermissions, PERMISSIONS_REQUEST_CODE);
        }
    }
}
