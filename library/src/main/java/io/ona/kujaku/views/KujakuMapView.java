package io.ona.kujaku.views;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.cocoahero.android.geojson.Feature;
import com.cocoahero.android.geojson.Point;
import com.google.android.gms.maps.model.LatLng;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
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
import io.ona.kujaku.callables.AsyncTaskCallable;
import io.ona.kujaku.callbacks.AddPointCallback;
import io.ona.kujaku.interfaces.IKujakuMapView;
import io.ona.kujaku.interfaces.ILocationClient;
import io.ona.kujaku.listeners.BaseLocationListener;
import io.ona.kujaku.listeners.OnFinishedListener;
import io.ona.kujaku.listeners.OnLocationChanged;
import io.ona.kujaku.location.clients.AndroidLocationClient;
import io.ona.kujaku.location.clients.GPSLocationClient;
import io.ona.kujaku.tasks.GenericAsyncTask;
import io.ona.kujaku.utils.LogUtil;
import io.ona.kujaku.utils.NetworkUtil;
import io.ona.kujaku.utils.Views;

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
    private ImageButton myLocationBtn;

    private LinearLayout addPointButtonsLayout;

    private SymbolLayer pointsLayer;
    private GeoJsonSource pointsSource;
    private String pointsLayerId = UUID.randomUUID().toString();
    private String pointsSourceId = UUID.randomUUID().toString();

    private ILocationClient locationClient;
    private Toast currentlyShownToast;
    private OnLocationChanged onLocationChanged;

    private static final int ANIMATE_TO_LOCATION_DURATION = 1000;

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
        myLocationBtn = findViewById(R.id.ib_mapview_focusOnMyLocationIcon);

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
    public void enableAddPoint(boolean canAddPoint, @NonNull final OnLocationChanged onLocationChanged) {
        this.enableAddPoint(canAddPoint);
        if (canAddPoint) {
            this.onLocationChanged = onLocationChanged;
            showMarkerLayout();
            GenericAsyncTask genericAsyncTask = new GenericAsyncTask(new AsyncTaskCallable() {
                @Override
                public Object[] call() throws Exception {
                    return new Object[]{ NetworkUtil.isInternetAvailable()};
                }
            });
            genericAsyncTask.setOnFinishedListener(new OnFinishedListener() {
                @Override
                public void onSuccess(Object[] objects) {
                    if ((boolean) objects[0]) {
                        // Use the fused location API
                        locationClient = new AndroidLocationClient(getContext());
                    } else {
                        // Use the GPS hardware
                        locationClient = new GPSLocationClient();
                    }

                    locationClient.requestLocationUpdates(new BaseLocationListener() {
                        @Override
                        public void onLocationChanged(Location location) {
                            onLocationChanged.onLocationChanged(location);

                            // Focus on the new location
                            centerMap(new com.mapbox.mapboxsdk.geometry.LatLng(location.getLatitude()
                                    , location.getLongitude()), ANIMATE_TO_LOCATION_DURATION);
                        }
                    });
                }

                @Override
                public void onError(Exception e) {
                    LogUtil.e(TAG, e);
                }
            });
            genericAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            // This should just disable the layout and any ongoing operations for focus
            this.onLocationChanged = null;

            if (locationClient != null) {
                locationClient.setListener(null);
                locationClient.stopLocationUpdates();
            }
        }
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
                LogUtil.e(TAG, Log.getStackTraceString(e));
            }
        }

        return null;
    }

    @Override
    public @Nullable JSONObject dropPoint(@Nullable com.mapbox.mapboxsdk.geometry.LatLng latLng) {
        if (mapboxMap != null && canAddPoint) {
            Feature feature = new Feature();
            feature.setGeometry(new Point(latLng.getLatitude(), latLng.getLongitude()));

            try {
                JSONObject jsonObject = feature.toJSON();

                // Add a layer with the current point
                centerMap(latLng, ANIMATE_TO_LOCATION_DURATION);
                dropPointOnMap(latLng);

                enableAddPoint(false);

                this.onLocationChanged = null;

                if (locationClient != null) {
                    locationClient.stopLocationUpdates();
                }

                return jsonObject;
            } catch (JSONException e) {
                LogUtil.e(TAG, Log.getStackTraceString(e));
            }
        }

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

    private void showToast(String text, int length, boolean override) {
        if (override && currentlyShownToast != null) {
            // TODO: This needs to be fixed because the currently showing toast will not be cancelled if another non-overriding toast was called after it
            currentlyShownToast.cancel();
        }

        currentlyShownToast = Toast.makeText(getContext(), text, length);
        currentlyShownToast.show();
    }

    private void showToast(String text) {
        showToast(text, Toast.LENGTH_LONG, false);
    }

    private void changeTargetIcon(int drawableIcon) {
        Views.changeDrawable(myLocationBtn, drawableIcon);
    }

    public void centerMap(@NonNull com.mapbox.mapboxsdk.geometry.LatLng point, int animateToNewTargetDuration) {
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(point)
                .build();

        if (mapboxMap != null) {
            mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), animateToNewTargetDuration);
        }
    }
}
