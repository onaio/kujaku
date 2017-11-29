package io.ona.kujaku.activities;

import android.app.Activity;
import android.graphics.PointF;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.services.commons.geojson.Feature;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import io.ona.kujaku.R;
import io.ona.kujaku.adapters.InfoWindowAdapter;
import io.ona.kujaku.adapters.InfoWindowObject;
import io.ona.kujaku.helpers.MapBoxStyleStorage;
import io.ona.kujaku.sorting.Sorter;
import io.ona.kujaku.sorting.objects.SortField;
import io.ona.kujaku.utils.Permissions;
import io.ona.kujaku.views.InfoWindowLayoutManager;
import utils.Constants;
import utils.helpers.converters.GeoJSONFeature;

/**
 * This activity displays a MapView once provided with a a MapBox Access Key & String array.
 * These are passed as:
 *      {@link Constants#PARCELABLE_KEY_MAPBOX_ACCESS_TOKEN} - MapBox Access Token ({@code String})
 *      {@link Constants#PARCELABLE_KEY_MAPBOX_STYLES} - MapBox Styles ({@code String[]}
 *
 *
 *      <p>
 *      - MapBox Styles - String array containing <a href="https://www.mapbox.com/mapbox-gl-js/style-spec/">valid MapBox Style</a> at index 0
 *      <p>
 *
 * Created by Ephraim Kigamba - ekigamba@ona.io
 */
public class MapActivity extends AppCompatActivity implements MapboxMap.OnMapClickListener {
    private static final int PERMISSIONS_REQUEST_CODE = 342;
    private MapView mapView;
    private String currentStylePath;

    private SortField[] sortFields;
    private String[] dataLayers;
    private JSONObject mapboxStyleJSON;
    private static final String TAG = MapActivity.class.getSimpleName();

    private LinkedHashMap<String, InfoWindowObject> featuresMap = new LinkedHashMap<>();
    private ArrayList featureIdList = new ArrayList();
    private MapboxMap mapboxMap;
    private boolean infoWindowDisplayed = false;

    private RecyclerView.OnScrollListener onScrollListener = null;

    // Info window stuff
    private RecyclerView infoWindowsRecyclerView;
    private InfoWindowLayoutManager linearLayoutManager;
    private int lastSelected = 0;

    private int animateToNewTargetDuration = 1000;
    private int screenWidth = 0;

    private InfoWindowAdapter infoWindowAdapter;
    private boolean startedScrolling = false;

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
            if (bundle != null){
                String mapBoxAccessToken = bundle.getString(Constants.PARCELABLE_KEY_MAPBOX_ACCESS_TOKEN);
                String[] stylesArray = bundle.getStringArray(Constants.PARCELABLE_KEY_MAPBOX_STYLES);
                currentStylePath = "";

                if (stylesArray != null) {
                    currentStylePath = stylesArray[0];
                    if (currentStylePath != null && !currentStylePath.isEmpty()) {
                        currentStylePath = new MapBoxStyleStorage()
                                .getStyleURL(currentStylePath);
                        mapboxStyleJSON = getStyleJSON(currentStylePath);

                        // Extract kujaku meta-data
                        try {
                            sortFields = extractSortFields(mapboxStyleJSON);
                            dataLayers = extractSourceNames(mapboxStyleJSON, sortFields);
                            featuresMap = extractLayerData(mapboxStyleJSON, dataLayers);
                            featuresMap = sortData(featuresMap, sortFields);
                            displayInitialFeatures(featuresMap);
                        } catch (JSONException e) {
                            Log.e(TAG, Log.getStackTraceString(e));
                        }
                    }
                }


                initMapBoxSdk(savedInstanceState, mapBoxAccessToken, currentStylePath);
            }
        }
    }

    private void initMapBoxSdk(Bundle savedInstanceState, String mapboxAccessToken, String mapBoxStylePath) {
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
            }
        });
    }

    private void initializeViews() {
        infoWindowsRecyclerView = (RecyclerView) findViewById(R.id.rv_mapActivity_infoWindow);
    }

    private String[] extractSourceNames(@NonNull JSONObject jsonObject, @NonNull SortField[] sortFields) throws JSONException {
        if (jsonObject.has("metadata")) {
            JSONObject metadata = jsonObject.getJSONObject("metadata");
            if (metadata.has("kujaku")) {
                JSONObject kujakuRelatedData = metadata.getJSONObject("kujaku");
                if (kujakuRelatedData.has("data_source_names")) {
                    JSONArray jsonArray = kujakuRelatedData.getJSONArray("data_source_names");
                    String[] dataLayers = new String[jsonArray.length()];

                    for(int i = 0; i < dataLayers.length; i++) {
                        dataLayers[i] = jsonArray.getString(i);
                    }

                    return dataLayers;
                }
            }
        }

        return null;
    }

    private LinkedHashMap<String, InfoWindowObject> extractLayerData(@NonNull JSONObject mapBoxStyleJSON, @NonNull String[] dataSourceNames) throws JSONException {
        if (mapBoxStyleJSON.has("sources")) {
            JSONObject sources = mapBoxStyleJSON.getJSONObject("sources");
            LinkedHashMap<String, InfoWindowObject> featuresMap = new LinkedHashMap<>();
            int counter = 0;

            for(String dataSourceName: dataSourceNames) {
                if (sources.has(dataSourceName)) {
                    JSONObject jsonObject = sources.getJSONObject(dataSourceName);
                    if (jsonObject.has("data")) {
                        JSONObject sourceDataJSONObject = jsonObject.getJSONObject("data");
                        if (sourceDataJSONObject.has("features")) {
                            JSONArray featuresJSONArray = sourceDataJSONObject.getJSONArray("features");
                            for(int i = 0; i < featuresJSONArray.length(); i++) {
                                JSONObject featureJSON = featuresJSONArray.getJSONObject(i);

                                String id = getFeatureId(featureJSON);
                                if (!id.isEmpty()) {
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

    private SortField[] extractSortFields(@NonNull JSONObject jsonObject) throws JSONException {
        if (jsonObject.has("metadata")) {
            JSONObject metadata = jsonObject.getJSONObject("metadata");
            if (metadata.has("kujaku")) {
                JSONObject kujakuRelatedData = metadata.getJSONObject("kujaku");
                if (kujakuRelatedData.has("sort_fields")) {
                    JSONArray jsonArray = kujakuRelatedData.getJSONArray("sort_fields");
                    SortField[] sortFields = new SortField[jsonArray.length()];

                    for(int i = 0; i < sortFields.length; i++) {
                        sortFields[i] = SortField.extract(jsonArray.getJSONObject(i));
                    }

                    return sortFields;
                }
            }
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

    private void displayInitialFeatures(@NonNull LinkedHashMap<String, InfoWindowObject> featuresMap) {
        if (!featuresMap.isEmpty()) {
            infoWindowAdapter = new InfoWindowAdapter(this, featuresMap, infoWindowsRecyclerView);
            infoWindowAdapter.setOnClickListener(new InfoWindowAdapter.OnClickListener() {
                @Override
                public void onClick(View v, int position) {
                    focusOnFeature(position);
                }
            });

            infoWindowsRecyclerView.setHasFixedSize(true);
            linearLayoutManager = new InfoWindowLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
            infoWindowsRecyclerView.setLayoutManager(linearLayoutManager);
            infoWindowsRecyclerView.setAdapter(infoWindowAdapter);
        }
    }

    private LinkedHashMap<String, InfoWindowObject> sortData(@NonNull LinkedHashMap<String, InfoWindowObject> featuresMap,@NonNull SortField[] sortFields) throws JSONException {
        //TODO: Add support for multiple sorts
        int counter = 0;
        if (sortFields.length > 0) {
            SortField sortField = sortFields[0];
            if (sortField.getType() == SortField.FieldType.DATE) {
                //Todo: Add sorter here
                //Todo: Change the order of ids' in the featureIdsList
                Sorter sorter = new Sorter(new ArrayList(featuresMap.values()));
                ArrayList<InfoWindowObject> infoWindowObjectArrayList = sorter.mergeSort(0, featuresMap.size() -1, sortField.getDataField(), sortField.getType());

                featuresMap.clear();

                for(InfoWindowObject infoWindowObject: infoWindowObjectArrayList) {
                    String id = getFeatureId(infoWindowObject);
                    if (!id.isEmpty()) {
                        infoWindowObject.setPosition(counter);
                        featuresMap.put(id, infoWindowObject);
                        counter++;
                    }
                }
            }
        }

        return featuresMap;
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
            for(Feature feature: features) {

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

    private void showInfoWindow() {
        if (!infoWindowDisplayed) {
            // Good enough for now
            infoWindowsRecyclerView.setVisibility(View.VISIBLE);
            infoWindowDisplayed = true;
        }
    }

    private void renderInfoWindow(final int position) {
        if (position > 1) {

            infoWindowAdapter.focusOnPosition(position);

            // Supposed to scroll to the selected position
            final InfoWindowAdapter.InfoWindowViewHolder infoWindowViewHolder = (InfoWindowAdapter.InfoWindowViewHolder) infoWindowsRecyclerView.findViewHolderForAdapterPosition(position);
            if (infoWindowViewHolder == null) {
                infoWindowsRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                    @Override
                    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                        super.onScrollStateChanged(recyclerView, newState);

                        if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                            InfoWindowAdapter.InfoWindowViewHolder infoWindowViewHolder = (InfoWindowAdapter.InfoWindowViewHolder) infoWindowsRecyclerView.findViewHolderForAdapterPosition(position);
                            if (infoWindowViewHolder != null) {
                                View v = infoWindowViewHolder.itemView;

                                final int offset = (screenWidth/2) - (v.getWidth()/2);
                                linearLayoutManager.scrollToPositionWithOffset(position,  offset);

                                infoWindowViewHolder.select();
                            }

                            infoWindowsRecyclerView.removeOnScrollListener(this);
                            startedScrolling = false;
                        }
                    }
                });
                startedScrolling = true;
                infoWindowsRecyclerView.smoothScrollToPosition(position);

            } else {
                View v = infoWindowViewHolder.itemView;
                animateToPosition(v, position, animateToNewTargetDuration);
                infoWindowViewHolder.select();
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


    private void animateToPosition(@NonNull View view, final int position, final int animationDuration) {
        final int left = view.getLeft()
                , right = view.getRight();
        
        int scrollX = (left - (screenWidth/2)) + (view.getWidth()/2);
        final int offset = (screenWidth/2) - (view.getWidth()/2);

        //infoWindowsRecyclerView.smoothScrollToPosition(position);
        linearLayoutManager.scrollToPositionWithOffset(position,  offset);
        //Todo Figure out how to handle another item being selected while this one is being animated
        /*new Thread(new Runnable() {
            @Override
            public void run() {
                int sleepCounts = 10;
                int sleepDuration = animationDuration/sleepCounts;
                int counter = 0;
                int eachMove = (offset - left)/sleepCounts;
                int startOffset = left;

                while (counter < sleepCounts) {
                    startOffset += eachMove;
                    counter++;

                    try {
                        Thread.sleep(sleepDuration);

                        final int finalStartOffset = startOffset;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                linearLayoutManager.scrollToPositionWithOffset(position, finalStartOffset);
                                //linearLayoutManager.smoothScrollToPosition();

                                //Todo add size animation
                            }
                        });
                    } catch (InterruptedException e) {
                        Log.e(TAG, Log.getStackTraceString(e));
                    }
                }
            }
        }).start(); */
    }

    private int getScreenWidth(Activity activity) {
        // Get the screen width
        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.widthPixels;
    }

    private void focusOnFeature(int position, @Nullable String id, @Nullable LatLng latLng) {
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

        showInfoWindow();
        centerMap(latLng);
        renderInfoWindow(position);
    }


    private void focusOnFeature(int position) {
        focusOnFeature(position, null, null);
    }

    private void focusOnFeature(String id, LatLng latLng) {
        focusOnFeature(-1, id, latLng);
    }

    private void focusOnFeature(String id) {
        focusOnFeature(-1, id, null);
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
        if (jsonObject.has("properties")) {
            JSONObject propertiesJSON = jsonObject.getJSONObject("properties");
            if (propertiesJSON.has("id")) {
                return propertiesJSON.getString("id");
            }
        }

        return "";
    }
}
