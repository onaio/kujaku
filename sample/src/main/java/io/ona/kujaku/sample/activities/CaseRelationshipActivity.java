package io.ona.kujaku.sample.activities;

import android.os.Bundle;
import android.widget.TextView;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdate;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.Property;
import com.mapbox.mapboxsdk.style.sources.GeoJsonOptions;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import java.text.DecimalFormat;
import java.util.ArrayList;

import io.ona.kujaku.listeners.BoundsChangeListener;
import io.ona.kujaku.sample.BuildConfig;
import io.ona.kujaku.sample.R;
import io.ona.kujaku.views.KujakuMapView;

import static com.mapbox.mapboxsdk.style.expressions.Expression.color;
import static com.mapbox.mapboxsdk.style.expressions.Expression.exponential;
import static com.mapbox.mapboxsdk.style.expressions.Expression.get;
import static com.mapbox.mapboxsdk.style.expressions.Expression.interpolate;
import static com.mapbox.mapboxsdk.style.expressions.Expression.literal;
import static com.mapbox.mapboxsdk.style.expressions.Expression.product;
import static com.mapbox.mapboxsdk.style.expressions.Expression.stop;
import static com.mapbox.mapboxsdk.style.expressions.Expression.switchCase;
import static com.mapbox.mapboxsdk.style.expressions.Expression.zoom;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineCap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineJoin;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineWidth;


/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 07/02/2019
 */

public class CaseRelationshipActivity extends BaseNavigationDrawerActivity {

    private KujakuMapView kujakuMapView;

    private static final String LINE_LAYER_SOURCE_ID = "case-arrows-source-id";
    private static final String ARROWS_LAYER_ID = "arrows-layer-id";

    private GeoJsonSource geoJsonSource;
    private LineLayer lineLayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, BuildConfig.MAPBOX_SDK_ACCESS_TOKEN);

        kujakuMapView = findViewById(R.id.kmv_boundingBoxListener_mapView);
        kujakuMapView.onCreate(savedInstanceState);

        // Add a arrow layer
        lineLayer = new LineLayer(ARROWS_LAYER_ID, LINE_LAYER_SOURCE_ID);
        lineLayer.withProperties(
                lineCap(Property.LINE_CAP_ROUND),
                lineJoin(Property.LINE_JOIN_ROUND),
                lineWidth(20f/*
                        interpolate(
                                exponential(1.5f), zoom(),
                                stop(10f, 7f)
                        )*/
                ),
                lineColor(getResources().getColor(R.color.mapbox_blue))
        );

        ArrayList<Point> points = new ArrayList<>();
        points.add(Point.fromLngLat(36.86286449432373, -1.2872232832369515));
        points.add(Point.fromLngLat(36.86282157897949, -1.2933157169746432));

        geoJsonSource = new GeoJsonSource(LINE_LAYER_SOURCE_ID);
        geoJsonSource.setGeoJson(FeatureCollection.fromFeatures(new Feature[]{
                Feature.fromGeometry(LineString.fromLngLats(points))
        }));

        kujakuMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap mapboxMap) {
                mapboxMap.addSource(geoJsonSource);
                mapboxMap.addLayer(lineLayer);

                mapboxMap.easeCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder()
                        .target(new LatLng(-1.2872232832369515, 36.86286449432373))
                        .zoom(12f)
                        .build()));
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
