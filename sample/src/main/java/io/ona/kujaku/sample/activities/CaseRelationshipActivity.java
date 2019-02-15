package io.ona.kujaku.sample.activities;

import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.util.Log;

import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Geometry;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.style.expressions.Expression;
import com.mapbox.mapboxsdk.style.layers.CircleLayer;
import com.mapbox.mapboxsdk.style.layers.FillLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.turf.TurfMeasurement;

import java.io.IOException;

import io.ona.kujaku.exceptions.InvalidArrowLineConfig;
import io.ona.kujaku.layers.ArrowLineLayer;
import io.ona.kujaku.sample.BuildConfig;
import io.ona.kujaku.sample.R;
import io.ona.kujaku.utils.FeatureFilter;
import io.ona.kujaku.views.KujakuMapView;

import static com.mapbox.mapboxsdk.style.expressions.Expression.eq;
import static com.mapbox.mapboxsdk.style.expressions.Expression.geometryType;
import static com.mapbox.mapboxsdk.style.expressions.Expression.get;
import static com.mapbox.mapboxsdk.style.expressions.Expression.literal;
import static com.mapbox.mapboxsdk.style.expressions.Expression.match;
import static com.mapbox.mapboxsdk.style.expressions.Expression.neq;
import static com.mapbox.mapboxsdk.style.expressions.Expression.rgba;
import static com.mapbox.mapboxsdk.style.expressions.Expression.stop;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleRadius;
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
            String featureCollection = readInputStreamAsString(getAssets().open("case-relationship-features.geojson"));
            sampleCases = FeatureCollection.fromJson(featureCollection);
            ArrowLineLayer.FeatureConfig featureConfig = new ArrowLineLayer.FeatureConfig(
                    new FeatureFilter.Builder(sampleCases)
                            .whereEq("testStatus", "positive"));

            ArrowLineLayer.SortConfig sortConfig = new ArrowLineLayer.SortConfig("dateTime"
                    , ArrowLineLayer.SortConfig.SortOrder.ASC
                    , ArrowLineLayer.SortConfig.PropertyType.DATE_TIME)
                    .setDateTimeFormat("yyyy-MM-dd HH:mm:ss");

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

                Expression colorExpression = match(get("testStatus")
                        , rgba(0, 0, 0, 0)
                        , stop(literal("positive"), Expression.color(getColorv16(R.color.positiveTasksColor)))
                        , stop(literal("negative"), Expression.color(getColorv16(R.color.negativeTasksColor))));

                FillLayer fillLayer = new FillLayer("sample-cases-fill", CASES_SOURCE_ID);
                fillLayer.withFilter(neq(geometryType(), "Point"));
                fillLayer.withProperties(fillColor(colorExpression));

                CircleLayer circleLayer = new CircleLayer("sample-cases-symbol", CASES_SOURCE_ID);
                circleLayer.withFilter(eq(geometryType(), "Point"));
                circleLayer.withProperties(circleColor(colorExpression), circleRadius(10f));

                mapboxMap.addLayer(circleLayer);
                mapboxMap.addLayer(fillLayer);

                // Zoom to the position
                mapboxMap.easeCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(0.15380840901698828, 37.66387939453125), 8d));
            }
        });
    }

    private int getColorv16(@ColorRes int colorRes) {
        return getResources().getColor(colorRes);
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
