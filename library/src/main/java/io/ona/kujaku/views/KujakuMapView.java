package io.ona.kujaku.views;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.IntentSender;
import android.content.res.TypedArray;
import android.graphics.PointF;
import android.location.Location;
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
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
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
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.expressions.Expression;
import com.mapbox.mapboxsdk.style.layers.Layer;
import com.mapbox.mapboxsdk.style.layers.RasterLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.mapboxsdk.style.sources.RasterSource;
import com.mapbox.mapboxsdk.style.sources.TileSet;

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
import io.ona.kujaku.exceptions.WmtsCapabilitiesException;
import io.ona.kujaku.helpers.MapboxLocationComponentWrapper;
import io.ona.kujaku.interfaces.IKujakuMapView;
import io.ona.kujaku.interfaces.ILocationClient;
import io.ona.kujaku.layers.KujakuLayer;
import io.ona.kujaku.listeners.BaseLocationListener;
import io.ona.kujaku.listeners.BoundsChangeListener;
import io.ona.kujaku.listeners.LocationClientStartedCallback;
import io.ona.kujaku.listeners.OnFeatureClickListener;
import io.ona.kujaku.listeners.OnLocationChanged;
import io.ona.kujaku.location.clients.GoogleLocationClient;
import io.ona.kujaku.utils.Constants;
import io.ona.kujaku.utils.LocationPermissionListener;
import io.ona.kujaku.utils.LocationSettingsHelper;
import io.ona.kujaku.utils.LogUtil;
import io.ona.kujaku.utils.Permissions;
import io.ona.kujaku.wmts.model.WmtsCapabilities;
import io.ona.kujaku.wmts.model.WmtsLayer;

/**
 *
 * Created by Ephraim Kigamba - ekigamba@ona.io on 26/09/2018
 *
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

    private ILocationClient locationClient;
    private Toast currentlyShownToast;

    private LinearLayout addPointButtonsLayout;

    private OnLocationChanged onLocationChangedListener;

    private boolean isMapScrolled = false;

    private static final int ANIMATE_TO_LOCATION_DURATION = 1000;

    protected Set<io.ona.kujaku.domain.Point> droppedPoints;

    private LatLng latestLocationCoordinates;

    private Location latestLocation;

    private MapboxLocationComponentWrapper mapboxLocationComponentWrapper;

    private boolean updateUserLocationOnMap = false;

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

    private boolean warmGps = true;
    private boolean hasAlreadyRequestedEnableLocation = false;
    private boolean isResumingFromRequestingEnableLocation = false;

    private String locationEnableRejectionDialogTitle;
    private String locationEnableRejectionDialogMessage;
    private OnLocationServicesEnabledCallBack onLocationServicesEnabledCallBack;

    private ArrayList<KujakuLayer> kujakuLayers = new ArrayList<>();
    private ArrayList<LocationClientStartedCallback> locationClientCallbacks = new ArrayList<>();

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
                    // Enable asking for enabling the location by resetting this flag in case it was true
                    hasAlreadyRequestedEnableLocation = false;
                    setWarmGps(true, null, null, new OnLocationServicesEnabledCallBack() {
                        @Override
                        public void onSuccess() {
                            focusOnUserLocation(true, locationBufferRadius);
                        }
                    });
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
        String locationBtnVisibilityKey = getContext().getString(R.string.current_location_btn_visibility);
        if (attributes.containsKey(locationBtnVisibilityKey)) {
            boolean isCurrentLocationBtnVisible = (boolean) attributes.get(locationBtnVisibilityKey);
            setVisibility(currentLocationBtn, isCurrentLocationBtnVisible);
        }

        String warmGPSKey = getContext().getString(R.string.mapbox_warmGps);
        if (attributes.containsKey(warmGPSKey)) {
            warmGps = (boolean) attributes.get(warmGPSKey);
        }

        featureMap = new HashMap<>();
        mapboxLocationComponentWrapper = new MapboxLocationComponentWrapper();
    }

    private void showUpdatedUserLocation(Float radius) {
        updateUserLocation(radius);
        if (updateUserLocationOnMap || !isMapScrolled) {
            // Focus on the new location
            centerMap(latestLocationCoordinates, ANIMATE_TO_LOCATION_DURATION, getZoomToUse(mapboxMap, LOCATION_FOCUS_ZOOM));
        }
    }

    private void warmUpLocationServices() {
        locationClient = new GoogleLocationClient(getContext());
        locationClient.requestLocationUpdates(new BaseLocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if (location != null) {
                    latestLocation = location;
                    latestLocationCoordinates = new LatLng(location.getLatitude()
                            , location.getLongitude());
                }

                if (onLocationChangedListener != null) {
                    onLocationChangedListener.onLocationChanged(location);
                }

                if (updateUserLocationOnMap) {
                    showUpdatedUserLocation(locationBufferRadius);
                }
            }
        });

        for (LocationClientStartedCallback locationClientStartedCallback: locationClientCallbacks) {
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
            getMapboxLocationComponentWrapper().getLocationComponent().forceLocationUpdate(latestLocation);
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

        addWmtsLayers();

        mapboxLocationComponentWrapper.init(KujakuMapView.this.mapboxMap, getContext());
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

    /**
     * Add all Wmts Layers in wmtsLayers on the map
     */
    private void addWmtsLayers() {
        // Add WmtsLayers
        if (wmtsLayers != null) {
            for (WmtsLayer layer : wmtsLayers) {
                if (mapboxMap.getStyle().getSource(layer.getIdentifier()) == null) {

                    TileSet tileSet = new TileSet("tileset", layer.getTemplateUrl("tile"));
                    tileSet.setMaxZoom(layer.getMaximumZoom());
                    tileSet.setMinZoom(layer.getMinimumZoom());

                    RasterSource webMapSource = new RasterSource(
                            layer.getIdentifier(),
                            tileSet, layer.getTilesSize());
                    mapboxMap.getStyle().addSource(webMapSource);

                    RasterLayer webMapLayer = new RasterLayer(layer.getIdentifier(), layer.getIdentifier());
                    mapboxMap.getStyle().addLayer(webMapLayer);
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
    public void addWmtsLayer(WmtsCapabilities capabilities) throws WmtsCapabilitiesException {
       this.addWmtsLayer(capabilities, null, null, null);
    }

    /**
     * Add identified layer to the wmtsLayer list
     *
     * @param layerIdentifier
     * @param capabilities
     */
    public void addWmtsLayer(WmtsCapabilities capabilities, String layerIdentifier) throws WmtsCapabilitiesException {
        this.addWmtsLayer(capabilities, layerIdentifier, null, null);
    }

    /**
     * Add identified layer with specific style to the wmtsLayer list
     *
     * @param capabilities
     * @param layerIdentifier
     * @param styleIdentifier
     */
    public void addWmtsLayer(WmtsCapabilities capabilities, String layerIdentifier, String styleIdentifier) throws WmtsCapabilitiesException {
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
    public void addWmtsLayer(WmtsCapabilities capabilities, String layerIdentifier, String styleIdentifier, String tileMatrixSetLinkIdentifier) throws WmtsCapabilitiesException {
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
        this.setZooms(layerIdentified, capabilities);
        this.setTilesSize(layerIdentified, capabilities);

        this.wmtsLayers.add(layerIdentified);

        if (mapboxMap != null && mapboxMap.getStyle() != null && mapboxMap.getStyle().isFullyLoaded()) {
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
    private void selectWmtsStyle (WmtsLayer layer, String styleIdentifier) throws WmtsCapabilitiesException {
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
    private void selectWmtsTileMatrix (WmtsLayer layer, String tileMatrixSetLinkIdentifier) throws WmtsCapabilitiesException {
        if (tileMatrixSetLinkIdentifier != null && !tileMatrixSetLinkIdentifier.isEmpty()) {
            // Check if style is known
            if (layer.getTileMatrixSetLink(tileMatrixSetLinkIdentifier) == null) {
                throw new WmtsCapabilitiesException(String.format("tileMatrixSetLink with identifier %1$s is not available for Layer %2$s", tileMatrixSetLinkIdentifier, layer.getIdentifier()));
            } else {
                layer.setSelectedTileMatrixLinkIdentifier(tileMatrixSetLinkIdentifier);
            }
        }
    }

    /**
     * Set the Maximum and Minimum Zoom for this layer
     *
     * @param layer
     * @param capabilities
     */
    private void setZooms(WmtsLayer layer, WmtsCapabilities capabilities){
        String tileMatrixSetIdentifier = layer.getSelectedTileMatrixLinkIdentifier();

        int maxZoom = capabilities.getMaximumTileMatrixZoom(tileMatrixSetIdentifier);
        int minZoom = capabilities.getMinimumTileMatrixZoom(tileMatrixSetIdentifier);

        layer.setMaximumZoom(maxZoom);
        layer.setMinimumZoom(minZoom);
    }

    /**
     * Set the tiles Size for this layer
     *
     * @param layer
     * @param capabilities
     */
    private void setTilesSize(WmtsLayer layer, WmtsCapabilities capabilities) {
        String tileMatrixSetIdentifier = layer.getSelectedTileMatrixLinkIdentifier();
        int tileSize = capabilities.getTilesSize(tileMatrixSetIdentifier);
        layer.setTilesSize(tileSize);
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
        focusOnUserLocation(focusOnMyLocation, DEFAULT_LOCATION_OUTER_CIRCLE_RADIUS);
    }

    @Override
    public void focusOnUserLocation(boolean focusOnMyLocation, Float radius) {
        if (focusOnMyLocation) {
            isMapScrolled = false;
            changeImageButtonResource(currentLocationBtn, R.drawable.ic_cross_hair_blue);

            // Enable the listener & show the current user location
            updateUserLocationOnMap = true;
            if (latestLocationCoordinates != null) {
                showUpdatedUserLocation(radius);
            }

        } else {
            updateUserLocationOnMap = false;
            changeImageButtonResource(currentLocationBtn, R.drawable.ic_cross_hair);
        }
    }

    @Override
    public void setBoundsChangeListener(@Nullable BoundsChangeListener boundsChangeListener) {
        this.boundsChangeListener = boundsChangeListener;

        callBoundsChangedListeners();
    }

    private void changeImageButtonResource(ImageButton imageButton, int resourceId) {
        imageButton.setImageResource(resourceId);
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

    private void initializeSourceAndFeatureCollectionFromStyle(@NonNull Style style) {
        try {
            FeatureCollection featureCollection = FeatureCollection.fromJson(getGeoJsonSourceString());
            primaryGeoJsonSource = style.getSourceAs(getPrimaryGeoJsonSourceId());
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
    }

    private void checkLocationSettingsAndStartLocationServices(boolean shouldStartNow, OnLocationServicesEnabledCallBack onLocationServicesEnabledCallBack) {
        if (getContext() instanceof Activity) {
            Activity activity = (Activity) getContext();

            LocationSettingsHelper.checkLocationEnabled(activity, new ResultCallback<LocationSettingsResult>() {
                @Override
                public void onResult(LocationSettingsResult result) {
                    final Status status = result.getStatus();

                    // The rejection dialog message is supposed to be configurable only when the setWarmGps is called
                    // and not a permanent change because we have other uses for warming GPS and in the widget already
                    if (status.getStatusCode() != LocationSettingsStatusCodes.RESOLUTION_REQUIRED) {
                        resetRejectionDialogContent();
                    }

                    switch (status.getStatusCode()) {
                        case LocationSettingsStatusCodes.SUCCESS:
                            Log.i(TAG, "All location settings are satisfied.");

                            // You can continue warming the GPS
                            if (shouldStartNow) {
                                warmUpLocationServices();
                            }
                            if (onLocationServicesEnabledCallBack != null) {
                                onLocationServicesEnabledCallBack.onSuccess();
                            }
                            break;
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            Log.i(TAG, "Location settings are not satisfied. Show the user a dialog to upgrade location settings");

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
                                    Log.i(TAG, "PendingIntent unable to execute request.");
                                }
                            } else {
                                // The user had already requested for permissions, so we should not request again
                                // We should disable these two modes since they cannot be achieved in the current stage
                                setWarmGps(false);
                                focusOnUserLocation(false);
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            Log.e(TAG, "Location settings are inadequate, and cannot be fixed here. Dialog cannot be created.");
                            break;

                        default:
                            Log.e(TAG, "Unknown status code returned after checking location settings");
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
        if (onFeatureClickListener != null) {
            PointF pixel = mapboxMap.getProjection().toScreenLocation(point);
            List<com.mapbox.geojson.Feature> features = mapboxMap.queryRenderedFeatures(pixel, featureClickExpressionFilter, featureClickLayerIdFilters);

            if (features.size() > 0) {
                onFeatureClickListener.onFeatureClick(features);
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
                public void onMapReady(MapboxMap mapboxMap) {
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
                public void onMapReady(MapboxMap mapboxMap) {
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
                public void onMapReady(MapboxMap mapboxMap) {
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
        if (updateInterval > -1 && fastestUpdateInterval > -1 && (accuracyLevel == LocationRequest.PRIORITY_HIGH_ACCURACY
                || accuracyLevel == LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
                || accuracyLevel == LocationRequest.PRIORITY_LOW_POWER
                || accuracyLevel == LocationRequest.PRIORITY_NO_POWER)
                && getLocationClient() != null) {
            LocationRequest locationRequest = new LocationRequest();
            locationRequest.setInterval(updateInterval);
            locationRequest.setFastestInterval(fastestUpdateInterval);
            locationRequest.setPriority(accuracyLevel);

            ((GoogleLocationClient) getLocationClient())
                    .requestLocationUpdates(getLocationClient().getLocationListener(), locationRequest);
            return true;
        }

        return false;
    }

    @Override
    public boolean isKujakuLayerAdded(@NonNull KujakuLayer kujakuLayer) {
        String[] layerIds = kujakuLayer.getLayerIds();
        if (mapboxMap != null && mapboxMap.getStyle() != null && mapboxMap.getStyle().isFullyLoaded()) {
            for (String layerId: layerIds) {
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
}

