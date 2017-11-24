package io.ona.kujaku.activities;

import android.graphics.PointF;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.google.gson.JsonElement;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.geometry.LatLng;
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
import java.util.Map;

import io.ona.kujaku.R;
import io.ona.kujaku.adapters.InfoWindowAdapter;
import io.ona.kujaku.adapters.InfoWindowObject;
import io.ona.kujaku.helpers.MapBoxStyleStorage;
import io.ona.kujaku.sorting.objects.SortField;
import io.ona.kujaku.utils.Permissions;
import utils.Constants;

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

    private LinkedHashMap<String, InfoWindowObject> featureList = new LinkedHashMap<>();
    private MapboxMap mapboxMap;
    private boolean infoWindowDisplayed = false;

    // Info window stuff
    private RecyclerView infoWindowsRecyclerView;
    private int lastSelected = 0;

    //Todo: Move reading data to another Thread

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        initializeViews();

        checkPermissions();

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
                            dataLayers = extractSourceNames(mapboxStyleJSON);
                            sortFields = extractSortFields(mapboxStyleJSON);
                            featureList = extractLayerData(mapboxStyleJSON, dataLayers);
                            displayInitialFeatures(featureList);
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

    private String[] extractSourceNames(@NonNull JSONObject jsonObject) throws JSONException {
        if (jsonObject.has("metadata")) {
            JSONObject metadata = jsonObject.getJSONObject("metadata");
            if (metadata.has("kujaku")) {
                JSONObject kujakuRelatedData = metadata.getJSONObject("kujaku");
                if (kujakuRelatedData.has("data_layers")) {
                    JSONArray jsonArray = kujakuRelatedData.getJSONArray("data_layers");
                    String[] dataLayers = new String[jsonArray.length()];

                    for(int i = 0; i < sortFields.length; i++) {
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
            LinkedHashMap<String, InfoWindowObject> featureList = new LinkedHashMap<>();
            int counter = 0;

            for(String dataSourceName: dataSourceNames) {
                if (sources.has(dataSourceName)) {
                    JSONObject jsonObject = sources.getJSONObject(dataSourceName);
                    if (jsonObject.has("id")) {
                        String id = jsonObject.getString("id");
                        //Todo: Should check for errors here & print them in LogCat or notify the dev somehow
                        featureList.put(id, new InfoWindowObject(counter, jsonObject));
                        counter++;
                    }
                }
            }

            return featureList;
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
            return new JSONObject(stylePathOrJSON);
        } catch (JSONException e) {
            Log.e(TAG, Log.getStackTraceString(e));
            return new JSONObject();
        }
    }

    //Todo handle this better --> For near future but not now
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

    private void displayInitialFeatures(@NonNull LinkedHashMap<String, InfoWindowObject> featureList) {
        if (!featureList.isEmpty()) {
            InfoWindowAdapter infoWindowAdapter = new InfoWindowAdapter(featureList, infoWindowsRecyclerView);
            infoWindowsRecyclerView.setAdapter(infoWindowAdapter);
        }
    }

    private LinkedHashMap<String, InfoWindowObject> sortData(@NonNull LinkedHashMap<String, InfoWindowObject> featureList,@NonNull SortField[] sortFields) {
        //TODO: Add support for multiple sorts
        if (sortFields.length > 0) {
            SortField sortField = sortFields[0];
            if (sortField.getType() == SortField.FieldType.DATE) {
                //Todo: Add sorter here
                return featureList;
            }
        }

        return featureList;
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

        if (currentStylePath != null && currentStylePath.startsWith("file://")) {
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
            com.mapbox.services.commons.geojson.Feature feature = features.get(0);

            // Ensure the feature has properties defined
            if (feature.getProperties() != null) {
                if (feature.hasProperty("id")) {
                    String id =  feature.getId();
                    if (featureList.containsKey(id)) {
                        showInfoWindow();
                        centerMap(point);
                        renderInfoWindow(featureList.get(id).getPosition());
                    }
                }

                for (Map.Entry<String, JsonElement> entry : feature.getProperties().entrySet()) {
                    // Log all the properties

                    Log.d(TAG, String.format("%s = %s", entry.getKey(), entry.getValue()));
                }
            }
        }

    }

    private void showInfoWindow() {
        if (!infoWindowDisplayed) {
            // Good enough for now
            infoWindowsRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void renderInfoWindow(int position) {
        if (position > 1) {

        }
    }

    private void centerMap(@NonNull LatLng point) {
        //Todo: finish this
    }
}
