package io.ona.kujaku.manager;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.Style;

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
import io.ona.kujaku.manager.options.SplittingManagerOptions;
import io.ona.kujaku.views.KujakuMapView;

/**
 * Created by Emmanuel Otin - eo@novel-t.ch 05/07/19.
 */
@RunWith(RobolectricTestRunner.class)
public class SplittingManagerTest extends BaseKujakuLayerTest {

    private MapboxMap mapboxMap = Mockito.mock(MapboxMap.class);
    private KujakuMapView mapView = Mockito.mock(KujakuMapView.class);
    private Style style = Mockito.mock(Style.class);

    private SplittingManager manager;

    @Before
    public void beforeTest() {
        Mockito.doReturn(true).when(style).isFullyLoaded();
        Mockito.doReturn(style).when(mapboxMap).getStyle();
        Mockito.doReturn(RuntimeEnvironment.application).when(mapView).getContext();

        manager = new SplittingManager(mapView, mapboxMap, style);
    }

    @Test
    public void createInstanceOfSplittingManagerAndTestDefaultValues() {
        Assert.assertFalse(manager.isSplittingEnabled());
        Assert.assertFalse(manager.isSplittingReady());
    }

    @Test
    public void startSplittingManager() {
        manager.startSplitting(getFillBoundaryLayer());

        Assert.assertTrue(manager.isSplittingEnabled());
        Assert.assertFalse(manager.isSplittingReady());

        manager.stopSplitting();
    }

    @Test
    public void testSplittingManagerOptions() {
        SplittingManagerOptions options = new SplittingManagerOptions() {
            @Override
            public String getCircleColor() {
                return "green";
            }

            @Override
            public String getLineColor() {
                return "blue";
            }

            @Override
            public Float getCircleRadius() {
                return 5f;
            }

            @Override
            public String getKujakuFillLayerColor() {
                return "black";
            }

            @Override
            public String getKujakuFillLayerColorSelected() {
                return "yellow";
            }

            @Override
            public KujakuCircleOptions getKujakuCircleOptions() {
                return new KujakuCircleOptions()
                        .withCircleRadius(getCircleRadius())
                        .withCircleColor(getCircleColor())
                        .withDraggable(true);
            }
        };

        manager.setSplittingManagerOptions(options);

        Assert.assertEquals(options.getCircleColor(), "green");
        Assert.assertEquals(options.getLineColor(), "blue");
        Assert.assertEquals(options.getCircleRadius(), 5f, 0);
        Assert.assertEquals(options.getKujakuFillLayerColor(), "black");
        Assert.assertEquals(options.getKujakuFillLayerColorSelected(), "yellow");
    }

    @Test
    public void createSplittingLineSplittingManager() {
        manager.startSplitting(getFillBoundaryLayer());
        manager.drawCircle(new LatLng(19,-9));
        Assert.assertFalse(manager.isSplittingReady());

        manager.drawCircle(new LatLng(9,-8));
        Assert.assertTrue(manager.isSplittingReady());

        List<List<Point>> polygons = manager.split();

        Assert.assertEquals(polygons.size(), 2);
        Assert.assertEquals(polygons.get(0).size(), 4);
        Assert.assertEquals(polygons.get(1).size(), 4);

        manager.stopSplitting();
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