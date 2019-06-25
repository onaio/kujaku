package io.ona.kujaku.manager;

import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.Style;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import io.ona.kujaku.layers.BaseKujakuLayerTest;

/**
 * Created by Emmanuel Otin - eo@novel-t.ch 24/06/19.
 */
@RunWith(RobolectricTestRunner.class)
public class DrawingManagerTest extends BaseKujakuLayerTest {

    private MapboxMap mapboxMap = Mockito.mock(MapboxMap.class);
    private MapView mapView = Mockito.mock(MapView.class);
    private Style style = Mockito.mock(Style.class);


    @Before
    public void beforeTest() {
        Mockito.doReturn(true).when(style).isFullyLoaded();
        Mockito.doReturn(style).when(mapboxMap).getStyle();
        Mockito.doReturn(RuntimeEnvironment.application).when(mapView).getContext();
    }

    @Test
    public void createInstanceOfDrawingManagerAndTestDefaultValues() {
        DrawingManager manager = new DrawingManager(mapView, mapboxMap, style);

        Assert.assertNull(manager.getCurrentKujakuCircle());
        Assert.assertFalse(manager.isDrawingEnabled());
    }
}