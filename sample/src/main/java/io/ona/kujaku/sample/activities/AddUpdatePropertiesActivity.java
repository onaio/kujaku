package io.ona.kujaku.sample.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.style.expressions.Expression;
import com.mapbox.mapboxsdk.style.layers.Layer;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import io.ona.kujaku.sample.BuildConfig;
import io.ona.kujaku.sample.R;
import io.ona.kujaku.views.KujakuMapView;

import static io.ona.kujaku.sample.utils.TestDataUtils.alterFeatureJsonProperties;
import static io.ona.kujaku.sample.utils.TestDataUtils.createFeatureList;
import static io.ona.kujaku.sample.utils.TestDataUtils.generateMapBoxLayer;

public class AddUpdatePropertiesActivity extends BaseNavigationDrawerActivity {

    private final String TAG = AddUpdatePropertiesActivity.class.getName();

    private KujakuMapView kujakuMapView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, BuildConfig.MAPBOX_SDK_ACCESS_TOKEN);
        kujakuMapView = findViewById(R.id.add_update_activity_map_view);

        // bootstrap
        kujakuMapView.initializePrimaryGeoJsonSource("kujaku_primary_source", false);
        Layer circleLayer = generateMapBoxLayer("kujaku_primary_layer", kujakuMapView.getPrimaryGeoJsonSource().getId());
        kujakuMapView.setPrimaryLayer(circleLayer);

        // test button actions
        Button btnAddFeaturePoints = findViewById(R.id.btn_test_feature_point_addition);
        btnAddFeaturePoints.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    List<Feature> existingFeatures = new ArrayList<>(kujakuMapView.getPrimaryGeoJsonSource().querySourceFeatures(Expression.all()));
                    List<Feature> newFeatures = createFeatureList(20, existingFeatures.size(), 36.768831, -1.284956);
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
                    FeatureCollection featureCollection = alterFeatureJsonProperties(features.size(), new JSONObject(FeatureCollection.fromFeatures(features).toJson()));
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
