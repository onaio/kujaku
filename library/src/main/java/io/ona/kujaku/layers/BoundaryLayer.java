package io.ona.kujaku.layers;

import android.graphics.Color;
import android.os.AsyncTask;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.turf.TurfMeasurement;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.mapbox.mapboxsdk.style.layers.Property.NONE;
import static com.mapbox.mapboxsdk.style.layers.Property.VISIBLE;
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
public class BoundaryLayer implements KujakuLayer {

    private static final String TAG = BoundaryLayer.class.getName();

    private Builder builder;

    private String BOUNDARY_FEATURE_SOURCE_ID = UUID.randomUUID().toString();
    private String BOUNDARY_LABEL_SOURCE_ID = UUID.randomUUID().toString();
    private String BOUNDARY_LINE_LAYER_ID = UUID.randomUUID().toString();
    private String BOUNDARY_LABEL_LAYER_ID = UUID.randomUUID().toString();

    private GeoJsonSource boundarySource;
    private GeoJsonSource boundaryLabelsSource;
    private LineLayer boundaryLineLayer;

    private SymbolLayer boundaryLabelLayer;
    private boolean visible = false;
    private boolean isRemoved = false;

    private BoundaryLayer(@NonNull Builder builder) {
        this.builder = builder;
    }

    private void createBoundaryLabelLayer(@NonNull Builder builder) {
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

    private void createBoundaryLineLayer(@NonNull Builder builder) {
        boundaryLineLayer = new LineLayer(BOUNDARY_LINE_LAYER_ID, BOUNDARY_FEATURE_SOURCE_ID)
                .withProperties(
                        PropertyFactory.lineJoin("round"),
                        PropertyFactory.lineWidth(builder.boundaryWidth),
                        PropertyFactory.lineColor(builder.boundaryColor)
                );
    }

    private void createBoundaryLabelSource() {
        boundaryLabelsSource = new GeoJsonSource(BOUNDARY_LABEL_SOURCE_ID);
    }

    private void createBoundaryFeatureSource(@NonNull Builder builder) {
        boundarySource = new GeoJsonSource(BOUNDARY_FEATURE_SOURCE_ID, builder.featureCollection);
    }

    @Override
    public void addLayerToMap(@NonNull MapboxMap mapboxMap) {
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
                    mapboxMap.getStyle().addLayerBelow(boundaryLineLayer, builder.belowLayerId);
                    mapboxMap.getStyle().addLayerBelow(boundaryLabelLayer, builder.belowLayerId);
                } else {
                    mapboxMap.getStyle().addLayer(boundaryLineLayer);
                    mapboxMap.getStyle().addLayer(boundaryLabelLayer);
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

    @Override
    public void enableLayerOnMap(@NonNull MapboxMap mapboxMap) {
        ArrayList<Layer> layers = new ArrayList<Layer>();
        layers.add(mapboxMap.getStyle().getLayerAs(BOUNDARY_LINE_LAYER_ID));
        layers.add(mapboxMap.getStyle().getLayerAs(BOUNDARY_LABEL_LAYER_ID));

        for (Layer layer: layers) {
            if (layer != null && NONE.equals(layer.getVisibility().getValue())) {
                layer.setProperties(visibility(VISIBLE));
                visible = true;
            }
        }
    }

    @Override
    public void disableLayerOnMap(@NonNull MapboxMap mapboxMap) {
        ArrayList<Layer> layers = new ArrayList<Layer>();
        layers.add(mapboxMap.getStyle().getLayerAs(BOUNDARY_LINE_LAYER_ID));
        layers.add(mapboxMap.getStyle().getLayerAs(BOUNDARY_LABEL_LAYER_ID));

        for (Layer layer: layers) {
            if (layer != null && VISIBLE.equals(layer.getVisibility().getValue())) {
                layer.setProperties(visibility(NONE));
                visible = false;
            }
        }
    }

    @Override
    public boolean isVisible() {
        return visible;
    }

    @Override
    public String[] getLayerIds() {
        return new String[] {BOUNDARY_LABEL_LAYER_ID, BOUNDARY_LINE_LAYER_ID};
    }

    @Override
    public boolean removeLayerOnMap(@NonNull MapboxMap mapboxMap) {
        setRemoved(true);

        // Remove the layers & sources
        Style style = mapboxMap.getStyle();
        if (style != null && style.isFullyLoaded()) {
            style.removeLayer(boundaryLabelLayer);
            style.removeLayer(boundaryLineLayer);

            style.removeSource(boundarySource);
            style.removeSource(boundaryLabelsSource);

            return true;
        } else {
            Log.e(TAG, "Could not remove the layers & source because the the style is null or not fully loaded");
            return false;
        }
    }

    @Override
    public boolean isRemoved() {
        return isRemoved;
    }

    @Override
    public void setRemoved(boolean isRemoved) {
        this.isRemoved = isRemoved;
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

    public static class Builder {

        private FeatureCollection featureCollection;
        private float boundaryWidth = 5;
        @ColorInt
        private int boundaryColor = Color.WHITE;
        private float labelTextSize;
        @ColorInt
        private int labelColorInt = Color.BLACK;
        private String belowLayerId;
        private String labelProperty = "";
        private Expression labelTextSizeExpression;

        public Builder(@NonNull FeatureCollection featureCollection) {
            this.featureCollection = featureCollection;
        }

        public FeatureCollection getFeatureCollection() {
            return featureCollection;
        }

        public Builder setBoundaryWidth(float boundaryWidth) {
            this.boundaryWidth = boundaryWidth;
            return this;
        }

        public Builder setBoundaryColor(@ColorInt int boundaryColor) {
            this.boundaryColor = boundaryColor;
            return this;
        }

        public Builder setLabelTextSize(float labelTextSize) {
            this.labelTextSize = labelTextSize;
            return this;
        }

        public Builder setLabelColorInt(@ColorInt int labelColorInt) {
            this.labelColorInt = labelColorInt;
            return this;
        }

        public Builder addBelowLayer(@NonNull String belowLayerId) {
            this.belowLayerId = belowLayerId;
            return this;
        }

        public Builder setLabelProperty(@NonNull String labelProperty) {
            this.labelProperty = labelProperty;
            return this;
        }

        /**
         * The passed {@code labelTextSizeExpression} overrides any previously set label size using
         * {@link Builder#setLabelTextSize(float)}. To remove this expression, pass a {@code null}
         * to this method so that the previously set label size is used. This method is availed because
         * the default label size maintains it's size irrespective of the zoom level
         *
         * @param labelTextSizeExpression
         * @return
         */
        public Builder setLabelTextSizeExpression(@Nullable Expression labelTextSizeExpression) {
            this.labelTextSizeExpression = labelTextSizeExpression;
            return this;
        }

        public BoundaryLayer build() {
            return new BoundaryLayer(this);
        }
    }
}
