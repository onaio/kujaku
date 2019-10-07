package io.ona.kujaku.mbtiles;

import android.content.Context;
import android.util.Pair;

import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.FillLayer;
import com.mapbox.mapboxsdk.style.layers.Layer;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.RasterLayer;
import com.mapbox.mapboxsdk.style.sources.RasterSource;
import com.mapbox.mapboxsdk.style.sources.Source;
import com.mapbox.mapboxsdk.utils.ThreadUtils;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.util.ReflectionHelpers;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import io.ona.kujaku.BaseTest;
import io.ona.kujaku.plugin.switcher.BaseLayerSwitcherPlugin;
import io.ona.kujaku.plugin.switcher.layer.MBTilesLayer;
import io.ona.kujaku.test.shadows.ShadowFillLayer;
import io.ona.kujaku.test.shadows.ShadowLayer;
import io.ona.kujaku.test.shadows.ShadowLineLayer;
import io.ona.kujaku.test.shadows.ShadowRasterLayer;
import io.ona.kujaku.test.shadows.ShadowRasterSource;
import io.ona.kujaku.test.shadows.ShadowSource;
import io.ona.kujaku.test.shadows.ShadowVectorSource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Created by samuelgithengi on 10/7/19.
 */
@Config(shadows = {ShadowFillLayer.class, ShadowSource.class, ShadowVectorSource.class, ShadowRasterSource.class, ShadowLayer.class, ShadowLineLayer.class, ShadowRasterLayer.class})
public class MBTilesHelperTest extends BaseTest {


    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    private Context context = RuntimeEnvironment.application;

    @Mock
    private Style style;

    @Mock
    private BaseLayerSwitcherPlugin switcherPlugin;

    private MBTilesHelper mbTilesHelper;

    @Before
    public void setUp() {
        ThreadUtils.init(context);
        mbTilesHelper = new MBTilesHelper();
    }

    @Test
    public void testInitializeMbTilesLayersWithVectorTiles() {
        List<File> offlineFiles = Collections.singletonList(new File("src/test/resources/trails.mbtiles"));
        mbTilesHelper.initializeMbTileslayers(style, offlineFiles);
        assertNotNull(mbTilesHelper.tileServer);
        verify(style).addLayer(any(FillLayer.class));
        verify(style).addLayer(any(LineLayer.class));
        verify(style).addSource(any(Source.class));
    }

    @Test
    public void testInitializeMbTilesLayersWithRasterTiles() {
        List<File> offlineFiles = Collections.singletonList(new File("src/test/resources/raster.mbtiles"));
        mbTilesHelper.initializeMbTileslayers(style, offlineFiles);
        assertNotNull(mbTilesHelper.tileServer);
        verify(style).addLayer(any(RasterLayer.class));
        verify(style).addSource(any(Source.class));
    }


    @Test
    public void testInitializeMbTilesLayersWithSingleFile() {
        Pair<Set<Source>, Set<Layer>> layersAndSources = mbTilesHelper.initializeMbTileslayers(new File("src/test/resources/raster.mbtiles"));
        assertNotNull(layersAndSources);
        assertEquals(1, layersAndSources.first.size());
        assertEquals(1, layersAndSources.second.size());
        assertNotNull(mbTilesHelper.tileServer);
        Source source = layersAndSources.first.iterator().next();
        Layer layer = layersAndSources.second.iterator().next();
        assertTrue(source instanceof RasterSource);
        assertTrue(layer instanceof RasterLayer);


    }


    @Test
    public void testOnDestroy() {
        mbTilesHelper.initializeMbTileslayers(new File("src/test/resources/raster.mbtiles"));
        mbTilesHelper.onDestroy();
        assertFalse(mbTilesHelper.tileServer.isStarted());
    }


    @Test
    public void testSetMBTileLayers() {
        ReflectionHelpers.setField(mbTilesHelper, "mbtilesDir", new File("src/test/resources/"));
        mbTilesHelper.setMBTileLayers(context, switcherPlugin);
        verify(switcherPlugin, times(2)).addBaseLayer(any(MBTilesLayer.class), eq(false));
    }
}
