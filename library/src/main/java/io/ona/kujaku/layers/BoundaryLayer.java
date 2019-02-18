package io.ona.kujaku.layers;

import android.graphics.Color;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;

import com.mapbox.geojson.FeatureCollection;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.style.expressions.Expression;
import com.mapbox.mapboxsdk.style.layers.Layer;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import java.util.ArrayList;
import java.util.UUID;

import static com.mapbox.mapboxsdk.style.layers.Property.NONE;
import static com.mapbox.mapboxsdk.style.layers.Property.VISIBLE;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.visibility;

/**
 * This layer enables one to add labelled foci boundaries to the {@link io.ona.kujaku.views.KujakuMapView}
 *
 * Sample usage
 *
 * Created by Ephraim Kigamba - ekigamba@ona.io on 18/02/2019
 */
public class BoundaryLayer implements KujakuLayer {

    private static final String TAG = BoundaryLayer.class.getName();
    private Builder builder;

    private String BOUNDARY_FEATURE_SOURCE_ID = UUID.randomUUID().toString();
    private String BOUNDARY_LINE_LAYER_ID = UUID.randomUUID().toString();
    private String BOUNDARY_LABEL_LAYER_ID = UUID.randomUUID().toString();

    private static final String ARROW_HEAD_ICON = "arrow-head-icon";

    private GeoJsonSource boundarySource;
    private LineLayer boundaryLineLayer;

    private SymbolLayer boundaryLabelLayer;
    private boolean visible = false;

    public static final int MIN_ARROW_ZOOM = 10;
    public static final int MAX_ARROW_ZOOM = 22;
    public static final float MIN_ZOOM_ARROW_HEAD_SCALE = 0.5f;
    public static final float MAX_ZOOM_ARROW_HEAD_SCALE = 1.0f;

    private BoundaryLayer(@NonNull Builder builder) {
        this.builder = builder;

        // Create the layers
        boundarySource = new GeoJsonSource(BOUNDARY_FEATURE_SOURCE_ID, builder.featureCollection);
        boundaryLineLayer = new LineLayer(BOUNDARY_LINE_LAYER_ID, BOUNDARY_FEATURE_SOURCE_ID)
                .withProperties(
                        PropertyFactory.lineJoin("round"),
                        PropertyFactory.lineWidth(builder.boundaryWidth),
                        PropertyFactory.lineColor(builder.boundaryColor)
                );

        boundaryLabelLayer = new SymbolLayer(BOUNDARY_LABEL_LAYER_ID, BOUNDARY_FEATURE_SOURCE_ID)
                .withProperties(
                        PropertyFactory.textField(Expression.toString(Expression.get(builder.labelProperty))),
                        PropertyFactory.textPadding(35f),
                        PropertyFactory.textRotationAlignment("map")
                );
    }

    @Override
    public void addLayerToMap(@NonNull MapboxMap mapboxMap) {
        if (mapboxMap.getLayer(BOUNDARY_LABEL_LAYER_ID) != null) {
            BOUNDARY_LABEL_LAYER_ID = UUID.randomUUID().toString();
        }

        if (mapboxMap.getSource(BOUNDARY_FEATURE_SOURCE_ID) != null) {
            BOUNDARY_FEATURE_SOURCE_ID = UUID.randomUUID().toString();
        }

        if (mapboxMap.getLayer(BOUNDARY_LINE_LAYER_ID) != null) {
            BOUNDARY_LINE_LAYER_ID = UUID.randomUUID().toString();
        }

        mapboxMap.addSource(boundarySource);

        if (builder.belowLayerId != null) {
            mapboxMap.addLayerBelow(boundaryLineLayer, builder.belowLayerId);
            mapboxMap.addLayerBelow(boundaryLabelLayer, builder.belowLayerId);
        } else {
            mapboxMap.addLayer(boundaryLineLayer);
            mapboxMap.addLayer(boundaryLabelLayer);
        }
    }

    @Override
    public void enableLayerOnMap(@NonNull MapboxMap mapboxMap) {
        ArrayList<Layer> layers = new ArrayList<Layer>();
        layers.add(mapboxMap.getLayerAs(BOUNDARY_LINE_LAYER_ID));
        layers.add(mapboxMap.getLayerAs(BOUNDARY_LABEL_LAYER_ID));

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
        layers.add(mapboxMap.getLayerAs(BOUNDARY_LINE_LAYER_ID));
        layers.add(mapboxMap.getLayerAs(BOUNDARY_LABEL_LAYER_ID));

        for (Layer layer: layers) {
            if (layer != null && VISIBLE.equals(layer.getVisibility().getValue())) {
                layer.setProperties(visibility(NONE));
                visible = false;
            }
        }
    }

    @Override
    public boolean isVisible() {
        return false;
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
        private String labelProperty = "name";

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
    }
}
