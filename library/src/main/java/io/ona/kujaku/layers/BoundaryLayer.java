package io.ona.kujaku.layers;


import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Geometry;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.expressions.Expression;
import com.mapbox.mapboxsdk.style.layers.Layer;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.Property;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.layers.PropertyValue;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.turf.TurfMeasurement;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.mapbox.mapboxsdk.style.layers.Property.NONE;
import static com.mapbox.mapboxsdk.style.layers.Property.VISIBLE;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.visibility;

import io.ona.kujaku.callables.AsyncTaskCallable;
import io.ona.kujaku.listeners.OnFinishedListener;
import io.ona.kujaku.tasks.GenericAsyncTask;

/**
 * This layer enables one to add labelled foci boundaries to the {@link io.ona.kujaku.views.KujakuMapView}
 * <p>
 * Sample usage:
 * <code>
 * <p>
 * BoundaryLayer.Builder builder = new BoundaryLayer.Builder(featureCollection)
 * .setLabelProperty("name")
 * .setLabelTextSize(20f)
 * .setLabelColorInt(Color.RED)
 * .setBoundaryColor(Color.RED)
 * .setBoundaryWidth(6f);
 * <p>
 * kujakuMapView.addLayer(builder.build());
 * </code>
 * <p>
 * Created by Ephraim Kigamba - ekigamba@ona.io on 18/02/2019
 */
public class BoundaryLayer extends KujakuLayer {

    private static final String TAG = BoundaryLayer.class.getName();

    protected KujakuLayer.Builder builder;

    protected String BOUNDARY_FEATURE_SOURCE_ID = UUID.randomUUID().toString();
    protected String BOUNDARY_LABEL_SOURCE_ID = UUID.randomUUID().toString();
    protected String BOUNDARY_LINE_LAYER_ID = UUID.randomUUID().toString();
    protected String BOUNDARY_LABEL_LAYER_ID = UUID.randomUUID().toString();

    private GeoJsonSource boundarySource;
    private GeoJsonSource boundaryLabelsSource;
    private LineLayer boundaryLineLayer;

    private SymbolLayer boundaryLabelLayer;

    BoundaryLayer(@NonNull KujakuLayer.Builder builder) {
        this.builder = builder;
    }

    private void createBoundaryLabelLayer(@NonNull KujakuLayer.Builder builder) {
        boundaryLabelLayer = new SymbolLayer(BOUNDARY_LABEL_LAYER_ID, BOUNDARY_LABEL_SOURCE_ID)
                .withProperties(
                        PropertyFactory.textField(Expression.toString(Expression.get(builder.labelProperty))),
                        PropertyFactory.textPadding(35f),
                        PropertyFactory.textColor(builder.labelColorInt),
                        PropertyFactory.textAllowOverlap(true)
                );

        if (builder.labelTextSize != 0f) {
            boundaryLabelLayer.setProperties(PropertyFactory.textSize(builder.labelTextSize));
        }

        if (builder.labelTextSizeExpression != null) {
            boundaryLabelLayer.setProperties(PropertyFactory.textSize(builder.labelTextSizeExpression));
        }
    }

    private void createBoundaryLineLayer(@NonNull KujakuLayer.Builder builder) {
        boundaryLineLayer = new LineLayer(BOUNDARY_LINE_LAYER_ID, BOUNDARY_FEATURE_SOURCE_ID)
                .withProperties(
                        PropertyFactory.lineJoin(Property.LINE_JOIN_ROUND),
                        PropertyFactory.lineWidth(builder.boundaryWidth),
                        PropertyFactory.lineColor(builder.boundaryColor)
                );

    }

    private void createBoundaryLabelSource() {
        boundaryLabelsSource = new GeoJsonSource(BOUNDARY_LABEL_SOURCE_ID);
    }

    private void createBoundaryFeatureSource(@NonNull KujakuLayer.Builder builder) {
        boundarySource = new GeoJsonSource(BOUNDARY_FEATURE_SOURCE_ID, builder.getFeatureCollection());
    }

    @Override
    public void  updateLineLayerProperties(@NonNull PropertyValue<?>... properties) {
        if (boundaryLineLayer != null) {
            boundaryLineLayer.setProperties(
                    properties
            );
        }
    }

    @Override
    public void addLayerToMap(@NonNull MapboxMap mapboxMap) {

        createLayers(mapboxMap);

        GenericAsyncTask genericAsyncTask = new GenericAsyncTask(new AsyncTaskCallable() {
            @Override
            public Object[] call() throws Exception {
                return new Object[]{calculateCenterPoints(builder.featureCollection)};
            }
        });

        genericAsyncTask.setOnFinishedListener(new OnFinishedListener() {
            @Override
            public void onSuccess(Object[] objects) {
                FeatureCollection boundaryCenterFeatures = (FeatureCollection) objects[0];

                boundaryLabelsSource.setGeoJson(boundaryCenterFeatures);

                mapboxMap.getStyle().addSource(boundaryLabelsSource);
                mapboxMap.getStyle().addSource(boundarySource);

                if (builder.belowLayerId != null) {
                    addLayersBelow(mapboxMap);
                } else {
                    addLayers(mapboxMap);
                }

                visible = true;
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, Log.getStackTraceString(e));
            }
        });

        genericAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    protected void createLayers(@NonNull MapboxMap mapboxMap) {
        // Create the sources
        if (mapboxMap.getStyle().getSource(BOUNDARY_LABEL_SOURCE_ID) != null) {
            BOUNDARY_LABEL_SOURCE_ID = UUID.randomUUID().toString();
        }
        createBoundaryLabelSource();

        if (mapboxMap.getStyle().getSource(BOUNDARY_FEATURE_SOURCE_ID) != null) {
            BOUNDARY_FEATURE_SOURCE_ID = UUID.randomUUID().toString();
        }
        createBoundaryFeatureSource(builder);

        // Create the layers
        if (mapboxMap.getStyle().getLayer(BOUNDARY_LABEL_LAYER_ID) != null) {
            BOUNDARY_LABEL_LAYER_ID = UUID.randomUUID().toString();
        }
        createBoundaryLabelLayer(builder);

        if (mapboxMap.getStyle().getLayer(BOUNDARY_LINE_LAYER_ID) != null) {
            BOUNDARY_LINE_LAYER_ID = UUID.randomUUID().toString();
        }
        createBoundaryLineLayer(builder);
    }

    protected void addLayersBelow(@NonNull MapboxMap mapboxMap) {
        mapboxMap.getStyle().addLayerBelow(boundaryLineLayer, builder.belowLayerId);
        mapboxMap.getStyle().addLayerBelow(boundaryLabelLayer, builder.belowLayerId);
    }

    protected void addLayers(@NonNull MapboxMap mapboxMap) {
        mapboxMap.getStyle().addLayer(boundaryLineLayer);
        mapboxMap.getStyle().addLayer(boundaryLabelLayer);
    }

    private FeatureCollection calculateCenterPoints(@NonNull FeatureCollection featureCollection) {
        ArrayList<Feature> centerPoints = new ArrayList<>();

        List<Feature> featureList = featureCollection.features();
        if (featureList != null) {
            for (Feature feature : featureList) {
                Geometry featureGeometry = feature.geometry();
                if (featureGeometry != null) {
                    Point featurePoint;
                    if (featureGeometry instanceof Point) {
                        featurePoint = (Point) featureGeometry;
                    } else {
                        featurePoint = getCenter(featureGeometry);
                    }

                    centerPoints.add(Feature.fromGeometry(featurePoint, feature.properties()));
                }
            }
        }

        return FeatureCollection.fromFeatures(centerPoints);
    }

    /**
     * Generates the center from the {@link Geometry} of a given {@link Feature} for {@link Geometry}
     * of types {@link com.mapbox.geojson.MultiPolygon}, {@link com.mapbox.geojson.Polygon} and
     * {@link com.mapbox.geojson.MultiPoint}
     *
     * @param featureGeometry
     * @return
     */
    private Point getCenter(@NonNull Geometry featureGeometry) {
        double[] bbox = TurfMeasurement.bbox(featureGeometry);

        LatLng centerLatLng  = LatLngBounds.from(bbox[3], bbox[2], bbox[1], bbox[0]).getCenter();
        return Point.fromLngLat(centerLatLng.getLongitude(), centerLatLng.getLatitude());
    }

    /**
     * Return a list of Layers
     * @param mapboxMap
     * @return
     */
    protected ArrayList<Layer> getLayers(@NonNull MapboxMap mapboxMap) {
        ArrayList<Layer> layers = new ArrayList<Layer>();
        layers.add(mapboxMap.getStyle().getLayerAs(BOUNDARY_LINE_LAYER_ID));
        layers.add(mapboxMap.getStyle().getLayerAs(BOUNDARY_LABEL_LAYER_ID));

        return layers;
    }

    @Override
    public void enableLayerOnMap(@NonNull MapboxMap mapboxMap) {
        for (Layer layer: getLayers(mapboxMap)) {
            if (layer != null && NONE.equals(layer.getVisibility().getValue())) {
                layer.setProperties(visibility(VISIBLE));
                visible = true;
            }
        }
    }

    @Override
    public void disableLayerOnMap(@NonNull MapboxMap mapboxMap) {
        for (Layer layer: getLayers(mapboxMap)) {
            if (layer != null && VISIBLE.equals(layer.getVisibility().getValue())) {
                layer.setProperties(visibility(NONE));
                visible = false;
            }
        }
    }

    @Override @NonNull
    public String[] getLayerIds() {
        return new String[] {BOUNDARY_LABEL_LAYER_ID, BOUNDARY_LINE_LAYER_ID};
    }

    @Override
    public boolean removeLayerOnMap(@NonNull MapboxMap mapboxMap) {
        setRemoved(true);

        // Remove the layers & sources
        Style style = mapboxMap.getStyle();
        if (style != null && style.isFullyLoaded()) {
            removeLayers(style) ;
            removeSources(style);

            return true;
        } else {
            Log.e(TAG, "Could not remove the layers & source because the the style is null or not fully loaded");
            return false;
        }
    }

    protected void removeLayers(@NonNull Style style) {
        style.removeLayer(boundaryLabelLayer);
        style.removeLayer(boundaryLineLayer);
    }

    protected void removeSources(@NonNull Style style) {
        style.removeSource(boundarySource);
        style.removeSource(boundaryLabelsSource);
    }

    @Override
    public void updateFeatures(@NonNull FeatureCollection featureCollection) {
        this.builder.featureCollection = featureCollection;

        if (boundaryLabelLayer != null) {
            GenericAsyncTask genericAsyncTask = new GenericAsyncTask(new AsyncTaskCallable() {
                @Override
                public Object[] call() throws Exception {
                    return new Object[]{calculateCenterPoints(builder.featureCollection)};
                }
            });

            genericAsyncTask.setOnFinishedListener(new OnFinishedListener() {
                @Override
                public void onSuccess(Object[] objects) {
                    FeatureCollection boundaryCenterFeatures = (FeatureCollection) objects[0];

                    boundaryLabelsSource.setGeoJson(boundaryCenterFeatures);
                    boundarySource.setGeoJson(featureCollection);
                }

                @Override
                public void onError(Exception e) {
                    Log.e(TAG, Log.getStackTraceString(e));
                }
            });

            genericAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    @Override
    public FeatureCollection getFeatureCollection() {
        return this.builder.getFeatureCollection();
    }

    public static class Builder extends KujakuLayer.Builder<BoundaryLayer, Builder> {

        public Builder(@NonNull FeatureCollection featureCollection) {
           super(featureCollection);
        }

        /** The solution for the unchecked cast warning. */
        public Builder getThis() {
            return this;
        }

        public BoundaryLayer build() {
            return new BoundaryLayer(this);
        }
    }
}
