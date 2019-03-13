package io.ona.kujaku.sample.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.Geometry;
import com.mapbox.geojson.MultiPoint;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Polyline;
import com.mapbox.mapboxsdk.annotations.PolylineOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.turf.TurfMeasurement;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

import es.dmoral.toasty.Toasty;
import io.ona.kujaku.sample.BuildConfig;
import io.ona.kujaku.sample.R;
import io.ona.kujaku.utils.CoordinateUtils;
import io.ona.kujaku.views.KujakuMapView;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 27/11/2018
 */

public class PaddedBboxCalculatorActivity extends BaseNavigationDrawerActivity {

    private KujakuMapView kujakuMapView;

    private ArrayList<Point> markerPoints = new ArrayList<>();
    private MapboxMap mapboxMap;

    private Polyline normalPolyline;
    private Polyline paddedPolyline;

    private boolean firstTime = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, BuildConfig.MAPBOX_SDK_ACCESS_TOKEN);

        kujakuMapView = findViewById(R.id.kmv_paddedBboxCalculator_mapView);
        kujakuMapView.onCreate(savedInstanceState);
        kujakuMapView.enableAddPoint(true);

        Button dropPointBtn = findViewById(R.id.btn_paddedBboxCalculator_dropPoint);
        Button generatePaddedBbox = findViewById(R.id.btn_paddedBboxCalculator_generatePaddedBbox);

        dropPointBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (kujakuMapView.isCanAddPoint()) {
                    JSONObject jsonObject = kujakuMapView.dropPoint();

                    if (jsonObject != null) {
                        Geometry geometry = Feature.fromJson(jsonObject.toString()).geometry();

                        if (geometry instanceof Point) {
                            markerPoints.add((Point) geometry);
                        }

                    }
                }
            }
        });

        kujakuMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap mapboxMap) {
                PaddedBboxCalculatorActivity.this.mapboxMap = mapboxMap;
                mapboxMap.setStyle(Style.MAPBOX_STREETS);
            }
        });

        generatePaddedBbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (markerPoints.size() > 0) {
                    // Generate bbox from Polygon, MultiPolygon or Points(MultiPoints)
                    double[] bbox = TurfMeasurement.bbox(MultiPoint.fromLngLats(markerPoints));

                    // Generate the padded bbox
                    double[] paddedBbox = CoordinateUtils.getPaddedBbox(bbox, 1000);

                    // Generate (4-sided)Polygon with 5 coordinates
                    LatLng[] normalBboxCoordinates = CoordinateUtils.generate5pointsFromBbox(bbox);
                    LatLng[] paddedBboxCoordinates = CoordinateUtils.generate5pointsFromBbox(paddedBbox);

                    if (normalBboxCoordinates != null && paddedBboxCoordinates != null && mapboxMap != null) {

                        if (firstTime) {
                            normalPolyline = mapboxMap.addPolyline(new PolylineOptions()
                                    .color(getResources().getColor(R.color.my_location_marker_color))
                                    .add(normalBboxCoordinates)
                                    .width(5));

                            paddedPolyline = mapboxMap.addPolyline(new PolylineOptions()
                                    .color(getResources().getColor(R.color.red))
                                    .add(paddedBboxCoordinates)
                                    .width(5));

                            firstTime = false;
                        } else {
                            normalPolyline.setPoints(Arrays.asList(normalBboxCoordinates));
                            paddedPolyline.setPoints(Arrays.asList(paddedBboxCoordinates));

                            mapboxMap.updatePolyline(normalPolyline);
                            mapboxMap.updatePolyline(paddedPolyline);
                        }
                    }

                } else {
                    Toasty.error(PaddedBboxCalculatorActivity.this, getString(R.string.first_add_some_points))
                            .show();
                }
            }
        });

    }

    @Override
    protected int getContentView() {
        return R.layout.activity_padded_bbox_activity;
    }

    @Override
    protected int getSelectedNavigationItem() {
        return R.id.nav_padded_bbox_calculator;
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
