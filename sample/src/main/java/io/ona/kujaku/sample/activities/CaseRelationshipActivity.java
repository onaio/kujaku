package io.ona.kujaku.sample.activities;

import android.os.Bundle;
import android.util.Log;

import com.mapbox.geojson.FeatureCollection;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.style.layers.CircleLayer;
import com.mapbox.mapboxsdk.style.layers.FillLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import java.io.IOException;

import io.ona.kujaku.exceptions.InvalidArrowLineConfig;
import io.ona.kujaku.layers.ArrowLineLayer;
import io.ona.kujaku.sample.BuildConfig;
import io.ona.kujaku.sample.R;
import io.ona.kujaku.views.KujakuMapView;

import static com.mapbox.mapboxsdk.style.expressions.Expression.eq;
import static com.mapbox.mapboxsdk.style.expressions.Expression.geometryType;
import static com.mapbox.mapboxsdk.style.expressions.Expression.neq;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillColor;
import static io.ona.kujaku.utils.IOUtil.readInputStreamAsString;


/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 07/02/2019
 */

public class CaseRelationshipActivity extends BaseNavigationDrawerActivity {

    private KujakuMapView kujakuMapView;

    private static final String TAG = CaseRelationshipActivity.class.getName();
    private static final String CASES_SOURCE_ID = "sample-cases-source";

    private FeatureCollection sampleCases;
    private ArrowLineLayer arrowLineLayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, BuildConfig.MAPBOX_SDK_ACCESS_TOKEN);

        kujakuMapView = findViewById(R.id.kmv_boundingBoxListener_mapView);
        kujakuMapView.onCreate(savedInstanceState);

        try {
            String featureCollection = readInputStreamAsString(getAssets().open("arrow-head-features.geojson"));
            sampleCases = FeatureCollection.fromJson(featureCollection);
            ArrowLineLayer.FeatureConfig featureConfig = new ArrowLineLayer.FeatureConfig(sampleCases);
            ArrowLineLayer.SortConfig sortConfig = new ArrowLineLayer.SortConfig(""
                    , ArrowLineLayer.SortConfig.SortOrder.DESC
                    , ArrowLineLayer.SortConfig.PropertyType.NUMBER);

            try {
                arrowLineLayer = new ArrowLineLayer.Builder(this, featureConfig, sortConfig)
                        .setArrowLineColor(R.color.mapbox_blue)
                        .setArrowLineWidth(3)
                        .setAddBelowLayerId("sample-cases-symbol")
                        .build();

                kujakuMapView.addArrowLineLayer(arrowLineLayer);
            } catch (InvalidArrowLineConfig invalidArrowLineConfig) {
                Log.e(TAG, Log.getStackTraceString(invalidArrowLineConfig));
            }

        } catch (IOException e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }

        kujakuMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap mapboxMap) {
                GeoJsonSource sampleCasesSource = new GeoJsonSource(CASES_SOURCE_ID, sampleCases);
                mapboxMap.addSource(sampleCasesSource);

                int redColor = getResources().getColor(R.color.mapbox_blue);

                FillLayer fillLayer = new FillLayer("sample-cases-fill", CASES_SOURCE_ID);
                fillLayer.withFilter(neq(geometryType(), "Point"));
                fillLayer.withProperties(fillColor(redColor));

                CircleLayer circleLayer = new CircleLayer("sample-cases-symbol", CASES_SOURCE_ID);
                circleLayer.withFilter(eq(geometryType(), "Point"));
                circleLayer.withProperties(circleColor(redColor));

                mapboxMap.addLayer(circleLayer);
                mapboxMap.addLayer(fillLayer);
            }
        });
    }

    @Override
    protected int getContentView() {
        return R.layout.activity_bounding_box_listener_activity;
    }

    @Override
    protected int getSelectedNavigationItem() {
        return R.id.nav_case_relationship_activity;
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
