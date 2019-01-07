package io.ona.kujaku.wmts;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.InputStreamReader;

import io.ona.kujaku.BuildConfig;
import io.ona.kujaku.wmts.model.WmtsCapabilities;
import io.ona.kujaku.wmts.serializer.WmtsCapabilitiesSerializer;

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
        Assert.assertEquals(capabilities.getVersion(), "1.0.0");
        Assert.assertEquals(capabilities.getLayers().size(), 1);
        Assert.assertNull(capabilities.getLayer("0123456789"));
        Assert.assertNotNull(capabilities.getLayer("Vegetation_Mapping_Texas_Ecological_Mapping_Systems_Data"));
    }
}
