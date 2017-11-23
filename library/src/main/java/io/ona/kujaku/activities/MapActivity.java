package io.ona.kujaku.activities;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.maps.MapView;

import io.ona.kujaku.R;
import io.ona.kujaku.helpers.MapBoxStyleStorage;
import io.ona.kujaku.utils.Permissions;
import utils.Constants;

/**
 * This activity displays a MapView once provided with a a MapBox Access Key & String array.
 * These are passed as:
 *      {@link Constants#PARCELABLE_KEY_MAPBOX_ACCESS_TOKEN} - MapBox Access Token ({@code String})
 *      {@link Constants#PARCELABLE_KEY_MAPBOX_STYLES} - MapBox Styles ({@code String[]}
 *
 *
 *      <p>
 *      - MapBox Styles - String array containing <a href="https://www.mapbox.com/mapbox-gl-js/style-spec/">valid MapBox Style</a> at index 0
 *      <p>
 *
 * Created by Ephraim Kigamba - ekigamba@ona.io
 */
public class MapActivity extends AppCompatActivity {
    private static final int PERMISSIONS_REQUEST_CODE = 342;
    private MapView mapView;
    private String currentStylePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        checkPermissions();

        if (getIntent() != null) {
            Bundle bundle = getIntent().getExtras();
            if (bundle != null){
                String mapBoxAccessToken = bundle.getString(Constants.PARCELABLE_KEY_MAPBOX_ACCESS_TOKEN);
                String[] stylesArray = bundle.getStringArray(Constants.PARCELABLE_KEY_MAPBOX_STYLES);
                currentStylePath = "";

                if (stylesArray != null) {
                    currentStylePath = stylesArray[0];
                    if (currentStylePath != null && !currentStylePath.isEmpty()) {
                        currentStylePath = new MapBoxStyleStorage()
                                .getStyleURL(currentStylePath);
                    }
                }


                initMapBoxSdk(savedInstanceState, mapBoxAccessToken, currentStylePath);
            }
        }
    }

    private void initMapBoxSdk(Bundle savedInstanceState, String mapboxAccessToken, String mapBoxStylePath) {
        Mapbox.getInstance(this, mapboxAccessToken);
        mapView = (MapView) findViewById(R.id.map_view);
        mapView.onCreate(savedInstanceState);

        if (!mapBoxStylePath.isEmpty()) {
            mapView.setStyleUrl(mapBoxStylePath);
        }
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

        if (currentStylePath != null && currentStylePath.startsWith("file://")) {
            new MapBoxStyleStorage()
                    .deleteFile(currentStylePath.replace("file://", ""), true);
        }

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
