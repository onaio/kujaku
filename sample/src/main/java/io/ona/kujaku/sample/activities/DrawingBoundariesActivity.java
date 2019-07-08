package io.ona.kujaku.sample.activities;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;

import com.mapbox.mapboxsdk.plugins.annotation.Circle;

import io.ona.kujaku.layers.FillBoundaryLayer;
import io.ona.kujaku.layers.KujakuLayer;
import io.ona.kujaku.listeners.OnKujakuLayerLongClickListener;
import io.ona.kujaku.manager.DrawingManager;
import io.ona.kujaku.sample.R;
import io.ona.kujaku.listeners.OnDrawingCircleClickListener;
import io.ona.kujaku.listeners.OnDrawingCircleLongClickListener;
import io.ona.kujaku.views.KujakuMapView;

public class DrawingBoundariesActivity extends BaseNavigationDrawerActivity {

    private static final String TAG = DrawingBoundariesActivity.class.getName();

    private KujakuMapView kujakuMapView;
    private DrawingManager drawingManager;

    private Button deleteBtn ;
    private Button drawingBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        kujakuMapView = findViewById(R.id.kmv_drawingBoundaries_mapView);
        kujakuMapView.onCreate(savedInstanceState);

        this.deleteBtn = findViewById(R.id.btn_drawingBoundaries_delete);
        this.deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawingManager.deleteDrawingCurrentCircle();
                view.setEnabled(false);
            }
        });

        this.drawingBtn = findViewById(R.id.btn_drawingBoundaries_drawing);
        this.drawingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // start Drawing from scratch
                if (drawingManager != null) {
                    if (!drawingManager.isDrawingEnabled()) {
                        startDrawing(null);
                    } else {
                        stopDrawing();
                    }
                } else {
                    Log.e(TAG, "Drawing manager instance is null");
                }
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

                        drawingManager.addOnDrawingCircleClickListener(new OnDrawingCircleClickListener() {
                            @Override
                            public void onCircleClick(@NonNull Circle circle) {
                                Toast.makeText(DrawingBoundariesActivity.this,
                                        getString(R.string.drawing_boundaries_circle_clicked), Toast.LENGTH_SHORT).show();

                                drawingManager.unsetCurrentCircleDraggable();

                                if (circle.isDraggable()) {
                                    deleteBtn.setEnabled(false);
                                    drawingManager.setDraggable(false, circle);

                                } else if (!circle.isDraggable()) {
                                    deleteBtn.setEnabled(true);
                                    drawingManager.setDraggable(true, circle);
                                }
                            }

                            @Override
                            public void onCircleNotClick(@NonNull LatLng latLng) {
                                Toast.makeText(DrawingBoundariesActivity.this,
                                        getString(R.string.drawing_boundaries_circle_not_clicked), Toast.LENGTH_SHORT).show();

                                if (drawingManager.getCurrentKujakuCircle() != null) {
                                    drawingManager.unsetCurrentCircleDraggable();
                                    deleteBtn.setEnabled(false);
                                } else {
                                    drawingManager.drawCircle(latLng);
                                }
                            }
                        });

                        drawingManager.addOnDrawingCircleLongClickListener(new OnDrawingCircleLongClickListener() {
                            @Override
                            public void onCircleLongClick(@NonNull Circle circle) {
                                Toast.makeText(DrawingBoundariesActivity.this,
                                        getString(R.string.drawing_boundaries_circle_long_clicked), Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onCircleNotLongClick(@NonNull LatLng point) {
                                Toast.makeText(DrawingBoundariesActivity.this,
                                        getString(R.string.drawing_boundaries_circle_not_long_clicked), Toast.LENGTH_SHORT).show();
                            }
                        });

                        kujakuMapView.setOnKujakuLayerLongClickListener(new OnKujakuLayerLongClickListener() {
                            @Override
                            public void onKujakuLayerLongClick(@NonNull KujakuLayer kujakuLayer) {
                                if (!drawingManager.isDrawingEnabled()) {
                                    startDrawing(kujakuLayer);
                                }
                            }
                        });
                    }
                });
            }
        });
    }

    private void startDrawing(KujakuLayer kujakuLayer) {
        if (kujakuLayer instanceof FillBoundaryLayer) {
            if (drawingManager.startDrawingKujakuLayer((FillBoundaryLayer)kujakuLayer)) {
                drawingBtn.setText(R.string.drawing_boundaries_stop_draw);
            }
        }
    }

    private void stopDrawing() {
        if (drawingManager.stopDrawingAndDisplayLayer()) {
            drawingBtn.setText(R.string.drawing_boundaries_start_draw);
            deleteBtn.setEnabled(false);
        }
    }

    @Override
    protected int getContentView() {
        return R.layout.activity_drawing_boundaries_map_view;
    }

    @Override
    protected int getSelectedNavigationItem() {
        return R.id.nav_drawing_boundaries;
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
