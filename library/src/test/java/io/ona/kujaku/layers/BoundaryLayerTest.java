package io.ona.kujaku.layers;

import android.graphics.Color;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.expressions.Expression;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.PropertyValue;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.mapboxsdk.utils.ColorUtils;

import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.ReflectionHelpers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.ona.kujaku.test.shadows.ShadowLayer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 18/02/2019
 */
public class BoundaryLayerTest extends BaseKujakuLayerTest {

    @Test
    public void testLineLayerProperties() throws NoSuchFieldException, IllegalAccessException {
        float textSize = 20f;
        float boundaryWidth = 6f;
        int colorInt = Color.GREEN;
        String labelProperty = "district-name";

        FeatureCollection featureCollection = FeatureCollection.fromFeatures(new ArrayList<Feature>());

        BoundaryLayer.Builder builder = new BoundaryLayer.Builder(featureCollection)
                .setLabelProperty(labelProperty)
                .setLabelTextSize(textSize)
                .setLabelColorInt(colorInt)
                .setBoundaryColor(colorInt)
                .setBoundaryWidth(boundaryWidth);
        BoundaryLayer boundaryLayer = builder.build();

        ReflectionHelpers.callInstanceMethod(boundaryLayer, "createBoundaryLineLayer"
                , ReflectionHelpers.ClassParameter.from(BoundaryLayer.Builder.class, builder));
        LineLayer lineLayer = (LineLayer) getValueInPrivateField(BoundaryLayer.class, boundaryLayer, "boundaryLineLayer");

        ShadowLayer shadowLayer = (ShadowLayer) Shadow.extract(lineLayer);
        HashMap<String, PropertyValue> propertyValues = shadowLayer.getPropertyValues();

        assertEquals(boundaryWidth, (float) propertyValues.get("line-width").value, 0f);
        assertEquals(ColorUtils.colorToRgbaString(colorInt), (String) propertyValues.get("line-color").value);
    }

    @Test
    public void testSymbolLayerProperties() throws NoSuchFieldException, IllegalAccessException {
        float textSize = 20f;
        float boundaryWidth = 6f;
        int colorInt = Color.GREEN;
        String labelProperty = "district-name";

        FeatureCollection featureCollection = FeatureCollection.fromFeatures(new ArrayList<Feature>());

        BoundaryLayer.Builder builder = new BoundaryLayer.Builder(featureCollection)
                .setLabelProperty(labelProperty)
                .setLabelTextSize(textSize)
                .setLabelColorInt(colorInt)
                .setBoundaryColor(colorInt)
                .setBoundaryWidth(boundaryWidth);
        BoundaryLayer boundaryLayer = builder.build();

        ReflectionHelpers.callInstanceMethod(boundaryLayer, "createBoundaryLabelLayer"
                , ReflectionHelpers.ClassParameter.from(BoundaryLayer.Builder.class, builder));

        SymbolLayer symbolLayer = (SymbolLayer) getValueInPrivateField(BoundaryLayer.class, boundaryLayer, "boundaryLabelLayer");

        ShadowLayer shadowLayer = (ShadowLayer) Shadow.extract(symbolLayer);
        HashMap<String, PropertyValue> propertyValues = shadowLayer.getPropertyValues();

        assertEquals(textSize, (float) propertyValues.get("text-size").value, 0f);
        assertEquals(ColorUtils.colorToRgbaString(colorInt), (String) propertyValues.get("text-color").value);
        assertTrue(((Expression) propertyValues.get("text-field").value).toString().contains(labelProperty));
    }

    @Test
    public void labelTextSizeExpressionOverridesTextSize() throws NoSuchFieldException, IllegalAccessException {
        float textSize = 20f;
        float boundaryWidth = 6f;
        int colorInt = Color.GREEN;
        String labelProperty = "district-name";

        FeatureCollection featureCollection = FeatureCollection.fromFeatures(new ArrayList<Feature>());

        BoundaryLayer.Builder builder = new BoundaryLayer.Builder(featureCollection)
                .setLabelProperty(labelProperty)
                .setLabelTextSize(textSize)
                .setLabelColorInt(colorInt)
                .setLabelTextSizeExpression(Expression.interpolate(Expression.linear(), Expression.zoom()
                        , Expression.stop(9, 0f)
                        , Expression.stop(10, 20f/2)
                        , Expression.stop(22, 20f)))
                .setBoundaryColor(colorInt)
                .setBoundaryWidth(boundaryWidth);
        BoundaryLayer boundaryLayer = builder.build();

        ReflectionHelpers.callInstanceMethod(BoundaryLayer.class, boundaryLayer, "createBoundaryLabelLayer"
                , ReflectionHelpers.ClassParameter.from(BoundaryLayer.Builder.class, builder));

        SymbolLayer symbolLayer = (SymbolLayer) getValueInPrivateField(BoundaryLayer.class, boundaryLayer, "boundaryLabelLayer");

        ShadowLayer shadowLayer = (ShadowLayer) Shadow.extract(symbolLayer);
        HashMap<String, PropertyValue> propertyValues = shadowLayer.getPropertyValues();

        assertTrue(propertyValues.get("text-size").isExpression());
    }
    
    @Test
    public void calculateCenterPoints() {
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

        BoundaryLayer.Builder builder = new BoundaryLayer.Builder(featureCollection)
                .setLabelProperty("name")
                .setLabelTextSize(20f)
                .setLabelColorInt(Color.RED)
                .setBoundaryColor(Color.RED)
                .setBoundaryWidth(6f);

        BoundaryLayer boundaryLayer = builder.build();

        FeatureCollection centerPointsFeatureCollection = ReflectionHelpers.callInstanceMethod(boundaryLayer
                , "calculateCenterPoints"
                , ReflectionHelpers.ClassParameter.from(FeatureCollection.class, featureCollection)
        );

        List<Feature> centerPointFeatures = centerPointsFeatureCollection.features();

        assertEquals(5, centerPointFeatures.size());
        Point point1 = (Point) centerPointFeatures.get(0).geometry();
        Point point2 = (Point) centerPointFeatures.get(1).geometry();

        assertPointEquals(Point.fromLngLat(6.5d, 6.5d), point1);
        assertPointEquals(Point.fromLngLat(9.1d, 9.1d), point2);
    }

    @Test
    public void getLayerIdsShouldReturnStringArrayWithLayerIds() {
        float textSize = 20f;
        float boundaryWidth = 6f;
        int colorInt = Color.GREEN;
        String labelProperty = "district-name";

        FeatureCollection featureCollection = FeatureCollection.fromFeatures(new ArrayList<Feature>());

        BoundaryLayer boundaryLayer = new BoundaryLayer.Builder(featureCollection)
                .setLabelProperty(labelProperty)
                .setLabelTextSize(textSize)
                .setLabelColorInt(colorInt)
                .setBoundaryColor(colorInt)
                .setBoundaryWidth(boundaryWidth)
                .build();

        String[] layerIds = boundaryLayer.getLayerIds();

        assertEquals(2, layerIds.length);
        assertNotNull(layerIds[0]);
        assertNotNull(layerIds[1]);
    }

    @Test
    public void createBoundaryLabelSourceShouldInstantiateBoundaryLabelsSourceVariable() {
        float textSize = 20f;
        float boundaryWidth = 6f;
        int colorInt = Color.GREEN;
        String labelProperty = "district-name";

        FeatureCollection featureCollection = FeatureCollection.fromFeatures(new ArrayList<Feature>());

        BoundaryLayer boundaryLayer = new BoundaryLayer.Builder(featureCollection)
                .setLabelProperty(labelProperty)
                .setLabelTextSize(textSize)
                .setLabelColorInt(colorInt)
                .setBoundaryColor(colorInt)
                .setBoundaryWidth(boundaryWidth)
                .build();

        assertNull(ReflectionHelpers.getField(boundaryLayer, "boundaryLabelsSource"));

        ReflectionHelpers.callInstanceMethod(boundaryLayer, "createBoundaryLabelSource");

        assertNotNull(ReflectionHelpers.getField(boundaryLayer, "boundaryLabelsSource"));
    }

    @Test
    public void createBoundaryFeatureSourceShouldInstantiateBoundaryFeatureSourceVariable() {
        float textSize = 20f;
        float boundaryWidth = 6f;
        int colorInt = Color.GREEN;
        String labelProperty = "district-name";

        FeatureCollection featureCollection = FeatureCollection.fromFeatures(new ArrayList<Feature>());

        BoundaryLayer.Builder builder = new BoundaryLayer.Builder(featureCollection)
                .setLabelProperty(labelProperty)
                .setLabelTextSize(textSize)
                .setLabelColorInt(colorInt)
                .setBoundaryColor(colorInt)
                .setBoundaryWidth(boundaryWidth);

        BoundaryLayer boundaryLayer = builder.build();

        assertNull(ReflectionHelpers.getField(boundaryLayer, "boundarySource"));

        ReflectionHelpers.callInstanceMethod(boundaryLayer, "createBoundaryFeatureSource", ReflectionHelpers.ClassParameter.from(BoundaryLayer.Builder.class, builder));

        assertNotNull(ReflectionHelpers.getField(boundaryLayer, "boundarySource"));
    }

    @Test
    public void removeLayerOnMapShouldReturnFalseWhenStyleIsNotReady() {
        float textSize = 20f;
        float boundaryWidth = 6f;
        int colorInt = Color.GREEN;
        String labelProperty = "district-name";

        FeatureCollection featureCollection = FeatureCollection.fromFeatures(new ArrayList<Feature>());

        BoundaryLayer.Builder builder = new BoundaryLayer.Builder(featureCollection)
                .setLabelProperty(labelProperty)
                .setLabelTextSize(textSize)
                .setLabelColorInt(colorInt)
                .setBoundaryColor(colorInt)
                .setBoundaryWidth(boundaryWidth);

        BoundaryLayer boundaryLayer = builder.build();

        MapboxMap mapboxMap = Mockito.mock(MapboxMap.class);
        Style style = Mockito.mock(Style.class);

        Mockito.doReturn(false)
                .when(style)
                .isFullyLoaded();

        Mockito.doReturn(style)
                .when(mapboxMap)
                .getStyle();

        assertFalse(boundaryLayer.removeLayerOnMap(mapboxMap));
    }


    @Test
    public void removeLayerOnMapShouldReturnTrueWhenStyleIsReadyAndRemoveLayersAndSources() {
        float textSize = 20f;
        float boundaryWidth = 6f;
        int colorInt = Color.GREEN;
        String labelProperty = "district-name";

        FeatureCollection featureCollection = FeatureCollection.fromFeatures(new ArrayList<Feature>());

        BoundaryLayer.Builder builder = new BoundaryLayer.Builder(featureCollection)
                .setLabelProperty(labelProperty)
                .setLabelTextSize(textSize)
                .setLabelColorInt(colorInt)
                .setBoundaryColor(colorInt)
                .setBoundaryWidth(boundaryWidth);

        BoundaryLayer boundaryLayer = builder.build();

        assertNull(ReflectionHelpers.getField(boundaryLayer, "boundarySource"));

        MapboxMap mapboxMap = Mockito.mock(MapboxMap.class);
        Style style = Mockito.mock(Style.class);

        SymbolLayer boundaryLabelLayer = Mockito.mock(SymbolLayer.class);
        LineLayer boundaryLineLayer = Mockito.mock(LineLayer.class);
        GeoJsonSource boundarySource = new GeoJsonSource("some-id");
        GeoJsonSource boundaryLabelsSource = new GeoJsonSource("some-id-2");

        Mockito.doReturn(true)
                .when(style)
                .isFullyLoaded();

        Mockito.doReturn(style)
                .when(mapboxMap)
                .getStyle();

        ReflectionHelpers.setField(boundaryLayer, "boundaryLabelLayer", boundaryLabelLayer);
        ReflectionHelpers.setField(boundaryLayer, "boundaryLineLayer", boundaryLineLayer);
        ReflectionHelpers.setField(boundaryLayer, "boundarySource", boundarySource);
        ReflectionHelpers.setField(boundaryLayer, "boundaryLabelsSource", boundaryLabelsSource);

        assertTrue(boundaryLayer.removeLayerOnMap(mapboxMap));

        Mockito.verify(style, Mockito.times(1))
                .removeLayer(ArgumentMatchers.eq(boundaryLabelLayer));
        Mockito.verify(style, Mockito.times(1))
                .removeLayer(ArgumentMatchers.eq(boundaryLineLayer));
        Mockito.verify(style, Mockito.times(1))
                .removeSource(ArgumentMatchers.eq(boundarySource));
        Mockito.verify(style, Mockito.times(1))
                .removeSource(ArgumentMatchers.eq(boundaryLabelsSource));
    }

    @Test
    public void updateFeaturesShouldUpdateFeatureCollection() {
        float textSize = 20f;
        float boundaryWidth = 6f;
        int colorInt = Color.GREEN;
        String labelProperty = "district-name";

        FeatureCollection featureCollection = FeatureCollection.fromFeatures(new ArrayList<Feature>());

        BoundaryLayer.Builder builder = new BoundaryLayer.Builder(featureCollection)
                .setLabelProperty(labelProperty)
                .setLabelTextSize(textSize)
                .setLabelColorInt(colorInt)
                .setBoundaryColor(colorInt)
                .setBoundaryWidth(boundaryWidth);

        BoundaryLayer boundaryLayer = builder.build();

        FeatureCollection updatedFeatureCollection = FeatureCollection.fromFeatures(new ArrayList<Feature>());

        boundaryLayer.updateFeatures(updatedFeatureCollection);

        assertEquals(updatedFeatureCollection, ReflectionHelpers.getField(
                ReflectionHelpers.getField(boundaryLayer, "builder"), "featureCollection")
        );
    }
}