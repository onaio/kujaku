package io.ona.kujaku.sample.activities;

import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.expressions.Expression;
import com.mapbox.mapboxsdk.style.layers.Layer;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.ona.kujaku.mbtiles.MBTilesHelper;
import io.ona.kujaku.sample.BuildConfig;
import io.ona.kujaku.sample.R;
import io.ona.kujaku.views.KujakuMapView;

import static io.ona.kujaku.sample.utils.TestDataUtils.alterFeatureJsonProperties;
import static io.ona.kujaku.sample.utils.TestDataUtils.createFeatureList;
import static io.ona.kujaku.sample.utils.TestDataUtils.generateMapBoxLayer;
import static io.ona.kujaku.sample.utils.TestDataUtils.readAssetContents;

public class AddUpdatePropertiesActivity extends BaseNavigationDrawerActivity {

    private final String TAG = AddUpdatePropertiesActivity.class.getName();

    private KujakuMapView kujakuMapView;

    private boolean isFirstClick = true;

    private final String[] genericFeatureGroup = {"White", "Black", "Hispanic", "Asian", "Other"};
    private final String[] featureGroup = {"Not Visited", "Sprayed", "Not Sprayable", "Not Sprayed"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, BuildConfig.MAPBOX_SDK_ACCESS_TOKEN);
        kujakuMapView = findViewById(R.id.add_update_activity_map_view);

        // initializeFromGenericLayer(); // Uncomment this to use a generic layer
        // setListeners(false); // Uncomment this to use a generic layer
        initializeFromStyleSource(); // Comment this out when using generic layer
        setListeners(true); // Comment this out when using generic layer
    }

    private void initializeFromGenericLayer() {
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

    private void initializeFromStyleSource() {
        String geoJson = readAssetContents(AddUpdatePropertiesActivity.this, "reveal-geojson.json");
        kujakuMapView.initializePrimaryGeoJsonSource("reveal-data-set", true, geoJson);
        // set camera position
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(-14.1706623, 32.5987837))
                .zoom(16)
                .build();
        kujakuMapView.setCameraPosition(cameraPosition);
        File mbFilesDir = new File(Environment.getExternalStorageDirectory().getPath() + MBTilesHelper.MB_TILES_DIRECTORY);
        kujakuMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull MapboxMap mapboxMap) {
                mapboxMap.setStyle(new Style.Builder().fromUrl("asset://reveal-streets-style.json"), new Style.OnStyleLoaded() {
                    @Override
                    public void onStyleLoaded(@NonNull Style style) {
                        if (mbFilesDir.exists() && mbFilesDir.isDirectory() && mbFilesDir.listFiles() != null)
                            kujakuMapView.getMbTilesHelper().initializeMbTileslayers(style, Arrays.asList(mbFilesDir.listFiles()));
                    }
                });
            }
        });
    }

    private void setListeners(boolean isFetchFromStyle) {
        // test button actions
        Button btnAddFeaturePoints = findViewById(R.id.btn_test_feature_point_addition);
        btnAddFeaturePoints.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    // helpful message
                    if (isFirstClick) {
                        isFirstClick = false;
                        Toast.makeText(AddUpdatePropertiesActivity.this, "Zoom out!", Toast.LENGTH_LONG).show();
                    }
                    // update existing features list
                    List<Feature> existingFeatures = new ArrayList<>(kujakuMapView.getPrimaryGeoJsonSource().querySourceFeatures(Expression.all()));
                    List<Feature> newFeatures;
                    if (isFetchFromStyle) {
                        newFeatures = createFeatureList(20, existingFeatures.size(), 32.5987837, -14.1706623, "taskBusinessStatus", "Point", false, featureGroup, 0.0009);
                    } else {
                        newFeatures = createFeatureList(20, existingFeatures.size(), 36.768831, -1.284956, "ethnicity", "Point", false, genericFeatureGroup, 0.0009);
                    }
                    existingFeatures.addAll(newFeatures);
                    // add features to map
                    kujakuMapView.addFeaturePoints(FeatureCollection.fromFeatures(existingFeatures));
                } catch (JSONException e) {
                    Log.e(TAG, e.getMessage());
                }
            }
        });

        Button btnModifyFeaturePoints = findViewById(R.id.btn_test_properties_modification);
        btnModifyFeaturePoints.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    // fetch existing features
                    List<Feature> features = kujakuMapView.getPrimaryGeoJsonSource().querySourceFeatures(Expression.all());
                    // modify properties
                    FeatureCollection featureCollection;
                    if (isFetchFromStyle) {
                        featureCollection = alterFeatureJsonProperties(features.size(), new JSONObject(FeatureCollection.fromFeatures(features).toJson()), "taskBusinessStatus", featureGroup);
                    } else {
                        featureCollection = alterFeatureJsonProperties(features.size(), new JSONObject(FeatureCollection.fromFeatures(features).toJson()), "ethnicity", genericFeatureGroup);
                    }
                    // update features on map
                    kujakuMapView.updateFeaturePointProperties(featureCollection);
                } catch (JSONException e) {
                    Log.e(TAG, e.getMessage());
                }
            }
        });
    }

    @Override
    protected int getContentView() {
        return R.layout.activity_add_update_properties;
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
        return R.id.nav_add_update_activity;
    }
}
