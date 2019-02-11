package io.ona.kujaku.layers;

import android.content.Context;
import android.support.annotation.NonNull;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Geometry;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.MultiPolygon;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.PropertyValue;
import com.mapbox.mapboxsdk.utils.ColorUtils;

import org.junit.Before;
import org.junit.Test;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.ReflectionHelpers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.ona.kujaku.BaseTest;
import io.ona.kujaku.test.shadows.ShadowGeoJsonSource;
import io.ona.kujaku.test.shadows.ShadowLayer;
import io.ona.kujaku.test.shadows.ShadowLineLayer;
import io.ona.kujaku.test.shadows.ShadowSymbolLayer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 11/02/2019
 */

@Config(shadows = {ShadowGeoJsonSource.class, ShadowSymbolLayer.class, ShadowLineLayer.class, ShadowLayer.class})
public class ArrowLineLayerTest extends BaseTest {

    private Context context;

    @Before
    public void setup() {
        context = RuntimeEnvironment.application;
    }

    @Test
    public void addLayerToMap() {

    }

    @Test
    public void getCenterWhenGivenPolygonFeature() {
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
    public void getCenterWhenGivenPointFeature() {
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
    public void getCenterWhenGivenPolygonWithHolesFeature() {
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
    public void getCenterWhenGivenMultiPolygonFeature() {
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
    public void calculateLineStringWhenGivenFeatureCollectionWithFeatures() {
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
    public void calculateLineStringWhenGivenFeatureCollectionWithNoFeatures() {
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
    public void generateArrowHeadFeatureCollection() {
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
    public void setLayerPropertiesFromBuilder() {
        ArrayList<Feature> featuresList = new ArrayList<>();
        FeatureCollection featureCollection = FeatureCollection.fromFeatures(featuresList);

        ArrowLineLayer.FeatureConfig featureConfig = new ArrowLineLayer.FeatureConfig(featureCollection);
        ArrowLineLayer.SortConfig sortConfig = new ArrowLineLayer.SortConfig("", ArrowLineLayer.SortConfig.SortOrder.DESC, ArrowLineLayer.SortConfig.PropertyType.NUMBER);

        ArrowLineLayer.Builder builder = new ArrowLineLayer.Builder(context, featureConfig, sortConfig);

        int colorRes = android.support.v7.appcompat.R.color.abc_btn_colored_text_material;
        int colorInt = context.getResources().getColor(android.support.v7.appcompat.R.color.abc_btn_colored_text_material);
        float lineWidth = 67f;

        builder.setArrowLineColor(colorRes);
        builder.setArrowLineWidth(lineWidth);

        ArrowLineLayer arrowLineLayer = builder.build();

        LineLayer lineLayer = arrowLineLayer.getLineLayer();
        ShadowLayer shadowLayer = (ShadowLineLayer) Shadow.extract(lineLayer);

        HashMap<String, PropertyValue> propertyValues = shadowLayer.getPropertyValues();

        assertEquals(propertyValues.get("line-color").value, ColorUtils.colorToRgbaString(colorInt));
        assertEquals(propertyValues.get("line-width").value, lineWidth);
    }

    private void assertPointEquals(@NonNull Point expected, @NonNull Point actual) {
        assertEquals(expected.latitude(), actual.latitude(), 0d);
        assertEquals(expected.longitude(), actual.longitude(), 0d);
    }
}