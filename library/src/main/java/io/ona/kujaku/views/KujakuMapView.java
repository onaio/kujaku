package io.ona.kujaku.views;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.cocoahero.android.geojson.Feature;
import com.cocoahero.android.geojson.Point;
import com.google.android.gms.maps.model.LatLng;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.MapboxMapOptions;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.UUID;

import io.ona.kujaku.R;
import io.ona.kujaku.callbacks.AddPointCallback;
import io.ona.kujaku.interfaces.IKujakuMapView;
import io.ona.kujaku.listeners.OnLocationChanged;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 26/09/2018
 */

public class KujakuMapView extends MapView implements IKujakuMapView {

    private static final String TAG = KujakuMapView.class.getName();

    private boolean canAddPoint = false;
    private ImageView markerlayout;
    private Button doneAddingPoint;
    private Button addPoint;
    private MapboxMap mapboxMap;

    private LinearLayout addPointButtonsLayout;

    private SymbolLayer pointsLayer;
    private GeoJsonSource pointsSource;
    private String pointsLayerId = UUID.randomUUID().toString();
    private String pointsSourceId = UUID.randomUUID().toString();

    public KujakuMapView(@NonNull Context context) {
        super(context);
        init();
    }

    public KujakuMapView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public KujakuMapView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public KujakuMapView(@NonNull Context context, @Nullable MapboxMapOptions options) {
        super(context, options);
        init();
    }

    private void init() {
        markerlayout = (ImageView) findViewById(R.id.iv_mapview_locationSelectionMarker);
        doneAddingPoint = findViewById(R.id.btn_mapview_locationSelectionBtn);
        addPointButtonsLayout = findViewById(R.id.ll_mapview_addBtnsLayout);
        addPoint = findViewById(R.id.btn_mapview_locationAdditionBtn);

        markerlayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                markerlayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                int height = markerlayout.getMeasuredHeight();
                markerlayout.setY(markerlayout.getY() - (height/2));
            }
        });
    }

    @Override
    public void addPoint(boolean useGPS, @NonNull final AddPointCallback addPointCallback) {
        // Implementation for adding a point
        if (useGPS) {
            // Todo: Finish the GPS implementation
        } else {
            // Enable the marker layout
            enableAddPoint(true);

            doneAddingPoint.setVisibility(VISIBLE);
            addPoint.setVisibility(VISIBLE);
            addPointButtonsLayout.setVisibility(VISIBLE);

            addPoint.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    JSONObject jsonObject = dropPoint();
                    addPointCallback.onPointAdd(jsonObject);
                }
            });
            doneAddingPoint.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    enableAddPoint(false);
                    addPointCallback.onCancel();

                    doneAddingPoint.setVisibility(GONE);
                    addPoint.setVisibility(GONE);
                }
            });
        }
    }

    @Override
    public void enableAddPoint(boolean canAddPoint) {
        // Implementation for enableAddPoint(boolean)
        this.canAddPoint = canAddPoint;
        getMapboxMap();

        if (this.canAddPoint) {
            // Show the layer with the marker in the middle
            showMarkerLayout();
        } else {
            hideMarkerLayout();
        }
    }

    @Override
    public void enableAddPoint(boolean canAddPoint, @NonNull OnLocationChanged onLocationChanged) {
        // Implementation for enableAddPoint(boolean, OnLocationChanged)
    }

    @Override
    public @Nullable JSONObject dropPoint() {
        if (mapboxMap != null && canAddPoint) {
            com.mapbox.mapboxsdk.geometry.LatLng latLng = mapboxMap.getCameraPosition().target;

            Feature feature = new Feature();
            feature.setGeometry(new Point(latLng.getLatitude(), latLng.getLongitude()));

            try {
                JSONObject jsonObject = feature.toJSON();

                // Add a layer with the current point
                dropPointOnMap(latLng);

                return jsonObject;
            } catch (JSONException e) {
                Log.e(TAG, Log.getStackTraceString(e));
            }
        }

        return null;
    }

    @Override
    public @Nullable JSONObject dropPoint(@Nullable LatLng latLng) {
        return null;
    }

    private void showMarkerLayout() {
        markerlayout.setVisibility(VISIBLE);
    }

    private void hideMarkerLayout() {
        markerlayout.setVisibility(GONE);
    }

    private void getMapboxMap() {
        if (mapboxMap == null) {
            getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(MapboxMap mapboxMap) {
                    KujakuMapView.this.mapboxMap = mapboxMap;
                }
            });
        }
    }

    private void dropPointOnMap(@NonNull com.mapbox.mapboxsdk.geometry.LatLng latLng) {
        /*if (pointsLayer == null || pointsSource == null) {
            pointsSource = new GeoJsonSource(pointsSourceId);

            com.mapbox.services.commons.geojson.Feature feature =
                    com.mapbox.services.commons.geojson.Feature.fromGeometry(
                            com.mapbox.services.commons.geojson.Point.fromCoordinates(
                                    new double[]{latLng.getLongitude(), latLng.getLatitude()}
                                    )
                    );
            pointsSource.setGeoJson(feature);

            if (mapboxMap != null) {
                mapboxMap.addSource(pointsSource);

                pointsLayer = new SymbolLayer(pointsLayerId, pointsSourceId);
                pointsLayer.
            }
        }*/

        MarkerOptions markerOptions = new MarkerOptions()
                .position(latLng);
        mapboxMap.addMarker(markerOptions);
    }

    public boolean isCanAddPoint() {
        return canAddPoint;
    }
}
