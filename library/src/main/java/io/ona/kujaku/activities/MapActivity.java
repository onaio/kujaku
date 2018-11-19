package io.ona.kujaku.activities;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PointF;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.mapbox.geojson.Feature;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import io.ona.kujaku.KujakuLibrary;
import io.ona.kujaku.R;
import io.ona.kujaku.adapters.InfoWindowAdapter;
import io.ona.kujaku.adapters.InfoWindowObject;
import io.ona.kujaku.adapters.holders.InfoWindowViewHolder;
import io.ona.kujaku.domain.Point;
import io.ona.kujaku.helpers.MapBoxStyleStorage;
import io.ona.kujaku.sorting.Sorter;
import io.ona.kujaku.utils.Constants;
import io.ona.kujaku.utils.Permissions;
import io.ona.kujaku.utils.config.DataSourceConfig;
import io.ona.kujaku.utils.config.KujakuConfig;
import io.ona.kujaku.utils.config.SortFieldConfig;
import io.ona.kujaku.utils.exceptions.InvalidMapBoxStyleException;
import io.ona.kujaku.utils.helpers.MapBoxStyleHelper;
import io.ona.kujaku.utils.helpers.converters.GeoJSONFeature;
import io.ona.kujaku.views.InfoWindowLayoutManager;
import io.ona.kujaku.views.KujakuMapView;

import static io.ona.kujaku.utils.Constants.ENABLE_DROP_POINT_BUTTON;
import static io.ona.kujaku.utils.Constants.NEW_FEATURE_POINTS_JSON;
import static io.ona.kujaku.utils.Constants.PARCELABLE_POINTS_LIST;


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
    private KujakuMapView kujakuMapView;
    private String currentStylePath;

    private SortFieldConfig[] sortFields;
    private String[] dataLayers;
    private JSONObject mapboxStyleJSON;
    private static final String TAG = MapActivity.class.getSimpleName();

    private LinkedHashMap<String, InfoWindowObject> featuresMap = new LinkedHashMap<>();
    private ArrayList<String> featureIdList = new ArrayList<>();
    private MapboxMap mapboxMap;
    private boolean infoWindowDisplayed = false;

    // Info window stuff
    private RecyclerView infoWindowsRecyclerView;
    private InfoWindowLayoutManager linearLayoutManager;
    private HashMap<Integer, AlertDialog> alertDialogs;
    private int lastSelected = -1;
    private ImageButton focusOnMyLocationImgBtn;

    private int animateToNewTargetDuration = 1000;
    private int animateToNewInfoWindowDuration = 300;
    private int screenWidth = 0;

    private InfoWindowAdapter infoWindowAdapter;

    private double maxZoom = -1;
    private double minZoom = -1;
    private Bundle savedInstanceState;

    private boolean enableDropPoint = false;

    private List<JSONObject> newPoints;

    //Todo: Move reading data to another Thread

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        screenWidth = getScreenWidth(this);

        newPoints = new ArrayList<>();

        Bundle bundle = getIntentExtras();
        List<Point> points = null;
        if (bundle != null) {
            String mapBoxAccessToken = bundle.getString(Constants.PARCELABLE_KEY_MAPBOX_ACCESS_TOKEN);
            Mapbox.getInstance(this, mapBoxAccessToken);
            points = bundle.getParcelableArrayList(PARCELABLE_POINTS_LIST);
            enableDropPoint = bundle.getBoolean(ENABLE_DROP_POINT_BUTTON, false);
        }
        initializeViews(points, enableDropPoint);
        checkPermissions(savedInstanceState);
    }

    private Bundle getIntentExtras() {
        if (getIntent() != null) {
            Bundle bundle = getIntent().getExtras();
            if (bundle != null) {
                return bundle;
            }
        }
        return null;
    }

    private void initializeMapActivityAfterPermissionsSupplied(Bundle savedInstanceState) {
        Bundle bundle = getIntentExtras();
        if (bundle != null) {
            String[] stylesArray = bundle.getStringArray(Constants.PARCELABLE_KEY_MAPBOX_STYLES);
            currentStylePath = "";

            if (bundle.containsKey(Constants.PARCELABLE_KEY_MAX_ZOOM)) {
                maxZoom = bundle.getDouble(Constants.PARCELABLE_KEY_MAX_ZOOM);
            }

            if (bundle.containsKey(Constants.PARCELABLE_KEY_MIN_ZOOM)) {
                minZoom = bundle.getDouble(Constants.PARCELABLE_KEY_MIN_ZOOM);
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
                        sortFields = SortFieldConfig.extractSortFieldConfigs(styleHelper);
                        dataLayers = DataSourceConfig.extractDataSourceNames(styleHelper.getKujakuConfig().getDataSourceConfigs());
                        featuresMap = extractLayerData(mapboxStyleJSON, dataLayers);
                        featuresMap = sortData(featuresMap, sortFields);
                        displayInitialFeatures(featuresMap, styleHelper.getKujakuConfig());
                    } catch (JSONException | InvalidMapBoxStyleException e) {
                        Log.e(TAG, Log.getStackTraceString(e));
                    }
                }
            }

            initMapBoxSdk(savedInstanceState, currentStylePath, maxZoom, minZoom);
        }
    }

    private void showIncompleteStyleError() {
        showAlertDialog(
                R.string.error_kujaku_config,
                R.string.error_kujaku_config_description,
                R.string.ok,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }, -1, null);
    }

    private void initMapBoxSdk(Bundle savedInstanceState, String mapBoxStylePath,
                               final double maxZoom, final double minZoom) {

        kujakuMapView.onCreate(savedInstanceState);

        if (mapBoxStylePath != null && !mapBoxStylePath.isEmpty()) {
            kujakuMapView.setStyleUrl(mapBoxStylePath);
        }
        kujakuMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap mapboxMap) {
                //Set listener for markers
                MapActivity.this.mapboxMap = mapboxMap;
                mapboxMap.addOnMapClickListener(MapActivity.this);

                if (minZoom != -1) {
                    mapboxMap.setMinZoomPreference(minZoom);
                }

                if (maxZoom != -1) {
                    mapboxMap.setMaxZoomPreference(maxZoom);
                }

                CameraPosition.Builder cameraPositionBuilder = new CameraPosition.Builder();
                boolean cameraPositionChanged = false;


                if (cameraPositionChanged) {
                    mapboxMap.setCameraPosition(cameraPositionBuilder.build());
                }

                if (!mapboxStyleJSON.has(MapBoxStyleHelper.KEY_MAP_CENTER)) {
                    kujakuMapView.focusOnUserLocation(true);
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

    private void initializeViews(List<Point> points, boolean enableDropPoint) {
        dismissAllDialogs();
        alertDialogs = new HashMap<>();
        infoWindowsRecyclerView = findViewById(R.id.rv_mapActivity_infoWindow);
        focusOnMyLocationImgBtn = findViewById(R.id.ib_mapview_focusOnMyLocationIcon);

        Button btnDone = findViewById(R.id.btn_done_map_activity);
        List<String> newPointsJSON = new ArrayList<>();
        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Done using the MapActivity, exiting ...");
                for (JSONObject featureJSON : newPoints) {
                    newPointsJSON.add(featureJSON.toString());
                }
                Intent intent = new Intent();
                intent.putStringArrayListExtra(NEW_FEATURE_POINTS_JSON, (ArrayList<String>) newPointsJSON);
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        });

        kujakuMapView = findViewById(R.id.map_view);
        if (enableDropPoint) {
            kujakuMapView.enableAddPoint(true);
            ImageButton locationAdditionBtn = findViewById(R.id.map_activity_location_addition_btn);
            locationAdditionBtn.setVisibility(View.VISIBLE);
            locationAdditionBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (kujakuMapView.isCanAddPoint()) {
                        JSONObject featurePoint = kujakuMapView.dropPoint();
                        Log.e("FEATURE POINT", featurePoint.toString());
                        newPoints.add(featurePoint);
                    }
                }
            });
        }

        // set previously dropped pins
        if (points != null) {
            kujakuMapView.setDroppedPoints(points);
        }
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

    private LinkedHashMap<String, InfoWindowObject> extractLayerData(@NonNull JSONObject mapBoxStyleJSON, String[] dataSourceNames) throws JSONException {
        if (dataSourceNames != null && mapBoxStyleJSON.has("sources")) {
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
        List<Point> droppedPoints = null;
        if (kujakuMapView != null) {
            kujakuMapView.onResume();
            droppedPoints = kujakuMapView.getDroppedPoints();
        }
        initializeViews(droppedPoints, enableDropPoint);
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

        if (currentStylePath != null && currentStylePath.startsWith("file://") && currentStylePath.contains(MapBoxStyleStorage.DIRECTORY)) {
            new MapBoxStyleStorage()
                    .deleteFile(currentStylePath.replace("file://", ""), true);
        }

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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            checkPermissions(this.savedInstanceState);
        }
    }

    private void checkPermissions(Bundle savedInstanceState) {
        this.savedInstanceState = savedInstanceState;
        String[] unauthorizedPermissions = Permissions.getUnauthorizedCriticalPermissions(this);
        if (unauthorizedPermissions.length > 0) {
            Permissions.request(this, unauthorizedPermissions, PERMISSIONS_REQUEST_CODE);
        } else {
            initializeMapActivityAfterPermissionsSupplied(savedInstanceState);
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
                if (feature.properties() != null && feature.hasProperty("id")) {
                    String id = feature.getProperty("id").getAsString();
                    if (featuresMap.containsKey(id)) {
                        focusOnFeature(id);
                        break;
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

            disableAlignBottomAndEnableAlignAbove(focusOnMyLocationImgBtn);
            infoWindowDisplayed = true;
        } else {
            scrollToInfoWindowPosition(position, informInfoWindowAdapter);
        }
    }

    private void scrollToInfoWindowPosition(final int position, boolean informInfoWindowAdapter) {
        if (position > -1) {
            if (informInfoWindowAdapter) infoWindowAdapter.focusOnPosition(position, false);

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

            id = featureIdList.get(position);
        }

        if (latLng == null) {
            latLng = getFeaturePoint(featuresMap.get(id)
                    .getJsonObject());

            if (latLng == null) {
                return;
            }
        }

        if (lastSelected == position) {
            performInfoWindowDoubleClickAction(featuresMap.get(id));
        } else {
            showInfoWindowListAndScrollToPosition(position, informInfoWindowAdapter);
            kujakuMapView.centerMap(latLng, animateToNewTargetDuration);
        }
        lastSelected = position;
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
                if (featureGeometry.has("type") && GeoJSONFeature.Type.POINT.toString().equalsIgnoreCase(featureGeometry.getString("type")) && featureGeometry.has("coordinates")) {
                    JSONArray coordinatesArray = featureGeometry.getJSONArray("coordinates");
                    if (coordinatesArray.length() > 2) {
                        double lng = coordinatesArray.getDouble(0);
                        double lat = coordinatesArray.getDouble(1);

                        return (new LatLng(lat, lng));
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

    private void performInfoWindowDoubleClickAction(InfoWindowObject infoWindowObject) {
        //For now, this will be a return Result with the current GeoJSON Feature
        Intent intent = new Intent();
        intent.putExtra(Constants.PARCELABLE_KEY_GEOJSON_FEATURE, infoWindowObject.getJsonObject().toString());

        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    private void disableAlignBottomAndEnableAlignAbove(View view) {
        ViewGroup.LayoutParams viewGroupParams = view.getLayoutParams();

        if (viewGroupParams instanceof RelativeLayout.LayoutParams) {
            RelativeLayout.LayoutParams relativeLayoutParams = (RelativeLayout.LayoutParams) viewGroupParams;

            relativeLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 0);
            relativeLayoutParams.addRule(RelativeLayout.ABOVE, R.id.rv_mapActivity_infoWindow);

            view.setLayoutParams(relativeLayoutParams);
        }
    }
}
