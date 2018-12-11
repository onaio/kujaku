package io.ona.kujaku.views;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PointF;
import android.location.Location;
import android.os.AsyncTask;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
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
import com.google.gson.JsonElement;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.listener.single.PermissionListener;
import com.mapbox.android.gestures.MoveGestureDetector;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.VisibleRegion;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.MapboxMapOptions;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.style.expressions.Expression;
import com.mapbox.mapboxsdk.style.layers.CircleLayer;
import com.mapbox.mapboxsdk.style.layers.RasterLayer;
import com.mapbox.mapboxsdk.style.layers.Layer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.mapboxsdk.style.sources.RasterSource;
import com.mapbox.mapboxsdk.style.sources.Source;
import com.mapbox.mapboxsdk.style.sources.TileSet;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import io.ona.kujaku.R;
import io.ona.kujaku.callables.AsyncTaskCallable;
import io.ona.kujaku.callbacks.AddPointCallback;
import io.ona.kujaku.exceptions.WmtsCapabilitiesException;
import io.ona.kujaku.interfaces.IKujakuMapView;
import io.ona.kujaku.interfaces.ILocationClient;
import io.ona.kujaku.listeners.BaseLocationListener;
import io.ona.kujaku.listeners.BoundsChangeListener;
import io.ona.kujaku.listeners.OnFeatureClickListener;
import io.ona.kujaku.listeners.OnFinishedListener;
import io.ona.kujaku.listeners.OnLocationChanged;
import io.ona.kujaku.location.clients.AndroidLocationClient;
import io.ona.kujaku.location.clients.GPSLocationClient;
import io.ona.kujaku.tasks.GenericAsyncTask;
import io.ona.kujaku.utils.LocationPermissionListener;
import io.ona.kujaku.utils.LocationSettingsHelper;
import io.ona.kujaku.utils.LogUtil;
import io.ona.kujaku.utils.NetworkUtil;
import io.ona.kujaku.utils.Permissions;
import io.ona.kujaku.utils.Views;
import io.ona.kujaku.utils.wmts.model.WmtsCapabilities;
import io.ona.kujaku.utils.wmts.model.WmtsLayer;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleOpacity;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleRadius;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleStrokeColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleStrokeOpacity;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleStrokeWidth;


import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleOpacity;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleRadius;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleStrokeColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleStrokeOpacity;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleStrokeWidth;


/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 26/09/2018
 */

public class KujakuMapView extends MapView implements IKujakuMapView, MapboxMap.OnMapClickListener {

    private static final String TAG = KujakuMapView.class.getName();
    public static final double LOCATION_FOCUS_ZOOM = 20d;

    private boolean canAddPoint = false;

    private ImageView markerLayout;
    private Button doneAddingPointBtn;
    private ImageButton addPointBtn;
    private Button cancelAddingPoint;
    private MapboxMap mapboxMap;
    private ImageButton currentLocationBtn;

    private CircleLayer userLocationInnerCircle;
    private CircleLayer userLocationOuterCircle;
    private GeoJsonSource pointsSource;
    private String pointsInnerLayerId = UUID.randomUUID().toString();
    private String pointsOuterLayerId = pointsInnerLayerId + "2";
    private String pointsSourceId = UUID.randomUUID().toString();

    private ILocationClient locationClient;
    private Toast currentlyShownToast;

    private LinearLayout addPointButtonsLayout;

    private OnLocationChanged onLocationChangedListener;

    private boolean isMapScrolled = false;

    private static final int ANIMATE_TO_LOCATION_DURATION = 1000;

    protected Set<io.ona.kujaku.domain.Point> droppedPoints;

    private LatLng latestLocation;

    private boolean updateUserLocationOnMap = false;

    /**
     * Wmts Layers to add on the map
     */
    private Set<WmtsLayer> wmtsLayers;

    private FeatureCollection featureCollection;

    private Map<String, Integer> featureMap;

    private Layer primaryLayer;

    private GeoJsonSource primaryGeoJsonSource;

    private String primaryGeoJsonSourceId;

    private String geoJsonSourceString;

    private boolean isFetchSourceFromStyle = false;

    private CameraPosition cameraPosition = null;

    private BoundsChangeListener boundsChangeListener;

    private OnFeatureClickListener onFeatureClickListener;
    private String[] featureClickLayerIdFilters;
    private Expression featureClickExpressionFilter;


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
        checkPermissions();

        markerLayout = findViewById(R.id.iv_mapview_locationSelectionMarker);

        droppedPoints = new HashSet<>();
        wmtsLayers = new HashSet<>();

        doneAddingPointBtn = findViewById(R.id.btn_mapview_locationSelectionBtn);
        addPointButtonsLayout = findViewById(R.id.ll_mapview_locationSelectionBtns);
        addPointBtn = findViewById(R.id.imgBtn_mapview_locationAdditionBtn);
        currentLocationBtn = findViewById(R.id.ib_mapview_focusOnMyLocationIcon);

        getMapboxMap();
        cancelAddingPoint = findViewById(R.id.btn_mapview_locationSelectionCancelBtn);

        currentLocationBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                focusOnUserLocation(true);
            }
        });

        markerLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                markerLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                int height = markerLayout.getMeasuredHeight();
                markerLayout.setY(markerLayout.getY() - (height / 2));
            }
        });

        Map<String, Object> attributes = extractStyleValues(attributeSet);
        String key = getContext().getString(R.string.current_location_btn_visibility);
        if (attributes.containsKey(key)) {
            boolean isCurrentLocationBtnVisible = (boolean) attributes.get(key);
            setVisibility(currentLocationBtn, isCurrentLocationBtnVisible);
        }
        featureMap = new HashMap<>();
    }

    private void showUpdatedUserLocation() {
        updateUserLocationLayer(latestLocation);

        if (updateUserLocationOnMap || !isMapScrolled) {
            // Focus on the new location
            centerMap(latestLocation, ANIMATE_TO_LOCATION_DURATION, getZoomToUse(mapboxMap, LOCATION_FOCUS_ZOOM));
        }
    }

    private void warmUpLocationServices() {
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
                    // Update the location every 5 seconds
                    locationClient.setUpdateIntervals(5000, 5000);
                }

                locationClient.requestLocationUpdates(new BaseLocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        latestLocation = new LatLng(location.getLatitude()
                                , location.getLongitude());


                        if (onLocationChangedListener != null) {
                            onLocationChangedListener.onLocationChanged(location);
                        }

                        if (updateUserLocationOnMap) {
                            showUpdatedUserLocation();
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
    }

    private Map<String, Object> extractStyleValues(@Nullable AttributeSet attrs) {
        Map<String, Object> attributes = new HashMap<>();
        if (attrs != null) {
            TypedArray typedArray = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.KujakuMapView, 0, 0);
            try {
                boolean isCurrentLocationBtnVisible = typedArray.getBoolean(R.styleable.KujakuMapView_current_location_btn_visibility, false);
                attributes.put(getContext().getString(R.string.current_location_btn_visibility), isCurrentLocationBtnVisible);
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
        addPoint(useGPS, addPointCallback, null);
    }

    @Override
    public void addPoint(boolean useGPS, @NonNull AddPointCallback addPointCallback, @Nullable MarkerOptions markerOptions) {

        addPointBtn.setVisibility(VISIBLE);
        addPointBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                addPointBtn.setVisibility(GONE);
                showAddPointLayout(true);

                if (useGPS) {
                    enableAddPoint(true, null);
                    doneAddingPointBtn.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            JSONObject featureJSON = dropPoint(markerOptions);
                            addPointCallback.onPointAdd(featureJSON);

                            enableAddPoint(false, null);

                            showAddPointLayout(false);
                            addPointBtn.setVisibility(VISIBLE);
                        }
                    });
                } else {
                    // Enable the marker layout
                    enableAddPoint(true);
                    doneAddingPointBtn.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            JSONObject featureJSON = dropPoint(markerOptions);
                            addPointCallback.onPointAdd(featureJSON);

                            enableAddPoint(false);

                            showAddPointLayout(false);
                            addPointBtn.setVisibility(VISIBLE);
                        }
                    });
                }
            }
        });

        cancelAddingPoint.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (useGPS) {
                    enableAddPoint(false, null);
                } else {
                    enableAddPoint(false);
                }

                showAddPointLayout(false);
                addPointBtn.setVisibility(VISIBLE);
            }
        });
    }

    private void showAddPointLayout(boolean showLayout) {
        int visible = showLayout ? VISIBLE : GONE;
        addPointButtonsLayout.setVisibility(visible);
    }

    @Override
    public void addPoint(boolean useGPS, @NonNull AddPointCallback addPointCallback, @DrawableRes int markerResourceId) {
        addPoint(useGPS, addPointCallback,
                new MarkerOptions().setIcon(IconFactory.getInstance(getContext()).fromResource(markerResourceId))
        );
    }

    @Override
    public void enableAddPoint(boolean canAddPoint) {
        this.canAddPoint = canAddPoint;

        if (this.canAddPoint) {
            // Show the layer with the marker in the middle
            showMarkerLayout();
        } else {
            hideMarkerLayout();
        }
    }

    public void setViewVisibility(View view, boolean isVisible) {
        view.setVisibility(isVisible ? VISIBLE : GONE);
    }

    @Override
    public void enableAddPoint(boolean canAddPoint, @Nullable final OnLocationChanged onLocationChanged) {
        isMapScrolled = false;
        this.enableAddPoint(canAddPoint);

        if (canAddPoint) {
            this.onLocationChangedListener = onLocationChanged;

            // 1. Focus on the location for the first time is a must
            // 2. Any sub-sequent location updates are dependent on whether the user has touched the UI
            // 3. Show the circle icon on the currrent position -> This will happen whenever there are location updates
            updateUserLocationOnMap = true;
            if (latestLocation != null) {
                showUpdatedUserLocation();
            }
        } else {
            // This should just disable the layout and any ongoing operations for focus
            this.onLocationChangedListener = null;
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
                        circleColor("#4387f4"),
                        circleRadius(5f),
                        circleStrokeWidth(1f),
                        circleStrokeColor("#dde2e4")
                );

                userLocationOuterCircle = new CircleLayer(pointsOuterLayerId, pointsSourceId);
                userLocationOuterCircle.setProperties(
                        circleColor("#81c2ee"),
                        circleRadius(25f),
                        circleStrokeWidth(1f),
                        circleStrokeColor("#74b7f6"),
                        circleOpacity(0.3f),
                        circleStrokeOpacity(0.6f)
                );

                mapboxMap.addLayer(userLocationOuterCircle);
                mapboxMap.addLayer(userLocationInnerCircle);
            }
            // TODO: What if the map already has a source layer with this source layer id
        } else {
            // Get the layer and update it
            if (mapboxMap != null) {
                Source source = mapboxMap.getSource(pointsSourceId);

                if (source instanceof GeoJsonSource) {
                    ((GeoJsonSource) source).setGeoJson(feature);
                }
            }
        }
    }

    @Override
    public @Nullable JSONObject dropPoint() {
        return dropPoint((MarkerOptions) null);
    }


    @Nullable
    @Override
    public JSONObject dropPoint(@DrawableRes int markerResourceId) {
        MarkerOptions markerOptions = new MarkerOptions()
                .setIcon(IconFactory.getInstance(getContext()).fromResource(markerResourceId));

        return dropPoint(markerOptions);
    }

    @Override
    public @Nullable JSONObject dropPoint(@Nullable LatLng latLng) {
        return dropPoint(
                new MarkerOptions()
                        .setPosition(latLng)
        );
    }

    @Nullable
    @Override
    public JSONObject dropPoint(@Nullable MarkerOptions markerOptions) {
        if (mapboxMap != null && canAddPoint) {
            if (markerOptions != null && markerOptions.getPosition() != null) {
                LatLng latLng = markerOptions.getPosition();
                Feature feature = new Feature();
                feature.setGeometry(new Point(latLng.getLatitude(), latLng.getLongitude()));

                try {
                    JSONObject jsonObject = feature.toJSON();

                    // Add a layer with the current point
                    centerMap(latLng, ANIMATE_TO_LOCATION_DURATION, getZoomToUse(mapboxMap, getZoomToUse(mapboxMap, LOCATION_FOCUS_ZOOM)));
                    dropPointOnMap(latLng, markerOptions);

                    enableAddPoint(false);

                    this.onLocationChangedListener = null;

                    if (locationClient != null) {
                        locationClient.stopLocationUpdates();
                    }

                    return jsonObject;
                } catch (JSONException e) {
                    LogUtil.e(TAG, Log.getStackTraceString(e));
                }
            } else {
                LatLng latLng = mapboxMap.getCameraPosition().target;

                Feature feature = new Feature();
                feature.setGeometry(new Point(latLng.getLatitude(), latLng.getLongitude()));

                try {
                    JSONObject jsonObject = feature.toJSON();

                    // Add a layer with the current point
                    dropPointOnMap(latLng, markerOptions);

                    return jsonObject;
                } catch (JSONException e) {
                    LogUtil.e(TAG, Log.getStackTraceString(e));
                }
            }
        }

        return null;
    }

    @Nullable
    @Override
    public JSONObject dropPoint(@Nullable LatLng latLng, @DrawableRes int markerResourceId) {
        MarkerOptions markerOptions = new MarkerOptions()
                .setPosition(latLng)
                .setIcon(
                        IconFactory.getInstance(getContext())
                                .fromResource(markerResourceId)
                );

        return dropPoint(markerOptions);
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
                    mapboxMap.getUiSettings().setCompassEnabled(false);
                    if (KujakuMapView.this.droppedPoints != null) {
                        List<io.ona.kujaku.domain.Point> droppedPoints = new ArrayList<>(KujakuMapView.this.droppedPoints);
                        for (io.ona.kujaku.domain.Point point : droppedPoints) {
                            dropPointOnMap(new LatLng(point.getLat(), point.getLng()));
                        }
                    }

                    if (getPrimaryGeoJsonSource() != null && mapboxMap.getSource(getPrimaryGeoJsonSource().getId()) == null) {
                        mapboxMap.addSource(getPrimaryGeoJsonSource());
                    }
                    if (getPrimaryLayer() != null && mapboxMap.getLayer(getPrimaryLayer().getId()) == null) {
                        mapboxMap.addLayer(getPrimaryLayer());
                    }
                    if (isFetchSourceFromStyle) {
                        initializeSourceAndFeatureCollectionFromStyle();
                        isFetchSourceFromStyle = false;
                    }
                    if (getCameraPosition() != null) {
                        mapboxMap.setCameraPosition(getCameraPosition());
                    }
                    // add bounds change listener
                    addMapScrollListenerAndBoundsChangeEmitterToMap(mapboxMap);
                    callBoundsChangedListeners();
                    enableFeatureClickListenerEmitter(mapboxMap);

                    addWmtsLayers();
                }
            });
        }
    }

    /**
     * Add all Wmts Layers in wmtsLayers on the map
     */
    private void addWmtsLayers() {
        // Add WmtsLayers
        if (wmtsLayers != null) {
            for (WmtsLayer layer : wmtsLayers) {
                if (mapboxMap.getSource(layer.getIdentifier()) == null) {
                    RasterSource webMapSource = new RasterSource(
                            layer.getIdentifier(),
                            new TileSet("tileset", layer.getTemplateUrl()), 256);
                    mapboxMap.addSource(webMapSource);

                    RasterLayer webMapLayer = new RasterLayer(layer.getIdentifier(), layer.getIdentifier());
                    mapboxMap.addLayer(webMapLayer);
                }
            }
        }
    }

    public Set<WmtsLayer> getWmtsLayers() {
        return this.wmtsLayers;
    }

    /**
     * Add first available layer to the wmtsLayer list
     *
     * @param capabilities
     */
    public void addWmtsLayer(WmtsCapabilities capabilities) throws Exception {
       this.addWmtsLayer(capabilities, null, null, null);
    }

    /**
     * Add identified layer to the wmtsLayer list
     *
     * @param layerIdentifier
     * @param capabilities
     */
    public void addWmtsLayer(WmtsCapabilities capabilities, String layerIdentifier) throws Exception {
        this.addWmtsLayer(capabilities, layerIdentifier, null, null);
    }

    /**
     * Add identified layer with specific style to the wmtsLayer list
     *
     * @param capabilities
     * @param layerIdentifier
     * @param styleIdentifier
     */
    public void addWmtsLayer(WmtsCapabilities capabilities, String layerIdentifier, String styleIdentifier) throws Exception {
        this.addWmtsLayer(capabilities, layerIdentifier, styleIdentifier, null);
    }

    /**
     * Add identified layer with specific style & specific tileMatrixSet to the wmtsLayer list
     *
     * @param capabilities
     * @param layerIdentifier
     * @param styleIdentifier
     * @param tileMatrixSetLinkIdentifier
     */
    public void addWmtsLayer(WmtsCapabilities capabilities, String layerIdentifier, String styleIdentifier, String tileMatrixSetLinkIdentifier) throws Exception {
        WmtsLayer layerIdentified;

        if (capabilities == null) {
            throw new WmtsCapabilitiesException ("capabilities object is null or empty");
        }

        if (layerIdentifier == null || layerIdentifier.isEmpty()) { // Take first layer accessible
            if (capabilities.getLayers().size() == 0) {
                // No layer available
                throw new WmtsCapabilitiesException("No layer available in the capacities object");
            } else {
                layerIdentified = capabilities.getLayers().get(0);
            }
        } else {
            // Get the identified layer
            layerIdentified = capabilities.getLayer(layerIdentifier);
        }

        if (layerIdentified == null) {
            throw new WmtsCapabilitiesException(String.format("Layer with identifier %1$s is unknown", layerIdentifier));
        }

        this.selectWmtsStyle(layerIdentified, styleIdentifier);

        this.selectWmtsTileMatrix(layerIdentified, tileMatrixSetLinkIdentifier);

        this.wmtsLayers.add(layerIdentified);

        if (mapboxMap != null) {
            addWmtsLayers();
        }
    }

    /**
     * Verify if Style exists for the Layer
     *
     * @param layer
     * @param styleIdentifier
     * @throws Exception
     */
    private void selectWmtsStyle (WmtsLayer layer, String styleIdentifier) throws Exception {
        if (styleIdentifier != null && !styleIdentifier.isEmpty()) {
            // Check if style is known
            if (layer.getStyle(styleIdentifier) == null) {
                throw new WmtsCapabilitiesException(String.format("Style with identifier %1$s is not available for Layer %2$s", styleIdentifier, layer.getIdentifier()));
            } else {
                layer.setSelectedStyleIdentifier(styleIdentifier);
            }
        }
    }

    /**
     * Verify if TileMatrixSetlink exists exists for the Layer
     *
     * @param layer
     * @param tileMatrixSetLinkIdentifier
     * @throws Exception
     */
    private void selectWmtsTileMatrix (WmtsLayer layer, String tileMatrixSetLinkIdentifier) throws Exception {
        if (tileMatrixSetLinkIdentifier != null && !tileMatrixSetLinkIdentifier.isEmpty()) {
            // Check if style is known
            if (layer.getTileMatrixSet(tileMatrixSetLinkIdentifier) == null) {
                throw new WmtsCapabilitiesException(String.format("tileMatrixSetLink with identifier %1$s is not available for Layer %2$s", tileMatrixSetLinkIdentifier, layer.getIdentifier()));
            } else {
                layer.setSelectedTileMatrixLinkIdentifier(tileMatrixSetLinkIdentifier);
            }
        }
    }


    private void addMapScrollListenerAndBoundsChangeEmitterToMap(@NonNull MapboxMap mapboxMap) {
        mapboxMap.addOnMoveListener(new MapboxMap.OnMoveListener() {
            @Override
            public void onMoveBegin(@NonNull MoveGestureDetector detector) {
                isMapScrolled = true;

                // We should assume the user no longer wants us to focus on their location
                focusOnUserLocation(false);
            }

            @Override
            public void onMove(@NonNull MoveGestureDetector detector) {
                // We are not going to do anything here
            }

            @Override
            public void onMoveEnd(@NonNull MoveGestureDetector detector) {
                callBoundsChangedListeners();
            }
        });
    }

    private void callBoundsChangedListeners() {
        if (boundsChangeListener != null) {
            VisibleRegion visibleRegion = getCurrentBounds();

            if (visibleRegion != null) {
                boundsChangeListener.onBoundsChanged(visibleRegion.farLeft, visibleRegion.farRight
                        , visibleRegion.nearRight, visibleRegion.nearLeft);
            }
        }
    }

    @VisibleForTesting
    @Nullable
    protected VisibleRegion getCurrentBounds() {
        return mapboxMap != null ? mapboxMap.getProjection().getVisibleRegion() : null;
    }

    private void enableFeatureClickListenerEmitter(@NonNull MapboxMap mapboxMap) {
        mapboxMap.removeOnMapClickListener(this);
        mapboxMap.addOnMapClickListener(this);
    }

    private void dropPointOnMap(@NonNull LatLng latLng) {
        dropPointOnMap(latLng, null);
    }

    private void dropPointOnMap(@NonNull LatLng latLng, @Nullable MarkerOptions markerOptionsParam) {
        MarkerOptions markerOptions = markerOptionsParam;
        if (markerOptions == null) {
            markerOptions = new MarkerOptions()
                    .position(latLng);
        } else if (markerOptions.getPosition() == null) {
            markerOptions.setPosition(latLng);
        }

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

    public void centerMap(@NonNull LatLng point, int animateToNewTargetDuration, double newZoom) {
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

    public void centerMap(@NonNull LatLng point, int animateToNewTargetDuration) {
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
            locationClient = null;
        }
    }

    @Override
    public void showCurrentLocationBtn(boolean isVisible) {
        currentLocationBtn.setVisibility(isVisible ? VISIBLE : GONE);
    }

    public void setVisibility(View view, boolean isVisible) {
       view.setVisibility(isVisible ? VISIBLE : GONE);
    }

    public Set<io.ona.kujaku.domain.Point> getDroppedPoints() {
        return droppedPoints;
    }

    public void updateDroppedPoints(List<io.ona.kujaku.domain.Point> droppedPoints) {
        if (droppedPoints == null) {
            return;
        }
        // remove duplicates
        for (io.ona.kujaku.domain.Point point : droppedPoints) {
            if (!this.droppedPoints.contains(point)) {
                // drop new unique points
                if (this.mapboxMap != null) {
                    dropPointOnMap(new LatLng(point.getLat(), point.getLng()));
                }
                this.droppedPoints.add(point);
            }
        }
    }

    @Override
    public void setOnFeatureClickListener(@NonNull OnFeatureClickListener onFeatureClickListener, @Nullable String... layerIds) {
        this.onFeatureClickListener = onFeatureClickListener;
        this.featureClickLayerIdFilters = layerIds;
        this.featureClickExpressionFilter = null;
    }

    @Override
    public void setOnFeatureClickListener(@NonNull OnFeatureClickListener onFeatureClickListener, @Nullable Expression expressionFilter, @Nullable String... layerIds) {
        this.onFeatureClickListener = onFeatureClickListener;
        this.featureClickLayerIdFilters = layerIds;
        this.featureClickExpressionFilter = expressionFilter;
    }

    @Override
    public void focusOnUserLocation(boolean focusOnMyLocation) {
        if (focusOnMyLocation) {
            isMapScrolled = false;
            changeTargetIcon(R.drawable.ic_cross_hair_blue);

            // Enable the listener & show the current user location
            updateUserLocationOnMap = true;
            if (latestLocation != null) {
                showUpdatedUserLocation();
            }

        } else {
            updateUserLocationOnMap = false;
            changeTargetIcon(R.drawable.ic_cross_hair);
        }
    }

    @Override
    public void setBoundsChangeListener(@Nullable BoundsChangeListener boundsChangeListener) {
        this.boundsChangeListener = boundsChangeListener;

        callBoundsChangedListeners();
    }

    private void changeTargetIcon(int drawableIcon) {
        Views.changeDrawable(currentLocationBtn, drawableIcon);
    }

    private void checkPermissions() {
        if (getContext() instanceof Activity) {
            final Activity activity = (Activity) getContext();
            PermissionListener dialogPermissionListener = new LocationPermissionListener(activity);

            Dexter.withActivity(activity)
                    .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                    .withListener(dialogPermissionListener)
                    .check();
        } else {
            Log.wtf(TAG, "KujakuMapView was not started in an activity!! This is very bad or it is being used in tests. We are going to ignore the permissions check! Good luck");
        }
    }

    @Override
    public void addFeaturePoints(FeatureCollection featureCollection) {
        List<com.mapbox.geojson.Feature> features = this.featureCollection.features();
        for (com.mapbox.geojson.Feature feature : featureCollection.features()) {
            String featureId = feature.id();
            if (featureId != null && !featureMap.containsKey(featureId)) {
                featureMap.put(featureId, features.size());
                features.add(feature);
            }
        }
        if (mapboxMap != null) {
            ((GeoJsonSource) mapboxMap.getSource(primaryGeoJsonSource.getId())).setGeoJson(this.featureCollection);
        }
    }

    @Override
    public void updateFeaturePointProperties(FeatureCollection featureCollection) throws JSONException {
        List<com.mapbox.geojson.Feature> currFeatures = this.featureCollection.features();
        List<com.mapbox.geojson.Feature> newFeatures = new ArrayList<>();
        for (com.mapbox.geojson.Feature feature : featureCollection.features()) {
            String featureId = feature.id();
            if (featureMap.containsKey(featureId)) {
                int featureIndex = featureMap.get(featureId);
                com.mapbox.geojson.Feature currFeature = currFeatures.get(featureIndex);
                for (Map.Entry<String, JsonElement> entry : feature.properties().entrySet()) {
                    currFeature.removeProperty(entry.getKey());
                    currFeature.addStringProperty(entry.getKey(), entry.getValue().getAsString());
                }
            } else {
                newFeatures.add(feature);
            }
        }
        // add new features if any
        FeatureCollection newFeatureCollection = FeatureCollection.fromFeatures(newFeatures);
        addFeaturePoints(newFeatureCollection);
        if (mapboxMap != null) {
            ((GeoJsonSource) mapboxMap.getSource(primaryGeoJsonSource.getId())).setGeoJson(this.featureCollection);
        }
    }

    public void initializePrimaryGeoJsonSource(String sourceId, boolean isFetchSourceFromStyle, String geoJsonSource) {
        if (sourceId == null || (isFetchSourceFromStyle && geoJsonSource == null)) {
            Log.e(TAG, "GeoJson source initialization failed! Ensure that the source id is not null or that the GeoJson source is not null.");
            return;
        }
        initializeFeatureCollection();
        if (isFetchSourceFromStyle) {
            this.isFetchSourceFromStyle = true;
            setPrimaryGeoJsonSourceId(sourceId);
            setGeoJsonSourceString(geoJsonSource);
        } else {
            primaryGeoJsonSource = new GeoJsonSource(sourceId, featureCollection);
        }
    }

    private void initializeSourceAndFeatureCollectionFromStyle() {
        try {
            FeatureCollection featureCollection = FeatureCollection.fromJson(getGeoJsonSourceString());
            primaryGeoJsonSource = mapboxMap.getSourceAs(getPrimaryGeoJsonSourceId());
            addFeaturePoints(featureCollection);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    private void initializeFeatureCollection() {
        featureCollection = FeatureCollection.fromFeatures(new ArrayList<>());
    }

    public CameraPosition getCameraPosition() {
        return cameraPosition;
    }

    public void setCameraPosition(CameraPosition cameraPosition) {
        this.cameraPosition = cameraPosition;
    }

    public GeoJsonSource getPrimaryGeoJsonSource() {
        return primaryGeoJsonSource;
    }

    public Layer getPrimaryLayer() {
        return primaryLayer;
    }

    public void setPrimaryLayer(Layer layer) {
        primaryLayer = layer;
    }

    public String getPrimaryGeoJsonSourceId() {
        return primaryGeoJsonSourceId;
    }

    public void setPrimaryGeoJsonSourceId(String primaryGeoJsonSourceId) {
        this.primaryGeoJsonSourceId = primaryGeoJsonSourceId;
    }

    public String getGeoJsonSourceString() {
        return geoJsonSourceString;
    }

    public void setGeoJsonSourceString(String geoJsonSourceString) {
        this.geoJsonSourceString = geoJsonSourceString;
    }

    @Override
    public void onPause() {
        if (locationClient != null && locationClient.isMonitoringLocation()) {
            locationClient.stopLocationUpdates();
            locationClient.close();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getMapboxMap();
        // This prevents an overlay issue the first time when requesting for permissions
        if (Permissions.check(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)) {
            if (getContext() instanceof Activity) {
                final Activity activity = (Activity) getContext();
                LocationSettingsHelper.checkLocationEnabled(activity);
            }
            warmUpLocationServices();
        }
    }

    @Override
    public void onMapClick(@NonNull LatLng point) {
        if (onFeatureClickListener != null) {
            PointF pixel = mapboxMap.getProjection().toScreenLocation(point);
            List<com.mapbox.geojson.Feature> features = mapboxMap.queryRenderedFeatures(pixel, featureClickExpressionFilter, featureClickLayerIdFilters);

            if (features.size() > 0) {
                onFeatureClickListener.onFeatureClick(features);
            }
        }
    }
}

