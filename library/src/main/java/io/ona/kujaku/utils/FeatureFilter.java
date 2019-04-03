package io.ona.kujaku.utils;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.ona.kujaku.comparisons.Comparison;
import io.ona.kujaku.comparisons.EqualToComparison;
import io.ona.kujaku.comparisons.RegexComparison;

/**
 * Provides the ability to filter locations or features before displaying them on the map. This filter
 * can only perform filter operations on {@link Feature} properties. The operations that can be performed
 * are {@code equalTo} and {@code regex} on {@code {@link String}}. Several conditions can be enforced
 * but only using the {@code and} operator.
 *
 * Example usage:
 * <code>
 *
 *     FeatureFilter.Builder builder = new FeatureFilter.Builder(myFeatureCollection)
 *                                          .whereEq("propertyName", "expectedPropertyValue")
 *                                          .whereEq("building-type", "commercial")
 *                                          .whereRegex("district", "(Rungwe|Kilombero|Kyela|Magu|Sikonge|Kasulu)");
 *     FeatureFilter featureFilter = builder.build();
 *     FeatureCollection filteredFeatureCollection = featureFilter.filter();
 * </code>
 *
 * Created by Ephraim Kigamba - ekigamba@ona.io on 14/02/2019
 */
public class FeatureFilter {

    private Builder builder;

    private FeatureFilter(@NonNull Builder builder) {
        this.builder = builder;
    }

    public FeatureCollection filter() {
        List<Feature> featuresList = builder.getFeatureCollection().features();
        ArrayList<Feature> filteredFeatures = new ArrayList<>();

        HashMap<String, Comparison> comparisons = new HashMap<>();

        EqualToComparison equalToComparison = new EqualToComparison();
        RegexComparison regexComparison = new RegexComparison();

        comparisons.put(equalToComparison.getFunctionName(), equalToComparison);
        comparisons.put(regexComparison.getFunctionName(), regexComparison);

        if (featuresList != null) {
            ArrayList<FilterCondition> filterConditions = builder.getFilterConditions();

            if (filterConditions.size() > 0) {
                for (Feature feature : featuresList) {
                    boolean skipFeature = false;
                    if (builder.getSortProperty() != null
                            && !feature.hasProperty(builder.getSortProperty())) {
                        continue;
                    }

                    for (FilterCondition filterCondition : filterConditions) {
                        String propertyName = filterCondition.getPropertyName();
                        if (feature.hasProperty(propertyName)) {
                            Comparison comparison = comparisons.get(filterCondition.getComparisionType());
                            if (comparison != null && !comparison.compare(
                                    feature.getStringProperty(propertyName),
                                    filterCondition.getValueType(),
                                    (String) filterCondition.getValue())) {
                                skipFeature = true;
                                break;
                            }
                        } else {
                            skipFeature = true;
                        }
                    }

                    if (!skipFeature) {
                        filteredFeatures.add(feature);
                    }
                }
            } else {
                filteredFeatures = new ArrayList<>(featuresList);
            }
        }

        return FeatureCollection.fromFeatures(filteredFeatures);
    }

    public FeatureCollection getFeatureCollection() {
        return builder.getFeatureCollection();
    }

    public static class Builder {

        private FeatureCollection featureCollection;
        private String sortProperty;
        private ArrayList<FilterCondition> filterConditions = new ArrayList<>();

        public Builder(@NonNull FeatureCollection featureCollection) {
            this.featureCollection = featureCollection;
        }

        public Builder setSortProperty(@Nullable String sortProperty) {
            this.sortProperty = sortProperty;
            return this;
        }

        public Builder whereEq(@NonNull String property, @NonNull String value) {
            filterConditions.add(new FilterCondition(EqualToComparison.COMPARISON_NAME, property, value, Comparison.TYPE_STRING));
            return this;
        }

        public Builder whereRegex(@NonNull String property, @NonNull String regexPattern) {
            filterConditions.add(new FilterCondition(RegexComparison.COMPARISON_NAME, property, regexPattern, Comparison.TYPE_STRING));
            return this;
        }

        public FeatureCollection getFeatureCollection() {
            return featureCollection;
        }

        public void setFeatureCollection(@NonNull FeatureCollection featureCollection) {
            this.featureCollection = featureCollection;
        }

        @Nullable
        public String getSortProperty() {
            return sortProperty;
        }

        public ArrayList<FilterCondition> getFilterConditions() {
            return filterConditions;
        }

        public FeatureFilter build() {
            return new FeatureFilter(this);
        }
    }

    public static class FilterCondition {
        private String comparisionType;
        private String propertyName;
        private Object value;
        private String valueType;

        public FilterCondition(@NonNull String comparisionType, @NonNull String propertyName, @NonNull Object value, @NonNull String valueType) {
            this.comparisionType = comparisionType;
            this.propertyName = propertyName;
            this.value = value;
            this.valueType = valueType;
        }

        public String getComparisionType() {
            return comparisionType;
        }

        public String getPropertyName() {
            return propertyName;
        }

        public Object getValue() {
            return value;
        }

        public String getValueType() {
            return valueType;
        }
    }
}
