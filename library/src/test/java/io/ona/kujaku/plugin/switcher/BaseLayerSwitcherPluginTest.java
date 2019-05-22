package io.ona.kujaku.plugin.switcher;

import com.mapbox.mapboxsdk.maps.Style;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.robolectric.annotation.Config;
import org.robolectric.util.ReflectionHelpers;

import java.util.ArrayList;

import io.ona.kujaku.BaseTest;
import io.ona.kujaku.plugin.switcher.layer.BaseLayer;
import io.ona.kujaku.plugin.switcher.layer.SatelliteBaseLayer;
import io.ona.kujaku.test.shadows.ShadowLayer;
import io.ona.kujaku.test.shadows.ShadowRasterLayer;
import io.ona.kujaku.test.shadows.ShadowRasterSource;
import io.ona.kujaku.views.KujakuMapView;

import static org.junit.Assert.*;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 2019-05-22
 */
@Config(shadows = {ShadowLayer.class, ShadowRasterLayer.class, ShadowRasterSource.class})
public class BaseLayerSwitcherPluginTest extends BaseTest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    private BaseLayerSwitcherPlugin baseLayerSwitcherPlugin;
    private KujakuMapView kujakuMapView;
    private Style style;

    @Before
    public void setUp() {
        kujakuMapView = Mockito.mock(KujakuMapView.class);
        style = Mockito.mock(Style.class);

        baseLayerSwitcherPlugin = Mockito.spy(new BaseLayerSwitcherPlugin(kujakuMapView, style));
    }

    @Test
    public void addBaseLayerShouldReturnFalseWhenBaseLayerInstanceIsSame() {
        BaseLayer satelliteLayer = new SatelliteBaseLayer();
        ArrayList<BaseLayer> baseLayers = new ArrayList<>();
        baseLayers.add(satelliteLayer);

        assertEquals(0, ((ArrayList<BaseLayer>) ReflectionHelpers.getField(baseLayerSwitcherPlugin, "baseLayers")).size());
        ReflectionHelpers.setField(baseLayerSwitcherPlugin, "baseLayers", baseLayers);

        boolean addLayerResult = baseLayerSwitcherPlugin.addBaseLayer(satelliteLayer, true);
        assertFalse(addLayerResult);
    }

    @Test
    public void addBaseLayerShouldReturnFalseWhenBaseLayerIdIsSame() {
        BaseLayer satelliteLayer = new SatelliteBaseLayer();
        ArrayList<BaseLayer> baseLayers = new ArrayList<>();
        baseLayers.add(satelliteLayer);

        assertEquals(0, ((ArrayList<BaseLayer>) ReflectionHelpers.getField(baseLayerSwitcherPlugin, "baseLayers")).size());
        ReflectionHelpers.setField(baseLayerSwitcherPlugin, "baseLayers", baseLayers);

        assertFalse(baseLayerSwitcherPlugin.addBaseLayer(new SatelliteBaseLayer(), true));
    }

    @Test
    public void addBaseLayerShouldReturnTrueWhenBaseLayerIsUnique() {
        BaseLayer satelliteLayer = new SatelliteBaseLayer();

        boolean addLayerResult = baseLayerSwitcherPlugin.addBaseLayer(satelliteLayer, true);
        assertTrue(addLayerResult);
        Mockito.verify(baseLayerSwitcherPlugin, Mockito.times(1))
                .showBaseLayer(Mockito.eq(satelliteLayer));
        assertEquals(1, ((ArrayList<BaseLayer>) ReflectionHelpers.getField(baseLayerSwitcherPlugin, "baseLayers")).size());
    }

    /*@Test
    public void removeBaseLayerFromMap() {
        BaseLayer satelliteLayer = new SatelliteBaseLayer();
        baseLayerSwitcherPlugin.addBaseLayer(satelliteLayer, false);

        assertTrue(baseLayerSwitcherPlugin.removeBaseLayer(satelliteLayer));
        Mockito.verify(kujakuMapView, Mockito.times(1))
                .removeLayer(Mockito.eq(satelliteLayer));
    }

    @Test
    public void removeBaseLayer() {
    }

    @Test
    public void getBaseLayers() {
    }

    @Test
    public void show() {
    }

    @Test
    public void showPluginSwitcherView() {
    }

    @Test
    public void showPopup() {
    }

    @Test
    public void onMenuItemClick() {
    }

    @Test
    public void showBaseLayer() {
    }*/
}