package io.ona.kujaku.layers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.graphics.drawable.DrawableCompat;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Geometry;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.MultiLineString;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.Layer;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.Property;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonOptions;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.turf.TurfMeasurement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import io.ona.kujaku.R;
import io.ona.kujaku.callables.AsyncTaskCallable;
import io.ona.kujaku.comparators.ArrowLineSortConfigComparator;
import io.ona.kujaku.exceptions.InvalidArrowLineConfigException;
import io.ona.kujaku.listeners.OnFinishedListener;
import io.ona.kujaku.tasks.GenericAsyncTask;
import io.ona.kujaku.utils.FeatureFilter;

import static com.mapbox.mapboxsdk.style.expressions.Expression.get;
import static com.mapbox.mapboxsdk.style.expressions.Expression.interpolate;
import static com.mapbox.mapboxsdk.style.expressions.Expression.linear;
import static com.mapbox.mapboxsdk.style.expressions.Expression.stop;
import static com.mapbox.mapboxsdk.style.expressions.Expression.zoom;
import static com.mapbox.mapboxsdk.style.layers.Property.ICON_ROTATION_ALIGNMENT_MAP;
import static com.mapbox.mapboxsdk.style.layers.Property.NONE;
import static com.mapbox.mapboxsdk.style.layers.Property.VISIBLE;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineCap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineJoin;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineWidth;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.visibility;

/**
 * Enables one to show a 1-to-1 or 1-to-many relationship between features. It's limits enables it
 * to show a many-to-many relationship by referencing the same child-case from multiple index-cases.
 * <p>
 * Sample code when creating a 1-to-1 relationship:
 * <p>
 * <p>
 * <code>
 * ArrowLineLayer.FeatureConfig featureConfig = new ArrowLineLayer.FeatureConfig(
 * new FeatureFilter.Builder(caseFeatureCollection1)
 * .whereEq("testStatus", "positive"));
 * <p>
 * ArrowLineLayer.SortConfig sortConfig = new ArrowLineLayer.SortConfig("dateTime"
 * , ArrowLineLayer.SortConfig.SortOrder.ASC
 * , ArrowLineLayer.SortConfig.PropertyType.DATE_TIME)
 * .setDateTimeFormat("yyyy-MM-dd HH:mm:ss");
 * <p>
 * <p>
 * arrowLineLayer = new ArrowLineLayer.Builder(this, featureConfig, sortConfig)
 * .setArrowLineColor(R.color.mapbox_blue)
 * .setArrowLineWidth(3)
 * .setAddBelowLayerId("sample-cases-symbol")
 * .build();
 * </code>
 * <p>
 * <p>
 * Sample code when creating a 1-to-many relationship:
 * <p>
 * <p>
 * <code>
 * ArrowLineLayer.FeatureConfig featureConfig = new ArrowLineLayer.FeatureConfig(
 * new FeatureFilter.Builder(caseFeatureCollection1)
 * .whereEq("testStatus", "positive"));
 * <p>
 * ArrowLineLayer.OneToManyConfig oneToManyConfig = new ArrowLineLayer.OneToManyConfig("childCases");
 * arrowLineLayer = new ArrowLineLayer.Builder(this, featureConfig, oneToManyConfig)
 * .setArrowLineColor(R.color.mapbox_blue)
 * .setArrowLineWidth(3)
 * .setAddBelowLayerId("sample-cases-symbol")
 * .build();
 * </code>
 * <p>
 * <p>
 * For the 1-to-many relationship, you need to have a property on the index case feature that defines
 * a string array of child cases. The string array is the id of each GeoJSON Feature that is a direct child
 * case of this index case
 * <p><p>
 * Created by Ephraim Kigamba - ekigamba@ona.io on 08/02/2019
 */

public class ArrowLineLayer extends KujakuLayer {

    private static final String TAG = ArrowLineLayer.class.getName();
    public static final String ARROW_HEAD_BEARING = "arrow-head-bearing";

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
    public static final float MIN_ZOOM_ARROW_HEAD_SCALE = 0.5f;
    public static final float MAX_ZOOM_ARROW_HEAD_SCALE = 1.0f;

    private ArrowLineLayer(@NonNull Builder builder) throws InvalidArrowLineConfigException {
        this.builder = builder;
        if (builder.sortConfig != null &&
                builder.sortConfig.getPropertyType() == SortConfig.PropertyType.DATE_TIME
                && TextUtils.isEmpty(builder.sortConfig.getDateTimeFormat())) {
            throw new InvalidArrowLineConfigException("Date time format for sort configuration on a DateTime property has not been set");
        }
    }

    private void createLineLayerSource() {
        lineLayerSource = new GeoJsonSource(LINE_LAYER_SOURCE_ID);
    }

    private void createArrowLineLayer(@NonNull Builder builder) {
        lineLayer = new LineLayer(LINE_LAYER_ID, LINE_LAYER_SOURCE_ID);
        lineLayer.withProperties(
                lineCap(Property.LINE_CAP_ROUND),
                lineJoin(Property.LINE_JOIN_ROUND),
                lineColor(builder.arrowLineColor)
        );

        if (builder.arrowLineWidth != 0f) {
            lineLayer.setProperties(lineWidth(builder.arrowLineWidth));
        }
    }

    private void createArrowHeadLayer() {
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
                PropertyFactory.iconRotationAlignment(ICON_ROTATION_ALIGNMENT_MAP),
                PropertyFactory.iconRotate(get(ARROW_HEAD_BEARING)),
                PropertyFactory.iconOpacity(1f)
        );
    }

    private void createArrowHeadSource() {
        arrowHeadSource = new GeoJsonSource(ARROW_HEAD_LAYER_SOURCE_ID
                , new GeoJsonOptions().withMaxZoom(16));
    }

    public LineLayer getLineLayer() {
        return lineLayer;
    }

    public SymbolLayer getArrowHeadLayer() {
        return arrowHeadLayer;
    }

    /**
     * Adds the layer to a {@link MapboxMap} after sorting the features, generating the
     * {@link LineString} to draw the arrow line and {@link FeatureCollection} to draw the arrow heads.
     *
     * @param mapboxMap
     */
    @Override
    public void addLayerToMap(@NonNull MapboxMap mapboxMap) {
        // Create arrow head source
        if (mapboxMap.getStyle().getSource(ARROW_HEAD_LAYER_SOURCE_ID) != null) {
            ARROW_HEAD_LAYER_SOURCE_ID = UUID.randomUUID().toString();
        }
        createArrowHeadSource();

        // Create the arrow line source
        if (mapboxMap.getStyle().getSource(LINE_LAYER_SOURCE_ID) != null) {
            LINE_LAYER_SOURCE_ID = UUID.randomUUID().toString();
        }
        createLineLayerSource();

        // Create the arrow head layer
        if (mapboxMap.getStyle().getLayer(ARROW_HEAD_LAYER_ID) != null) {
            ARROW_HEAD_LAYER_ID = UUID.randomUUID().toString();
        }
        createArrowHeadLayer();

        //Create the arrow line laye
        if (mapboxMap.getStyle().getLayer(LINE_LAYER_ID) != null) {
            LINE_LAYER_ID = UUID.randomUUID().toString();
        }
        createArrowLineLayer(builder);

        GenericAsyncTask genericAsyncTask = new GenericAsyncTask(new AsyncTaskCallable() {
            @Override
            public Object[] call() throws Exception {
                FeatureCollection filteredFeatureCollection = filterFeatures(builder.featureConfig, builder.sortConfig);
                FeatureCollection arrowHeadFeatures;

                if (builder.sortConfig != null) {
                    FeatureCollection sortedFeatureCollection = sortFeatures(filteredFeatureCollection, builder.sortConfig);
                    LineString arrowLine = calculateLineString(sortedFeatureCollection);
                    arrowHeadFeatures = generateArrowHeadFeatureCollection(arrowLine);

                    return new Object[]{arrowLine, arrowHeadFeatures};
                } else if (builder.oneToManyConfig != null) {
                    MultiLineString arrowLine = calculateMultiLineString(filteredFeatureCollection, builder.oneToManyConfig);
                    arrowHeadFeatures = generateArrowHeadFeatureCollection(arrowLine);

                    return new Object[]{arrowLine, arrowHeadFeatures};
                } else {
                    throw new IllegalArgumentException("SortConfig & OneToManyConfig not available to draw the line layer");
                }
            }
        });
        genericAsyncTask.setOnFinishedListener(new OnFinishedListener() {
            @Override
            public void onSuccess(Object[] objects) {
                Geometry arrowLine = (Geometry) objects[0];
                FeatureCollection arrowHeadFeatures = (FeatureCollection) objects[1];

                arrowHeadSource.setGeoJson(arrowHeadFeatures);
                lineLayerSource.setGeoJson(arrowLine);

                Drawable arrowHead = AppCompatResources.getDrawable(builder.context, R.drawable.ic_arrow_head);
                if (arrowHead == null) {
                    return;
                }

                Drawable head = DrawableCompat.wrap(arrowHead);
                DrawableCompat.setTint(head.mutate(), builder.arrowLineColor);
                Bitmap icon = getBitmapFromDrawable(head);

                mapboxMap.getStyle().addImage(ARROW_HEAD_ICON, icon);

                mapboxMap.getStyle().addSource(arrowHeadSource);
                mapboxMap.getStyle().addSource(lineLayerSource);

                if (builder.addBelowLayerId != null && mapboxMap.getStyle().getLayer(builder.addBelowLayerId) != null) {
                    mapboxMap.getStyle().addLayerBelow(lineLayer, builder.addBelowLayerId);
                    mapboxMap.getStyle().addLayerBelow(arrowHeadLayer, builder.addBelowLayerId);
                } else {
                    mapboxMap.getStyle().addLayer(lineLayer);
                    mapboxMap.getStyle().addLayer(arrowHeadLayer);
                }

                visible = true;
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, Log.getStackTraceString(e));
            }
        });

        genericAsyncTask.execute();
    }

    @Override
    public void enableLayerOnMap(@NonNull MapboxMap mapboxMap) {
        ArrayList<Layer> layers = new ArrayList<Layer>();
        layers.add(mapboxMap.getStyle().getLayerAs(LINE_LAYER_ID));
        layers.add(mapboxMap.getStyle().getLayerAs(ARROW_HEAD_LAYER_ID));

        for (Layer layer: layers) {
            if (layer != null && NONE.equals(layer.getVisibility().getValue())) {
                layer.setProperties(visibility(VISIBLE));
                visible = true;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void disableLayerOnMap(@NonNull MapboxMap mapboxMap) {
        ArrayList<Layer> layers = new ArrayList<Layer>();
        layers.add(mapboxMap.getStyle().getLayerAs(LINE_LAYER_ID));
        layers.add(mapboxMap.getStyle().getLayerAs(ARROW_HEAD_LAYER_ID));

        for (Layer layer: layers) {
            if (layer != null && VISIBLE.equals(layer.getVisibility().getValue())) {
                layer.setProperties(visibility(NONE));
                visible = false;
            }
        }
    }


    @Override @NonNull
    public String[] getLayerIds() {
        return new String[] {ARROW_HEAD_LAYER_ID, LINE_LAYER_ID};
    }

    @Override
    public boolean removeLayerOnMap(@NonNull MapboxMap mapboxMap) {
        setRemoved(true);

        // Remove the layers & sources
        Style style = mapboxMap.getStyle();
        if (style != null && style.isFullyLoaded()) {
            style.removeLayer(arrowHeadLayer);
            style.removeLayer(lineLayer);

            style.removeSource(arrowHeadSource);
            style.removeSource(lineLayerSource);

            visible = false;

            return true;
        } else {
            Log.e(TAG, "Could not remove the layers & source because the the style is null or not fully loaded");
            return false;
        }
    }

   @Override
    public void updateFeatures(@NonNull FeatureCollection featureCollection) {
        if (this.builder.featureConfig.featureCollection != null) {
            this.builder.featureConfig.featureCollection = featureCollection;
        }

        if (this.builder.featureConfig.featureFilterBuilder != null) {
            this.builder.featureConfig.featureFilterBuilder.setFeatureCollection(featureCollection);
        }

        if (lineLayer != null) {
            GenericAsyncTask genericAsyncTask = new GenericAsyncTask(new AsyncTaskCallable() {
                @Override
                public Object[] call() throws Exception {
                    FeatureCollection filteredFeatureCollection = filterFeatures(builder.featureConfig, builder.sortConfig);
                    FeatureCollection arrowHeadFeatures;

                    if (builder.sortConfig != null) {
                        FeatureCollection sortedFeatureCollection = sortFeatures(filteredFeatureCollection, builder.sortConfig);
                        LineString arrowLine = calculateLineString(sortedFeatureCollection);
                        arrowHeadFeatures = generateArrowHeadFeatureCollection(arrowLine);

                        return new Object[]{arrowLine, arrowHeadFeatures};
                    } else if (builder.oneToManyConfig != null) {
                        MultiLineString arrowLine = calculateMultiLineString(filteredFeatureCollection, builder.oneToManyConfig);
                        arrowHeadFeatures = generateArrowHeadFeatureCollection(arrowLine);

                        return new Object[]{arrowLine, arrowHeadFeatures};
                    } else {
                        throw new IllegalArgumentException("SortConfig & OneToManyConfig not available to draw the line layer");
                    }
                }
            });
            genericAsyncTask.setOnFinishedListener(new OnFinishedListener() {
                @Override
                public void onSuccess(Object[] objects) {
                    Geometry arrowLine = (Geometry) objects[0];
                    FeatureCollection arrowHeadFeatures = (FeatureCollection) objects[1];

                    arrowHeadSource.setGeoJson(arrowHeadFeatures);
                    lineLayerSource.setGeoJson(arrowLine);
                }

                @Override
                public void onError(Exception e) {
                    Log.e(TAG, Log.getStackTraceString(e));
                }
            });

            genericAsyncTask.execute();
        }
    }

    private Bitmap getBitmapFromDrawable(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        } else {
            Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(),
                    Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
            return bitmap;
        }
    }

    /**
     * Sorts the features using the {@link SortConfig} defined
     *
     * @param featureCollection
     * @param sortConfig
     * @return
     */
    private FeatureCollection sortFeatures(@NonNull FeatureCollection featureCollection, @NonNull SortConfig sortConfig) {
        List<Feature> featuresList = featureCollection.features();
        if (featuresList != null) {
            Collections.sort(featuresList, new ArrowLineSortConfigComparator(sortConfig));
        }

        return FeatureCollection.fromFeatures(featuresList);
    }

    private FeatureCollection filterFeatures(@NonNull FeatureConfig featureConfig, @Nullable SortConfig sortConfig) {
        if (featureConfig.getFeatureFilterBuilder() != null) {
            FeatureFilter.Builder featureFilterBuilder = featureConfig.getFeatureFilterBuilder();

            if (sortConfig != null) {
                featureFilterBuilder.setSortProperty(sortConfig.getSortProperty());
            }

            return featureFilterBuilder.build().filter();
        } else {
            return featureConfig.getFeatureCollection();
        }
    }

    /**
     * Generates a {@link FeatureCollection} which is a list of {@link Feature}s that represent the
     * midpoint between every two vertices. Each of the {@link Feature}s has a bearing property that
     * tells the bearing if one was moving from the first {@link Feature} to the second {@link Feature}.
     *
     * @param lineString the line string for which to generate arrow head features
     * @return a {@link FeatureCollection} which represents the locations of the arrow heads and bearing property
     */
    private FeatureCollection generateArrowHeadFeatureCollection(@NonNull LineString lineString) {
        ArrayList<Feature> featureList = new ArrayList<>();

        List<Point> lineStringPoints = lineString.coordinates();
        for (int i = 0; i < lineStringPoints.size() - 1; i++) {
            Point startPoint = lineStringPoints.get(i);
            Point endPoint = lineStringPoints.get(i + 1);

            Feature arrowHeadFeature = Feature.fromGeometry(TurfMeasurement.midpoint(startPoint, endPoint));
            arrowHeadFeature.addNumberProperty(ARROW_HEAD_BEARING, TurfMeasurement.bearing(startPoint, endPoint));

            featureList.add(arrowHeadFeature);
        }

        return FeatureCollection.fromFeatures(featureList);
    }


    /**
     * Generates a {@link FeatureCollection} which is a list of {@link Feature}s that represent the
     * midpoint between every two vertices. Each of the {@link Feature}s has a bearing property that
     * tells the bearing if one was moving from the first {@link Feature} to the second {@link Feature}.
     *
     * @param multiLineString the line string for which to generate arrow head features
     * @return a {@link FeatureCollection} which represents the locations of the arrow heads and bearing property
     */
    private FeatureCollection generateArrowHeadFeatureCollection(@NonNull MultiLineString multiLineString) {
        ArrayList<Feature> featureList = new ArrayList<>();

        List<LineString> lineStrings = multiLineString.lineStrings();
        for (int i = 0; i < lineStrings.size(); i++) {
            LineString lineString = lineStrings.get(i);

            Point startPoint = lineString.coordinates().get(0);
            Point endPoint = lineString.coordinates().get(1);

            Feature arrowHeadFeature = Feature.fromGeometry(TurfMeasurement.midpoint(startPoint, endPoint));
            arrowHeadFeature.addNumberProperty(ARROW_HEAD_BEARING, TurfMeasurement.bearing(startPoint, endPoint));

            featureList.add(arrowHeadFeature);
        }

        return FeatureCollection.fromFeatures(featureList);
    }

    /**
     * Calculates the center points from the polygons, multi-polygons and point features and generates
     * a {@link LineString} which will be used on the {@link LineLayer}
     *
     * @param featureCollection including Polygons and Multi-Polygons to convert to {@link LineString}
     * @return a {@link LineString} which can be used to draw a {@link LineLayer} on the map
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
     * Calculates the center points from the polygons, multi-polygons and point features and generates
     * a {@link MultiLineString} which will be used on the {@link LineLayer}
     *
     * @param featureCollection including Polygons and Multi-Polygons to convert to {@link MultiLineString}
     * @return a {@link MultiLineString} which can be used to draw a {@link LineLayer} on the map
     */
    private MultiLineString calculateMultiLineString(@NonNull FeatureCollection featureCollection, @NonNull OneToManyConfig oneToManyConfig) {
        ArrayList<LineString> centerPoints = new ArrayList<>();

        HashMap<String, Feature> featureMap = new HashMap<>();

        List<Feature> featureList = featureCollection.features();

        // Generate the center points and read the feature into the search data structure
        if (featureList != null) {
            for (Feature feature : featureList) {
                Geometry featureGeometry = feature.geometry();
                if (featureGeometry != null) {
                    Point featurePoint = null;
                    if (featureGeometry instanceof Point) {
                        Point originalFeaturePoint = (Point) featureGeometry;

                        // This prevents reference access problems
                        featurePoint = Point.fromLngLat(originalFeaturePoint.longitude(), originalFeaturePoint.latitude());
                    } else {
                        featurePoint = getCenter(featureGeometry);
                    }

                    String featureId = feature.id() != null ? feature.id() : UUID.randomUUID().toString();
                    JsonObject featureProperties = feature.properties();
                    Feature pointFeature = Feature.fromGeometry(featurePoint, featureProperties, featureId);

                    featureMap.put(featureId, pointFeature);
                }
            }
        }

        // Create the multi-lines between center points
        for (Feature feature: featureMap.values()) {
            JsonElement childCasesElement = feature.getProperty(oneToManyConfig.getChildrenDefinitionProperty());

            if (feature.hasProperty(oneToManyConfig.getChildrenDefinitionProperty()) && childCasesElement instanceof JsonArray) {
                JsonArray childCases = (JsonArray) feature.getProperty(oneToManyConfig.getChildrenDefinitionProperty());
                for (JsonElement childCase : childCases) {
                    if (childCase.isJsonPrimitive()) {
                        Feature childFeature = featureMap.get(childCase.getAsString());

                        if (childFeature != null) {
                            ArrayList<Point> points = new ArrayList<>();
                            points.add((Point) feature.geometry());
                            points.add((Point) childFeature.geometry());

                            centerPoints.add(LineString.fromLngLats(points));
                        }
                    }
                }
            }
        }

        return MultiLineString.fromLineStrings(centerPoints);
    }

    /**
     * Generates the center from the {@link Geometry} of a given {@link Feature} for {@link Geometry}
     * of types {@link com.mapbox.geojson.MultiPolygon}, {@link com.mapbox.geojson.Polygon} and
     * {@link com.mapbox.geojson.MultiPoint}
     *
     * @param featureGeometry
     * @return
     */
    public static Point getCenter(@NonNull Geometry featureGeometry) {
        double[] bbox = TurfMeasurement.bbox(featureGeometry);
        return Point.fromLngLat((bbox[2] + bbox[0]) / 2, (bbox[3] + bbox[1]) / 2);
    }

    @Override
    public FeatureCollection getFeatureCollection() {
        return this.builder.featureConfig.getFeatureCollection();
    }

    public static class Builder {

        private FeatureConfig featureConfig;
        @Nullable
        private SortConfig sortConfig;
        @Nullable
        private OneToManyConfig oneToManyConfig;
        private Context context;
        private String addBelowLayerId;

        @ColorInt
        private int arrowLineColor;
        private float arrowLineWidth = 3f;
        public Builder(@NonNull Context context, @NonNull FeatureConfig featureConfig, @NonNull SortConfig sortConfig) {
            this.featureConfig = featureConfig;
            this.sortConfig = sortConfig;
            this.context = context;

            setArrowLineColor(R.color.mapbox_blue);
        }

        public Builder(@NonNull Context context, @NonNull FeatureConfig featureConfig, @NonNull  OneToManyConfig oneToManyConfig) {
            this.featureConfig = featureConfig;
            this.oneToManyConfig = oneToManyConfig;
            this.context = context;

            setArrowLineColor(R.color.mapbox_blue);
        }

        public Builder setArrowLineColor(@ColorRes int colorInt) {
            this.arrowLineColor = context.getResources().getColor(colorInt);
            return this;
        }

        public Builder setArrowLineWidth(float arrowLineWidth) {
            this.arrowLineWidth = arrowLineWidth;
            return this;
        }

        public Builder setAddBelowLayerId(@NonNull String addBelowLayerId) {
            this.addBelowLayerId = addBelowLayerId;
            return this;
        }

        public ArrowLineLayer build() throws InvalidArrowLineConfigException {
            return new ArrowLineLayer(this);
        }
    }

    /**
     * It supports adding the {@link Feature}s for which a relationship is supposed to be shown.
     * <p>
     * This FeatureConfig class is supposed to support adding either adding:
     * - a {@link FeatureCollection}
     * - a list/array of layer-ids/source-ids from which the {@link Feature}s are supposed to be queried
     * - a Mapbox {@link com.mapbox.mapboxsdk.style.expressions.Expression} which defines the properties
     * of the features that we want.
     * <p>
     * but currently it only supports adding the {@link FeatureCollection}
     */
    public static class FeatureConfig {

        private FeatureCollection featureCollection;

        private FeatureFilter.Builder featureFilterBuilder;

        public FeatureConfig(@NonNull FeatureCollection featureCollection) {
            this.featureCollection = featureCollection;
        }

        public FeatureConfig(@NonNull FeatureFilter.Builder featureFilterBuilder) {
            this.featureFilterBuilder = featureFilterBuilder;
            this.featureCollection = featureFilterBuilder.getFeatureCollection();
        }

        public FeatureCollection getFeatureCollection() {
            return featureCollection;
        }

        @Nullable
        public FeatureFilter.Builder getFeatureFilterBuilder() {
            return featureFilterBuilder;
        }
    }

    /**
     * It supports adding the sorting configuration that is going to be used to link {@Link Feature}s
     * for which we are drawing an arrow line. It enables for a one-to-one relationship between {@link Feature}s
     */
    public static class SortConfig {

        public enum SortOrder {
            ASC,
            DESC
        }

        public enum PropertyType {
            DATE_TIME,
            STRING,
            NUMBER
        }

        private String sortProperty;
        private SortOrder sortOrder;
        private PropertyType propertyType;
        private String dateTimeFormat;

        public SortConfig(@NonNull String sortProperty, @NonNull SortOrder sortOrder, @NonNull PropertyType propertyType) {
            this.sortProperty = sortProperty;
            this.sortOrder = sortOrder;
            this.propertyType = propertyType;
        }

        /**
         * The dateTimeFormat should use the patterns as described in
         * <a href="https://developer.android.com/reference/java/time/format/DateTimeFormatter#patterns">this page</a>
         *
         * @param dateTimeFormat
         * @return
         */
        public SortConfig setDateTimeFormat(@NonNull String dateTimeFormat) {
            this.dateTimeFormat = dateTimeFormat;
            return this;
        }

        public String getSortProperty() {
            return sortProperty;
        }

        public SortOrder getSortOrder() {
            return sortOrder;
        }

        public PropertyType getPropertyType() {
            return propertyType;
        }

        public String getDateTimeFormat() {
            return dateTimeFormat;
        }
    }

    /**
     * It supports adding the one-to-many configuration that is going to be used to link {@Link Feature}s
     * for which we are drawing an arrow line. It enables for a one-to-many relationship between {@link Feature}s. You
     * define the property which contains the child-case IDs
     */
    public static class OneToManyConfig {

        private String childrenDefinitionProperty;

        public OneToManyConfig(@NonNull String childrenDefinitionProperty) {
            this.childrenDefinitionProperty = childrenDefinitionProperty;
        }

        public String getChildrenDefinitionProperty() {
            return childrenDefinitionProperty;
        }
    }
}
