package io.ona.kujaku.layers;

import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;

/**
 *
 * Created by Ephraim Kigamba - ekigamba@ona.io on 08/02/2019
 */

public class ArrowLineLayer {

    private Builder builder;

    private ArrowLineLayer(@NonNull Builder builder) {
        this.builder = builder;
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
