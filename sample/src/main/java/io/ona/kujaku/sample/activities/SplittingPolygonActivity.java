package io.ona.kujaku.sample.activities;

import android.os.Bundle;
import androidx.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.annotation.Circle;

import io.ona.kujaku.layers.KujakuLayer;
import io.ona.kujaku.listeners.OnDrawingCircleClickListener;
import io.ona.kujaku.listeners.OnKujakuLayerClickListener;
import io.ona.kujaku.listeners.OnKujakuLayerLongClickListener;
import io.ona.kujaku.listeners.OnSplittingClickListener;
import io.ona.kujaku.manager.DrawingManager;
import io.ona.kujaku.manager.SplittingManager;
import io.ona.kujaku.sample.R;
import io.ona.kujaku.views.KujakuMapView;

public class SplittingPolygonActivity extends BaseNavigationDrawerActivity {

    private static final String TAG = SplittingPolygonActivity.class.getName();

    private KujakuMapView kujakuMapView;
    private DrawingManager drawingManager;
    private SplittingManager splittingManager;

    private Button deleteBtn;
    private Button drawingBtn;
    private Button splitBtn;
    private Button cancelBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        kujakuMapView = findViewById(R.id.kmv_splittingPolygon_mapView);
        kujakuMapView.onCreate(savedInstanceState);

        this.splitBtn = findViewById(R.id.btn_splittingPolygon_split);
        this.splitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (splittingManager != null && splittingManager.isSplittingReady()) {
                    splittingManager.split();
                    view.setEnabled(false);
                }
            }
        });

        this.cancelBtn = findViewById(R.id.btn_splittingPolygon_cancel);
        this.cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (splittingManager != null) {
                    splittingManager.stopSplitting();
                    splitBtn.setEnabled(false);
                    cancelBtn.setEnabled(false);
                }
            }
        });

        this.deleteBtn = findViewById(R.id.btn_splittingPolygon_delete);
        this.deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (drawingManager != null) {
                    drawingManager.deleteDrawingCurrentCircle();
                    view.setEnabled(false);
                }
            }
        });

        this.drawingBtn = findViewById(R.id.btn_splittingPolygon_drawing);
        this.drawingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // start Drawing from scratch
                if (drawingManager != null && splittingManager != null) {
                    if (!drawingManager.isDrawingEnabled()) {
                        splittingManager.stopSplitting();

                        if (drawingManager.startDrawing(null)) {
                            drawingBtn.setText(R.string.drawing_boundaries_stop_draw);
                        }
                    } else {
                        drawingManager.stopDrawingAndDisplayLayer();
                        drawingBtn.setText(R.string.drawing_boundaries_start_draw);
                    }

                    cancelBtn.setEnabled(false);
                } else {
                    Log.e(TAG, "Drawing manager instance is null");
                }

                deleteBtn.setEnabled(false);
            }
        });

        kujakuMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull MapboxMap mapboxMap) {
                kujakuMapView.focusOnUserLocation(true);
                mapboxMap.setStyle(Style.MAPBOX_STREETS,  new Style.OnStyleLoaded() {
                    @Override
                    public void onStyleLoaded(@NonNull Style style) {
                        drawingManager = new DrawingManager(kujakuMapView, mapboxMap, style);
                        splittingManager = new SplittingManager(kujakuMapView, mapboxMap, style);

                        drawingManager.addOnDrawingCircleClickListener(new OnDrawingCircleClickListener() {
                            @Override
                            public void onCircleClick(@NonNull Circle circle) {
                                deleteBtn.setEnabled(drawingManager.getCurrentKujakuCircle() != null);
                            }

                            @Override
                            public void onCircleNotClick(@NonNull LatLng latLng) {
                                deleteBtn.setEnabled(false);
                            }
                        });

                        drawingManager.addOnKujakuLayerLongClickListener(new OnKujakuLayerLongClickListener() {
                            @Override
                            public void onKujakuLayerLongClick(@NonNull KujakuLayer kujakuLayer) {
                                if (drawingManager.isDrawingEnabled()) {
                                    drawingBtn.setText(R.string.drawing_boundaries_stop_draw);
                                }
                            }
                        });

                        splittingManager.addOnKujakuLayerClickListener(new OnKujakuLayerClickListener() {
                            @Override
                            public void onKujakuLayerClick(@NonNull KujakuLayer kujakuLayer) {
                                if (drawingManager.isDrawingEnabled()) {
                                    splittingManager.stopSplitting();
                                } else {
                                    cancelBtn.setEnabled(true);
                                }
                            }
                        });

                        splittingManager.addOnSplittingClickListener(new OnSplittingClickListener() {
                            @Override
                            public void onSplittingClick(@NonNull LatLng latLng) {
                                splitBtn.setEnabled(splittingManager.isSplittingReady());
                            }
                        });
                    }
                });
            }
        });
    }

    @Override
    protected int getContentView() {
        return R.layout.activity_splitting_polygon_map_view;
    }

    @Override
    protected int getSelectedNavigationItem() {
        return R.id.nav_splitting_polygon;
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
