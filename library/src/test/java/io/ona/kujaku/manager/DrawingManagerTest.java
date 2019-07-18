package io.ona.kujaku.manager;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.annotation.Circle;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.util.ArrayList;
import java.util.List;

import io.ona.kujaku.layers.BaseKujakuLayerTest;
import io.ona.kujaku.layers.FillBoundaryLayer;
import io.ona.kujaku.views.KujakuMapView;

/**
 * Created by Emmanuel Otin - eo@novel-t.ch 24/06/19.
 */
@RunWith(RobolectricTestRunner.class)
public class DrawingManagerTest extends BaseKujakuLayerTest {

    private MapboxMap mapboxMap = Mockito.mock(MapboxMap.class);
    private KujakuMapView mapView = Mockito.mock(KujakuMapView.class);
    private Style style = Mockito.mock(Style.class);

    private DrawingManager manager;

    @Before
    public void beforeTest() {
        Mockito.doReturn(true).when(style).isFullyLoaded();
        Mockito.doReturn(style).when(mapboxMap).getStyle();
        Mockito.doReturn(RuntimeEnvironment.application).when(mapView).getContext();

        manager = new DrawingManager(mapView, mapboxMap, style);
    }

    @Test
    public void createInstanceOfDrawingManagerAndTestDefaultValues() {
        Assert.assertNull(manager.getCurrentKujakuCircle());
        Assert.assertFalse(manager.isDrawingEnabled());
    }

    @Test
    public void startDrawingWithoutPoints() {
        manager.startDrawingPoints(null);

        Assert.assertTrue(manager.isDrawingEnabled());
        Assert.assertNull(manager.getCurrentKujakuCircle());
        Assert.assertEquals(manager.getKujakuCircles().size(), 0);
    }

    @Test
    public void startDrawingWithPoints() {
        List<Point> points = new ArrayList<>();
        points.add(Point.fromLngLat(1,1));
        points.add(Point.fromLngLat(2,2));
        points.add(Point.fromLngLat(3,3));
        points.add(Point.fromLngLat(4,4));

        manager.startDrawingPoints(points);

        Assert.assertTrue(manager.isDrawingEnabled());
        Assert.assertNull(manager.getCurrentKujakuCircle());
        // Middle Circles created between each point
        Assert.assertEquals(8, manager.getKujakuCircles().size());
    }

    @Test
    public void startDrawingWithExistingLayer() {
        manager.startDrawing(getFillBoundaryLayer());

        Assert.assertTrue(manager.isDrawingEnabled());
        Assert.assertNull(manager.getCurrentKujakuCircle());
        // Middle Circles created between each point
        Assert.assertEquals(8, manager.getKujakuCircles().size());

        Assert.assertFalse(manager.getKujakuCircles().get(0).isMiddleCircle());
        Assert.assertTrue(manager.getKujakuCircles().get(1).isMiddleCircle());
        Assert.assertFalse(manager.getKujakuCircles().get(2).isMiddleCircle());
        Assert.assertTrue(manager.getKujakuCircles().get(3).isMiddleCircle());
        Assert.assertFalse(manager.getKujakuCircles().get(4).isMiddleCircle());
        Assert.assertTrue(manager.getKujakuCircles().get(5).isMiddleCircle());
        Assert.assertFalse(manager.getKujakuCircles().get(6).isMiddleCircle());
        Assert.assertTrue(manager.getKujakuCircles().get(7).isMiddleCircle());
    }

    @Test
    public void drawPoints() {
        manager.startDrawingPoints(null);

        manager.create(DrawingManager.getKujakuCircleOptions().withLatLng(new LatLng(1,1)));
        manager.create(DrawingManager.getKujakuCircleOptions().withLatLng(new LatLng(2,2)));
        manager.create(DrawingManager.getKujakuCircleOptions().withLatLng(new LatLng(3,3)));

        // Middle Circles created between each point
        Assert.assertEquals(6, manager.getKujakuCircles().size());
    }

    @Test
    public void setPointDraggable() {
        manager.startDrawingPoints(null);

        manager.create(DrawingManager.getKujakuCircleOptions().withLatLng(new LatLng(1,1)));
        manager.create(DrawingManager.getKujakuCircleOptions().withLatLng(new LatLng(2,2)));
        manager.create(DrawingManager.getKujakuCircleOptions().withLatLng(new LatLng(3,3)));

        Circle circle = manager.getKujakuCircles().get(0).getCircle();
        manager.setDraggable(true, circle);

        Assert.assertTrue(circle.isDraggable());
        Assert.assertNotNull(manager.getCurrentKujakuCircle());
        Assert.assertTrue(manager.getCurrentKujakuCircle().getCircle().isDraggable());

        manager.setDraggable(false, circle);
        Assert.assertNull(manager.getCurrentKujakuCircle());
        Assert.assertFalse(circle.isDraggable());
    }

    @Test
    public void relationsBetweenKujakuCircles() {
        List<Point> points = new ArrayList<>();
        points.add(Point.fromLngLat(1,1));
        points.add(Point.fromLngLat(2,2));
        points.add(Point.fromLngLat(3,3));
        points.add(Point.fromLngLat(4,4));

        manager.startDrawingPoints(points);

        Assert.assertEquals(8, manager.getKujakuCircles().size());
        Assert.assertEquals(manager.getKujakuCircles().get(0).getNextKujakuCircle(), manager.getKujakuCircles().get(1));
        Assert.assertEquals(manager.getKujakuCircles().get(1).getNextKujakuCircle(), manager.getKujakuCircles().get(2));
        Assert.assertEquals(manager.getKujakuCircles().get(7).getNextKujakuCircle(), manager.getKujakuCircles().get(0));

        Assert.assertEquals(manager.getKujakuCircles().get(0).getPreviousKujakuCircle(), manager.getKujakuCircles().get(7));
        Assert.assertEquals(manager.getKujakuCircles().get(1).getPreviousKujakuCircle(), manager.getKujakuCircles().get(0));
    }

    @Test
    public void stopDrawing() {
        List<Point> points = new ArrayList<>();
        points.add(Point.fromLngLat(1,1));
        points.add(Point.fromLngLat(2,2));
        points.add(Point.fromLngLat(3,3));
        points.add(Point.fromLngLat(4,4));

        manager.startDrawingPoints(points);

        Assert.assertTrue(manager.isDrawingEnabled());
        Assert.assertNull(manager.getCurrentKujakuCircle());

        Polygon polygon = manager.stopDrawingAndDisplayLayer();

        Assert.assertFalse(manager.isDrawingEnabled());
        Assert.assertNull(manager.getCurrentKujakuCircle());

        Assert.assertNotNull(polygon);
        Assert.assertEquals(polygon.coordinates().get(0).size(), points.size());
    }

    @Test
    public void deletePoint() {
        List<Point> points = new ArrayList<>();
        points.add(Point.fromLngLat(1,1));
        points.add(Point.fromLngLat(2,2));
        points.add(Point.fromLngLat(3,3));
        points.add(Point.fromLngLat(4,4));

        manager.startDrawingPoints(points);

        Assert.assertEquals(8, manager.getKujakuCircles().size());

        manager.delete(null); // Delete nothing
        manager.delete(manager.getKujakuCircles().get(0));

        Assert.assertEquals(6, manager.getKujakuCircles().size());
    }

    @Test
    public void areMiddleCircles() {
        List<Point> points = new ArrayList<>();
        points.add(Point.fromLngLat(1,1));
        points.add(Point.fromLngLat(2,2));
        points.add(Point.fromLngLat(3,3));
        points.add(Point.fromLngLat(4,4));

        manager.startDrawingPoints(points);

        Assert.assertFalse(manager.getKujakuCircles().get(0).isMiddleCircle());
        Assert.assertTrue(manager.getKujakuCircles().get(1).isMiddleCircle());
        Assert.assertFalse(manager.getKujakuCircles().get(2).isMiddleCircle());
        Assert.assertTrue(manager.getKujakuCircles().get(3).isMiddleCircle());
        Assert.assertFalse(manager.getKujakuCircles().get(4).isMiddleCircle());
        Assert.assertTrue(manager.getKujakuCircles().get(5).isMiddleCircle());
        Assert.assertFalse(manager.getKujakuCircles().get(6).isMiddleCircle());
        Assert.assertTrue(manager.getKujakuCircles().get(7).isMiddleCircle());
    }

    private FillBoundaryLayer getFillBoundaryLayer() {
        List<Feature> features = new ArrayList<Feature>();
        List<List<Point>> lists = new ArrayList<>();
        List<Point> points = new ArrayList<>();

        points.add(Point.fromLngLat(-11,15));
        points.add(Point.fromLngLat(-5,15));
        points.add(Point.fromLngLat(-5,11));
        points.add(Point.fromLngLat(-11,11));
        lists.add(points);

        features.add(Feature.fromGeometry(Polygon.fromLngLats(lists)));

        FeatureCollection featureCollection = FeatureCollection.fromFeatures(features);
        FillBoundaryLayer.Builder builder = new FillBoundaryLayer.Builder(featureCollection);
        return builder.build();
    }
}