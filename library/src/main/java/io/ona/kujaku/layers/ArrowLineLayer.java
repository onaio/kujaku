package io.ona.kujaku.layers;

import android.os.AsyncTask;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.util.Log;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Geometry;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.Property;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonOptions;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.turf.TurfMeasurement;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import io.ona.kujaku.callables.AsyncTaskCallable;
import io.ona.kujaku.listeners.OnFinishedListener;
import io.ona.kujaku.tasks.GenericAsyncTask;

import static com.mapbox.mapboxsdk.style.expressions.Expression.get;
import static com.mapbox.mapboxsdk.style.expressions.Expression.interpolate;
import static com.mapbox.mapboxsdk.style.expressions.Expression.linear;
import static com.mapbox.mapboxsdk.style.expressions.Expression.stop;
import static com.mapbox.mapboxsdk.style.expressions.Expression.zoom;
import static com.mapbox.mapboxsdk.style.layers.Property.ICON_ROTATION_ALIGNMENT_MAP;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineCap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineJoin;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineWidth;

/**
 *
 * Created by Ephraim Kigamba - ekigamba@ona.io on 08/02/2019
 */

public class ArrowLineLayer {

    private static final String TAG = ArrowLineLayer.class.getName();

    private Builder builder;

    private String LINE_LAYER_SOURCE_ID = UUID.randomUUID().toString();
    private String LINE_LAYER_ID = UUID.randomUUID().toString();

    private String ARROW_HEAD_LAYER_SOURCE_ID = UUID.randomUUID().toString();
    private String ARROW_HEAD_LAYER_ID = UUID.randomUUID().toString();

    private static final String ARROW_HEAD_ICON = "arrow-head-icon";

    private GeoJsonSource lineLayerSource;
    private LineLayer lineLayer;

    private GeoJsonSource arrowHeadSource;
    private SymbolLayer arrowHeadLayer;

    public static final int MIN_ARROW_ZOOM = 10;
    public static final int MAX_ARROW_ZOOM = 22;
    public static final float MIN_ZOOM_ARROW_HEAD_SCALE = 1.2f;
    public static final float MAX_ZOOM_ARROW_HEAD_SCALE = 1.8f;
    public static final Float[] ARROW_HEAD_OFFSET = {0f, -7f};
    public static final String ARROW_BEARING = "case-relationship-arrow-bearing";
    public static final float OPAQUE = 0.0f;
    public static final int ARROW_HIDDEN_ZOOM_LEVEL = 14;
    public static final float TRANSPARENT = 1.0f;

    private ArrowLineLayer(@NonNull Builder builder) {
        this.builder = builder;

        // Create arrow source
        arrowHeadSource = new GeoJsonSource(ARROW_HEAD_LAYER_SOURCE_ID
                ,new GeoJsonOptions().withMaxZoom(16));

        //Add arrow layer
        arrowHeadLayer = new SymbolLayer(ARROW_HEAD_LAYER_ID, ARROW_HEAD_LAYER_SOURCE_ID);
        arrowHeadLayer.withProperties(
                PropertyFactory.iconImage(ARROW_HEAD_ICON),
                iconAllowOverlap(true),
                iconIgnorePlacement(true),
                PropertyFactory.iconSize(interpolate(linear(), zoom(),
                        stop(MIN_ARROW_ZOOM, MIN_ZOOM_ARROW_HEAD_SCALE),
                        stop(MAX_ARROW_ZOOM, MAX_ZOOM_ARROW_HEAD_SCALE)
                        )
                ),
                PropertyFactory.iconOffset(ARROW_HEAD_OFFSET),
                PropertyFactory.iconRotationAlignment(ICON_ROTATION_ALIGNMENT_MAP),
                PropertyFactory.iconRotate(get(ARROW_BEARING)),
                PropertyFactory.iconOpacity(1f )
        );

        // Add a line layer
        lineLayer = new LineLayer(LINE_LAYER_ID, LINE_LAYER_SOURCE_ID);
        lineLayer.withProperties(
                lineCap(Property.LINE_CAP_ROUND),
                lineJoin(Property.LINE_JOIN_ROUND),
                lineWidth(builder.arrowLineWidth),
                lineColor(builder.arrowLineColor)
        );

        lineLayerSource = new GeoJsonSource(LINE_LAYER_SOURCE_ID);
    }

    public LineLayer getLineLayer() {
        return lineLayer;
    }

    public SymbolLayer getArrowHeadLayer() {
        return arrowHeadLayer;
    }

    public void addLayerToMap(@NonNull MapboxMap mapboxMap) {
        if (mapboxMap.getSource(ARROW_HEAD_LAYER_SOURCE_ID) != null) {
            ARROW_HEAD_LAYER_SOURCE_ID = UUID.randomUUID().toString();
        }

        if (mapboxMap.getLayer(ARROW_HEAD_LAYER_ID) != null) {
            ARROW_HEAD_LAYER_ID = UUID.randomUUID().toString();
        }

        if (mapboxMap.getSource(LINE_LAYER_SOURCE_ID) != null) {
            LINE_LAYER_SOURCE_ID = UUID.randomUUID().toString();
        }

        if (mapboxMap.getLayer(LINE_LAYER_ID) != null) {
            LINE_LAYER_ID = UUID.randomUUID().toString();
        }

        GenericAsyncTask genericAsyncTask = new GenericAsyncTask(new AsyncTaskCallable() {
            @Override
            public Object[] call() throws Exception {
                // TODO: Sort the feature collection
                // TODO: Generate the arrow features(the feature on which the arrows are going to be drawn)

                return new Object[]{calculateLineString(builder.featureConfig.featureCollection)};
            }
        });
        genericAsyncTask.setOnFinishedListener(new OnFinishedListener() {
            @Override
            public void onSuccess(Object[] objects) {
                //TODO: Add the GeoJSON Sources to the layer now
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, Log.getStackTraceString(e));
            }
        });

        genericAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    /**
     * Calculates the center points from the polygons, multi-polygons and point features and generates
     * a {@link LineString} which will be used on the {@link LineLayer}
     *
     * @param featureCollection
     * @return
     */
    private LineString calculateLineString(@NonNull FeatureCollection featureCollection) {
        ArrayList<Point> centerPoints = new ArrayList<>();

        List<Feature> featureList = featureCollection.features();
        if (featureList != null) {
            for (Feature feature : featureList) {
                Geometry featureGeometry = feature.geometry();
                if (featureGeometry != null) {
                    if (featureGeometry instanceof Point) {
                        Point featurePoint = (Point) featureGeometry;
                        centerPoints.add(Point.fromLngLat(featurePoint.longitude(), featurePoint.latitude()));
                    } else {
                        centerPoints.add(getCenter(featureGeometry));
                    }
                }
            }
        }

        return LineString.fromLngLats(centerPoints);
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
        return Point.fromLngLat((bbox[2] + bbox[0])/2, (bbox[3] + bbox[1])/2);
    }

    public static class Builder {

        private FeatureConfig featureConfig;
        private SortConfig sortConfig;

        private int arrowLineColor;
        private float arrowLineWidth;

        public Builder(@NonNull FeatureConfig featureConfig, @NonNull SortConfig sortConfig) {
            this.featureConfig = featureConfig;
            this.sortConfig = sortConfig;
        }

        public Builder setArrowLineColor(@ColorInt int colorInt) {
            this.arrowLineColor = colorInt;
            return this;
        }

        public Builder setArrowLineWidth(float arrowLineWidth) {
            this.arrowLineWidth = arrowLineWidth;
            return this;
        }

        public ArrowLineLayer build() {
            return new ArrowLineLayer(this);
        }
    }

    /**
     * It supports adding the {@link Feature}s for which a relationship is supposed to be shown.
     *
     * This FeatureConfig class is supposed to support adding either adding:
     * - a {@link FeatureCollection}
     * - a list/array of layer-ids/source-ids from which the {@link Feature}s are supposed to be queried
     * - a Mapbox {@link com.mapbox.mapboxsdk.style.expressions.Expression} which defines the properties
     * of the features that we want.
     *
     * but currently it only supports adding the {@link FeatureCollection}
     *
     */
    public static class FeatureConfig {

        private FeatureCollection featureCollection;

        public FeatureConfig(@NonNull FeatureCollection featureCollection) {
            this.featureCollection = featureCollection;
        }
    }

    /**
     * It supports adding the sorting configuration that is going to be used to link {@Link Feature}s
     * for which we are drawing an arrow line.
     */
    public static class SortConfig {

        public enum SortOrder {
            ASC,
            DESC
        }

        public enum PropertyType {
            DATE_TIME,
            STRING,
            NUMBERS
        }

        private String sortProperty;
        private SortOrder sortOrder;
        private PropertyType propertyType;

        public SortConfig(@NonNull String sortProperty, @NonNull SortOrder sortOrder, @NonNull PropertyType propertyType) {
            this.sortProperty = sortProperty;
            this.sortOrder = sortOrder;
            this.propertyType = propertyType;
        }
    }
}
