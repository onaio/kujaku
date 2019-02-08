package io.ona.kujaku.sample.activities;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.content.res.AppCompatResources;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.Property;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonOptions;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import java.util.ArrayList;

import io.ona.kujaku.sample.BuildConfig;
import io.ona.kujaku.sample.R;
import io.ona.kujaku.views.KujakuMapView;

import static com.mapbox.mapboxsdk.style.expressions.Expression.get;
import static com.mapbox.mapboxsdk.style.expressions.Expression.interpolate;
import static com.mapbox.mapboxsdk.style.expressions.Expression.linear;
import static com.mapbox.mapboxsdk.style.expressions.Expression.stop;
import static com.mapbox.mapboxsdk.style.expressions.Expression.zoom;
import static com.mapbox.mapboxsdk.style.layers.Property.ICON_ROTATION_ALIGNMENT_MAP;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
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

    private static final String ARROW_HEAD_LAYER_SOURCE_ID = "arrow-head-layer-source-id";
    private static final String ARROW_HEAD_LAYER_ID = "arrow-head-layer-id";

    private static final String ARROW_HEAD_ICON = "arrow-head-icon";

    private GeoJsonSource geoJsonSource;
    private LineLayer lineLayer;

    static final int MIN_ARROW_ZOOM = 10;
    static final int MAX_ARROW_ZOOM = 22;
    static final float MIN_ZOOM_ARROW_HEAD_SCALE = 1.2f;
    static final float MAX_ZOOM_ARROW_HEAD_SCALE = 1.8f;
    static final Float[] ARROW_HEAD_OFFSET = {0f, -7f};
    static final String ARROW_BEARING = "case-relationship-arrow-bearing";
    static final float OPAQUE = 0.0f;
    static final int ARROW_HIDDEN_ZOOM_LEVEL = 14;
    static final float TRANSPARENT = 1.0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, BuildConfig.MAPBOX_SDK_ACCESS_TOKEN);

        kujakuMapView = findViewById(R.id.kmv_boundingBoxListener_mapView);
        kujakuMapView.onCreate(savedInstanceState);

        // Create arrow source
        GeoJsonSource arrowHeadSource = new GeoJsonSource(ARROW_HEAD_LAYER_SOURCE_ID,
                Feature.fromGeometry(Point.fromLngLat(36.86282157897949,-1.2902158713504284))
                ,new GeoJsonOptions().withMaxZoom(16));

        //Add arrow layer
        SymbolLayer arrowHeadLayer = new SymbolLayer(ARROW_HEAD_LAYER_ID, ARROW_HEAD_LAYER_SOURCE_ID);
        arrowHeadLayer.withProperties(
                PropertyFactory.iconImage(ARROW_HEAD_ICON),
                iconAllowOverlap(true),
                iconIgnorePlacement(true),
                PropertyFactory.iconSize(interpolate(linear(), zoom(),
                        stop(MIN_ARROW_ZOOM, MIN_ZOOM_ARROW_HEAD_SCALE),
                        stop(MAX_ARROW_ZOOM, MAX_ZOOM_ARROW_HEAD_SCALE)
                        )
                ),
                PropertyFactory.iconOffset(ARROW_HEAD_OFFSET),
                PropertyFactory.iconRotationAlignment(ICON_ROTATION_ALIGNMENT_MAP),
                PropertyFactory.iconRotate(get(ARROW_BEARING)),
                PropertyFactory.iconOpacity(1f )
        );

        // Add a line layer
        lineLayer = new LineLayer(ARROWS_LAYER_ID, LINE_LAYER_SOURCE_ID);
        lineLayer.withProperties(
                lineCap(Property.LINE_CAP_ROUND),
                lineJoin(Property.LINE_JOIN_ROUND),
                lineWidth(20f),
                lineColor(getColorv16(R.color.mapbox_blue))
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

                mapboxMap.addSource(arrowHeadSource);
                mapboxMap.addLayer(arrowHeadLayer);

                mapboxMap.easeCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder()
                        .target(new LatLng(-1.2872232832369515, 36.86286449432373))
                        .zoom(12f)
                        .bearing(33)
                        .build()));


                addArrowHeadIcon(mapboxMap);
            }
        });
    }



    private void addArrowHeadIcon(@NonNull MapboxMap mapboxMap) {
        int headResId = R.drawable.ic_arrow_head;
        Drawable arrowHead = AppCompatResources.getDrawable(kujakuMapView.getContext(), headResId);
        if (arrowHead == null) {
            return;
        }
        Drawable head = DrawableCompat.wrap(arrowHead);
        DrawableCompat.setTint(head.mutate(), getColorv16(R.color.mapbox_blue));
        Bitmap icon = getBitmapFromDrawable(head);
        mapboxMap.addImage(ARROW_HEAD_ICON, icon);
    }

    private int getColorv16(@ColorRes int colorId) {
        return getResources().getColor(colorId);
    }

    public static Bitmap getBitmapFromDrawable(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        } else {
            Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(),
                    Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
            return bitmap;
        }
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
