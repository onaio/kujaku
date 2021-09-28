package io.ona.kujaku.layers;

import android.content.Context;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Geometry;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.MultiLineString;
import com.mapbox.geojson.MultiPolygon;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.PropertyValue;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.mapboxsdk.utils.ColorUtils;
import com.mapbox.mapboxsdk.utils.ThreadUtils;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.ReflectionHelpers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import io.ona.kujaku.exceptions.InvalidArrowLineConfigException;
import io.ona.kujaku.test.shadows.ShadowLayer;
import io.ona.kujaku.test.shadows.ShadowLineLayer;
import io.ona.kujaku.utils.FeatureFilter;
import io.ona.kujaku.utils.helpers.converters.GeoJSONFeature;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 11/02/2019
 */
public class ArrowLineLayerTest extends BaseKujakuLayerTest {

    private Context context;

    @Before
    public void setUp() {
        context = RuntimeEnvironment.application;
        ThreadUtils.init(context);
    }

    @Test
    public void getCenterWhenGivenPolygonFeature() throws InvalidArrowLineConfigException {
        ArrayList<Point> pointsList = new ArrayList<>();

        pointsList.add(Point.fromLngLat(9.1d, 9.1d));
        pointsList.add(Point.fromLngLat(11.1d, 9.1d));
        pointsList.add(Point.fromLngLat(11.1d, 2.1d));
        pointsList.add(Point.fromLngLat(9.1d, 2.1d));

        ArrayList<List<Point>> pointsList2 = new ArrayList<>();
        pointsList2.add(pointsList);

        Polygon polygon = Polygon.fromLngLats(pointsList2);

        ArrowLineLayer.FeatureConfig featureConfig = new ArrowLineLayer.FeatureConfig(
                FeatureCollection.fromFeature(Feature.fromGeometry(LineString.fromLngLats(pointsList)))
        );
        ArrowLineLayer.SortConfig sortConfig = new ArrowLineLayer.SortConfig("", ArrowLineLayer.SortConfig.SortOrder.DESC, ArrowLineLayer.SortConfig.PropertyType.NUMBER);

        ArrowLineLayer.Builder builder = new ArrowLineLayer.Builder(context, featureConfig, sortConfig);
        ArrowLineLayer arrowLineLayer = builder.build();

        Point center = ReflectionHelpers.callInstanceMethod(arrowLineLayer
                , "getCenter"
                , ReflectionHelpers.ClassParameter.from(Geometry.class, polygon)
        );

        Point expectedPoint = Point.fromLngLat(10.1d, 5.6d);
        assertPointEquals(expectedPoint, center);
    }

    @Test
    public void getCenterWhenGivenPointFeature() throws InvalidArrowLineConfigException {
        Point pointGeometry = Point.fromLngLat(9.1d, 9.1d);
        ArrowLineLayer.FeatureConfig featureConfig = new ArrowLineLayer.FeatureConfig(
                FeatureCollection.fromFeature(Feature.fromGeometry(pointGeometry))
        );
        ArrowLineLayer.SortConfig sortConfig = new ArrowLineLayer.SortConfig(""
                , ArrowLineLayer.SortConfig.SortOrder.DESC
                , ArrowLineLayer.SortConfig.PropertyType.NUMBER);

        ArrowLineLayer.Builder builder = new ArrowLineLayer.Builder(context, featureConfig, sortConfig);
        ArrowLineLayer arrowLineLayer = builder.build();

        Point center = ReflectionHelpers.callInstanceMethod(arrowLineLayer
                , "getCenter"
                , ReflectionHelpers.ClassParameter.from(Geometry.class, pointGeometry)
        );

        assertPointEquals(pointGeometry, center);
    }

    @Test
    public void getCenterWhenGivenPolygonWithHolesFeature() throws InvalidArrowLineConfigException {
        ArrayList<Point> outerBoundary = new ArrayList<>();

        outerBoundary.add(Point.fromLngLat(9.1d, 9.1d));
        outerBoundary.add(Point.fromLngLat(11.1d, 9.1d));
        outerBoundary.add(Point.fromLngLat(11.1d, 2.1d));
        outerBoundary.add(Point.fromLngLat(9.1d, 2.1d));

        ArrayList<List<Point>> pointsListUpper1 = new ArrayList<>();
        pointsListUpper1.add(outerBoundary);

        ArrayList<Point> innerBoundary = new ArrayList<>();

        innerBoundary.add(Point.fromLngLat(10d, 8d));
        innerBoundary.add(Point.fromLngLat(11d, 8d));
        innerBoundary.add(Point.fromLngLat(11d, 5d));
        innerBoundary.add(Point.fromLngLat(10d, 5d));

        pointsListUpper1.add(innerBoundary);

        Polygon polygon = Polygon.fromLngLats(pointsListUpper1);

        ArrowLineLayer.FeatureConfig featureConfig = new ArrowLineLayer.FeatureConfig(
                FeatureCollection.fromFeature(Feature.fromGeometry(LineString.fromLngLats(outerBoundary)))
        );
        ArrowLineLayer.SortConfig sortConfig = new ArrowLineLayer.SortConfig("", ArrowLineLayer.SortConfig.SortOrder.DESC, ArrowLineLayer.SortConfig.PropertyType.NUMBER);

        ArrowLineLayer.Builder builder = new ArrowLineLayer.Builder(context, featureConfig, sortConfig);
        ArrowLineLayer arrowLineLayer = builder.build();

        Point center = ReflectionHelpers.callInstanceMethod(arrowLineLayer
                , "getCenter"
                , ReflectionHelpers.ClassParameter.from(Geometry.class, polygon)
        );

        Point expectedPoint = Point.fromLngLat(10.1d, 5.6d);
        assertPointEquals(expectedPoint, center);
    }

    @Test
    public void getCenterWhenGivenMultiPolygonFeature() throws InvalidArrowLineConfigException {
        ArrayList<Point> pointsListLower1 = new ArrayList<>();

        pointsListLower1.add(Point.fromLngLat(9.1d, 9.1d));
        pointsListLower1.add(Point.fromLngLat(11.1d, 9.1d));
        pointsListLower1.add(Point.fromLngLat(11.1d, 2.1d));
        pointsListLower1.add(Point.fromLngLat(9.1d, 2.1d));

        ArrayList<List<Point>> pointsListUpper1 = new ArrayList<>();
        pointsListUpper1.add(pointsListLower1);

        ArrayList<Point> pointsListLower2 = new ArrayList<>();

        pointsListLower2.add(Point.fromLngLat(5d, 8d));
        pointsListLower2.add(Point.fromLngLat(8d, 8d));
        pointsListLower2.add(Point.fromLngLat(8d, 5d));
        pointsListLower2.add(Point.fromLngLat(5d, 5d));

        ArrayList<List<Point>> pointsListUpper2 = new ArrayList<>();
        pointsListUpper2.add(pointsListLower2);

        ArrayList<List<List<Point>>> multiPolygonPoints = new ArrayList<>();
        multiPolygonPoints.add(pointsListUpper1);
        multiPolygonPoints.add(pointsListUpper2);

        MultiPolygon polygon = MultiPolygon.fromLngLats(multiPolygonPoints);
        ArrowLineLayer.FeatureConfig featureConfig = new ArrowLineLayer.FeatureConfig(
                FeatureCollection.fromFeature(Feature.fromGeometry(LineString.fromLngLats(pointsListLower1)))
        );
        ArrowLineLayer.SortConfig sortConfig = new ArrowLineLayer.SortConfig("", ArrowLineLayer.SortConfig.SortOrder.DESC, ArrowLineLayer.SortConfig.PropertyType.NUMBER);

        ArrowLineLayer.Builder builder = new ArrowLineLayer.Builder(context, featureConfig, sortConfig);
        ArrowLineLayer arrowLineLayer = builder.build();

        Point center = ReflectionHelpers.callInstanceMethod(arrowLineLayer
                , "getCenter"
                , ReflectionHelpers.ClassParameter.from(Geometry.class, polygon)
        );

        Point expectedPoint = Point.fromLngLat(8.05d, 5.6d);
        assertPointEquals(expectedPoint, center);
    }

    @Test
    public void calculateLineStringWhenGivenFeatureCollectionWithFeatures() throws InvalidArrowLineConfigException {
        ArrayList<Feature> featuresList = new ArrayList<>();

        ArrayList<Point> pointsListLower1 = new ArrayList<>();

        pointsListLower1.add(Point.fromLngLat(5d, 8d));
        pointsListLower1.add(Point.fromLngLat(8d, 8d));
        pointsListLower1.add(Point.fromLngLat(8d, 5d));
        pointsListLower1.add(Point.fromLngLat(5d, 5d));

        ArrayList<List<Point>> pointsListUpper1 = new ArrayList<>();
        pointsListUpper1.add(pointsListLower1);

        featuresList.add(Feature.fromGeometry(Polygon.fromLngLats(pointsListUpper1)));
        featuresList.add(Feature.fromGeometry(Point.fromLngLat(9.1d, 9.1d)));
        featuresList.add(Feature.fromGeometry(Point.fromLngLat(11.1d, 9.1d)));
        featuresList.add(Feature.fromGeometry(Point.fromLngLat(11.1d, 2.1d)));
        featuresList.add(Feature.fromGeometry(Point.fromLngLat(9.1d, 2.1d)));

        FeatureCollection featureCollection = FeatureCollection.fromFeatures(featuresList);

        ArrowLineLayer.FeatureConfig featureConfig = new ArrowLineLayer.FeatureConfig(featureCollection);
        ArrowLineLayer.SortConfig sortConfig = new ArrowLineLayer.SortConfig("", ArrowLineLayer.SortConfig.SortOrder.DESC, ArrowLineLayer.SortConfig.PropertyType.NUMBER);

        ArrowLineLayer.Builder builder = new ArrowLineLayer.Builder(context, featureConfig, sortConfig);
        ArrowLineLayer arrowLineLayer = builder.build();

        LineString lineString = ReflectionHelpers.callInstanceMethod(arrowLineLayer
                , "calculateLineString"
                , ReflectionHelpers.ClassParameter.from(FeatureCollection.class, featureCollection)
        );

        assertEquals(5, lineString.coordinates().size());
        Point point1 = lineString.coordinates().get(0);
        Point point2 = lineString.coordinates().get(1);

        assertPointEquals(Point.fromLngLat(6.5d, 6.5d), point1);
        assertPointEquals(Point.fromLngLat(9.1d, 9.1d), point2);
    }

    @Test
    public void calculateMultiLineStringWhenGivenFeatureCollectionWithFeatures() throws InvalidArrowLineConfigException {
        ArrayList<Feature> featuresList = new ArrayList<>();

        ArrayList<Point> pointsListLower1 = new ArrayList<>();

        pointsListLower1.add(Point.fromLngLat(5d, 8d));
        pointsListLower1.add(Point.fromLngLat(8d, 8d));
        pointsListLower1.add(Point.fromLngLat(8d, 5d));
        pointsListLower1.add(Point.fromLngLat(5d, 5d));

        ArrayList<List<Point>> pointsListUpper1 = new ArrayList<>();
        pointsListUpper1.add(pointsListLower1);

        String feature1Id = UUID.randomUUID().toString();
        String feature2Id = UUID.randomUUID().toString();
        String feature3Id = UUID.randomUUID().toString();
        String feature4Id = UUID.randomUUID().toString();
        String feature5Id = UUID.randomUUID().toString();

        JsonArray jsonArrayIds = new JsonArray();
        jsonArrayIds.add(feature2Id);
        jsonArrayIds.add(feature3Id);

        JsonObject feature1Properties = new JsonObject();
        feature1Properties.add("childCases", jsonArrayIds);

        JsonObject feature3Properties = new JsonObject();
        JsonArray jsonArrayIdsFeature3 = new JsonArray();
        jsonArrayIdsFeature3.add(feature5Id);
        feature3Properties.add("childCases", jsonArrayIdsFeature3);

        featuresList.add(Feature.fromGeometry(Polygon.fromLngLats(pointsListUpper1), feature1Properties, feature1Id));
        featuresList.add(Feature.fromGeometry(Point.fromLngLat(9.1d, 9.1d), null, feature2Id));
        featuresList.add(Feature.fromGeometry(Point.fromLngLat(11.1d, 9.1d), feature3Properties, feature3Id));
        featuresList.add(Feature.fromGeometry(Point.fromLngLat(11.1d, 2.1d), null, feature4Id));
        featuresList.add(Feature.fromGeometry(Point.fromLngLat(9.1d, 2.1d), null, feature5Id));

        FeatureCollection featureCollection = FeatureCollection.fromFeatures(featuresList);

        ArrowLineLayer.FeatureConfig featureConfig = new ArrowLineLayer.FeatureConfig(featureCollection);
        ArrowLineLayer.OneToManyConfig oneToManyConfig = new ArrowLineLayer.OneToManyConfig("childCases");

        ArrowLineLayer.Builder builder = new ArrowLineLayer.Builder(context, featureConfig, oneToManyConfig);
        ArrowLineLayer arrowLineLayer = builder.build();

        MultiLineString multiLineString = ReflectionHelpers.callInstanceMethod(arrowLineLayer
                , "calculateMultiLineString"
                , ReflectionHelpers.ClassParameter.from(FeatureCollection.class, featureCollection)
                , ReflectionHelpers.ClassParameter.from(ArrowLineLayer.OneToManyConfig.class, oneToManyConfig)
        );

        assertEquals(3, multiLineString.coordinates().size());

        boolean firstPointFound = false;
        boolean secondPointFound = false;
        boolean thirdPointFound = false;

        for (List<Point> lineString: multiLineString.coordinates()) {
            if (!firstPointFound && lineString.get(0).latitude() == 6.5d && lineString.get(0).longitude() == 6.5d) {
                firstPointFound = true;
            }

            if (!secondPointFound && lineString.get(0).latitude() == 9.1d && lineString.get(0).longitude() == 11.1d) {
                secondPointFound = true;
            }

            if (!thirdPointFound && lineString.get(1).latitude() == 2.1d && lineString.get(1).longitude() == 9.1d) {
                thirdPointFound = true;
            }
        }

        assertTrue(firstPointFound);
        assertTrue(secondPointFound);
        assertTrue(thirdPointFound);
    }

    @Test
    public void calculateLineStringWhenGivenFeatureCollectionWithNoFeatures() throws InvalidArrowLineConfigException {
        ArrayList<Feature> featuresList = new ArrayList<>();
        FeatureCollection featureCollection = FeatureCollection.fromFeatures(featuresList);

        ArrowLineLayer.FeatureConfig featureConfig = new ArrowLineLayer.FeatureConfig(featureCollection);
        ArrowLineLayer.SortConfig sortConfig = new ArrowLineLayer.SortConfig("", ArrowLineLayer.SortConfig.SortOrder.DESC, ArrowLineLayer.SortConfig.PropertyType.NUMBER);

        ArrowLineLayer.Builder builder = new ArrowLineLayer.Builder(context, featureConfig, sortConfig);
        ArrowLineLayer arrowLineLayer = builder.build();

        LineString lineString = ReflectionHelpers.callInstanceMethod(arrowLineLayer
                , "calculateLineString"
                , ReflectionHelpers.ClassParameter.from(FeatureCollection.class, featureCollection)
        );

        assertEquals(0, lineString.coordinates().size());
    }
    
    @Test
    public void generateArrowHeadFeatureCollection() throws InvalidArrowLineConfigException {
        ArrayList<Point> pointsList = new ArrayList<>();
        
        pointsList.add(Point.fromLngLat(9.1d, 9.1d));
        pointsList.add(Point.fromLngLat(11.1d, 9.1d));
        pointsList.add(Point.fromLngLat(11.1d, 2.1d));
        pointsList.add(Point.fromLngLat(9.1d, 2.1d));

        LineString lineString = LineString.fromLngLats(pointsList);

        ArrowLineLayer.FeatureConfig featureConfig = new ArrowLineLayer.FeatureConfig(
                FeatureCollection.fromFeature(Feature.fromGeometry(lineString))
        );
        ArrowLineLayer.SortConfig sortConfig = new ArrowLineLayer.SortConfig("", ArrowLineLayer.SortConfig.SortOrder.DESC, ArrowLineLayer.SortConfig.PropertyType.NUMBER);

        ArrowLineLayer.Builder builder = new ArrowLineLayer.Builder(context, featureConfig, sortConfig);
        ArrowLineLayer arrowLineLayer = builder.build();

        FeatureCollection arrowHeadFeatureCollection = ReflectionHelpers.callInstanceMethod(arrowLineLayer
                , "generateArrowHeadFeatureCollection"
                , ReflectionHelpers.ClassParameter.from(LineString.class, lineString)
        );

        assertEquals(3, arrowHeadFeatureCollection.features().size());
        Feature arrowHeadFeature = arrowHeadFeatureCollection.features().get(0);

        assertTrue(arrowHeadFeature.hasProperty("arrow-head-bearing"));
        assertTrue(arrowHeadFeature.hasNonNullValueForProperty("arrow-head-bearing"));

        Point expectedPoint = Point.fromLngLat(10.1d, 9.101363d);
        Point actual = (Point) arrowHeadFeatureCollection.features().get(0).geometry();
        assertEquals(expectedPoint.latitude(), actual.latitude(), 0.0000001d);
        assertEquals(expectedPoint.longitude(), actual.longitude(), 0d);
    }

    @Test
    public void setLayerPropertiesFromBuilder() throws InvalidArrowLineConfigException {
        ArrayList<Feature> featuresList = new ArrayList<>();
        FeatureCollection featureCollection = FeatureCollection.fromFeatures(featuresList);

        ArrowLineLayer.FeatureConfig featureConfig = new ArrowLineLayer.FeatureConfig(featureCollection);
        ArrowLineLayer.SortConfig sortConfig = new ArrowLineLayer.SortConfig("", ArrowLineLayer.SortConfig.SortOrder.DESC, ArrowLineLayer.SortConfig.PropertyType.NUMBER);

        ArrowLineLayer.Builder builder = new ArrowLineLayer.Builder(context, featureConfig, sortConfig);

        int colorRes = android.support.v7.appcompat.R.color.abc_btn_colored_text_material;
        int colorInt = context
                .getResources()
                .getColor(android.support.v7.appcompat.R.color.abc_btn_colored_text_material);
        float lineWidth = 67f;

        builder.setArrowLineColor(colorRes);
        builder.setArrowLineWidth(lineWidth);

        ArrowLineLayer arrowLineLayer = builder.build();
        ReflectionHelpers.callInstanceMethod(arrowLineLayer, "createArrowLineLayer", ReflectionHelpers.ClassParameter.from(ArrowLineLayer.Builder.class, builder));

        LineLayer lineLayer = arrowLineLayer.getLineLayer();
        ShadowLayer shadowLayer = (ShadowLineLayer) Shadow.extract(lineLayer);

        HashMap<String, PropertyValue> propertyValues = shadowLayer.getPropertyValues();

        assertEquals(propertyValues.get("line-color").value, ColorUtils.colorToRgbaString(colorInt));
        assertEquals(propertyValues.get("line-width").value, lineWidth);
    }

    @Test
    public void constructorShouldThrowExceptionWhenDateTimeFormatIsNotSet() {
        ArrayList<Feature> featuresList = new ArrayList<>();
        FeatureCollection featureCollection = FeatureCollection.fromFeatures(featuresList);

        ArrowLineLayer.FeatureConfig featureConfig = new ArrowLineLayer.FeatureConfig(featureCollection);
        ArrowLineLayer.SortConfig sortConfig = new ArrowLineLayer.SortConfig(""
                , ArrowLineLayer.SortConfig.SortOrder.DESC
                , ArrowLineLayer.SortConfig.PropertyType.DATE_TIME);
        boolean exceptionCaught = false;

        try {
            ArrowLineLayer.Builder builder = new ArrowLineLayer.Builder(context, featureConfig, sortConfig);
            builder.build();
        } catch (InvalidArrowLineConfigException invalidArrowLineConfigException) {
            assertEquals("Date time format for sort configuration on a DateTime property has not been set"
                    , invalidArrowLineConfigException.getMessage());
            exceptionCaught = true;
        }

        assertTrue(exceptionCaught);
    }

    @Test
    public void sortFeaturesShouldSortAscByDateTime() throws InvalidArrowLineConfigException {
        ArrayList<Feature> featuresList = new ArrayList<>();

        featuresList.add(generateRandomFeatureWithProperties(new GeoJSONFeature.Property("sample-date", "2019-01-01 00:00:03")
                , new GeoJSONFeature.Property("position", 4)));
        featuresList.add(generateRandomFeatureWithProperties(new GeoJSONFeature.Property("sample-date", "2019-01-01 00:00:00")
                , new GeoJSONFeature.Property("position", 3)));
        featuresList.add(generateRandomFeatureWithProperties(new GeoJSONFeature.Property("sample-date", "2014-01-01 00:00:00")
                , new GeoJSONFeature.Property("position", 0)));
        featuresList.add(generateRandomFeatureWithProperties(new GeoJSONFeature.Property("sample-date", "2016-01-01 00:00:00")
                , new GeoJSONFeature.Property("position", 2)));
        featuresList.add(generateRandomFeatureWithProperties(new GeoJSONFeature.Property("sample-date", "2015-01-01 00:00:00")
                , new GeoJSONFeature.Property("position", 1)));

        FeatureCollection featureCollection = FeatureCollection.fromFeatures(featuresList);

        ArrowLineLayer.FeatureConfig featureConfig = new ArrowLineLayer.FeatureConfig(featureCollection);
        ArrowLineLayer.SortConfig sortConfig = new ArrowLineLayer.SortConfig("sample-date"
                , ArrowLineLayer.SortConfig.SortOrder.ASC
                , ArrowLineLayer.SortConfig.PropertyType.DATE_TIME)
                .setDateTimeFormat("yyyy-MM-dd HH:mm:ss");

        ArrowLineLayer.Builder builder = new ArrowLineLayer.Builder(context, featureConfig, sortConfig);
        ArrowLineLayer arrowLineLayer = builder.build();

        FeatureCollection sortedFeatureCollection = ReflectionHelpers.callInstanceMethod(arrowLineLayer
                , "sortFeatures"
                , ReflectionHelpers.ClassParameter.from(FeatureCollection.class, featureCollection)
                , ReflectionHelpers.ClassParameter.from(ArrowLineLayer.SortConfig.class, sortConfig)
        );

        List<Feature> sortedFeatures = sortedFeatureCollection.features();
        assertEquals(5, sortedFeatures.size());

        for (int i = 0; i < 5; i++) {
            Feature sortedFeature = sortedFeatures.get(i);
            assertEquals((int) sortedFeature.getNumberProperty("position"), i);
        }
    }

    @Test
    public void sortFeaturesShouldSortDescByDateTime() throws InvalidArrowLineConfigException {
        ArrayList<Feature> featuresList = new ArrayList<>();

        featuresList.add(generateRandomFeatureWithProperties(new GeoJSONFeature.Property("sample-date", "2019-01-01 00:00:03")
                , new GeoJSONFeature.Property("position", 0)));
        featuresList.add(generateRandomFeatureWithProperties(new GeoJSONFeature.Property("sample-date", "2019-01-01 00:00:00")
                , new GeoJSONFeature.Property("position", 1)));
        featuresList.add(generateRandomFeatureWithProperties(new GeoJSONFeature.Property("sample-date", "2014-01-01 00:00:00")
                , new GeoJSONFeature.Property("position", 4)));
        featuresList.add(generateRandomFeatureWithProperties(new GeoJSONFeature.Property("sample-date", "2016-01-01 00:00:00")
                , new GeoJSONFeature.Property("position", 2)));
        featuresList.add(generateRandomFeatureWithProperties(new GeoJSONFeature.Property("sample-date", "2015-01-01 00:00:00")
                , new GeoJSONFeature.Property("position", 3)));

        FeatureCollection featureCollection = FeatureCollection.fromFeatures(featuresList);

        ArrowLineLayer.FeatureConfig featureConfig = new ArrowLineLayer.FeatureConfig(featureCollection);
        ArrowLineLayer.SortConfig sortConfig = new ArrowLineLayer.SortConfig("sample-date"
                , ArrowLineLayer.SortConfig.SortOrder.DESC
                , ArrowLineLayer.SortConfig.PropertyType.DATE_TIME)
                .setDateTimeFormat("yyyy-MM-dd HH:mm:ss");

        ArrowLineLayer.Builder builder = new ArrowLineLayer.Builder(context, featureConfig, sortConfig);
        ArrowLineLayer arrowLineLayer = builder.build();

        FeatureCollection sortedFeatureCollection = ReflectionHelpers.callInstanceMethod(arrowLineLayer
                , "sortFeatures"
                , ReflectionHelpers.ClassParameter.from(FeatureCollection.class, featureCollection)
                , ReflectionHelpers.ClassParameter.from(ArrowLineLayer.SortConfig.class, sortConfig)
        );

        List<Feature> sortedFeatures = sortedFeatureCollection.features();
        assertEquals(5, sortedFeatures.size());

        for (int i = 0; i < 5; i++) {
            Feature sortedFeature = sortedFeatures.get(i);
            assertEquals((int) sortedFeature.getNumberProperty("position"), i);
        }
    }

    @Test
    public void sortFeaturesShouldSortDescByNumber() throws InvalidArrowLineConfigException {
        ArrayList<Feature> featuresList = new ArrayList<>();

        featuresList.add(generateRandomFeatureWithProperties(new GeoJSONFeature.Property("sample-number", 2000.67)
                , new GeoJSONFeature.Property("position", 0)));
        featuresList.add(generateRandomFeatureWithProperties(new GeoJSONFeature.Property("sample-number", 1900)
                , new GeoJSONFeature.Property("position", 1)));
        featuresList.add(generateRandomFeatureWithProperties(new GeoJSONFeature.Property("sample-number", 1200)
                , new GeoJSONFeature.Property("position", 4)));
        featuresList.add(generateRandomFeatureWithProperties(new GeoJSONFeature.Property("sample-number", 1800.989348394)
                , new GeoJSONFeature.Property("position", 2)));
        featuresList.add(generateRandomFeatureWithProperties(new GeoJSONFeature.Property("sample-number", 1500)
                , new GeoJSONFeature.Property("position", 3)));

        FeatureCollection featureCollection = FeatureCollection.fromFeatures(featuresList);

        ArrowLineLayer.FeatureConfig featureConfig = new ArrowLineLayer.FeatureConfig(featureCollection);
        ArrowLineLayer.SortConfig sortConfig = new ArrowLineLayer.SortConfig("sample-number"
                , ArrowLineLayer.SortConfig.SortOrder.DESC
                , ArrowLineLayer.SortConfig.PropertyType.NUMBER);

        ArrowLineLayer.Builder builder = new ArrowLineLayer.Builder(context, featureConfig, sortConfig);
        ArrowLineLayer arrowLineLayer = builder.build();

        FeatureCollection sortedFeatureCollection = ReflectionHelpers.callInstanceMethod(arrowLineLayer
                , "sortFeatures"
                , ReflectionHelpers.ClassParameter.from(FeatureCollection.class, featureCollection)
                , ReflectionHelpers.ClassParameter.from(ArrowLineLayer.SortConfig.class, sortConfig)
        );

        List<Feature> sortedFeatures = sortedFeatureCollection.features();
        assertEquals(5, sortedFeatures.size());

        for (int i = 0; i < 5; i++) {
            Feature sortedFeature = sortedFeatures.get(i);
            assertEquals((int) sortedFeature.getNumberProperty("position"), i);
        }
    }

    @Test
    public void sortFeaturesShouldSortAscByNumber() throws InvalidArrowLineConfigException {
        ArrayList<Feature> featuresList = new ArrayList<>();

        featuresList.add(generateRandomFeatureWithProperties(new GeoJSONFeature.Property("sample-number", 2000.67)
                , new GeoJSONFeature.Property("position", 4)));
        featuresList.add(generateRandomFeatureWithProperties(new GeoJSONFeature.Property("sample-number", 1900)
                , new GeoJSONFeature.Property("position", 3)));
        featuresList.add(generateRandomFeatureWithProperties(new GeoJSONFeature.Property("sample-number", 1200)
                , new GeoJSONFeature.Property("position", 0)));
        featuresList.add(generateRandomFeatureWithProperties(new GeoJSONFeature.Property("sample-number", 1800.989348394)
                , new GeoJSONFeature.Property("position", 2)));
        featuresList.add(generateRandomFeatureWithProperties(new GeoJSONFeature.Property("sample-number", 1500)
                , new GeoJSONFeature.Property("position", 1)));

        FeatureCollection featureCollection = FeatureCollection.fromFeatures(featuresList);

        ArrowLineLayer.FeatureConfig featureConfig = new ArrowLineLayer.FeatureConfig(featureCollection);
        ArrowLineLayer.SortConfig sortConfig = new ArrowLineLayer.SortConfig("sample-number"
                , ArrowLineLayer.SortConfig.SortOrder.ASC
                , ArrowLineLayer.SortConfig.PropertyType.NUMBER);

        ArrowLineLayer.Builder builder = new ArrowLineLayer.Builder(context, featureConfig, sortConfig);
        ArrowLineLayer arrowLineLayer = builder.build();

        FeatureCollection sortedFeatureCollection = ReflectionHelpers.callInstanceMethod(arrowLineLayer
                , "sortFeatures"
                , ReflectionHelpers.ClassParameter.from(FeatureCollection.class, featureCollection)
                , ReflectionHelpers.ClassParameter.from(ArrowLineLayer.SortConfig.class, sortConfig)
        );

        List<Feature> sortedFeatures = sortedFeatureCollection.features();
        assertEquals(5, sortedFeatures.size());

        for (int i = 0; i < 5; i++) {
            Feature sortedFeature = sortedFeatures.get(i);
            assertEquals((int) sortedFeature.getNumberProperty("position"), i);
        }
    }

    @Test
    public void sortFeaturesShouldSortAscByString() throws InvalidArrowLineConfigException {
        ArrayList<Feature> featuresList = new ArrayList<>();

        featuresList.add(generateRandomFeatureWithProperties(new GeoJSONFeature.Property("sample-string", "efgh")
                , new GeoJSONFeature.Property("position", 4)));
        featuresList.add(generateRandomFeatureWithProperties(new GeoJSONFeature.Property("sample-string", "defg")
                , new GeoJSONFeature.Property("position", 3)));
        featuresList.add(generateRandomFeatureWithProperties(new GeoJSONFeature.Property("sample-string", "abcd")
                , new GeoJSONFeature.Property("position", 0)));
        featuresList.add(generateRandomFeatureWithProperties(new GeoJSONFeature.Property("sample-string", "cdef")
                , new GeoJSONFeature.Property("position", 2)));
        featuresList.add(generateRandomFeatureWithProperties(new GeoJSONFeature.Property("sample-string", "bcde")
                , new GeoJSONFeature.Property("position", 1)));

        FeatureCollection featureCollection = FeatureCollection.fromFeatures(featuresList);

        ArrowLineLayer.FeatureConfig featureConfig = new ArrowLineLayer.FeatureConfig(featureCollection);
        ArrowLineLayer.SortConfig sortConfig = new ArrowLineLayer.SortConfig("sample-string"
                , ArrowLineLayer.SortConfig.SortOrder.ASC
                , ArrowLineLayer.SortConfig.PropertyType.STRING);

        ArrowLineLayer.Builder builder = new ArrowLineLayer.Builder(context, featureConfig, sortConfig);
        ArrowLineLayer arrowLineLayer = builder.build();

        FeatureCollection sortedFeatureCollection = ReflectionHelpers.callInstanceMethod(arrowLineLayer
                , "sortFeatures"
                , ReflectionHelpers.ClassParameter.from(FeatureCollection.class, featureCollection)
                , ReflectionHelpers.ClassParameter.from(ArrowLineLayer.SortConfig.class, sortConfig)
        );

        List<Feature> sortedFeatures = sortedFeatureCollection.features();
        assertEquals(5, sortedFeatures.size());

        for (int i = 0; i < 5; i++) {
            Feature sortedFeature = sortedFeatures.get(i);
            assertEquals((int) sortedFeature.getNumberProperty("position"), i);
        }
    }


    @Test
    public void sortFeaturesShouldSortDescByString() throws InvalidArrowLineConfigException {
        ArrayList<Feature> featuresList = new ArrayList<>();

        featuresList.add(generateRandomFeatureWithProperties(new GeoJSONFeature.Property("sample-string", "efgh")
                , new GeoJSONFeature.Property("position", 0)));
        featuresList.add(generateRandomFeatureWithProperties(new GeoJSONFeature.Property("sample-string", "defg")
                , new GeoJSONFeature.Property("position", 1)));
        featuresList.add(generateRandomFeatureWithProperties(new GeoJSONFeature.Property("sample-string", "abcd")
                , new GeoJSONFeature.Property("position", 4)));
        featuresList.add(generateRandomFeatureWithProperties(new GeoJSONFeature.Property("sample-string", "cdef")
                , new GeoJSONFeature.Property("position", 2)));
        featuresList.add(generateRandomFeatureWithProperties(new GeoJSONFeature.Property("sample-string", "bcde")
                , new GeoJSONFeature.Property("position", 3)));

        FeatureCollection featureCollection = FeatureCollection.fromFeatures(featuresList);

        ArrowLineLayer.FeatureConfig featureConfig = new ArrowLineLayer.FeatureConfig(featureCollection);
        ArrowLineLayer.SortConfig sortConfig = new ArrowLineLayer.SortConfig("sample-string"
                , ArrowLineLayer.SortConfig.SortOrder.DESC
                , ArrowLineLayer.SortConfig.PropertyType.STRING);

        ArrowLineLayer.Builder builder = new ArrowLineLayer.Builder(context, featureConfig, sortConfig);
        ArrowLineLayer arrowLineLayer = builder.build();

        FeatureCollection sortedFeatureCollection = ReflectionHelpers.callInstanceMethod(arrowLineLayer
                , "sortFeatures"
                , ReflectionHelpers.ClassParameter.from(FeatureCollection.class, featureCollection)
                , ReflectionHelpers.ClassParameter.from(ArrowLineLayer.SortConfig.class, sortConfig)
        );

        List<Feature> sortedFeatures = sortedFeatureCollection.features();
        assertEquals(5, sortedFeatures.size());

        for (int i = 0; i < 5; i++) {
            Feature sortedFeature = sortedFeatures.get(i);
            assertEquals((int) sortedFeature.getNumberProperty("position"), i);
        }
    }

    @Test
    public void filterFeaturesByStringEqualTo() throws InvalidArrowLineConfigException {
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

        FeatureCollection featureCollection = FeatureCollection.fromFeatures(featuresList);

        ArrowLineLayer.FeatureConfig featureConfig = new ArrowLineLayer.FeatureConfig(
                new FeatureFilter.Builder(featureCollection)
                        .whereEq("task-status", "positive"));
        ArrowLineLayer.SortConfig sortConfig = new ArrowLineLayer.SortConfig("sample-string"
                , ArrowLineLayer.SortConfig.SortOrder.DESC
                , ArrowLineLayer.SortConfig.PropertyType.STRING);

        ArrowLineLayer.Builder builder = new ArrowLineLayer.Builder(context, featureConfig, sortConfig);
        ArrowLineLayer arrowLineLayer = builder.build();

        FeatureCollection filteredFeatureCollection = ReflectionHelpers.callInstanceMethod(arrowLineLayer
                , "filterFeatures"
                , ReflectionHelpers.ClassParameter.from(ArrowLineLayer.FeatureConfig.class, featureConfig)
                , ReflectionHelpers.ClassParameter.from(ArrowLineLayer.SortConfig.class, sortConfig)
        );

        List<Feature> filteredFeatures = filteredFeatureCollection.features();
        assertEquals(4, filteredFeatures.size());

        assertEquals(0, filteredFeatures.get(0).getNumberProperty("position"));
        assertEquals(3, filteredFeatures.get(1).getNumberProperty("position"));
        assertEquals(4, filteredFeatures.get(2).getNumberProperty("position"));
        assertEquals(5, filteredFeatures.get(3).getNumberProperty("position"));
    }

    @Test
    public void filterFeaturesShouldReturnAllFeaturesIfFeatureFilterIsNotSpecified() throws InvalidArrowLineConfigException {
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

        FeatureCollection featureCollection = FeatureCollection.fromFeatures(featuresList);

        ArrowLineLayer.FeatureConfig featureConfig = new ArrowLineLayer.FeatureConfig(featureCollection);
        ArrowLineLayer.SortConfig sortConfig = new ArrowLineLayer.SortConfig("sample-string"
                , ArrowLineLayer.SortConfig.SortOrder.DESC
                , ArrowLineLayer.SortConfig.PropertyType.STRING);

        ArrowLineLayer.Builder builder = new ArrowLineLayer.Builder(context, featureConfig, sortConfig);
        ArrowLineLayer arrowLineLayer = builder.build();

        FeatureCollection filteredFeatureCollection = ReflectionHelpers.callInstanceMethod(arrowLineLayer
                , "filterFeatures"
                , ReflectionHelpers.ClassParameter.from(ArrowLineLayer.FeatureConfig.class, featureConfig)
                , ReflectionHelpers.ClassParameter.from(ArrowLineLayer.SortConfig.class, sortConfig)
        );

        List<Feature> filteredFeatures = filteredFeatureCollection.features();
        assertEquals(7, filteredFeatures.size());

        assertEquals(0, filteredFeatures.get(0).getNumberProperty("position"));
        assertEquals(1, filteredFeatures.get(1).getNumberProperty("position"));
        assertEquals(2, filteredFeatures.get(2).getNumberProperty("position"));
        assertEquals(3, filteredFeatures.get(3).getNumberProperty("position"));
        assertEquals(4, filteredFeatures.get(4).getNumberProperty("position"));
        assertEquals(5, filteredFeatures.get(5).getNumberProperty("position"));
        assertEquals(6, filteredFeatures.get(6).getNumberProperty("position"));
    }

    @Test
    public void filterFeaturesByRegex() throws InvalidArrowLineConfigException {
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

        FeatureCollection featureCollection = FeatureCollection.fromFeatures(featuresList);

        ArrowLineLayer.FeatureConfig featureConfig = new ArrowLineLayer.FeatureConfig(
                new FeatureFilter.Builder(featureCollection)
                        .whereRegex("sample-string", ".*[abc]+.*"));
        ArrowLineLayer.SortConfig sortConfig = new ArrowLineLayer.SortConfig("sample-string"
                , ArrowLineLayer.SortConfig.SortOrder.DESC
                , ArrowLineLayer.SortConfig.PropertyType.STRING);

        ArrowLineLayer.Builder builder = new ArrowLineLayer.Builder(context, featureConfig, sortConfig);
        ArrowLineLayer arrowLineLayer = builder.build();

        FeatureCollection filteredFeatureCollection = ReflectionHelpers.callInstanceMethod(arrowLineLayer
                , "filterFeatures"
                , ReflectionHelpers.ClassParameter.from(ArrowLineLayer.FeatureConfig.class, featureConfig)
                , ReflectionHelpers.ClassParameter.from(ArrowLineLayer.SortConfig.class, sortConfig)
        );

        List<Feature> filteredFeatures = filteredFeatureCollection.features();
        assertEquals(4, filteredFeatures.size());

        assertEquals(2, filteredFeatures.get(0).getNumberProperty("position"));
        assertEquals(3, filteredFeatures.get(1).getNumberProperty("position"));
        assertEquals(4, filteredFeatures.get(2).getNumberProperty("position"));
        assertEquals(5, filteredFeatures.get(3).getNumberProperty("position"));
    }

    @Test
    public void getLayerIdsShouldReturnStringArrayWithLayerIds() throws InvalidArrowLineConfigException {
        ArrayList<Feature> featuresList = new ArrayList<>();
        FeatureCollection featureCollection = FeatureCollection.fromFeatures(featuresList);

        ArrowLineLayer.FeatureConfig featureConfig = new ArrowLineLayer.FeatureConfig(featureCollection);
        ArrowLineLayer.SortConfig sortConfig = new ArrowLineLayer.SortConfig("", ArrowLineLayer.SortConfig.SortOrder.DESC, ArrowLineLayer.SortConfig.PropertyType.NUMBER);

        ArrowLineLayer.Builder builder = new ArrowLineLayer.Builder(context, featureConfig, sortConfig);
        ArrowLineLayer arrowLineLayer = builder.build();

        String[] layerIds = arrowLineLayer.getLayerIds();
        assertEquals(2, layerIds.length);
        assertNotNull(layerIds[0]);
        assertNotNull(layerIds[1]);
    }


    @Test
    public void removeLayerOnMapShouldReturnFalseWhenStyleIsNotReady() throws InvalidArrowLineConfigException {
        ArrayList<Feature> featuresList = new ArrayList<>();
        FeatureCollection featureCollection = FeatureCollection.fromFeatures(featuresList);

        ArrowLineLayer.FeatureConfig featureConfig = new ArrowLineLayer.FeatureConfig(featureCollection);
        ArrowLineLayer.SortConfig sortConfig = new ArrowLineLayer.SortConfig("", ArrowLineLayer.SortConfig.SortOrder.DESC, ArrowLineLayer.SortConfig.PropertyType.NUMBER);

        ArrowLineLayer.Builder builder = new ArrowLineLayer.Builder(context, featureConfig, sortConfig);
        ArrowLineLayer arrowLineLayer = builder.build();

        MapboxMap mapboxMap = Mockito.mock(MapboxMap.class);
        Style style = Mockito.mock(Style.class);

        Mockito.doReturn(false)
                .when(style)
                .isFullyLoaded();

        Mockito.doReturn(style)
                .when(mapboxMap)
                .getStyle();

        assertFalse(arrowLineLayer.removeLayerOnMap(mapboxMap));
    }


    @Test
    public void removeLayerOnMapShouldReturnTrueWhenStyleIsReadyAndRemoveLayersAndSources() throws InvalidArrowLineConfigException {
        ArrayList<Feature> featuresList = new ArrayList<>();
        FeatureCollection featureCollection = FeatureCollection.fromFeatures(featuresList);

        ArrowLineLayer.FeatureConfig featureConfig = new ArrowLineLayer.FeatureConfig(featureCollection);
        ArrowLineLayer.SortConfig sortConfig = new ArrowLineLayer.SortConfig("", ArrowLineLayer.SortConfig.SortOrder.DESC, ArrowLineLayer.SortConfig.PropertyType.NUMBER);

        ArrowLineLayer.Builder builder = new ArrowLineLayer.Builder(context, featureConfig, sortConfig);
        ArrowLineLayer arrowLineLayer = builder.build();

        MapboxMap mapboxMap = Mockito.mock(MapboxMap.class);
        Style style = Mockito.mock(Style.class);

        SymbolLayer arrowHeadLayer = Mockito.mock(SymbolLayer.class);
        LineLayer lineLayer = Mockito.mock(LineLayer.class);
        GeoJsonSource arrowHeadSource = new GeoJsonSource("some-id");
        GeoJsonSource lineLayerSource = new GeoJsonSource("some-id-2");

        Mockito.doReturn(true)
                .when(style)
                .isFullyLoaded();

        Mockito.doReturn(style)
                .when(mapboxMap)
                .getStyle();

        ReflectionHelpers.setField(arrowLineLayer, "arrowHeadLayer", arrowHeadLayer);
        ReflectionHelpers.setField(arrowLineLayer, "lineLayer", lineLayer);
        ReflectionHelpers.setField(arrowLineLayer, "arrowHeadSource", arrowHeadSource);
        ReflectionHelpers.setField(arrowLineLayer, "lineLayerSource", lineLayerSource);

        assertTrue(arrowLineLayer.removeLayerOnMap(mapboxMap));

        Mockito.verify(style, Mockito.times(1))
                .removeLayer(ArgumentMatchers.eq(arrowHeadLayer));
        Mockito.verify(style, Mockito.times(1))
                .removeLayer(ArgumentMatchers.eq(lineLayer));
        Mockito.verify(style, Mockito.times(1))
                .removeSource(ArgumentMatchers.eq(arrowHeadSource));
        Mockito.verify(style, Mockito.times(1))
                .removeSource(ArgumentMatchers.eq(lineLayerSource));
    }

    @Test
    public void updateFeaturesShouldUpdateFeatureCollectionWhenNoFilterIsBeingUsed() throws InvalidArrowLineConfigException {
        ArrayList<Feature> featuresList = new ArrayList<>();
        FeatureCollection featureCollection = FeatureCollection.fromFeatures(featuresList);

        ArrowLineLayer.FeatureConfig featureConfig = new ArrowLineLayer.FeatureConfig(featureCollection);
        ArrowLineLayer.SortConfig sortConfig = new ArrowLineLayer.SortConfig("", ArrowLineLayer.SortConfig.SortOrder.DESC, ArrowLineLayer.SortConfig.PropertyType.NUMBER);

        ArrowLineLayer.Builder builder = new ArrowLineLayer.Builder(context, featureConfig, sortConfig);
        ArrowLineLayer arrowLineLayer = builder.build();

        FeatureCollection updatedFeatureCollection = FeatureCollection.fromFeatures(new ArrayList<Feature>());

        arrowLineLayer.updateFeatures(updatedFeatureCollection);

        assertEquals(updatedFeatureCollection, ReflectionHelpers.getField(
                ReflectionHelpers.getField(
                        ReflectionHelpers.getField(arrowLineLayer, "builder")
                        , "featureConfig")
                , "featureCollection")
        );
    }

    @Test
    public void updateFeaturesShouldUpdateFilterBuilderFeatureCollectionWhenFeatureFilterIsBeingUsed() throws InvalidArrowLineConfigException {
        ArrayList<Feature> featuresList = new ArrayList<>();
        FeatureCollection featureCollection = FeatureCollection.fromFeatures(featuresList);

        FeatureFilter.Builder builder = new FeatureFilter.Builder(featureCollection)
                .whereEq("type", "building");

        ArrowLineLayer.FeatureConfig featureConfig = new ArrowLineLayer.FeatureConfig(builder);
        ArrowLineLayer.SortConfig sortConfig = new ArrowLineLayer.SortConfig("", ArrowLineLayer.SortConfig.SortOrder.DESC, ArrowLineLayer.SortConfig.PropertyType.NUMBER);

        ArrowLineLayer arrowLineLayer = new ArrowLineLayer.Builder(context, featureConfig, sortConfig).build();

        FeatureCollection updatedFeatureCollection = FeatureCollection.fromFeatures(new ArrayList<Feature>());

        arrowLineLayer.updateFeatures(updatedFeatureCollection);

        assertEquals(updatedFeatureCollection, ((FeatureFilter.Builder) ReflectionHelpers.getField(
                ReflectionHelpers.getField(
                        ReflectionHelpers.getField(arrowLineLayer, "builder")
                        , "featureConfig")
                , "featureFilterBuilder")).getFeatureCollection()
        );
    }

    @Test
    public void createLineLayerSource() throws InvalidArrowLineConfigException {
        ArrayList<Feature> featuresList = new ArrayList<>();
        FeatureCollection featureCollection = FeatureCollection.fromFeatures(featuresList);

        FeatureFilter.Builder builder = new FeatureFilter.Builder(featureCollection)
                .whereEq("type", "building");

        ArrowLineLayer.FeatureConfig featureConfig = new ArrowLineLayer.FeatureConfig(builder);
        ArrowLineLayer.SortConfig sortConfig = new ArrowLineLayer.SortConfig("", ArrowLineLayer.SortConfig.SortOrder.DESC, ArrowLineLayer.SortConfig.PropertyType.NUMBER);

        ArrowLineLayer arrowLineLayer = new ArrowLineLayer.Builder(context, featureConfig, sortConfig).build();

        assertNull((GeoJsonSource) ReflectionHelpers.getField(arrowLineLayer, "lineLayerSource"));
        ReflectionHelpers.callInstanceMethod(arrowLineLayer, "createLineLayerSource");
        assertNotNull((GeoJsonSource) ReflectionHelpers.getField(arrowLineLayer, "lineLayerSource"));
    }

    @Test
    public void createArrowHeadLayer() throws InvalidArrowLineConfigException {
        ArrayList<Feature> featuresList = new ArrayList<>();
        FeatureCollection featureCollection = FeatureCollection.fromFeatures(featuresList);

        FeatureFilter.Builder builder = new FeatureFilter.Builder(featureCollection)
                .whereEq("type", "building");

        ArrowLineLayer.FeatureConfig featureConfig = new ArrowLineLayer.FeatureConfig(builder);
        ArrowLineLayer.SortConfig sortConfig = new ArrowLineLayer.SortConfig("", ArrowLineLayer.SortConfig.SortOrder.DESC, ArrowLineLayer.SortConfig.PropertyType.NUMBER);

        ArrowLineLayer arrowLineLayer = new ArrowLineLayer.Builder(context, featureConfig, sortConfig).build();

        assertNull((SymbolLayer) ReflectionHelpers.getField(arrowLineLayer, "arrowHeadLayer"));
        ReflectionHelpers.callInstanceMethod(arrowLineLayer, "createArrowHeadLayer");

        SymbolLayer arrowHeadLayer = (SymbolLayer) ReflectionHelpers.getField(arrowLineLayer, "arrowHeadLayer");
        assertNotNull(arrowHeadLayer);

        ShadowLayer shadowLayer = (ShadowLayer) Shadow.extract(arrowHeadLayer);
        HashMap<String, PropertyValue> propertyValueHashMap = shadowLayer.getPropertyValues();
        assertEquals(7, propertyValueHashMap.size());

        assertEquals(1f, propertyValueHashMap.get("icon-opacity").value);
        assertTrue(propertyValueHashMap.get("icon-rotate").toString().contains("arrow-head-bearing"));
        assertEquals("map", propertyValueHashMap.get("icon-rotation-alignment").value);
        assertEquals(true, propertyValueHashMap.get("icon-allow-overlap").value);
        assertEquals(true, propertyValueHashMap.get("icon-ignore-placement").value);
    }

    @Test
    public void createArrowHeadSource() throws InvalidArrowLineConfigException {
        ArrayList<Feature> featuresList = new ArrayList<>();
        FeatureCollection featureCollection = FeatureCollection.fromFeatures(featuresList);

        FeatureFilter.Builder builder = new FeatureFilter.Builder(featureCollection)
                .whereEq("type", "building");

        ArrowLineLayer.FeatureConfig featureConfig = new ArrowLineLayer.FeatureConfig(builder);
        ArrowLineLayer.SortConfig sortConfig = new ArrowLineLayer.SortConfig("", ArrowLineLayer.SortConfig.SortOrder.DESC, ArrowLineLayer.SortConfig.PropertyType.NUMBER);

        ArrowLineLayer arrowLineLayer = new ArrowLineLayer.Builder(context, featureConfig, sortConfig).build();

        assertNull((GeoJsonSource) ReflectionHelpers.getField(arrowLineLayer, "arrowHeadSource"));
        ReflectionHelpers.callInstanceMethod(arrowLineLayer, "createArrowHeadSource");
        assertNotNull((GeoJsonSource) ReflectionHelpers.getField(arrowLineLayer, "arrowHeadSource"));
    }
}