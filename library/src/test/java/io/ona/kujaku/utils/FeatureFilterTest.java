package io.ona.kujaku.utils;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;

import junit.framework.Assert;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import io.ona.kujaku.BaseTest;
import io.ona.kujaku.utils.helpers.converters.GeoJSONFeature;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 14/02/2019
 */

public class FeatureFilterTest extends BaseTest {

    @Test
    public void filterShouldReturnOnlyFeaturesWithFilterProperty() {
        ArrayList<Feature> featuresList = new ArrayList<>();
        featuresList.add(generateRandomFeatureWithProperties(new GeoJSONFeature.Property("sample-string", "efgh")
                , new GeoJSONFeature.Property("task-status", "positive")
                , new GeoJSONFeature.Property("position", 0)));
        featuresList.add(generateRandomFeatureWithProperties(new GeoJSONFeature.Property("sample-string", "defg")
                , new GeoJSONFeature.Property("position", 1)));
        featuresList.add(generateRandomFeatureWithProperties(new GeoJSONFeature.Property("sample-string", "abcd")
                , new GeoJSONFeature.Property("position", 2)));
        featuresList.add(generateRandomFeatureWithProperties(new GeoJSONFeature.Property("sample-string", "cdef")
                , new GeoJSONFeature.Property("task-status", "positive")
                , new GeoJSONFeature.Property("position", 3)));
        featuresList.add(generateRandomFeatureWithProperties(new GeoJSONFeature.Property("sample-string", "bcde")
                , new GeoJSONFeature.Property("task-status", "positive")
                , new GeoJSONFeature.Property("position", 4)));
        featuresList.add(generateRandomFeatureWithProperties(new GeoJSONFeature.Property("sample-string", "bcde")
                , new GeoJSONFeature.Property("task-status", "positive")
                , new GeoJSONFeature.Property("position", 5)));
        featuresList.add(generateRandomFeatureWithProperties(new GeoJSONFeature.Property("task-status", "positive")
                , new GeoJSONFeature.Property("position", 6)));

        FeatureCollection actualFeatureCollection = new FeatureFilter.Builder(FeatureCollection.fromFeatures(featuresList))
                .whereRegex("sample-string", "[a-zA-z]*")
                .whereRegex("task-status", "[a-zA-Z]*")
                .build()
                .filter();

        List<Feature> actualFeatures = actualFeatureCollection.features();
        Assert.assertEquals(4, actualFeatures.size());
        Assert.assertEquals(0, (int) actualFeatures.get(0).getNumberProperty("position"));
        Assert.assertEquals(3, (int) actualFeatures.get(1).getNumberProperty("position"));
        Assert.assertEquals(4, (int) actualFeatures.get(2).getNumberProperty("position"));
        Assert.assertEquals(5, (int) actualFeatures.get(3).getNumberProperty("position"));
    }

    @Test
    public void filterShouldReturnAllFeaturesIfThereAreNoConditions() {
        int expectedFeaturesLen = 10;
        ArrayList<Feature> featuresList = new ArrayList<>();

        for (int i = 0; i < expectedFeaturesLen; i++) {
            featuresList.add(generateRandomFeatureWithProperties(new GeoJSONFeature.Property("sample-string", "efgh")
                    , new GeoJSONFeature.Property("task-status", "positive")
                    , new GeoJSONFeature.Property("position", 0)));
        }

        FeatureCollection actualFeatureCollection = new FeatureFilter.Builder(FeatureCollection.fromFeatures(featuresList))
                .build()
                .filter();

        Assert.assertEquals(expectedFeaturesLen, actualFeatureCollection.features().size());
    }

    @Test
    public void filterShouldReturnEmptyFeatureCollectionWhenGivenEmptyFeatureCollection() {
        int expectedFeaturesLen = 0;
        ArrayList<Feature> featuresList = new ArrayList<>();
        FeatureCollection actualFeatureCollection = new FeatureFilter.Builder(FeatureCollection.fromFeatures(featuresList))
                .build()
                .filter();

        Assert.assertEquals(expectedFeaturesLen, actualFeatureCollection.features().size());
    }

    @Test
    public void filterShouldChainConditions() {
        ArrayList<Feature> featuresList = new ArrayList<>();
        FeatureFilter.Builder builder = new FeatureFilter.Builder(FeatureCollection.fromFeatures(featuresList))
                .whereEq("property1", "value1")
                .whereRegex("property1", "value")
                .whereEq("property2", "value1");

        Assert.assertEquals(3, builder.getFilterConditions().size());
    }

    @Test
    public void filterShouldFilterWithChainedProperties() {
        ArrayList<Feature> featuresList = new ArrayList<>();
        featuresList.add(generateRandomFeatureWithProperties(new GeoJSONFeature.Property("sample-string", "efgh")
                , new GeoJSONFeature.Property("task-status", "positive")
                , new GeoJSONFeature.Property("position", 0)));
        featuresList.add(generateRandomFeatureWithProperties(new GeoJSONFeature.Property("sample-string", "defg")
                , new GeoJSONFeature.Property("position", 1)));
        featuresList.add(generateRandomFeatureWithProperties(new GeoJSONFeature.Property("sample-string", "abcd")
                , new GeoJSONFeature.Property("position", 2)));
        featuresList.add(generateRandomFeatureWithProperties(new GeoJSONFeature.Property("sample-string", "cdef")
                , new GeoJSONFeature.Property("task-status", "positive")
                , new GeoJSONFeature.Property("position", 3)));
        featuresList.add(generateRandomFeatureWithProperties(new GeoJSONFeature.Property("sample-string", "bcde")
                , new GeoJSONFeature.Property("task-status", "positive")
                , new GeoJSONFeature.Property("position", 4)));
        featuresList.add(generateRandomFeatureWithProperties(new GeoJSONFeature.Property("sample-string", "bcde")
                , new GeoJSONFeature.Property("task-status", "positive")
                , new GeoJSONFeature.Property("position", 5)));
        featuresList.add(generateRandomFeatureWithProperties(new GeoJSONFeature.Property("task-status", "positive")
                , new GeoJSONFeature.Property("position", 6)));

        FeatureCollection actualFeatureCollection = new FeatureFilter.Builder(FeatureCollection.fromFeatures(featuresList))
                .whereRegex("sample-string", ".*[abc]+.*")
                .whereEq("task-status", "positive")
                .build()
                .filter();

        List<Feature> actualFeatures = actualFeatureCollection.features();

        Assert.assertEquals(3, actualFeatures.size());
        Assert.assertEquals(3, (int) actualFeatures.get(0).getNumberProperty("position"));
        Assert.assertEquals(4, (int) actualFeatures.get(1).getNumberProperty("position"));
        Assert.assertEquals(5, (int) actualFeatures.get(2).getNumberProperty("position"));
    }
}