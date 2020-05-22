package io.ona.kujaku.views;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.res.TypedArray;
import android.graphics.PointF;
import android.location.Location;
import android.location.LocationListener;
import android.os.IBinder;
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

import com.cocoahero.android.geojson.Feature;
import com.cocoahero.android.geojson.Point;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.gson.JsonElement;
import com.mapbox.android.gestures.MoveGestureDetector;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.VisibleRegion;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.MapboxMapOptions;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.expressions.Expression;
import com.mapbox.mapboxsdk.style.layers.Layer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.ona.kujaku.R;
import io.ona.kujaku.callbacks.AddPointCallback;
import io.ona.kujaku.callbacks.OnLocationServicesEnabledCallBack;
import io.ona.kujaku.exceptions.TrackingServiceNotInitializedException;
import io.ona.kujaku.exceptions.WmtsCapabilitiesException;
import io.ona.kujaku.helpers.MapboxLocationComponentWrapper;
import io.ona.kujaku.helpers.PermissionsHelper;
import io.ona.kujaku.helpers.wmts.WmtsHelper;
import io.ona.kujaku.interfaces.IKujakuMapView;
import io.ona.kujaku.interfaces.ILocationClient;
import io.ona.kujaku.layers.KujakuLayer;
import io.ona.kujaku.listeners.BaseLocationListener;
import io.ona.kujaku.listeners.BoundsChangeListener;
import io.ona.kujaku.listeners.LocationClientStartedCallback;
import io.ona.kujaku.listeners.OnFeatureClickListener;
import io.ona.kujaku.listeners.OnKujakuLayerClickListener;
import io.ona.kujaku.listeners.OnKujakuLayerLongClickListener;
import io.ona.kujaku.listeners.OnLocationChanged;
import io.ona.kujaku.listeners.TrackingServiceListener;
import io.ona.kujaku.location.KujakuLocation;
import io.ona.kujaku.location.clients.AndroidGpsLocationClient;
import io.ona.kujaku.location.clients.GoogleLocationClient;
import io.ona.kujaku.manager.AnnotationRepositoryManager;
import io.ona.kujaku.mbtiles.MBTilesHelper;
import io.ona.kujaku.services.TrackingService;
import io.ona.kujaku.services.configurations.TrackingServiceDefaultUIConfiguration;
import io.ona.kujaku.services.configurations.TrackingServiceUIConfiguration;
import io.ona.kujaku.services.options.TrackingServiceHighAccuracyOptions;
import io.ona.kujaku.services.options.TrackingServiceOptions;
import io.ona.kujaku.utils.Constants;
import io.ona.kujaku.utils.LocationSettingsHelper;
import io.ona.kujaku.utils.LogUtil;
import io.ona.kujaku.utils.Permissions;
import io.ona.kujaku.wmts.model.WmtsCapabilities;
import io.ona.kujaku.wmts.model.WmtsLayer;
import timber.log.Timber;

import static com.mapbox.mapboxsdk.Mapbox.getApplicationContext;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 26/09/2018
 */
public class KujakuMapView extends MapView implements IKujakuMapView, MapboxMap.OnMapClickListener, MapboxMap.OnMapLongClickListener {

    private static final String TAG = KujakuMapView.class.getName();
    public static final double LOCATION_FOCUS_ZOOM = 20d;

    private boolean canAddPoint = false;

    private ImageView markerLayout;
    private Button doneAddingPointBtn;
    private ImageButton addPointBtn;
    private Button cancelAddingPoint;
    private MapboxMap mapboxMap;
    private ImageButton currentLocationBtn;
    private ImageView trackingServiceStatusButton;

    private ILocationClient locationClient;

    private LinearLayout addPointButtonsLayout;

    private OnLocationChanged onLocationChangedListener;

    private static final int ANIMATE_TO_LOCATION_DURATION = 1000;

    protected Set<io.ona.kujaku.domain.Point> droppedPoints;

    private LatLng latestLocationCoordinates;

    private Location latestLocation;

    private MapboxLocationComponentWrapper mapboxLocationComponentWrapper;

    private boolean updateUserLocationOnMap = false;
    private boolean updateCameraUserLocationOnMap = false;
    private int locationRenderMode = RenderMode.NORMAL;

    private final float DEFAULT_LOCATION_OUTER_CIRCLE_RADIUS = 25f;

    private float locationBufferRadius = DEFAULT_LOCATION_OUTER_CIRCLE_RADIUS;

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

    private OnKujakuLayerClickListener onKujakuLayerClickListener;
    private OnKujakuLayerLongClickListener onKujakuLayerLongClickListener;

    private boolean warmGps = true;
    private boolean hasAlreadyRequestedEnableLocation = false;
    private boolean isResumingFromRequestingEnableLocation = false;

    private String locationEnableRejectionDialogTitle;
    private String locationEnableRejectionDialogMessage;
    private OnLocationServicesEnabledCallBack onLocationServicesEnabledCallBack;

    private ArrayList<KujakuLayer> kujakuLayers = new ArrayList<>();
    private ArrayList<LocationClientStartedCallback> locationClientCallbacks = new ArrayList<>();

    /**
     * Tracking Service
     **/
    private TrackingService trackingService = null;
    private boolean trackingServiceBound = false;
    private TrackingServiceListener trackingServiceListener = null;
    private TrackingServiceUIConfiguration trackingServiceUIConfiguration = null;
    private TrackingServiceOptions trackingServiceOptions = null;
    private boolean trackingServiceInitialized = false;

    private int resourceId;
    private boolean disableMyLocationOnMapMove = false;

    private boolean useGoogleLocationClientInsteadOfAndroidGpsClient = true;

    /**
     * MBtiles
     **/
    private MBTilesHelper mbTilesHelper;

    /**
     * Drawing Manager
     **/
    // private DrawingManager drawingManager = null ;
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
        PermissionsHelper.checkPermissions(TAG, getContext());

        markerLayout = findViewById(R.id.iv_mapview_locationSelectionMarker);

        droppedPoints = new HashSet<>();
        wmtsLayers = new HashSet<>();

        doneAddingPointBtn = findViewById(R.id.btn_mapview_locationSelectionBtn);
        addPointButtonsLayout = findViewById(R.id.ll_mapview_locationSelectionBtns);
        addPointBtn = findViewById(R.id.imgBtn_mapview_locationAdditionBtn);
        currentLocationBtn = findViewById(R.id.ib_mapview_focusOnMyLocationIcon);
        trackingServiceStatusButton = findViewById(R.id.iv_mapview_tracking_service_status);
        mbTilesHelper = new MBTilesHelper();

        getMapboxMap();
        cancelAddingPoint = findViewById(R.id.btn_mapview_locationSelectionCancelBtn);

        currentLocationBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // Enable asking for enabling the location by resetting this flag in case it was true
                hasAlreadyRequestedEnableLocation = false;
                updateCameraUserLocationOnMap = true;
                setWarmGps(true, null, null, new OnLocationServicesEnabledCallBack() {
                    @Override
                    public void onSuccess() {
                        focusOnUserLocation(resourceId == 0 || resourceId == R.drawable.ic_cross_hair, locationBufferRadius);
                    }
                });
            }
        });

        markerLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                markerLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                int height = markerLayout.getMeasuredHeight();
                markerLayout.setY(markerLayout.getY() - (height / 2f));
            }
        });

        Map<String, Object> attributes = extractStyleValues(attributeSet);
        String locationBtnVisibilityKey = getContext().getString(R.string.current_location_btn_visibility);
        if (attributes.containsKey(locationBtnVisibilityKey)) {
            boolean isCurrentLocationBtnVisible = (boolean) attributes.get(locationBtnVisibilityKey);
            setVisibility(currentLocationBtn, isCurrentLocationBtnVisible);
        }

        String warmGPSKey = getContext().getString(R.string.mapbox_warmGps);
        if (attributes.containsKey(warmGPSKey)) {
            warmGps = (boolean) attributes.get(warmGPSKey);
        }

        String locationClientKey = getContext().getString(R.string.locationClient);
        if (attributes.containsKey(locationClientKey)) {
            useGoogleLocationClientInsteadOfAndroidGpsClient = ((int) attributes.get(locationClientKey)) == 0;
        }

        featureMap = new HashMap<>();
        mapboxLocationComponentWrapper = new MapboxLocationComponentWrapper();
    }

    private void showUpdatedUserLocation(Float radius) {
        showUpdatedUserLocation(radius, 1f);
    }

    private void showUpdatedUserLocation(Float radius, Float distanceMoved) {
        if (updateUserLocationOnMap) {
            updateUserLocation(radius);
        }

        if (updateCameraUserLocationOnMap && distanceMoved != 0) {
            // Focus on the new location only if device moved
            centerMap(latestLocationCoordinates, ANIMATE_TO_LOCATION_DURATION, getZoomToUse(mapboxMap, LOCATION_FOCUS_ZOOM));
        }
    }

    private void warmUpLocationServices() {
        locationClient = useGoogleLocationClientInsteadOfAndroidGpsClient ? new GoogleLocationClient(getContext()) : new AndroidGpsLocationClient(getContext());
        locationClient.requestLocationUpdates(new BaseLocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                float distanceMoved = -1;
                if (location != null) {
                    if (latestLocation != null) {
                        distanceMoved = latestLocation.distanceTo(location);
                    }
                    latestLocation = location;
                    latestLocationCoordinates = new LatLng(location.getLatitude()
                            , location.getLongitude());
                }

                if (onLocationChangedListener != null) {
                    onLocationChangedListener.onLocationChanged(location);
                }

                showUpdatedUserLocation(locationBufferRadius, distanceMoved);
            }
        });

        for (LocationClientStartedCallback locationClientStartedCallback : locationClientCallbacks) {
            locationClientStartedCallback.onStarted(locationClient);
        }

        locationClientCallbacks.clear();
    }

    private Map<String, Object> extractStyleValues(@Nullable AttributeSet attrs) {
        Map<String, Object> attributes = new HashMap<>();
        if (attrs != null) {
            TypedArray typedArray = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.KujakuMapView, 0, 0);
            try {
                boolean isCurrentLocationBtnVisible = typedArray.getBoolean(R.styleable.KujakuMapView_current_location_btn_visibility, false);
                boolean isWarmGps = typedArray.getBoolean(R.styleable.KujakuMapView_mapbox_warmGps, true);
                attributes.put(getContext().getString(R.string.current_location_btn_visibility), isCurrentLocationBtnVisible);
                attributes.put(getContext().getString(R.string.mapbox_warmGps), isWarmGps);
                attributes.put(getContext().getString(R.string.locationClient), typedArray.getInt(R.styleable.KujakuMapView_locationClient, 0));
            } catch (Exception e) {
                LogUtil.e(TAG, e);
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

    private void showMarkerLayout() {
        markerLayout.setVisibility(VISIBLE);
    }

    private void hideMarkerLayout() {
        markerLayout.setVisibility(GONE);
    }

    public void setViewVisibility(View view, boolean isVisible) {
        view.setVisibility(isVisible ? VISIBLE : GONE);
    }

    @Override
    public void enableAddPoint(boolean canAddPoint, @Nullable final OnLocationChanged onLocationChanged) {
        this.enableAddPoint(canAddPoint);

        if (canAddPoint) {
            this.onLocationChangedListener = onLocationChanged;

            // 1. Focus on the location for the first time is a must
            // 2. Any sub-sequent location updates are dependent on whether the user has touched the UI
            // 3. Show the circle icon on the current position -> This will happen whenever there are location updates
            updateUserLocationOnMap = true;
            updateCameraUserLocationOnMap = true;
            if (latestLocationCoordinates != null) {
                showUpdatedUserLocation(locationBufferRadius);
            }
        } else {
            // This should just disable the layout and any ongoing operations for focus
            this.onLocationChangedListener = null;
        }
    }

    private void updateUserLocation(Float locationBufferRadius) {
        this.locationBufferRadius = locationBufferRadius == null ? this.locationBufferRadius : locationBufferRadius;
        if (latestLocation != null) {
            latestLocation.setAccuracy(this.locationBufferRadius);

            if (getMapboxLocationComponentWrapper().getLocationComponent() != null) {
                getMapboxLocationComponentWrapper().getLocationComponent().forceLocationUpdate(latestLocation);
            }
        }
    }

    @Override
    public @Nullable
    JSONObject dropPoint() {
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
    public @Nullable
    JSONObject dropPoint(@Nullable LatLng latLng) {
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

    private void getMapboxMap() {
        if (mapboxMap == null) {
            getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(@NonNull MapboxMap mapboxMap) {
                    KujakuMapView.this.mapboxMap = mapboxMap;
                    mapboxMap.getUiSettings().setCompassEnabled(false);

                    // Operations that require the style to be loaded
                    mapboxMap.getStyle(new Style.OnStyleLoaded() {
                        @Override
                        public void onStyleLoaded(@NonNull Style style) {
                            afterStyleLoadedOperations(style);
                        }
                    });
                }
            });
        }
    }

    private void afterStyleLoadedOperations(@NonNull Style style) {
        if (KujakuMapView.this.droppedPoints != null) {
            List<io.ona.kujaku.domain.Point> droppedPoints = new ArrayList<>(KujakuMapView.this.droppedPoints);
            for (io.ona.kujaku.domain.Point point : droppedPoints) {
                dropPointOnMap(new LatLng(point.getLat(), point.getLng()));
            }
        }

        addPrimaryGeoJsonSourceAndLayerToStyle(style);

        if (getCameraPosition() != null) {
            mapboxMap.setCameraPosition(getCameraPosition());
        }

        // add bounds change listener
        addMapScrollListenerAndBoundsChangeEmitterToMap(mapboxMap);
        callBoundsChangedListeners();
        enableFeatureClickListenerEmitter(mapboxMap);

        WmtsHelper.addWmtsLayers(wmtsLayers, style);

        mapboxLocationComponentWrapper.init(KujakuMapView.this.mapboxMap, getContext(), locationRenderMode);
    }

    private void addPrimaryGeoJsonSourceAndLayerToStyle(@NonNull Style style) {
        if (getPrimaryGeoJsonSource() != null && style.getSource(getPrimaryGeoJsonSource().getId()) == null) {
            style.addSource(getPrimaryGeoJsonSource());
        }

        if (getPrimaryLayer() != null && style.getLayer(getPrimaryLayer().getId()) == null) {
            style.addLayer(getPrimaryLayer());
        }

        if (isFetchSourceFromStyle) {
            initializeSourceAndFeatureCollectionFromStyle(style);
            isFetchSourceFromStyle = false;
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
    @Override
    public void addWmtsLayer(@NonNull WmtsCapabilities capabilities) throws WmtsCapabilitiesException {
        this.addWmtsLayer(capabilities, null, null, null);
    }

    /**
     * Add identified layer to the wmtsLayer list
     *
     * @param layerIdentifier
     * @param capabilities
     */
    @Override
    public void addWmtsLayer(@NonNull WmtsCapabilities capabilities, @Nullable String layerIdentifier) throws WmtsCapabilitiesException {
        this.addWmtsLayer(capabilities, layerIdentifier, null, null);
    }

    /**
     * Add identified layer with specific style to the wmtsLayer list
     *
     * @param capabilities
     * @param layerIdentifier
     * @param styleIdentifier
     */
    @Override
    public void addWmtsLayer(@NonNull WmtsCapabilities capabilities, @Nullable String layerIdentifier, @Nullable String styleIdentifier) throws WmtsCapabilitiesException {
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
    @Override
    public void addWmtsLayer(@NonNull WmtsCapabilities capabilities, @Nullable String layerIdentifier, @Nullable String styleIdentifier, @Nullable String tileMatrixSetLinkIdentifier) throws WmtsCapabilitiesException {
        this.wmtsLayers.add(WmtsHelper.identifyLayer(capabilities, layerIdentifier, styleIdentifier, tileMatrixSetLinkIdentifier));

        if (mapboxMap != null && mapboxMap.getStyle() != null && mapboxMap.getStyle().isFullyLoaded()) {
            WmtsHelper.addWmtsLayers(this.wmtsLayers, mapboxMap.getStyle());
        }
    }

    private void addMapScrollListenerAndBoundsChangeEmitterToMap(@NonNull MapboxMap mapboxMap) {
        mapboxMap.addOnMoveListener(new MapboxMap.OnMoveListener() {
            @Override
            public void onMoveBegin(@NonNull MoveGestureDetector detector) {
                if (!disableMyLocationOnMapMove) {
                    // We should assume the user no longer wants us to focus on their location
                    focusOnUserLocation(false);
                }
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

        mapboxMap.removeOnMapLongClickListener(this);
        mapboxMap.addOnMapLongClickListener(this);
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

        // Unbind TrackingService if bound
        this.unBindTrackingService(getApplicationContext());
        AnnotationRepositoryManager.onStop();
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
        this.setOnFeatureClickListener(onFeatureClickListener, null, layerIds);
    }

    @Override
    public void setOnFeatureClickListener(@NonNull OnFeatureClickListener onFeatureClickListener, @Nullable Expression expressionFilter, @Nullable String... layerIds) {
        this.onFeatureClickListener = onFeatureClickListener;
        this.featureClickLayerIdFilters = layerIds;
        this.featureClickExpressionFilter = expressionFilter;
    }

    /**
     * Set listener when pressing a KujakuLayer
     *
     * @param onKujakuLayerClickListener
     */
    public void setOnKujakuLayerClickListener(@NonNull OnKujakuLayerClickListener onKujakuLayerClickListener) {
        this.onKujakuLayerClickListener = onKujakuLayerClickListener;
    }

    /**
     * Set listener when long pressing a KujakuLayer
     *
     * @param onKujakuLayerLongClickListener
     */
    public void setOnKujakuLayerLongClickListener(@NonNull OnKujakuLayerLongClickListener onKujakuLayerLongClickListener) {
        this.onKujakuLayerLongClickListener = onKujakuLayerLongClickListener;
    }

    @Override
    public void focusOnUserLocation(boolean focusOnMyLocation) {
        focusOnUserLocation(focusOnMyLocation, DEFAULT_LOCATION_OUTER_CIRCLE_RADIUS, RenderMode.NORMAL);
    }

    @Override
    public void focusOnUserLocation(boolean focusOnMyLocation, int renderMode) {
        focusOnUserLocation(focusOnMyLocation, DEFAULT_LOCATION_OUTER_CIRCLE_RADIUS, renderMode);
    }

    @Override
    public void focusOnUserLocation(boolean focusOnMyLocation, Float radius) {
        focusOnUserLocation(focusOnMyLocation, radius, RenderMode.NORMAL);
    }

    @Override
    public void focusOnUserLocation(boolean focusOnMyLocation, Float radius, int renderMode) {
        if (focusOnMyLocation) {
            changeImageButtonResource(currentLocationBtn, R.drawable.ic_cross_hair_blue);

            // Enable the listener & show the current user location
            updateUserLocationOnMap = true;
            updateCameraUserLocationOnMap = true;
            if (latestLocationCoordinates != null) {
                showUpdatedUserLocation(radius);
            }

        } else {
            updateCameraUserLocationOnMap = false;
            changeImageButtonResource(currentLocationBtn, R.drawable.ic_cross_hair);
        }

        locationRenderMode = renderMode;
    }

    @Override
    public void setBoundsChangeListener(@Nullable BoundsChangeListener boundsChangeListener) {
        this.boundsChangeListener = boundsChangeListener;

        callBoundsChangedListeners();
    }

    private void changeImageButtonResource(ImageButton imageButton, int resourceId) {
        imageButton.setImageResource(resourceId);
        this.resourceId = resourceId;
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
            mapboxMap.getStyle(new Style.OnStyleLoaded() {
                @Override
                public void onStyleLoaded(@NonNull Style style) {
                    ((GeoJsonSource) style.getSource(primaryGeoJsonSource.getId())).setGeoJson(KujakuMapView.this.featureCollection);
                }
            });
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
            ((GeoJsonSource) mapboxMap.getStyle().getSource(primaryGeoJsonSource.getId())).setGeoJson(this.featureCollection);
        }
    }

    public void initializePrimaryGeoJsonSource(String sourceId, boolean isFetchSourceFromStyle, String geoJsonSource) {
        if (sourceId == null || (isFetchSourceFromStyle && geoJsonSource == null)) {
            Timber.e(new Exception("GeoJson source initialization failed! Ensure that the source id is not null or that the GeoJson source is not null."));
            return;
        }
        featureCollection = FeatureCollection.fromFeatures(new ArrayList<>());

        if (isFetchSourceFromStyle) {
            this.isFetchSourceFromStyle = true;
            setPrimaryGeoJsonSourceId(sourceId);
            setGeoJsonSourceString(geoJsonSource);
        } else {
            primaryGeoJsonSource = new GeoJsonSource(sourceId, featureCollection);
        }
    }

    private void initializeSourceAndFeatureCollectionFromStyle(@NonNull Style style) {
        try {
            FeatureCollection featureCollection = FeatureCollection.fromJson(getGeoJsonSourceString());
            primaryGeoJsonSource = style.getSourceAs(getPrimaryGeoJsonSourceId());
            addFeaturePoints(featureCollection);
        } catch (Exception e) {
            Timber.e(e);
        }
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
            locationClient.close();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getMapboxMap();
        // This prevents an overlay issue the first time when requesting for permissions
        if (warmGps && Permissions.check(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) && !isResumingFromRequestingEnableLocation) {
            checkLocationSettingsAndStartLocationServices(true, null);
        } else if (warmGps && Permissions.check(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) && isResumingFromRequestingEnableLocation) {
            checkLocationSettingsAndStartLocationServices(true, onLocationServicesEnabledCallBack);
        }

        // Explain the consequence of rejecting enabling location
        if (isResumingFromRequestingEnableLocation) {
            isResumingFromRequestingEnableLocation = false;
            Activity activity = (Activity) getContext();
            Dialogs.showDialogIfLocationDisabled(activity, locationEnableRejectionDialogTitle, locationEnableRejectionDialogMessage);

            // The dialog message is supposed to be configurable only when the setWarmGps is called
            // and not a permanent change because we have other uses for warming GPS and in the widget already
            resetRejectionDialogContent();
        }

        this.resumeTrackingService(getApplicationContext());
    }

    private void checkLocationSettingsAndStartLocationServices(boolean shouldStartNow, OnLocationServicesEnabledCallBack onLocationServicesEnabledCallBack) {
        if (getContext() instanceof Activity) {
            Activity activity = (Activity) getContext();

            LocationSettingsHelper.checkLocationEnabled(activity, new ResultCallback<LocationSettingsResult>() {
                @Override
                public void onResult(@NonNull LocationSettingsResult result) {
                    final Status status = result.getStatus();

                    // The rejection dialog message is supposed to be configurable only when the setWarmGps is called
                    // and not a permanent change because we have other uses for warming GPS and in the widget already
                    if (status.getStatusCode() != LocationSettingsStatusCodes.RESOLUTION_REQUIRED) {
                        resetRejectionDialogContent();
                    }

                    switch (status.getStatusCode()) {
                        case LocationSettingsStatusCodes.SUCCESS:
                            Timber.i("All location settings are satisfied.");

                            // You can continue warming the GPS
                            if (shouldStartNow) {
                                warmUpLocationServices();
                            }
                            if (onLocationServicesEnabledCallBack != null) {
                                onLocationServicesEnabledCallBack.onSuccess();
                            }
                            break;
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            Timber.i("Location settings are not satisfied. Show the user a dialog to upgrade location settings");

                            // This enables us to back-off(in onResume) in case the user has already denied the request
                            // to turn on location settings
                            if (!hasAlreadyRequestedEnableLocation) {
                                hasAlreadyRequestedEnableLocation = true;
                                isResumingFromRequestingEnableLocation = true;

                                try {
                                    // Show the dialog by calling startResolutionForResult(), and check the result
                                    // in onActivityResult().
                                    status.startResolutionForResult(activity, Constants.RequestCode.LOCATION_SETTINGS);
                                } catch (IntentSender.SendIntentException e) {
                                    Timber.i("PendingIntent unable to execute request.");
                                }
                            } else {
                                // The user had already requested for permissions, so we should not request again
                                // We should disable these two modes since they cannot be achieved in the current stage
                                setWarmGps(false);
                                focusOnUserLocation(false);
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            Timber.e(new Exception("Location settings are inadequate, and cannot be fixed here. Dialog cannot be created."));
                            break;

                        default:
                            Timber.e(new Exception("Unknown status code returned after checking location settings"));
                            break;
                    }
                }
            });
        } else {
            LogUtil.e(TAG, "KujakuMapView is not started in an Activity and can therefore not start location services");
        }
    }

    @Override
    public boolean onMapClick(@NonNull LatLng point) {
        PointF pixel = mapboxMap.getProjection().toScreenLocation(point);

        if (onFeatureClickListener != null) {
            List<com.mapbox.geojson.Feature> features = mapboxMap.queryRenderedFeatures(pixel, featureClickExpressionFilter, featureClickLayerIdFilters);

            if (features.size() > 0) {
                onFeatureClickListener.onFeatureClick(features);
            }
        }

        if (onKujakuLayerClickListener != null) {
            KujakuLayer layer = KujakuLayer.getKujakuLayerSelected(pixel, kujakuLayers, mapboxMap);
            if (layer != null) {
                onKujakuLayerClickListener.onKujakuLayerClick(layer);
            }
        }

        return false;
    }

    @Override
    public boolean onMapLongClick(@NonNull LatLng point) {
        PointF pixel = mapboxMap.getProjection().toScreenLocation(point);

        if (onKujakuLayerLongClickListener != null) {
            KujakuLayer layer = KujakuLayer.getKujakuLayerSelected(pixel, kujakuLayers, mapboxMap);
            if (layer != null) {
                onKujakuLayerLongClickListener.onKujakuLayerLongClick(layer);
            }
        }
        return false;
    }

    public boolean isWarmGps() {
        return warmGps;
    }

    public void setWarmGps(boolean warmGps) {
        setWarmGps(warmGps, null, null);
    }

    public void setWarmGps(boolean warmGps, @Nullable String rejectionDialogTitle, @Nullable String rejectionDialogMessage) {
        setWarmGps(warmGps, rejectionDialogTitle, rejectionDialogMessage, null);
    }

    public void setWarmGps(boolean warmGps, @Nullable String rejectionDialogTitle, @Nullable String rejectionDialogMessage, OnLocationServicesEnabledCallBack onLocationServicesEnabledCallBack) {
        // If it was not warming(started) the location services, do that now
        boolean shouldStartNow = !this.warmGps && warmGps;
        this.warmGps = warmGps;

        locationEnableRejectionDialogTitle = rejectionDialogTitle;
        locationEnableRejectionDialogMessage = rejectionDialogMessage;
        this.onLocationServicesEnabledCallBack = onLocationServicesEnabledCallBack;
        if (warmGps) {
            // Don't back-off from request location enable since this was an explicit call to enable location
            hasAlreadyRequestedEnableLocation = false;
            // In case the location settings were turned off while the warmGps is still true, it means that the LocationClient is also on
            // We should just re-enable the location so that the LocationClient can reconnect to the location services
            checkLocationSettingsAndStartLocationServices(shouldStartNow, onLocationServicesEnabledCallBack);
        }
    }

    @Nullable
    @Override
    public ILocationClient getLocationClient() {
        return locationClient;
    }

    public void setLocationBufferRadius(float locationBufferRadius) {
        this.locationBufferRadius = locationBufferRadius;
    }

    @Override
    public void getLocationClient(@Nullable LocationClientStartedCallback locationClientStartedCallback) {
        if (getLocationClient() != null) {
            locationClientStartedCallback.onStarted(getLocationClient());
        } else {
            if (!locationClientCallbacks.contains(locationClientStartedCallback)) {
                locationClientCallbacks.add(locationClientStartedCallback);
            }
        }
    }

    @Override
    public void addLayer(@NonNull KujakuLayer kujakuLayer) {
        kujakuLayer.setRemoved(false);
        if (!kujakuLayers.contains(kujakuLayer)) {
            kujakuLayers.add(kujakuLayer);
            getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(@NonNull MapboxMap mapboxMap) {
                    mapboxMap.getStyle(new Style.OnStyleLoaded() {
                        @Override
                        public void onStyleLoaded(@NonNull Style style) {
                            if (!kujakuLayer.isRemoved()) {
                                kujakuLayer.addLayerToMap(mapboxMap);
                            }
                        }
                    });
                }
            });
        } else {
            getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(@NonNull MapboxMap mapboxMap) {
                    mapboxMap.getStyle(new Style.OnStyleLoaded() {
                        @Override
                        public void onStyleLoaded(@NonNull Style style) {
                            if (!kujakuLayer.isRemoved()) {
                                kujakuLayer.enableLayerOnMap(mapboxMap);
                            }
                        }
                    });
                }
            });
        }
    }

    @Override
    public void disableLayer(@NonNull KujakuLayer kujakuLayer) {
        if (kujakuLayers.contains(kujakuLayer)) {
            getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(@NonNull MapboxMap mapboxMap) {
                    mapboxMap.getStyle(new Style.OnStyleLoaded() {
                        @Override
                        public void onStyleLoaded(@NonNull Style style) {
                            kujakuLayer.disableLayerOnMap(mapboxMap);
                        }
                    });
                }
            });
        }
    }

    @Override
    public boolean changeLocationUpdates(long updateInterval, long fastestUpdateInterval, int accuracyLevel) {
        //TODO: Update this to work for cases where the AndroidGpsLocationClient is in use and not the GoogleLocationClient
        ILocationClient locationClient = getLocationClient();
        if (updateInterval > -1 && fastestUpdateInterval > -1 && (accuracyLevel == LocationRequest.PRIORITY_HIGH_ACCURACY
                || accuracyLevel == LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
                || accuracyLevel == LocationRequest.PRIORITY_LOW_POWER
                || accuracyLevel == LocationRequest.PRIORITY_NO_POWER)
                && locationClient != null) {
            LocationRequest locationRequest = new LocationRequest();
            locationRequest.setInterval(updateInterval);
            locationRequest.setFastestInterval(fastestUpdateInterval);
            locationRequest.setPriority(accuracyLevel);

            for (LocationListener locationListener : locationClient.getLocationListeners()) {
                ((GoogleLocationClient)locationClient)
                        .requestLocationUpdates(locationListener, locationRequest);
            }
            return locationClient.getLocationListeners().size() > 0;
        }

        return false;
    }

    @Override
    public boolean isKujakuLayerAdded(@NonNull KujakuLayer kujakuLayer) {
        String[] layerIds = kujakuLayer.getLayerIds();
        if (mapboxMap != null && mapboxMap.getStyle() != null && mapboxMap.getStyle().isFullyLoaded()) {
            for (String layerId : layerIds) {
                if (mapboxMap.getStyle().getLayer(layerId) == null) {
                    return false;
                }
            }

            return true;
        }

        return false;
    }

    @Override
    public void removeLayer(@NonNull KujakuLayer kujakuLayer) {
        if (isKujakuLayerAdded(kujakuLayer)) {
            kujakuLayer.removeLayerOnMap(mapboxMap);
        } else {
            kujakuLayer.setRemoved(true);
        }

        kujakuLayers.remove(kujakuLayer);
    }

    private void resetRejectionDialogContent() {
        locationEnableRejectionDialogTitle = null;
        locationEnableRejectionDialogMessage = null;
    }

    public MapboxLocationComponentWrapper getMapboxLocationComponentWrapper() {
        return mapboxLocationComponentWrapper;
    }


    /************** Tracking Service ***************/

    /**
     * Init TrackingService
     *
     * @param trackingServiceListener
     * @param uiConfiguration
     * @param options
     */
    public void initTrackingService(@NonNull TrackingServiceListener trackingServiceListener,
                                    TrackingServiceUIConfiguration uiConfiguration,
                                    TrackingServiceOptions options) {
        this.trackingServiceListener = trackingServiceListener;
        this.trackingServiceUIConfiguration = uiConfiguration != null ? uiConfiguration : new TrackingServiceDefaultUIConfiguration();
        this.trackingServiceOptions = options != null ? options : new TrackingServiceHighAccuracyOptions();
        this.trackingServiceInitialized = true;
    }


    /**
     * Rebind to a running TrackingService instance
     *
     * @param context
     * @return
     */
    public boolean resumeTrackingService(Context context) {
        // TrackingService reconnection if connection was lost
        if (!trackingServiceBound && TrackingService.isRunning() && trackingServiceInitialized) {
            initTrackingServiceIcon();
            return TrackingService.bindService(context, TrackingService.getIntent(context, null, null), connection);
        } else {
            return false;
        }
    }

    /**
     * Start TrackingService
     *
     * @param context
     * @param cls
     */
    public void startTrackingService(@NonNull Context context,
                                     @NonNull Class<?> cls) throws TrackingServiceNotInitializedException {
        if (!this.trackingServiceInitialized) {
            throw new TrackingServiceNotInitializedException();
        }

        TrackingService.startAndBindService(context,
                cls,
                connection,
                this.trackingServiceOptions);

        initTrackingServiceIcon();
    }

    /**
     * Stop TrackingService
     *
     * @param context
     * @return
     */
    public List<KujakuLocation> stopTrackingService(@NonNull Context context) {
        if (trackingServiceBound && trackingService != null) {
            List<KujakuLocation> locations = trackingService.getRecordedKujakuLocations();
            trackingService.unregisterTrackingServiceListener();
            TrackingService.stopAndUnbindService(context, connection);
            trackingServiceBound = false;
            trackingService = null;
            trackingServiceStatusButton.setImageResource(trackingServiceUIConfiguration.getStoppedDrawable());
            return locations;
        } else {
            Timber.d("Tracking Service instance is null or not Tracking Service is not bounded");
        }

        return null;
    }

    /**
     * Unbind from TrackingService instance
     *
     * @param context
     */
    private void unBindTrackingService(@NonNull Context context) {
        if (trackingServiceBound && trackingService != null) {
            trackingService.unregisterTrackingServiceListener();
            TrackingService.unBindService(context, connection);
            trackingServiceBound = false;
            trackingService = null;
        } else {
            Timber.d("Tracking Service instance is null");
        }
    }

    /**
     * Set Tag
     *
     * @param tag
     * @return {@code true} if tag is set, {@code false} otherwise
     */
    public boolean setTag(long tag) {
        if (trackingServiceBound && trackingService != null) {
            trackingService.setTag(tag);
            return true;
        }

        return false;
    }

    /**
     * Take a location
     */
    public void trackingServiceTakeLocation(long tag) {
        if (trackingServiceBound && trackingService != null) {
            trackingService.takeLocation(tag);
        } else {
            Timber.e(new Exception(), "Tracking Service instance is null");
        }
    }

    /**
     * Take a location
     */
    public void trackingServiceTakeLocation() {
        this.trackingServiceTakeLocation(TrackingService.NO_FORCED_TAG);
    }

    /**
     * Get the recorded locations
     *
     * @return
     */
    public List<KujakuLocation> getTrackingServiceRecordedKujakuLocations() {
        if (trackingServiceBound && trackingService != null) {
            return trackingService.getRecordedKujakuLocations();
        }

        return new ArrayList<KujakuLocation>();
    }

    /**
     * Connection to bind to the TrackingService instance
     */
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to TrackingService, cast the IBinder and get TrackingService instance
            TrackingService.LocalBinder binder = (TrackingService.LocalBinder) service;
            trackingService = binder.getService();
            trackingService.registerTrackingServiceListener(trackingServiceListener);
            trackingServiceBound = true;

            trackingServiceStatusButton.setImageResource(trackingServiceUIConfiguration.getRecordingDrawable());

            ((Activity) getContext()).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    trackingServiceListener.onServiceConnected(trackingService);
                }
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            if (trackingService != null) {
                trackingService.unregisterTrackingServiceListener();
            }
            trackingServiceBound = false;
            trackingService = null;
            trackingServiceStatusButton.setImageResource(trackingServiceUIConfiguration.getStoppedDrawable());

            ((Activity) getContext()).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    trackingServiceListener.onServiceDisconnected();
                }
            });
        }
    };

    /**
     * Init TrackingService icon
     */
    private void initTrackingServiceIcon() {
        if (this.trackingServiceInitialized && this.trackingServiceUIConfiguration != null) {
            LayoutParams layoutParams = new LayoutParams((int) getResources().getDimension(trackingServiceUIConfiguration.getLayoutWidth())
                    , (int) getResources().getDimension(trackingServiceUIConfiguration.getLayoutHeight()));
            layoutParams.gravity = trackingServiceUIConfiguration.getLayoutGravity();

            layoutParams.setMargins((int) (getResources().getDimension(trackingServiceUIConfiguration.getLayoutMarginLeft())),
                    (int) (getResources().getDimension(trackingServiceUIConfiguration.getLayoutMarginTop())),
                    (int) (getResources().getDimension(trackingServiceUIConfiguration.getLayoutMarginRight())),
                    (int) (getResources().getDimension(trackingServiceUIConfiguration.getLayoutMarginBottom())));

            trackingServiceStatusButton.setLayoutParams(layoutParams);

            trackingServiceStatusButton.setPadding((int) getResources().getDimension(trackingServiceUIConfiguration.getPadding()),
                    (int) getResources().getDimension(trackingServiceUIConfiguration.getPadding()),
                    (int) getResources().getDimension(trackingServiceUIConfiguration.getPadding()),
                    (int) getResources().getDimension(trackingServiceUIConfiguration.getPadding()));

            trackingServiceStatusButton.setBackgroundResource(trackingServiceUIConfiguration.getBackgroundDrawable());
            trackingServiceStatusButton.setImageResource(trackingServiceUIConfiguration.getStoppedDrawable());
            trackingServiceStatusButton.setVisibility(trackingServiceUIConfiguration.displayIcons() ? VISIBLE : GONE);
        }
    }

    /************** End of Tracking Service ***************/

    @Override
    public void setDisableMyLocationOnMapMove(boolean disableMyLocationOnMapMove) {
        this.disableMyLocationOnMapMove = disableMyLocationOnMapMove;
    }

    /**
     * MBtiles support
     **/

    public MBTilesHelper getMbTilesHelper() {
        return mbTilesHelper;
    }

    @Override
    public void onDestroy() {
        if (mbTilesHelper != null) {
            mbTilesHelper.onDestroy();
        }
        super.onDestroy();
    }
}