package io.ona.kujaku.sample.activities;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.google.gson.Gson;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.expressions.Expression;
import com.mapbox.mapboxsdk.style.layers.FillLayer;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.vividsolutions.jts.geom.Point;

import org.commongeoregistry.adapter.android.AndroidHttpCredentialConnector;
import org.commongeoregistry.adapter.android.AndroidRegistryClient;
import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.commongeoregistry.adapter.dataaccess.ChildTreeNode;
import org.commongeoregistry.adapter.dataaccess.GeoObject;

import java.util.ArrayList;
import java.util.List;

import io.ona.kujaku.callables.AsyncTaskCallable;
import io.ona.kujaku.layers.ArrowLineLayer;
import io.ona.kujaku.listeners.OnFeatureClickListener;
import io.ona.kujaku.listeners.OnFinishedListener;
import io.ona.kujaku.sample.BuildConfig;
import io.ona.kujaku.sample.R;
import io.ona.kujaku.tasks.GenericAsyncTask;
import io.ona.kujaku.views.KujakuMapView;
import timber.log.Timber;

public class CGRIntegrationActivity extends BaseNavigationDrawerActivity implements OnFeatureClickListener {

    private KujakuMapView kujakuMapView;

    private GeoJsonSource locationsGeoJsonSource;
    private SymbolLayer symbolLayer;
    private GeoJsonSource labelSource;

    private GeoObject cambodiaCountry;
    private ChildTreeNode currentDrillDown;

    private AndroidRegistryClient client;

    private String layerId = "cgr-admin-boundaries";
    private static final double ZOOM = 6.45262d;

    private static final String LABEL_LAYER_ID = "label-layer-name";
    private static final String LABEL_SOURCE_ID = "label-source-id";

    private String[] adminHierarchy = new String[]{"Cambodia", "Province", "District", "Commune", "Village"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        kujakuMapView = findViewById(R.id.kmv_cgrIntegration_mapView);
        kujakuMapView.onCreate(savedInstanceState);

        kujakuMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull MapboxMap mapboxMap) {
                mapboxMap.setStyle(Style.SATELLITE);

                mapboxMap.getStyle(new Style.OnStyleLoaded() {
                    @Override
                    public void onStyleLoaded(@NonNull Style style) {
                        style.removeLayer("Thailand 2");
                        addAdministrativeLayer(style);
                        loadCGR();
                    }
                });

                // TODO: Focus on Cambodia
                // TODO: Explain to the user that they should click on a specific location to get focus and get more
                // TODO: Enable double click to zoom(Maybe)
                // TODO: Load the

                // TODO: Create a layer to show the boundary and CGR locations
            }
        });
    }

    private void addAdministrativeLayer(@NonNull Style style) {
        String adminSource = "cgr-admin-boundary-source";

        FillLayer fillLayer = new FillLayer(layerId, adminSource);
        fillLayer.withProperties(PropertyFactory.fillOpacity(0.75f)
                , PropertyFactory.fillColor("rgba(161, 202, 241, 0.75)"));

        locationsGeoJsonSource = new GeoJsonSource(adminSource);
        symbolLayer =  new SymbolLayer(LABEL_LAYER_ID, LABEL_SOURCE_ID)
                        .withProperties(
                                PropertyFactory.textField(Expression.toString(Expression.get("displayLabel"))),
                                PropertyFactory.textPadding(35f),
                                PropertyFactory.textColor(Color.BLACK),
                                PropertyFactory.textAllowOverlap(true)
                        );
        labelSource = new GeoJsonSource(LABEL_SOURCE_ID);

        style.addSource(locationsGeoJsonSource);
        style.addSource(labelSource);
        style.addLayer(fillLayer);
        style.addLayer(symbolLayer);
    }

    private void loadCGR() {
        // Configure our connection with the remote registry server
        AndroidHttpCredentialConnector connector = new AndroidHttpCredentialConnector();
        connector.setCredentials(BuildConfig.CGR_USERNAME, BuildConfig.CGR_PASSWORD);
        connector.setServerUrl(BuildConfig.CGR_URL);
        connector.initialize();

        GenericAsyncTask genericAsyncTask = new GenericAsyncTask(new AsyncTaskCallable() {
            @Override
            public Object[] call() throws Exception {
                // Synchronize with the Registry server (requires an internet connection)
                client = new AndroidRegistryClient(connector, CGRIntegrationActivity.this);
                // Pull our metadata objects which we'll use later
                client.refreshMetadataCache();
                // Fetch 500 ids from the GeoRegistry which we'll use later to create GeoObjects. These ids are persisted in an offline database on the android phone.
                client.getIdService().populate(30);

                GeoObject country = client.getGeoObjectByCode("1", "Cambodia");
                ChildTreeNode countryChildren = client.getChildGeoObjects(country.getUid(), country.getType().getCode(), adminHierarchy, true);

                return new Object[]{country, countryChildren};
            }
        });

        genericAsyncTask.setOnFinishedListener(new OnFinishedListener() {
            @Override
            public void onSuccess(Object[] objects) {
                // Add the country geoobject
                cambodiaCountry = (GeoObject) objects[0];
                currentDrillDown = (ChildTreeNode) objects[1];

                locationsGeoJsonSource.setGeoJson(cambodiaCountry.toJSON().toString());
                centerOnGeoObject(cambodiaCountry);
                createLabelSource(cambodiaCountry);

                // Set click listener
                kujakuMapView.setOnFeatureClickListener(CGRIntegrationActivity.this, layerId);
            }

            @Override
            public void onError(Exception e) {
                Timber.e(e);
            }
        });

        genericAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    protected int getContentView() {
        return R.layout.activity_cgr_integration_activity;
    }

    @Override
    protected int getSelectedNavigationItem() {
        return R.id.nav_cgr_integration;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (kujakuMapView != null) kujakuMapView.onResume();
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
    public void onFeatureClick(List<Feature> features) {
        if (features.size() > 0) {
            final Feature feature = features.get(0);

            if (feature.hasProperty(DefaultAttribute.UID.getName()) && feature.hasProperty(DefaultAttribute.CODE.getName()) && feature.hasProperty(DefaultAttribute.TYPE.getName())) {
                Timber.e("Feature clicked %s", new Gson().toJson(feature));
                centerOnGeoObject(feature);

                if (feature.hasProperty(DefaultAttribute.TYPE.getName()) && feature.hasProperty(DefaultAttribute.UID.getName()) && feature.hasProperty(DefaultAttribute.CODE.getName())) {
                /*GenericAsyncTask genericAsyncTask = new GenericAsyncTask(new AsyncTaskCallable() {
                    @Override
                    public Object[] call() throws Exception {
                        return new Object[]{
                                client.getChildGeoObjects(feature.getStringProperty(DefaultAttribute.UID.getName())
                                , feature.getStringProperty(DefaultAttribute.TYPE.getName())
                                , new String[]{}
                                , false)
                        };
                    }
                });
                genericAsyncTask.setOnFinishedListener(new OnFinishedListener() {
                    @Override
                    public void onSuccess(Object[] objects) {
                        ChildTreeNode childGeoObjects = (ChildTreeNode) objects[0];

                        // Show the children on the map
                        List<ChildTreeNode> childTreeNodes = childGeoObjects.getChildren();
                        List<Feature> features = new ArrayList<>();

                        for (ChildTreeNode childTreeNode: childTreeNodes) {
                            features.add(Feature.fromJson(childTreeNode.getGeoObject().toJSON().toString()));
                        }

                        FeatureCollection featureCollection = FeatureCollection.fromFeatures(features);
                        locationsGeoJsonSource.setGeoJson(featureCollection);
                    }

                    @Override
                    public void onError(Exception e) {
                        Timber.e(e);
                    }
                });
                genericAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);*/

                    ChildTreeNode childTreeNode = null;

                    if (feature.getStringProperty(DefaultAttribute.UID.getName()).equals(currentDrillDown.getGeoObject().getUid())) {
                        childTreeNode = currentDrillDown;
                    } else {
                        for (ChildTreeNode childTreeNode1 : currentDrillDown.getChildren()) {
                            if (childTreeNode1.getGeoObject().getUid().equals(feature.getStringProperty(DefaultAttribute.UID.getName()))) {
                                currentDrillDown = childTreeNode1;
                                childTreeNode = childTreeNode1;
                            }
                        }
                    }

                    List<ChildTreeNode> childTreeNodes = childTreeNode.getChildren();

                    List<GeoObject> renderFeatures = new ArrayList<>();
                    List<Feature> listFeatures = new ArrayList<>();

                    for (ChildTreeNode childTreeNode1 : childTreeNodes) {
                        renderFeatures.add(childTreeNode1.getGeoObject());
                        listFeatures.add(Feature.fromJson(childTreeNode1.getGeoObject().toJSON().toString()));
                    }

                    if (listFeatures.size() > 0) {

                        FeatureCollection featureCollection = FeatureCollection.fromFeatures(listFeatures);
                        locationsGeoJsonSource.setGeoJson(featureCollection);
                        createLabelSource(renderFeatures.toArray(new GeoObject[]{}));
                    } else {
                        Toast.makeText(CGRIntegrationActivity.this, "This is the last hierarchy item", Toast.LENGTH_LONG)
                                .show();
                    }
                }
            }
        }
    }

    private void centerOnGeoObject(@NonNull GeoObject geoObject) {
        Point center = getCenter(geoObject);

        kujakuMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull MapboxMap mapboxMap) {
                mapboxMap.easeCamera(CameraUpdateFactory.newLatLngZoom(
                        new LatLng(center.getY()
                                , center.getX())
                        , ZOOM)
                );
            }
        });
    }

    private void centerOnGeoObject(@NonNull Feature geoObject) {
        com.mapbox.geojson.Point center = ArrowLineLayer.getCenter(geoObject.geometry());

        kujakuMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull MapboxMap mapboxMap) {
                double currentZoom = mapboxMap.getCameraPosition().zoom;

                mapboxMap.easeCamera(CameraUpdateFactory.newLatLngZoom(
                        new LatLng(center.latitude()
                                , center.longitude())
                        , currentZoom)
                );
            }
        });
    }

    private void createLabelSource(@NonNull GeoObject... geoObjects) {
        ArrayList<Feature> features = new ArrayList<>();

        for (GeoObject geoObject: geoObjects) {
            //Get the center
            Point center = geoObject.getGeometry().getCentroid();
            com.mapbox.geojson.Point point = com.mapbox.geojson.Point.fromLngLat(center.getX(), center.getY());

            Feature feature = Feature.fromGeometry(point);
            feature.addStringProperty("displayLabel", geoObject.getDisplayLabel().getValue());

            features.add(feature);
        }

        labelSource.setGeoJson(FeatureCollection.fromFeatures(features));
    }

    private Point getCenter(@NonNull GeoObject geoObject) {
        return geoObject.getGeometry().getCentroid();
    }
}