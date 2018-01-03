package io.ona.kujaku.activities;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.PointF;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;

import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.services.commons.geojson.Feature;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import io.ona.kujaku.R;
import io.ona.kujaku.adapters.InfoWindowAdapter;
import io.ona.kujaku.adapters.InfoWindowObject;
import io.ona.kujaku.adapters.holders.InfoWindowViewHolder;
import io.ona.kujaku.helpers.MapBoxStyleStorage;
import io.ona.kujaku.sorting.Sorter;
import io.ona.kujaku.utils.Permissions;
import io.ona.kujaku.views.InfoWindowLayoutManager;

import utils.Constants;
import utils.config.DataSourceConfig;
import utils.config.KujakuConfig;
import utils.config.SortFieldConfig;
import utils.exceptions.InvalidMapBoxStyleException;
import utils.helpers.MapBoxStyleHelper;
import utils.helpers.converters.GeoJSONFeature;

/**
 * This activity displays a MapView once provided with a a MapBox Access Key & String array.
 * These are passed as:
 * {@link Constants#PARCELABLE_KEY_MAPBOX_ACCESS_TOKEN} - MapBox Access Token ({@code String})
 * {@link Constants#PARCELABLE_KEY_MAPBOX_STYLES} - MapBox Styles ({@code String[]}
 * <p>
 * <p>
 * <p>
 * - MapBox Styles - String array containing <a href="https://www.mapbox.com/mapbox-gl-js/style-spec/">valid MapBox Style</a> at index 0
 * <p>
 * <p>
 * Created by Ephraim Kigamba - ekigamba@ona.io
 */
public class MapActivity extends AppCompatActivity implements MapboxMap.OnMapClickListener {
    private static final int PERMISSIONS_REQUEST_CODE = 342;
    private MapView mapView;
    private String currentStylePath;

    private SortFieldConfig[] sortFields;
    private String[] dataLayers;
    private JSONObject mapboxStyleJSON;
    private static final String TAG = MapActivity.class.getSimpleName();

    private LinkedHashMap<String, InfoWindowObject> featuresMap = new LinkedHashMap<>();
    private ArrayList featureIdList = new ArrayList();
    private MapboxMap mapboxMap;
    private boolean infoWindowDisplayed = false;

    // Info window stuff
    private RecyclerView infoWindowsRecyclerView;
    private InfoWindowLayoutManager linearLayoutManager;
    private HashMap<Integer, AlertDialog> alertDialogs;
    private int lastSelected = 0;

    private int animateToNewTargetDuration = 1000;
    private int animateToNewInfoWindowDuration = 300;
    private int screenWidth = 0;

    private InfoWindowAdapter infoWindowAdapter;

    private LatLng topLeftBound;
    private LatLng bottomRightBound;
    private LatLng cameraTargetLatLng;
    private double cameraZoom = -1;
    private double cameraTilt = -1;
    private double cameraBearing = -1;
    private double maxZoom = -1;
    private double minZoom = -1;

    //Todo: Move reading data to another Thread

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        initializeViews();

        checkPermissions();
        screenWidth = getScreenWidth(this);

        if (getIntent() != null) {
            Bundle bundle = getIntent().getExtras();
            if (bundle != null) {
                String mapBoxAccessToken = bundle.getString(Constants.PARCELABLE_KEY_MAPBOX_ACCESS_TOKEN);
                String[] stylesArray = bundle.getStringArray(Constants.PARCELABLE_KEY_MAPBOX_STYLES);
                currentStylePath = "";

                if (bundle.containsKey(Constants.PARCELABLE_KEY_TOP_LEFT_BOUND)) {
                    topLeftBound = bundle.getParcelable(Constants.PARCELABLE_KEY_TOP_LEFT_BOUND);

                    if (bundle.containsKey(Constants.PARCELABLE_KEY_BOTTOM_RIGHT_BOUND)) {
                        bottomRightBound = bundle.getParcelable(Constants.PARCELABLE_KEY_BOTTOM_RIGHT_BOUND);
                    }
                }

                if (bundle.containsKey(Constants.PARCELABLE_KEY_MAX_ZOOM)) {
                    maxZoom = bundle.getDouble(Constants.PARCELABLE_KEY_MAX_ZOOM);
                }

                if (bundle.containsKey(Constants.PARCELABLE_KEY_MIN_ZOOM)) {
                    minZoom = bundle.getDouble(Constants.PARCELABLE_KEY_MIN_ZOOM);
                }

                if (bundle.containsKey(Constants.PARCELABLE_KEY_CAMERA_TARGET_LATLNG)) {
                    cameraTargetLatLng = bundle.getParcelable(Constants.PARCELABLE_KEY_CAMERA_TARGET_LATLNG);
                }

                if (bundle.containsKey(Constants.PARCELABLE_KEY_CAMERA_ZOOM)) {
                    cameraZoom = bundle.getDouble(Constants.PARCELABLE_KEY_CAMERA_ZOOM);
                }

                if (bundle.containsKey(Constants.PARCELABLE_KEY_CAMERA_TILT)) {
                    cameraTilt = bundle.getDouble(Constants.PARCELABLE_KEY_CAMERA_TILT);
                }

                if (bundle.containsKey(Constants.PARCELABLE_KEY_CAMERA_BEARING)) {
                    cameraBearing = bundle.getDouble(Constants.PARCELABLE_KEY_CAMERA_BEARING);
                }

                if (stylesArray != null) {
                    currentStylePath = stylesArray[0];
                    if (currentStylePath != null && !currentStylePath.isEmpty()) {
                        currentStylePath = new MapBoxStyleStorage()
                                .getStyleURL(currentStylePath);
                        mapboxStyleJSON = getStyleJSON(currentStylePath);

                        // Extract kujaku meta-data
                        try {
                            MapBoxStyleHelper styleHelper = new MapBoxStyleHelper(mapboxStyleJSON);
                            if (!styleHelper.getKujakuConfig().isValid()) {
                                showIncompleteStyleError();
                            }
                            sortFields = SortFieldConfig.extractSortFieldConfigs(styleHelper);;
                            dataLayers = DataSourceConfig.extractDataSourceNames(styleHelper.getKujakuConfig().getDataSourceConfigs());
                            featuresMap = extractLayerData(mapboxStyleJSON, dataLayers);
                            featuresMap = sortData(featuresMap, sortFields);
                            displayInitialFeatures(featuresMap, styleHelper.getKujakuConfig());
                        } catch (JSONException e) {
                            Log.e(TAG, Log.getStackTraceString(e));
                            showIncompleteStyleError();
                        } catch (InvalidMapBoxStyleException e) {
                            Log.e(TAG, Log.getStackTraceString(e));
                            showIncompleteStyleError();
                        }
                    }
                }

                initMapBoxSdk(savedInstanceState, mapBoxAccessToken, currentStylePath, topLeftBound, bottomRightBound,
                        cameraTargetLatLng, cameraZoom, cameraTilt, cameraBearing, maxZoom, minZoom);
            }
        }
    }

    private void showIncompleteStyleError() {
        showAlertDialog(
                R.string.kujaku_config,
                R.string.error_kujaku_config,
                R.string.ok,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }, -1, null);
    }

    private void initMapBoxSdk(Bundle savedInstanceState, String mapboxAccessToken, String mapBoxStylePath,
                               @Nullable final LatLng topLeftBound, @Nullable final LatLng bottomRightBound,
                               @Nullable final LatLng cameraTargetLatLng, final double cameraZoom, final double cameraTilt,
                               final double cameraBearing, final double maxZoom, final double minZoom) {
        Mapbox.getInstance(this, mapboxAccessToken);
        mapView = (MapView) findViewById(R.id.map_view);
        mapView.onCreate(savedInstanceState);

        if (!mapBoxStylePath.isEmpty()) {
            mapView.setStyleUrl(mapBoxStylePath);
        }

        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap mapboxMap) {
                //Set listener for markers
                MapActivity.this.mapboxMap = mapboxMap;
                mapboxMap.setOnMapClickListener(MapActivity.this);

                if (topLeftBound != null && bottomRightBound != null) {
                    LatLngBounds latLngBounds = new LatLngBounds.Builder()
                            .include(topLeftBound)
                            .include(bottomRightBound)
                            .build();

                    mapboxMap.easeCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 50), 50);
                }

                if (minZoom != -1) {
                    mapboxMap.setMinZoomPreference(minZoom);
                }

                if (maxZoom != -1) {
                    mapboxMap.setMaxZoomPreference(maxZoom);
                }

                CameraPosition.Builder cameraPositionBuilder = new CameraPosition.Builder();
                boolean cameraPositionChanged = false;

                if (cameraTargetLatLng != null) {
                    cameraPositionBuilder.target(cameraTargetLatLng);
                    mapboxMap.setLatLng(cameraTargetLatLng);
                    cameraPositionChanged = true;
                }

                if (cameraZoom != -1) {
                    cameraPositionBuilder.zoom(cameraZoom);
                    cameraPositionChanged = true;
                }

                if (cameraTilt != -1) {
                    cameraPositionBuilder.tilt(cameraTilt);
                    cameraPositionChanged = true;
                }

                if (cameraBearing != -1) {
                    cameraPositionBuilder.bearing(cameraBearing);
                    cameraPositionChanged = true;
                }

                if (cameraPositionChanged) {

                    if (bottomRightBound != null & topLeftBound != null) {
                        cameraPositionBuilder.target(getBoundsCenter(topLeftBound, bottomRightBound));
                    }

                    mapboxMap.setCameraPosition(cameraPositionBuilder.build());
                }
            }
        });
    }

    private LatLng getBoundsCenter(LatLng topLeftBound, LatLng bottomRightBound) {
        double latDifference = topLeftBound.getLatitude() - bottomRightBound.getLatitude();
        double lngDifference = bottomRightBound.getLongitude() - topLeftBound.getLongitude();

        return new LatLng(
                bottomRightBound.getLatitude() + (latDifference / 2),
                topLeftBound.getLongitude() + (lngDifference / 2)
        );
    }

    private void initializeViews() {
        dismissAllDialogs();
        alertDialogs = new HashMap<>();
        infoWindowsRecyclerView = (RecyclerView) findViewById(R.id.rv_mapActivity_infoWindow);
    }

    private void dismissAllDialogs() {
        if (alertDialogs != null) {
            for (AlertDialog curDialog : alertDialogs.values()) {
                if (curDialog.isShowing()) {
                    curDialog.dismiss();
                }
            }
        }
    }

    @Nullable
    private LinkedHashMap<String, InfoWindowObject> extractLayerData(@NonNull JSONObject mapBoxStyleJSON, @NonNull String[] dataSourceNames) throws JSONException {
        if (mapBoxStyleJSON.has("sources")) {
            JSONObject sources = mapBoxStyleJSON.getJSONObject("sources");
            LinkedHashMap<String, InfoWindowObject> featuresMap = new LinkedHashMap<>();
            int counter = 0;

            for (String dataSourceName : dataSourceNames) {
                if (sources.has(dataSourceName)) {
                    JSONObject jsonObject = sources.getJSONObject(dataSourceName);
                    if (jsonObject.has("data")) {
                        JSONObject sourceDataJSONObject = jsonObject.getJSONObject("data");
                        if (sourceDataJSONObject.has("features")) {
                            JSONArray featuresJSONArray = sourceDataJSONObject.getJSONArray("features");
                            for (int i = 0; i < featuresJSONArray.length(); i++) {
                                JSONObject featureJSON = featuresJSONArray.getJSONObject(i);

                                String id = getFeatureId(featureJSON);
                                if (!TextUtils.isEmpty(id)) {
                                    //Todo: Should check for errors here & print them in LogCat or notify the dev somehow
                                    featuresMap.put(id, new InfoWindowObject(counter, featureJSON));
                                    featureIdList.add(id);
                                    counter++;
                                }
                            }
                        }
                    }
                }
            }

            return featuresMap;
        }

        return null;
    }

    private JSONObject getStyleJSON(@NonNull String stylePathOrJSON) {
        try {
            return new JSONObject(getStyleJSONString(stylePathOrJSON));
        } catch (JSONException e) {
            Log.e(TAG, Log.getStackTraceString(e));
            return new JSONObject();
        }
    }

    //Todo handle this better --> In the near future but not now
    private String getStyleJSONString(@NonNull String stylePathOrJSON) {
        String defaultStyleJSONString = "";
        if (stylePathOrJSON.matches(Constants.MAP_BOX_URL_FORMAT)) {
            return defaultStyleJSONString;
        } else if (stylePathOrJSON.contains("asset://")) {
            return defaultStyleJSONString;
        } else if (stylePathOrJSON.contains("file://")) {
            return (new MapBoxStyleStorage())
                    .readStyle(stylePathOrJSON);
        } else {
            return defaultStyleJSONString;
        }
    }

    private void displayInitialFeatures(@NonNull LinkedHashMap<String, InfoWindowObject> featuresMap, @NonNull KujakuConfig kujakuConfig) {
        if (!featuresMap.isEmpty()) {
            infoWindowAdapter = new InfoWindowAdapter(this, featuresMap, infoWindowsRecyclerView, kujakuConfig);
            infoWindowsRecyclerView.setHasFixedSize(true);
            linearLayoutManager = new InfoWindowLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
            infoWindowsRecyclerView.setLayoutManager(linearLayoutManager);
            infoWindowsRecyclerView.setAdapter(infoWindowAdapter);
        }
    }

    private LinkedHashMap<String, InfoWindowObject> sortData(@NonNull LinkedHashMap<String, InfoWindowObject> featuresMap, @NonNull SortFieldConfig[] sortFields) throws JSONException {
        //TODO: Add support for multiple sorts
        int counter = 0;
        if (sortFields.length > 0) {
            SortFieldConfig sortField = sortFields[0];
            if (sortField.getType() == SortFieldConfig.FieldType.DATE) {
                //Todo: Add sorter here
                //Todo: Change the order of ids' in the featureIdsList
                Sorter sorter = new Sorter(new ArrayList(featuresMap.values()));
                ArrayList<InfoWindowObject> infoWindowObjectArrayList = sorter.mergeSort(0, featuresMap.size() - 1, sortField.getDataField(), sortField.getType());

                featuresMap.clear();

                for (InfoWindowObject infoWindowObject : infoWindowObjectArrayList) {
                    String id = getFeatureId(infoWindowObject);
                    if (!TextUtils.isEmpty(id)) {
                        infoWindowObject.setPosition(counter);
                        featuresMap.put(id, infoWindowObject);
                        counter++;
                    }
                }
            }
        }

        return featuresMap;
    }

    public void showAlertDialog(int title, int message,
                                int posButtonText,
                                @Nullable DialogInterface.OnClickListener posOnClickListener,
                                int negButtonText,
                                @Nullable DialogInterface.OnClickListener negOnClickListener) {
        if (!alertDialogs.containsKey(message)
                || !alertDialogs.get(message).isShowing()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(title);
            builder.setMessage(message);
            builder.setCancelable(true);
            if (posOnClickListener != null) {
                builder.setPositiveButton(posButtonText, posOnClickListener);
                builder.setCancelable(false);
            }
            if (negOnClickListener != null) {
                builder.setNegativeButton(negButtonText, negOnClickListener);
                builder.setCancelable(false);
            }
            alertDialogs.put(message, builder.show());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mapView != null) mapView.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mapView != null) mapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mapView != null) mapView.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mapView != null) mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (currentStylePath != null && currentStylePath.startsWith("file://") && currentStylePath.contains(MapBoxStyleStorage.DIRECTORY)) {
            new MapBoxStyleStorage()
                    .deleteFile(currentStylePath.replace("file://", ""), true);
        }

        if (mapView != null) mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mapView != null) mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mapView != null) mapView.onLowMemory();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            checkPermissions();
        }
    }

    private void checkPermissions() {
        String[] unauthorizedPermissions = Permissions.getUnauthorizedCriticalPermissions(this);
        if (unauthorizedPermissions.length > 0) {
            Permissions.request(this, unauthorizedPermissions, PERMISSIONS_REQUEST_CODE);
        }
    }

    @Override
    public void onMapClick(@NonNull LatLng point) {
        // Convert LatLng coordinates to screen pixel and only query the rendered features.
        final PointF pixel = mapboxMap.getProjection().toScreenLocation(point);
        List<Feature> features = mapboxMap.queryRenderedFeatures(pixel);

        // Get the first feature within the list if one exist
        if (features.size() > 0) {
            for (Feature feature : features) {

                // Ensure the feature has properties defined
                if (feature.getProperties() != null) {
                    if (feature.hasProperty("id")) {
                        String id = feature.getProperty("id").getAsString();
                        if (featuresMap.containsKey(id)) {
                            focusOnFeature(id);
                        }
                    }
                }
            }
        }

    }

    private void showInfoWindowListAndScrollToPosition(final int position, final boolean informInfoWindowAdapter) {
        if (!infoWindowDisplayed) {
            // Good enough for now
            infoWindowsRecyclerView.setVisibility(View.VISIBLE);
            infoWindowsRecyclerView.getViewTreeObserver()
                    .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                            infoWindowsRecyclerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                            scrollToInfoWindowPosition(position, informInfoWindowAdapter);
                        }
                    });
            infoWindowDisplayed = true;
        } else {
            scrollToInfoWindowPosition(position, informInfoWindowAdapter);
        }
    }

    private void scrollToInfoWindowPosition(final int position, boolean informInfoWindowAdapter) {
        if (position > -1) {
            if (informInfoWindowAdapter) infoWindowAdapter.focusOnPosition(position);

            // Supposed to scroll to the selected position
            final InfoWindowViewHolder infoWindowViewHolder = (InfoWindowViewHolder) infoWindowsRecyclerView.findViewHolderForAdapterPosition(position);
            int lastVisiblePosition = linearLayoutManager.findLastVisibleItemPosition();
            int firstVisiblePosition = linearLayoutManager.findFirstVisibleItemPosition();

            if (infoWindowViewHolder == null || (position < firstVisiblePosition || position > lastVisiblePosition)) {
                infoWindowsRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                    @Override
                    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                        super.onScrollStateChanged(recyclerView, newState);

                        if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                            InfoWindowViewHolder infoWindowViewHolder = (InfoWindowViewHolder) infoWindowsRecyclerView.findViewHolderForAdapterPosition(position);
                            if (infoWindowViewHolder != null) {
                                View v = infoWindowViewHolder.itemView;

                                int offset = (screenWidth / 2) - (v.getWidth() / 2);
                                linearLayoutManager.scrollToPositionWithOffset(position, offset);

                                infoWindowViewHolder.select();
                            }

                            infoWindowsRecyclerView.removeOnScrollListener(this);
                        }
                    }
                });
                infoWindowsRecyclerView.smoothScrollToPosition(position);

            } else {
                View v = infoWindowViewHolder.itemView;
                animateScrollToPosition(v, position, infoWindowViewHolder, animateToNewInfoWindowDuration);
            }
        }
    }

    private void centerMap(@NonNull LatLng point) {
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(point)
                .build();

        if (mapboxMap != null) {
            mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), animateToNewTargetDuration);
        }
    }

    private void animateScrollToPosition(@NonNull View view, final int position, @NonNull final InfoWindowViewHolder infoWindowViewHolder, final int animationDuration) {
        final int left = view.getLeft();
        final int offset = (screenWidth / 2) - (view.getWidth() / 2);
        final float totalOffset = offset - left;

        ValueAnimator valueAnimator = ValueAnimator.ofFloat(totalOffset);
        valueAnimator.setDuration(animationDuration);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float updatedValue = (float) animation.getAnimatedValue();
                float currentOffset = updatedValue + left;

                linearLayoutManager.scrollToPositionWithOffset(position, (int) currentOffset);

                if (updatedValue == totalOffset) {
                    infoWindowViewHolder.select();
                }
            }
        });

        valueAnimator.start();
        //Todo Figure out how to handle another item being selected while this one is being animated
    }

    private int getScreenWidth(Activity activity) {
        // Get the screen width
        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.widthPixels;
    }

    private void focusOnFeature(int position, @Nullable String id, @Nullable LatLng latLng, boolean informInfoWindowAdapter) {
        if (position == -1 && id != null && !id.isEmpty()) {
            if (featuresMap.containsKey(id)) {
                position = featuresMap.get(id).getPosition();
            } else {
                return;
            }
        }

        if (id == null && position > -1) {
            if (featureIdList.isEmpty()) {
                return;
            }

            id = (String) featureIdList.get(position);
        }


        if (latLng == null) {
            latLng = getFeaturePoint(featuresMap.get(id)
                    .getJsonObject());

            if (latLng == null) {
                return;
            }
        }

        showInfoWindowListAndScrollToPosition(position, informInfoWindowAdapter);
        centerMap(latLng);
    }


    public void focusOnFeature(int position) {
        focusOnFeature(position, null, null, false);
    }

    private void focusOnFeature(String id, LatLng latLng) {
        focusOnFeature(-1, id, latLng, true);
    }

    private void focusOnFeature(String id) {
        focusOnFeature(-1, id, null, true);
    }

    private LatLng getFeaturePoint(JSONObject featureJSON) {
        if (featureJSON.has("geometry")) {
            try {
                JSONObject featureGeometry = featureJSON.getJSONObject("geometry");
                if (featureGeometry.has("type") && GeoJSONFeature.Type.POINT.toString().equalsIgnoreCase(featureGeometry.getString("type"))) {
                    if (featureGeometry.has("coordinates")) {
                        JSONArray coordinatesArray = featureGeometry.getJSONArray("coordinates");
                        if (coordinatesArray.length() > 2) {
                            double lng = coordinatesArray.getDouble(0);
                            double lat = coordinatesArray.getDouble(1);

                            return (new LatLng(lat, lng));
                        }
                    }
                }
            } catch (JSONException e) {
                Log.e(TAG, Log.getStackTraceString(e));
            }
        }

        return null;
    }

    private String getFeatureId(@NonNull InfoWindowObject infoWindowObject) throws JSONException {
        JSONObject jsonObject = infoWindowObject.getJsonObject();
        return getFeatureId(jsonObject);
    }

    private String getFeatureId(@NonNull JSONObject jsonObject) throws JSONException {
        if (jsonObject.has("id")) {
            return jsonObject.getString("id");
        }

        if (jsonObject.has("properties")) {
            JSONObject propertiesJSON = jsonObject.getJSONObject("properties");
            if (propertiesJSON.has("id")) {
                return propertiesJSON.getString("id");
            }
        }

        return null;
    }
}
