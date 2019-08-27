package io.ona.kujaku.helpers;

import android.content.Context;

import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.Style;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class MapBoxLocationComponentWrapperTest {

    private MapboxMap mapboxMap = Mockito.mock(MapboxMap.class);
    private Style style = Mockito.mock(Style.class);
    private Context context = Mockito.mock(Context.class);
    private LocationComponent locationComponent = Mockito.mock(LocationComponent.class);

    @Before
    public void beforeTest() {
        Mockito.doReturn(true).when(style).isFullyLoaded();
        Mockito.doReturn(style).when(mapboxMap).getStyle();
        Mockito.doReturn(locationComponent).when(mapboxMap).getLocationComponent();
    }

    @Test
    public void notInitMapBoxLocationComponentWrapper() {
        MapboxLocationComponentWrapper wrapper = new MapboxLocationComponentWrapper();
        Assert.assertNull(wrapper.getLocationComponent());
    }

    @Test
    public void initMapBoxLocationComponentWrapper() {
        MapboxLocationComponentWrapper wrapper = new MapboxLocationComponentWrapper();
        wrapper.init(mapboxMap, context, RenderMode.NORMAL);
        Assert.assertNotNull(wrapper.getLocationComponent());
    }
}
