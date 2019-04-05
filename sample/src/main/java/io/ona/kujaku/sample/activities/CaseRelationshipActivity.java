package io.ona.kujaku.sample.activities;

import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.mapbox.geojson.FeatureCollection;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.expressions.Expression;
import com.mapbox.mapboxsdk.style.layers.CircleLayer;
import com.mapbox.mapboxsdk.style.layers.FillLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import java.io.IOException;

import io.ona.kujaku.exceptions.InvalidArrowLineConfigException;
import io.ona.kujaku.layers.ArrowLineLayer;
import io.ona.kujaku.layers.BoundaryLayer;
import io.ona.kujaku.sample.BuildConfig;
import io.ona.kujaku.sample.R;
import io.ona.kujaku.utils.FeatureFilter;
import io.ona.kujaku.utils.IOUtil;
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

    private ArrowLineLayer arrowLineLayer;
    private GeoJsonSource sampleCasesSource;

    private Button drawArrowsBtn;
    private Button changeFeatureBtn;

    private FeatureCollection boundaryFeatureCollection1;
    private FeatureCollection boundaryFeatureCollection2;

    private LatLng focusPoint1;
    private LatLng focusPoint2;

    private BoundaryLayer boundaryLayer;

    private boolean showingFeatureCollection1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, BuildConfig.MAPBOX_SDK_ACCESS_TOKEN);

        kujakuMapView = findViewById(R.id.kmv_caseRelationship_mapView);
        kujakuMapView.onCreate(savedInstanceState);

        drawArrowsBtn = findViewById(R.id.btn_caseRelationshipAct_drawArrows);
        changeFeatureBtn = findViewById(R.id.btn_caseRelationshipAct_change);

        drawArrowsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleDrawingArrowsShowingRelationship();
                changeFeatureBtn.setEnabled(true);
            }
        });

        try {
            boundaryFeatureCollection1 = FeatureCollection.fromJson(
                    readInputStreamAsString(getAssets().open("case-relationship-features.geojson"))
            );
            boundaryFeatureCollection2 = FeatureCollection.fromJson(
                    IOUtil.readInputStreamAsString(getAssets().open("alternative_arrow_line.geojson"))
            );
        } catch (IOException e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }

        focusPoint1 = new LatLng(0.15380840901698828, 37.66387939453125);
        focusPoint2 = new LatLng(-0.44219531715407406, 37.5457763671875);

        kujakuMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap mapboxMap) {
                mapboxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
                    @Override
                    public void onStyleLoaded(@NonNull Style style) {
                        addStructuresToMap(style);

                        // Zoom to the position
                        mapboxMap.easeCamera(CameraUpdateFactory.newLatLngZoom(focusPoint1, 8d));
                    }
                });
            }
        });

        changeFeatureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (showingFeatureCollection1) {
                    sampleCasesSource.setGeoJson(boundaryFeatureCollection2);
                    arrowLineLayer.updateFeatures(boundaryFeatureCollection2);
                    changeFocus(focusPoint2);
                    showingFeatureCollection1 = false;
                } else {
                    sampleCasesSource.setGeoJson(boundaryFeatureCollection1);
                    arrowLineLayer.updateFeatures(boundaryFeatureCollection1);
                    changeFocus(focusPoint1);
                    showingFeatureCollection1 = true;
                }
            }
        });
    }

    private void addStructuresToMap(@NonNull Style style) {
        sampleCasesSource = new GeoJsonSource(CASES_SOURCE_ID, boundaryFeatureCollection1);
        style.addSource(sampleCasesSource);

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

        style.addLayer(circleLayer);
        style.addLayer(fillLayer);
    }

    private void toggleDrawingArrowsShowingRelationship() {
        if (arrowLineLayer == null) {
            ArrowLineLayer.FeatureConfig featureConfig = new ArrowLineLayer.FeatureConfig(
                    new FeatureFilter.Builder(boundaryFeatureCollection1)
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
            } catch (InvalidArrowLineConfigException invalidArrowLineConfigException) {
                Log.e(TAG, Log.getStackTraceString(invalidArrowLineConfigException));
            }

            showingFeatureCollection1 = true;
        }

        if (arrowLineLayer.isVisible()) {
            kujakuMapView.disableLayer(arrowLineLayer);
            drawArrowsBtn.setText(R.string.draw_arrows);
        } else {
            kujakuMapView.addLayer(arrowLineLayer);
            drawArrowsBtn.setText(R.string.disable_arrows_showing_relationship);
        }
    }

    private void changeFocus(@NonNull LatLng point) {
        kujakuMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull MapboxMap mapboxMap) {
                mapboxMap.easeCamera(CameraUpdateFactory.newLatLngZoom(point, 8d));
            }
        });
    }

    private int getColorv16(@ColorRes int colorRes) {
        return getResources().getColor(colorRes);
    }

    @Override
    protected int getContentView() {
        return R.layout.activity_case_relationship_activity;
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
