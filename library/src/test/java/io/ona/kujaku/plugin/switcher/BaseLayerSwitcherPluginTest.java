package io.ona.kujaku.plugin.switcher;

import android.support.design.widget.FloatingActionButton;
import android.view.MenuItem;
import android.view.View;

import com.mapbox.mapboxsdk.maps.Style;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.util.ReflectionHelpers;

import java.util.ArrayList;

import io.ona.kujaku.BaseTest;
import io.ona.kujaku.R;
import io.ona.kujaku.plugin.switcher.layer.BaseLayer;
import io.ona.kujaku.plugin.switcher.layer.SatelliteBaseLayer;
import io.ona.kujaku.plugin.switcher.layer.StreetsBaseLayer;
import io.ona.kujaku.test.shadows.ShadowBackgroundLayer;
import io.ona.kujaku.test.shadows.ShadowLayer;
import io.ona.kujaku.test.shadows.ShadowRasterLayer;
import io.ona.kujaku.test.shadows.ShadowRasterSource;
import io.ona.kujaku.test.shadows.ShadowVectorSource;
import io.ona.kujaku.views.KujakuMapView;

import static org.junit.Assert.*;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 2019-05-22
 */
@Config(shadows = {ShadowBackgroundLayer.class, ShadowLayer.class, ShadowRasterLayer.class, ShadowRasterSource.class, ShadowVectorSource.class})
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

    @Test
    public void removeBaseLayerFromMapShouldCallMapViewRemoveLayerAndReturnTrue() {
        BaseLayer satelliteLayer = new SatelliteBaseLayer();
        baseLayerSwitcherPlugin.addBaseLayer(satelliteLayer, false);

        assertTrue(baseLayerSwitcherPlugin.removeBaseLayerFromMap(satelliteLayer));
        Mockito.verify(kujakuMapView, Mockito.times(1))
                .removeLayer(Mockito.eq(satelliteLayer));
    }

    @Test
    public void removeBaseLayerShouldRemoveBaseLayerWhenLayerIdIsSame() {
        BaseLayer satelliteLayer = new SatelliteBaseLayer();
        assertEquals(0, ((ArrayList<BaseLayer>) ReflectionHelpers.getField(baseLayerSwitcherPlugin, "baseLayers")).size());
        baseLayerSwitcherPlugin.addBaseLayer(satelliteLayer, false);

        assertEquals(1, ((ArrayList<BaseLayer>) ReflectionHelpers.getField(baseLayerSwitcherPlugin, "baseLayers")).size());
        assertTrue(baseLayerSwitcherPlugin.removeBaseLayer(new SatelliteBaseLayer()));
        assertEquals(0, ((ArrayList<BaseLayer>) ReflectionHelpers.getField(baseLayerSwitcherPlugin, "baseLayers")).size());
    }

    @Test
    public void showShouldCallShowPluginSwitcherView() {
        Mockito.doNothing()
                .when(baseLayerSwitcherPlugin)
                .showPluginSwitcherView();

        baseLayerSwitcherPlugin.show();
        Mockito.verify(baseLayerSwitcherPlugin, Mockito.times(1))
                .showPluginSwitcherView();
    }

    @Test
    public void showPluginSwitcherViewShouldEnableFab() {
        FloatingActionButton floatingActionButton = Mockito.mock(FloatingActionButton.class);

        Mockito.doReturn(floatingActionButton)
                .when(kujakuMapView)
                .findViewById(Mockito.eq(R.id.fab_mapview_layerSwitcher));

        baseLayerSwitcherPlugin.showPluginSwitcherView();

        Mockito.verify(kujakuMapView, Mockito.times(1))
                .findViewById(Mockito.eq(R.id.fab_mapview_layerSwitcher));
        Mockito.verify(floatingActionButton, Mockito.times(1))
                .setVisibility(Mockito.eq(View.VISIBLE));
    }

    @Test
    public void onMenuItemClickShouldCallShowBaseLayerAndReturnTrueWhenDifferentBaseLayerIsClicked() {
        SatelliteBaseLayer satelliteBaseLayer = new SatelliteBaseLayer();
        MenuItem menuItem = Mockito.mock(MenuItem.class);

        Mockito.doReturn(satelliteBaseLayer.hashCode())
                .when(menuItem)
                .getItemId();

        baseLayerSwitcherPlugin.addBaseLayer(satelliteBaseLayer, false);

        boolean result = baseLayerSwitcherPlugin.onMenuItemClick(menuItem);

        assertTrue(result);
        Mockito.verify(baseLayerSwitcherPlugin, Mockito.times(1))
                .showBaseLayer(Mockito.eq(satelliteBaseLayer));
        Mockito.verify(menuItem, Mockito.times(1))
                .setChecked(Mockito.eq(true));
    }

    /*@Test
    public void showBaseLayerShouldAddBaseLayerAndMakeItCurrentBaseLayer() {
        SatelliteBaseLayer satelliteBaseLayer = new SatelliteBaseLayer();
        StreetsBaseLayer streetsBaseLayer = new StreetsBaseLayer(RuntimeEnvironment.application);

        baseLayerSwitcherPlugin.addBaseLayer(satelliteBaseLayer, false);
        baseLayerSwitcherPlugin.addBaseLayer(streetsBaseLayer, false);

        Mockito.doReturn(true)
                .when(style)
                .isFullyLoaded();

        assertNull(ReflectionHelpers.getField(baseLayerSwitcherPlugin, "currentBaseLayer"));

        baseLayerSwitcherPlugin.showBaseLayer(satelliteBaseLayer);

        Mockito.verify(kujakuMapView, Mockito.times(1))
                .addLayer(Mockito.eq(satelliteBaseLayer));
        assertEquals(satelliteBaseLayer, ReflectionHelpers.getField(baseLayerSwitcherPlugin, "currentBaseLayer"));
    }*/

    /*@Test
    public void showBaseLayerShouldDisablePreviousBaseLayer() {
        SatelliteBaseLayer satelliteBaseLayer = new SatelliteBaseLayer();
        StreetsBaseLayer streetsBaseLayer = new StreetsBaseLayer(RuntimeEnvironment.application);

        baseLayerSwitcherPlugin.addBaseLayer(satelliteBaseLayer, false);
        baseLayerSwitcherPlugin.addBaseLayer(streetsBaseLayer, false);

        Mockito.doReturn(true)
                .when(style)
                .isFullyLoaded();

        ReflectionHelpers.setField(baseLayerSwitcherPlugin, "currentBaseLayer", streetsBaseLayer);

        baseLayerSwitcherPlugin.showBaseLayer(satelliteBaseLayer);

        Mockito.verify(kujakuMapView, Mockito.times(1))
                .removeLayer(Mockito.eq(satelliteBaseLayer));
    }*/
}