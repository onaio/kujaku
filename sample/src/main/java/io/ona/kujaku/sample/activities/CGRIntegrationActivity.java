package io.ona.kujaku.sample.activities;

import static com.mapbox.mapboxsdk.style.expressions.Expression.eq;
import static com.mapbox.mapboxsdk.style.expressions.Expression.geometryType;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.gson.Gson;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Geometry;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.expressions.Expression;
import com.mapbox.mapboxsdk.style.layers.CircleLayer;
import com.mapbox.mapboxsdk.style.layers.FillLayer;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.turf.TurfMeasurement;
import com.vividsolutions.jts.geom.Point;

import org.commongeoregistry.adapter.android.AndroidHttpCredentialConnector;
import org.commongeoregistry.adapter.android.AndroidRegistryClient;
import org.commongeoregistry.adapter.constants.DefaultAttribute;
import org.commongeoregistry.adapter.dataaccess.ChildTreeNode;
import org.commongeoregistry.adapter.dataaccess.GeoObject;

import java.util.ArrayList;
import java.util.List;

import io.ona.kujaku.callables.AsyncTaskCallable;
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
    private GeoJsonSource labelSource;

    private GeoObject cambodiaCountry;
    private ChildTreeNode currentDrillDown;
    private ChildTreeNode cambodiaCountryDrillDown;

    private Button restartDrillDownBtn;

    private AndroidRegistryClient client;

    private String fillLayerId = "cgr-admin-boundaries";
    private String pointLayerId = "cgr-admin-location-points";

    private static final String LABEL_LAYER_ID = "label-layer-name";
    private static final String LABEL_SOURCE_ID = "label-source-id";

    private String[] adminHierarchy = new String[]{"Cambodia", "Province", "District", "Commune", "Village"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        kujakuMapView = findViewById(R.id.kmv_cgrIntegration_mapView);
        kujakuMapView.onCreate(savedInstanceState);

        restartDrillDownBtn = findViewById(R.id.btn_cgrIntegration_restartDrillDownBtn);

        kujakuMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull MapboxMap mapboxMap) {
                mapboxMap.setStyle(Style.SATELLITE);

                mapboxMap.getStyle(new Style.OnStyleLoaded() {
                    @Override
                    public void onStyleLoaded(@NonNull Style style) {
                        style.removeLayer("Thailand 2");
                        addAdministrativeLayer(style);

                        callSafeLoadCGR();
                    }
                });
            }
        });

        restartDrillDownBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentDrillDown = cambodiaCountryDrillDown;
                startDrillDown();
            }
        });
    }

    private void callSafeLoadCGR() {
        try {
            loadCGR();
        } catch (Exception e) {
            Timber.e(e);

            Snackbar snackbar = Snackbar.make(kujakuMapView, "Oops! An error occurred", Snackbar.LENGTH_INDEFINITE);
            snackbar.setAction(R.string.retry, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    callSafeLoadCGR();
                }
            });
            snackbar.show();
        }
    }

    private void startDrillDown() {
        locationsGeoJsonSource.setGeoJson(cambodiaCountry.toJSON().toString());
        centerOnBoundingBox(cambodiaCountry);
        createLabelSource(cambodiaCountry);
    }

    private void addAdministrativeLayer(@NonNull Style style) {
        String adminSource = "cgr-admin-boundary-source";

        FillLayer fillLayer = new FillLayer(fillLayerId, adminSource);
        fillLayer.withProperties(PropertyFactory.fillOpacity(0.75f)
                , PropertyFactory.fillColor("rgba(161, 202, 241, 0.75)")
                , PropertyFactory.fillOutlineColor(Color.BLACK));

        CircleLayer cityDotLayer = new CircleLayer(pointLayerId, adminSource);
        cityDotLayer.withFilter(eq(geometryType(), "Point"))
                .withProperties(PropertyFactory.circleColor(Color.BLACK)
                , PropertyFactory.circleRadius(5F)
                , PropertyFactory.circleStrokeColor(Color.WHITE)
                , PropertyFactory.circleStrokeWidth(1F));

        locationsGeoJsonSource = new GeoJsonSource(adminSource);
        SymbolLayer polygonAdminLayer = new SymbolLayer(LABEL_LAYER_ID, LABEL_SOURCE_ID)
                .withProperties(
                        PropertyFactory.textField(Expression.toString(Expression.get("displayLabel"))),
                        PropertyFactory.textSize(20f),
                        PropertyFactory.textColor(Color.BLACK),
                        PropertyFactory.textHaloColor(Color.WHITE),
                        PropertyFactory.textHaloWidth(1f),
                        PropertyFactory.textHaloBlur(.5f),
                        PropertyFactory.textAllowOverlap(true),
                        PropertyFactory.textOffset(new Float[]{0F, 1F})
                );

        labelSource = new GeoJsonSource(LABEL_SOURCE_ID);

        style.addSource(locationsGeoJsonSource);
        style.addSource(labelSource);
        style.addLayer(fillLayer);
        style.addLayer(cityDotLayer);
        style.addLayer(polygonAdminLayer);
    }

    private void loadCGR() {
        // Configure our connection with the remote registry server
        AndroidHttpCredentialConnector connector = new AndroidHttpCredentialConnector();
        connector.setCredentials(BuildConfig.CGR_USERNAME, BuildConfig.CGR_PASSWORD);
        connector.setServerUrl(BuildConfig.CGR_URL);
        connector.initialize();

        AlertDialog alertDialog = setProgressDialog();

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
                alertDialog.dismiss();
                restartDrillDownBtn.setEnabled(true);

                cambodiaCountry = (GeoObject) objects[0];
                currentDrillDown = (ChildTreeNode) objects[1];
                cambodiaCountryDrillDown = currentDrillDown;

                showToast(R.string.cgr_instructions);

                startDrillDown();

                // Set click listener
                kujakuMapView.setOnFeatureClickListener(CGRIntegrationActivity.this, fillLayerId);
            }

            @Override
            public void onError(Exception e) {
                Timber.e(e);
                alertDialog.dismiss();

                Snackbar snackbar = Snackbar.make(kujakuMapView, R.string.error_occurred_check_internet_connection, Snackbar.LENGTH_INDEFINITE);
                snackbar.setAction(R.string.retry, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        loadCGR();
                    }
                });
                snackbar.show();
            }
        });

        genericAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void showToast(@StringRes int stringRes) {
        Toast.makeText(CGRIntegrationActivity.this, stringRes, Toast.LENGTH_LONG)
                .show();
    }

    public AlertDialog setProgressDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setView(LayoutInflater.from(this).inflate(R.layout.dialog_progress, null));

        AlertDialog dialog = builder.create();
        dialog.show();

        return dialog;
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
        try {
            if (features.size() > 0) {
                final Feature feature = features.get(0);

                if (feature.hasProperty(DefaultAttribute.UID.getName()) && feature.hasProperty(DefaultAttribute.CODE.getName()) && feature.hasProperty(DefaultAttribute.TYPE.getName())) {
                    Timber.e("Feature clicked %s", new Gson().toJson(feature));

                    if (feature.hasProperty(DefaultAttribute.TYPE.getName()) && feature.hasProperty(DefaultAttribute.UID.getName()) && feature.hasProperty(DefaultAttribute.CODE.getName())) {
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

                        double[] bbox = null;

                        for (ChildTreeNode childTreeNode1 : childTreeNodes) {
                            GeoObject childGeoObject = childTreeNode1.getGeoObject();

                            if (childGeoObject.getGeometry() != null) {
                                renderFeatures.add(childGeoObject);
                                Feature mapboxFeature = Feature.fromJson(childTreeNode1.getGeoObject().toJSON().toString());

                                Geometry featureGeometry = mapboxFeature.geometry();
                                if (featureGeometry != null) {
                                    double[] featureBbox = TurfMeasurement.bbox(featureGeometry);

                                    if (bbox == null) {
                                        bbox = featureBbox;
                                    } else {
                                        if (featureBbox[0] < bbox[0]) {
                                            bbox[0] = featureBbox[0];
                                        }

                                        if (featureBbox[1] < bbox[1]) {
                                            bbox[1] = featureBbox[1];
                                        }

                                        if (featureBbox[2] > bbox[2]) {
                                            bbox[2] = featureBbox[2];
                                        }

                                        if (featureBbox[3] > bbox[3]) {
                                            bbox[3] = featureBbox[3];
                                        }
                                    }

                                    listFeatures.add(mapboxFeature);
                                }
                            }
                        }

                        if (listFeatures.size() > 0) {
                            if (bbox != null) {
                                centerOnBoundingBox(bbox);
                            }

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
        } catch (Exception e) {
            Timber.e(e);
            Snackbar snackbar = Snackbar.make(kujakuMapView, R.string.an_error_occurred_click_feature_again, Snackbar.LENGTH_INDEFINITE);
            snackbar.show();
        }
    }

    private void centerOnBoundingBox(@NonNull GeoObject geoObject) {
        kujakuMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull MapboxMap mapboxMap) {
                Geometry geometry = Feature.fromJson(geoObject.toJSON().toString()).geometry();
                if (geometry != null) {
                    double[] bbox = TurfMeasurement.bbox(geometry);
                    mapboxMap.easeCamera(CameraUpdateFactory.newLatLngBounds(
                            LatLngBounds.from(bbox[3], bbox[2], bbox[1], bbox[0]), 50)
                    );
                }
            }
        });
    }

    private void centerOnBoundingBox(@NonNull double[] bbox) {
        kujakuMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull MapboxMap mapboxMap) {
                mapboxMap.easeCamera(CameraUpdateFactory.newLatLngBounds(
                        LatLngBounds.from(bbox[3], bbox[2], bbox[1], bbox[0]), 50)
                );
            }
        });
    }

    private void createLabelSource(@NonNull GeoObject... geoObjects) {
        ArrayList<Feature> features = new ArrayList<>();

        for (GeoObject geoObject: geoObjects) {
            //Get the center
            com.vividsolutions.jts.geom.Geometry geometry = geoObject.getGeometry();
            if (geometry != null) {
                Point center = geometry.getCentroid();
                com.mapbox.geojson.Point point = com.mapbox.geojson.Point.fromLngLat(center.getX(), center.getY());

                Feature feature = Feature.fromGeometry(point);
                feature.addStringProperty("displayLabel", geoObject.getDisplayLabel().getValue());

                features.add(feature);
            }
        }

        labelSource.setGeoJson(FeatureCollection.fromFeatures(features));
    }
}