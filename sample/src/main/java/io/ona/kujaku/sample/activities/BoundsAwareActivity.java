package io.ona.kujaku.sample.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.style.expressions.Expression;
import com.mapbox.mapboxsdk.style.layers.Layer;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import io.ona.kujaku.listeners.BoundsChangeListener;
import io.ona.kujaku.sample.BuildConfig;
import io.ona.kujaku.sample.R;
import io.ona.kujaku.views.KujakuMapView;

import static io.ona.kujaku.sample.utils.TestDataUtils.createFeatureList;
import static io.ona.kujaku.sample.utils.TestDataUtils.generateMapBoxLayer;

public class BoundsAwareActivity extends BaseNavigationDrawerActivity {

    private final String TAG = BoundsAwareActivity.class.getName();

    private KujakuMapView kujakuMapView;

    private boolean isBoundsAware = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, BuildConfig.MAPBOX_SDK_ACCESS_TOKEN);

        kujakuMapView = findViewById(R.id.bounds_aware_activity_map_view);
        kujakuMapView.setBoundsChangeListener(new BoundsChangeListener() {
            @Override
            public void onBoundsChanged(LatLng topLeft, LatLng topRight, LatLng bottomRight, LatLng bottomLeft) {
                if (isBoundsAware) {
                    Log.i(TAG, "Recording bounds change!");
                    addPointsToMap(calculateCenterCoordinates(topLeft, bottomRight));
                }
            }
        });

        Button btnToggleBoundsListener = findViewById(R.id.btn_toggle_bounds_listener);
        btnToggleBoundsListener.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isBoundsAware = !isBoundsAware;
                ((Button) v).setText(!isBoundsAware ? R.string.enable_bounds_listener : R.string.disable_bounds_listener);
            }
        });

        initializeGenericLayer();
    }


    private LatLng calculateCenterCoordinates(LatLng topLeft, LatLng bottomRight) {
        double longitude = (topLeft.getLongitude() + bottomRight.getLongitude()) / 2.0;
        double latitude = (topLeft.getLatitude() + bottomRight.getLatitude()) / 2.0;
        return new LatLng(latitude, longitude);
    }

    private void initializeGenericLayer() {
        kujakuMapView.initializePrimaryGeoJsonSource("kujaku_primary_source", false, null);
        Layer circleLayer = generateMapBoxLayer("kujaku_primary_layer", kujakuMapView.getPrimaryGeoJsonSource().getId());
        kujakuMapView.setPrimaryLayer(circleLayer);
        // set camera position
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(-1.284956, 36.768831))
                .zoom(16)
                .build();
        kujakuMapView.setCameraPosition(cameraPosition);
    }

    private void addPointsToMap(LatLng latLng) {
        try {
            final String[] featureGroup = {"White", "Black", "Hispanic", "Asian", "Other"};
            List<Feature> existingFeatures = new ArrayList<>(kujakuMapView.getPrimaryGeoJsonSource().querySourceFeatures(Expression.all()));
            List<Feature> newFeatures = createFeatureList(20, existingFeatures.size(), latLng.getLongitude(), latLng.getLatitude(), "ethnicity", "Point", false, featureGroup, 0.0009);
            existingFeatures.addAll(newFeatures);
            // add features to map
            kujakuMapView.addFeaturePoints(FeatureCollection.fromFeatures(existingFeatures));
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
        }
    }


    @Override
    protected int getContentView() {
        return R.layout.activity_bounds_aware;
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
    protected int getSelectedNavigationItem() { return  R.id.nav_bounds_aware_activity; }
}
