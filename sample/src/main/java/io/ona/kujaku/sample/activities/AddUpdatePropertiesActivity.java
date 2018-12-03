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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import io.ona.kujaku.sample.BuildConfig;
import io.ona.kujaku.sample.R;
import io.ona.kujaku.views.KujakuMapView;

import static io.ona.kujaku.sample.utils.TestDataUtils.alterFeatureJsonProperties;
import static io.ona.kujaku.sample.utils.TestDataUtils.createFeatureList;
import static io.ona.kujaku.sample.utils.TestDataUtils.readAssetContents;

public class AddUpdatePropertiesActivity extends BaseNavigationDrawerActivity {

    private final String TAG = AddUpdatePropertiesActivity.class.getName();

    private KujakuMapView kujakuMapView;

//    private final String[] featureGroup =  {"White", "Black", "Hispanic", "Asian", "Other"}; // TODO: uncomment this to test using randomly-generated features
    private final String[] featureGroup =  {"not_visited",  "sprayed", "not_sprayable",  "not_sprayed"}; // TODO: uncomment this to test using user's style-defined GeoJson source

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, BuildConfig.MAPBOX_SDK_ACCESS_TOKEN);
        kujakuMapView = findViewById(R.id.add_update_activity_map_view);
        // bootstrap
        kujakuMapView.setStyleUrl("asset://reveal-streets-style.json"); // TODO: uncomment this to test using user's style-defined GeoJson source
        String geoJson = readAssetContents(this, "reveal-geojson.json"); // TODO: uncomment this to test using user's style-defined GeoJson source
        kujakuMapView.initializePrimaryGeoJsonSource("reveal-data-set", true, geoJson); // TODO: uncomment this to test using user's style-defined GeoJson source
        // set camera position
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(-14.1706623, 32.5987837))
                .zoom(18)
                .build(); // TODO: uncomment this to test using user's style-defined GeoJson source
        kujakuMapView.setCameraPosition(cameraPosition); // TODO: uncomment this to test using user's style-defined GeoJson source

//        kujakuMapView.initializePrimaryGeoJsonSource("kujaku_primary_source", false, null); // TODO: uncomment this to test using randomly-generated features
//        Layer circleLayer = generateMapBoxLayer("kujaku_primary_layer", kujakuMapView.getPrimaryGeoJsonSource().getId()); // TODO: uncomment this to test using randomly-generated features
//        kujakuMapView.setPrimaryLayer(circleLayer); // TODO: uncomment this to test using randomly-generated features

        // test button actions
        Button btnAddFeaturePoints = findViewById(R.id.btn_test_feature_point_addition);
        btnAddFeaturePoints.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    List<Feature> existingFeatures = new ArrayList<>(kujakuMapView.getPrimaryGeoJsonSource().querySourceFeatures(Expression.all()));
//                    List<Feature> newFeatures = createFeatureList(20, existingFeatures.size(), 36.768831, -1.284956, "ethnicity", "Point", featureGroup); // TODO: uncomment this to test using randomly-generated features
                    List<Feature> newFeatures = createFeatureList(20, existingFeatures.size(), 32.5987837, -14.1706623, "taskBusinessStatus", "Point", false, featureGroup); // TODO: uncomment this to test using user's  style-defined GeoJson source
                    existingFeatures.addAll(newFeatures);
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
                    List<Feature> features  = kujakuMapView.getPrimaryGeoJsonSource().querySourceFeatures(Expression.all());
//                    FeatureCollection featureCollection = alterFeatureJsonProperties(features.size(), new JSONObject(FeatureCollection.fromFeatures(features).toJson()), "ethnicity", featureGroup); // TODO: uncomment this to test using randomly-generated features
                    FeatureCollection featureCollection = alterFeatureJsonProperties(features.size(), new JSONObject(FeatureCollection.fromFeatures(features).toJson()), "taskBusinessStatus", featureGroup); // TODO: uncomment this to test using user's style-defined GeoJson source
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
    protected int getSelectedNavigationItem() { return  R.id.nav_add_update_activity; }
}
