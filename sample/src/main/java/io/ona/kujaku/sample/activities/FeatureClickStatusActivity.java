package io.ona.kujaku.sample.activities;

import android.graphics.PointF;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.style.layers.Layer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.ona.kujaku.sample.BuildConfig;
import io.ona.kujaku.sample.R;
import io.ona.kujaku.views.KujakuMapView;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 27/11/2018
 */

public class FeatureClickStatusActivity extends BaseNavigationDrawerActivity {

    private static final String TAG = FeatureClickStatusActivity.class.getName();
    private KujakuMapView kujakuMapView;
    private HashMap<String, Feature> selectedFeatures = new HashMap<>();

    private String[] selectableLayers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, BuildConfig.MAPBOX_SDK_ACCESS_TOKEN);

        kujakuMapView = findViewById(R.id.kmv_featureClickStatusActivity_mapView);
        kujakuMapView.setStyleUrl("asset://basic-feature-select-style.json");

        kujakuMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap mapboxMap) {
                selectableLayers = getLayerNames(mapboxMap.getLayers());

                mapboxMap.addOnMapClickListener(new MapboxMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(@NonNull LatLng point) {
                        final PointF pixel = mapboxMap.getProjection().toScreenLocation(point);
                        List<Feature> features = mapboxMap.queryRenderedFeatures(pixel, selectableLayers);
                        Log.e(TAG, "LEN: " + features.size());

                        if (features.size() > 0) {
                            Feature feature = features.get(features.size() - 1);
                            String geometryString = feature.geometry().toString();
                            if (selectedFeatures.containsKey(geometryString)) {
                                selectedFeatures.remove(geometryString);
                            } else {
                                selectedFeatures.put(geometryString, feature);
                            }
                        }

                        FeatureCollection featureCollection = FeatureCollection.fromFeatures((Feature[]) selectedFeatures.values().toArray(new Feature[]{}));

                        // Update the select layer
                        GeoJsonSource geoJsonSource = mapboxMap.getSourceAs("select-data");
                        if (geoJsonSource != null) {
                            geoJsonSource.setGeoJson(featureCollection);
                        }
                    }

                });
            }
        });
    }

    private String[] getLayerNames(List<Layer> layers) {
        int size = layers.size();
        ArrayList<String> layerNames = new ArrayList<>();

        for(int i = 0; i < size; i++) {
            String layerId = layers.get(i).getId();

            if (!TextUtils.isEmpty(layerId) && !"select-layer".equals(layerId)) {
                layerNames.add(layerId);
            }
        }

        return layerNames.toArray(new String[layerNames.size()]);
    }

    @Override
    protected int getContentView() {
        return R.layout.activity_feature_click_status;
    }

    @Override
    protected int getSelectedNavigationItem() {
        return R.id.nav_feature_click_status;
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
