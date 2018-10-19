package io.ona.kujaku.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.location.Location;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.cocoahero.android.geojson.Feature;
import com.cocoahero.android.geojson.Point;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.MapboxMapOptions;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.style.layers.CircleLayer;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.mapboxsdk.style.sources.Source;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    public static final double LOCATION_FOCUS_ZOOM = 20d;

    private boolean canAddPoint = false;

    private ImageView markerLayout;
    private Button doneAddingPointBtn;
    private Button addPointBtn;
    private MapboxMap mapboxMap;
    private ImageButton currentLocationBtn;

    private LinearLayout addPointButtonsLayout;

    private CircleLayer userLocationInnerCircle;
    private CircleLayer userLocationOuterCircle;
    private GeoJsonSource pointsSource;
    private String pointsInnerLayerId = UUID.randomUUID().toString();
    private String pointsOuterLayerId = pointsInnerLayerId + "2";
    private String pointsSourceId = UUID.randomUUID().toString();

    private ILocationClient locationClient;
    private Toast currentlyShownToast;
    private OnLocationChanged onLocationChanged;

    private boolean isCurrentLocationBtnClicked = false;
    private boolean isMapScrolled = false;

    private static final int ANIMATE_TO_LOCATION_DURATION = 1000;

    private List<io.ona.kujaku.domain.Point> droppedPoints;

    private Location latestLocation;

    public KujakuMapView(@NonNull Context context) {
        super(context);
        init(null);
    }

    public KujakuMapView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public KujakuMapView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    public KujakuMapView(@NonNull Context context, @Nullable MapboxMapOptions options) {
        super(context, options);
        init(null);
    }

    private void init(@Nullable AttributeSet attributeSet) {
        markerLayout = findViewById(R.id.iv_mapview_locationSelectionMarker);
        doneAddingPointBtn = findViewById(R.id.btn_mapview_locationSelectionBtn);
        addPointButtonsLayout = findViewById(R.id.ll_mapview_addBtnsLayout);
        addPointBtn = findViewById(R.id.btn_mapview_locationAdditionBtn);
        currentLocationBtn = findViewById(R.id.ib_mapview_focusOnMyLocationIcon);

        getMapboxMap();

        markerLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                markerLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                int height = markerLayout.getMeasuredHeight();
                markerLayout.setY(markerLayout.getY() - (height/2));
            }
        });

        this.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                isMapScrolled = true;
                return false;
            }
        });

        Map<String, Object> attributes = extractStyleValues(attributeSet);
        String key = getContext().getString(R.string.current_location_btn_visibility);
        if (attributes.containsKey(key)) {
            boolean isCurrentLocationBtnVisible = (boolean) attributes.get(key);
            setVisibility(currentLocationBtn, isCurrentLocationBtnVisible);
        }

        key = getContext().getString(R.string.add_btn_visibility);
        if (attributes.containsKey(key)) {
            boolean isAddPointBtnVisible = (boolean) attributes.get(key);
            setVisibility(addPointBtn, isAddPointBtnVisible);
        }

        key = getContext().getString(R.string.done_btn_visibility);
        if (attributes.containsKey(key)) {
            boolean isDoneAddingPointBtnVisible = (boolean) attributes.get(key);
            setVisibility(doneAddingPointBtn, isDoneAddingPointBtnVisible);
        }
    }

    private Map<String, Object> extractStyleValues(@Nullable AttributeSet attrs) {
        Map<String, Object> attributes = new HashMap<>();
        if (attrs != null) {
            TypedArray typedArray = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.KujakuMapView, 0, 0);
            try {
                boolean isCurrentLocationBtnVisible = typedArray.getBoolean(R.styleable.KujakuMapView_current_location_btn_visibility, false);
                attributes.put(getContext().getString(R.string.current_location_btn_visibility), isCurrentLocationBtnVisible);

                boolean isAddPointBtnVisible  = typedArray.getBoolean(R.styleable.KujakuMapView_add_btn_visibility, false);
                attributes.put(getContext().getString(R.string.add_btn_visibility), isAddPointBtnVisible);

                boolean isDoneAddingPointBtnVisible = typedArray.getBoolean(R.styleable.KujakuMapView_done_btn_visibility, false);
                attributes.put(getContext().getString(R.string.done_btn_visibility), isDoneAddingPointBtnVisible);
            } catch (Exception e) {
                Log.d(TAG, e.getMessage());
            } finally {
                typedArray.recycle();
            }
        }
        return attributes;
    }

    @Override
    public void addPoint(boolean useGPS, @NonNull final AddPointCallback addPointCallback) {

        if (useGPS) {
            enableAddPoint(true, null);
            doneAddingPointBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    enableAddPoint(false, null);
                    showAddPointLayout(false);
                }
            });
        } else {
            // Enable the marker layout
            enableAddPoint(true);
            doneAddingPointBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    enableAddPoint(false);
                    addPointCallback.onCancel();

                    showAddPointLayout(false);
                }
            });
        }

        showAddPointLayout(true);
        addPointBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                JSONObject feature = dropPoint();
                addPointCallback.onPointAdd(feature);
            }
        });
    }

    private void showAddPointLayout(boolean showLayout) {
        int visible = showLayout ? VISIBLE : GONE;

        doneAddingPointBtn.setVisibility(visible);
        addPointBtn.setVisibility(visible);
        addPointButtonsLayout.setVisibility(visible);
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
    public void enableAddPoint(boolean canAddPoint, @Nullable final OnLocationChanged onLocationChanged) {
        isCurrentLocationBtnClicked = false;
        isMapScrolled = false;
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
                        locationClient = new GPSLocationClient(getContext());
                    }

                    locationClient.requestLocationUpdates(new BaseLocationListener() {
                        @Override
                        public void onLocationChanged(Location location) {
                            if (KujakuMapView.this.onLocationChanged != null) {
                                KujakuMapView.this.onLocationChanged.onLocationChanged(location);
                            }
                            // 1. Focus on the location for the first time is a must
                            // 2. Any sub-sequent location updates are dependent on whether the user has touched the UI
                            // 3. Show the circle icon on the currrent position -> This will happen whenever there are location updates

                            LatLng userLatLng = new com.mapbox.mapboxsdk.geometry.LatLng(location.getLatitude()
                                    , location.getLongitude());
                            updateUserLocationLayer(userLatLng);

                            if (!isCurrentLocationBtnClicked || !isMapScrolled) {
                                // Focus on the new location
                                centerMap(new com.mapbox.mapboxsdk.geometry.LatLng(location.getLatitude()
                                        , location.getLongitude()), ANIMATE_TO_LOCATION_DURATION, getZoomToUse(mapboxMap, LOCATION_FOCUS_ZOOM));
                                isCurrentLocationBtnClicked = true;
                            }
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
                locationClient.stopLocationUpdates();
            }
        }
    }

    private void updateUserLocationLayer(@NonNull LatLng latLng) {
        com.mapbox.geojson.Feature feature =
                com.mapbox.geojson.Feature.fromGeometry(
                        com.mapbox.geojson.Point.fromLngLat(
                                latLng.getLongitude(), latLng.getLatitude()
                        )
                );

        if (userLocationOuterCircle == null || userLocationInnerCircle == null || pointsSource == null) {
            pointsSource = new GeoJsonSource(pointsSourceId);
            pointsSource.setGeoJson(feature);

            if (mapboxMap != null && mapboxMap.getSource(pointsSourceId) == null) {
                mapboxMap.addSource(pointsSource);

                userLocationInnerCircle = new CircleLayer(pointsInnerLayerId, pointsSourceId);
                userLocationInnerCircle.setProperties(
                        PropertyFactory.circleColor("#4387f4"),
                        PropertyFactory.circleRadius(5f),
                        PropertyFactory.circleStrokeWidth(1f),
                        PropertyFactory.circleStrokeColor("#dde2e4")
                );

                userLocationOuterCircle = new CircleLayer(pointsOuterLayerId, pointsSourceId);
                userLocationOuterCircle.setProperties(
                        PropertyFactory.circleColor("#deeaf2"),
                        PropertyFactory.circleRadius(25f),
                        PropertyFactory.circleStrokeWidth(3f),
                        PropertyFactory.circleStrokeColor("#cfdeed"),
                        PropertyFactory.circleOpacity(0.72f)
                );

                mapboxMap.addLayer(userLocationOuterCircle);
                mapboxMap.addLayer(userLocationInnerCircle);
            }
            // TODO: What if the map already has a source layer with this source layer id
        } else {
            // Get the layer and update it
            Source source = mapboxMap.getSource(pointsSourceId);

            if (source instanceof GeoJsonSource) {
                ((GeoJsonSource) source).setGeoJson(feature);
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
        if (latLng != null && mapboxMap != null && canAddPoint) {
            Feature feature = new Feature();
            feature.setGeometry(new Point(latLng.getLatitude(), latLng.getLongitude()));

            try {
                JSONObject jsonObject = feature.toJSON();

                // Add a layer with the current point
                centerMap(latLng, ANIMATE_TO_LOCATION_DURATION, getZoomToUse(mapboxMap, getZoomToUse(mapboxMap, LOCATION_FOCUS_ZOOM)));
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
        markerLayout.setVisibility(VISIBLE);
    }

    private void hideMarkerLayout() {
        markerLayout.setVisibility(GONE);
    }

    private void getMapboxMap() {
        if (mapboxMap == null) {
            getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(MapboxMap mapboxMap) {
                    KujakuMapView.this.mapboxMap = mapboxMap;
                    if (droppedPoints != null) {
                        for (io.ona.kujaku.domain.Point point : droppedPoints) {
                            dropPointOnMap(new LatLng(point.getLat(), point.getLng()));
                        }
                    }
                }
            });
        }
    }

    public void dropPointOnMap(@NonNull com.mapbox.mapboxsdk.geometry.LatLng latLng) {
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

                pointsLayer = new SymbolLayer(pointsInnerLayerId, pointsSourceId);
                pointsLayer.
            }
        }*/

        MarkerOptions markerOptions = new MarkerOptions()
                .position(latLng);
        if (mapboxMap != null) {
            mapboxMap.addMarker(markerOptions);
        }
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

    private void changeTargetIcon(int drawableIcon) {
        Views.changeDrawable(currentLocationBtn, drawableIcon);
    }

    public void centerMap(@NonNull com.mapbox.mapboxsdk.geometry.LatLng point, int animateToNewTargetDuration, double newZoom) {
        CameraPosition.Builder cameraPositionBuilder = new CameraPosition.Builder()
                .target(point);
        if (newZoom != -1d) {
            cameraPositionBuilder.zoom(newZoom);
        }

        CameraPosition cameraPosition = cameraPositionBuilder.build();

        if (mapboxMap != null) {
            mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), animateToNewTargetDuration);
        }
    }

    public void centerMap(@NonNull com.mapbox.mapboxsdk.geometry.LatLng point, int animateToNewTargetDuration) {
        centerMap(point, animateToNewTargetDuration, -1d);
    }

    private double getZoomToUse(@NonNull MapboxMap mapboxMap, double zoomLevel) {
        return mapboxMap == null ? zoomLevel : mapboxMap.getCameraPosition().zoom > zoomLevel ? -1d : zoomLevel;
    }

    @Override
    public void onStop() {
        super.onStop();

        // Clean up location services
        if (locationClient != null && locationClient.isMonitoringLocation()) {
            locationClient.setListener(null);
            locationClient.stopLocationUpdates();
            locationClient = null;
        }
    }

    public void setVisibility(View view, boolean isVisible) {
       view.setVisibility(isVisible ? VISIBLE : GONE);
    }

    public List<io.ona.kujaku.domain.Point> getDroppedPoints() {
        return droppedPoints;
    }

    public void setDroppedPoints(List<io.ona.kujaku.domain.Point> droppedPoints) {
        this.droppedPoints = droppedPoints;
    }
}
