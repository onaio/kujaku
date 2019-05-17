package io.ona.kujaku.sample.activities;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;

import com.mapbox.mapboxsdk.plugins.annotation.Circle;
import io.ona.kujaku.sample.R;
import io.ona.kujaku.manager.DrawingManager;
import io.ona.kujaku.listeners.OnDrawingCircleClickListener;
import io.ona.kujaku.listeners.OnDrawingCircleLongClickListener;
import io.ona.kujaku.views.KujakuMapView;

public class DrawingBoundariesActivity extends BaseNavigationDrawerActivity {

    private static final String TAG = DrawingBoundariesActivity.class.getName();

    private KujakuMapView kujakuMapView;
    private DrawingManager drawingManager;
    private Button deleteBtn ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        kujakuMapView = findViewById(R.id.kmv_drawingBoundaries_mapView);
        kujakuMapView.onCreate(savedInstanceState);

        this.deleteBtn = findViewById(R.id.btn_drawingBoundaries_Delete);
        this.deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawingManager.delete(drawingManager.getCurrentKujakuCircle());
                view.setEnabled(false);
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

                       /* try {
                            InputStreamReader streamReader = new InputStreamReader(getBaseContext().getResources().getAssets().open("annotations.json"));
                            BufferedReader reader = new BufferedReader(streamReader);
                            StringBuilder out = new StringBuilder();
                            String line;
                            while ((line = reader.readLine()) != null) {
                                out.append(line);
                            }
                            reader.close();

                            LineManager lineManager = new LineManager(kujakuMapView, mapboxMap, style);
                            lineManager.create(out.toString());
                        } catch (IOException e) {
                            throw new RuntimeException("Unable to parse annotations.json");
                        }*/

                        drawingManager.addOnDrawingCircleClickListener(new OnDrawingCircleClickListener() {
                            @Override
                            public void onCircleClick(Circle circle) {
                                Toast.makeText(DrawingBoundariesActivity.this,
                                        String.format("Circle clicked"),Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onCircleNotClick(@NonNull LatLng point) {
                                Toast.makeText(DrawingBoundariesActivity.this,
                                        String.format("Circle NOT clicked"),Toast.LENGTH_SHORT).show();

                                drawingManager.create(DrawingManager.circleOptions.withLatLng(point));
                                drawingManager.refresh();
                            }
                        });

                        drawingManager.addOnDrawingCircleLongClickListener(new OnDrawingCircleLongClickListener() {
                            @Override
                            public void onCircleLongClick(Circle circle) {
                                Toast.makeText(DrawingBoundariesActivity.this,
                                        String.format("Circle long clicked"),Toast.LENGTH_SHORT).show();

                                if (circle.isDraggable()) {
                                    deleteBtn.setEnabled(false);
                                    drawingManager.setDraggable(false, circle);
                                    drawingManager.refresh();

                                } else if (!circle.isDraggable() && drawingManager.getCurrentKujakuCircle() == null) {
                                    deleteBtn.setEnabled(!drawingManager.isMiddleCircle(circle));
                                    drawingManager.setDraggable(true, circle);
                                }
                            }

                            @Override
                            public void onCircleNotLongClick(@NonNull LatLng point) {
                                Toast.makeText(DrawingBoundariesActivity.this,
                                        String.format("Circle NOT long clicked"),Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });

            }
        });
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
