package io.ona.kujaku.wmts;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.InputStreamReader;

import io.ona.kujaku.BuildConfig;
import io.ona.kujaku.wmts.model.WmtsCapabilities;
import io.ona.kujaku.wmts.model.WmtsLayer;
import io.ona.kujaku.wmts.model.WmtsStyle;
import io.ona.kujaku.wmts.serializer.WmtsCapabilitiesSerializer;
import io.realm.internal.Capabilities;

/**
 *
 * Created by Emmanuel Otin - eo@novel-t.ch 12/04/18.
 */
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, manifest = Config.NONE)
public class WmtsCapabilitiesSerializerTest {

    private WmtsCapabilities getCapabilities() throws Exception {

        WmtsCapabilities capabilities = null ;

        InputStreamReader streamReader = new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream("Capabilities.xml"));
        WmtsCapabilitiesSerializer serializer = new WmtsCapabilitiesSerializer();
        capabilities = serializer.read(WmtsCapabilities.class, streamReader, false);

        return capabilities;
    }

    @Test
    public void readCapabilities() throws Exception {

        WmtsCapabilities capabilities = getCapabilities() ;
        Assert.assertNotNull(capabilities);
    }

    @Test
    public void testCapabilitiesContent() throws Exception {
        WmtsCapabilities capabilities = getCapabilities() ;
        Assert.assertEquals(capabilities.getServiceIdentification().getTitles().get(0).getValue(), "Vegetation_Mapping_Texas_Ecological_Mapping_Systems_Data");
        Assert.assertEquals(capabilities.getServiceIdentification().getTitle("fr"), "No Title found");
        Assert.assertEquals(capabilities.getVersion(), "1.0.0");
        Assert.assertEquals(capabilities.getLayers().size(), 1);
        Assert.assertNull(capabilities.getLayer("0123456789"));
        Assert.assertNotNull(capabilities.getLayer("Vegetation_Mapping_Texas_Ecological_Mapping_Systems_Data"));
        Assert.assertEquals(capabilities.getMaximumTileMatrixZoom("unknownTileMatrixIdentifier"), 0);
        Assert.assertEquals(capabilities.getMaximumTileMatrixZoom("default028mm"), 19);
        Assert.assertEquals(capabilities.getMinimumTileMatrixZoom("default028mm"), 0);
        Assert.assertEquals(capabilities.getMaximumTileMatrixZoom("GoogleMapsCompatible"), 18);
        Assert.assertEquals(capabilities.getTilesSize("default028mm"), 256);
        Assert.assertEquals(capabilities.getTilesSize("GoogleMapsCompatible"), 256);
        Assert.assertEquals(capabilities.getTilesSize("unknownTileMatrixIdentifier"), 0);

        WmtsLayer layer = capabilities.getLayer("Vegetation_Mapping_Texas_Ecological_Mapping_Systems_Data");

        // Style :
        WmtsStyle style = layer.getStyle("default");
        Assert.assertTrue(style.isDefault());
        Assert.assertEquals(style.getIdentifier(), "default");
        Assert.assertEquals(style.getTitles().size(), 1);

        //TileMatrixSet
        Assert.assertNotNull(layer.getTileMatrixSetLink("default028mm"));

        // WmtsLayers
        layer.setMaximumZoom(capabilities.getMaximumTileMatrixZoom("default028mm"));
        layer.setMinimumZoom(capabilities.getMinimumTileMatrixZoom("default028mm"));

        Assert.assertEquals(layer.getMaximumZoom(),19);
        Assert.assertEquals(layer.getMinimumZoom(),0);

    }
}
