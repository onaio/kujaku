package io.ona.kujaku.layers;

import android.graphics.Color;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.mapbox.geojson.FeatureCollection;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.style.expressions.Expression;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 18/02/2019
 */
public abstract class KujakuLayer {

    private boolean isRemoved = false;
    protected boolean visible = false;

    /**
     * Adds the layer to a {@link MapboxMap}
     *
     * @param mapboxMap
     */
    public abstract void addLayerToMap(@NonNull MapboxMap mapboxMap);

    /**
     * Enables the layer on the {@link MapboxMap} in case it is not already
     * {@link com.mapbox.mapboxsdk.style.layers.Property#VISIBLE}. If the layer is not on the map,
     * nothing happens
     *
     * @param mapboxMap
     */
    public abstract void enableLayerOnMap(@NonNull MapboxMap mapboxMap);

    /**
     * Disables the layer if it is already on the {@link MapboxMap}
     *
     * @param mapboxMap
     */
    public abstract void disableLayerOnMap(@NonNull MapboxMap mapboxMap);

    /**
     * Used to retrieve the layer IDs used by this {@link KujakuLayer}
     *
     * @return an array of layer IDs
     */
    @NonNull
    public abstract String[] getLayerIds();

    public abstract boolean removeLayerOnMap(@NonNull MapboxMap mapboxMap);

    public abstract void updateFeatures(@NonNull FeatureCollection featureCollection);

    public abstract FeatureCollection getFeatureCollection() ;

    public boolean isRemoved() {
        return isRemoved;
    }

    public void setRemoved(boolean isRemoved) {
        this.isRemoved = isRemoved;
    }

    /**
     * Used to check if the layer has been enabled on the {@link MapboxMap}
     *
     * @return {@code true} if the layer is enabled, {@code false} otherwise
     */
    public boolean isVisible() {
        return visible;
    }

    /**
     * Abstract class for Builder
     * @param <T> Object to build
     * @param <B> Builder
     */
    public abstract static class Builder<T extends KujakuLayer, B extends Builder<T, B>> {

        protected FeatureCollection featureCollection;
        protected float boundaryWidth = 5;
        @ColorInt
        protected int boundaryColor = Color.WHITE;
        protected float labelTextSize;
        @ColorInt
        protected int labelColorInt = Color.BLACK;
        protected String belowLayerId;
        protected String labelProperty = "";
        protected Expression labelTextSizeExpression;

        public Builder(@NonNull FeatureCollection featureCollection) {
            this.featureCollection = featureCollection;
        }

        /**
         * The solution for the unchecked cast warning.
         */
        public abstract B getThis();

        public FeatureCollection getFeatureCollection() {
            return featureCollection;
        }

        public B setBoundaryWidth(float boundaryWidth) {
            this.boundaryWidth = boundaryWidth;
            return getThis();
        }

        public B setBoundaryColor(@ColorInt int boundaryColor) {
            this.boundaryColor = boundaryColor;
            return getThis();
        }

        public B setLabelTextSize(float labelTextSize) {
            this.labelTextSize = labelTextSize;
            return getThis();
        }

        public B setLabelColorInt(@ColorInt int labelColorInt) {
            this.labelColorInt = labelColorInt;
            return getThis();
        }

        public B addBelowLayer(@NonNull String belowLayerId) {
            this.belowLayerId = belowLayerId;
            return getThis();
        }

        public B setLabelProperty(@NonNull String labelProperty) {
            this.labelProperty = labelProperty;
            return getThis();
        }

        /**
         * The passed {@code labelTextSizeExpression} overrides any previously set label size using
         * {@link BoundaryLayer.Builder#setLabelTextSize(float)}. To remove this expression, pass a {@code null}
         * to this method so that the previously set label size is used. This method is availed because
         * the default label size maintains it's size irrespective of the zoom level
         *
         * @param labelTextSizeExpression
         * @return
         */
        public B setLabelTextSizeExpression(@Nullable Expression labelTextSizeExpression) {
            this.labelTextSizeExpression = labelTextSizeExpression;
            return getThis();
        }

        public abstract T build();
    }
}