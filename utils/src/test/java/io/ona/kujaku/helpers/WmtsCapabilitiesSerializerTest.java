package io.ona.kujaku.helpers;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.InputStreamReader;

import io.ona.kujaku.utils.BuildConfig;
import io.ona.kujaku.utils.helpers.WmtsCapabilitiesSerializer;
import io.ona.kujaku.utils.wmts.model.WmtsCapabilities;

/**
 *
 * Created by Emmanuel Otin - eo@novel-t.ch 12/04/18.
 */
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, manifest = Config.NONE)
public class WmtsCapabilitiesSerializerTest {

    private WmtsCapabilities getCapabilities() {

        WmtsCapabilities capabilities = null ;

        try {
            InputStreamReader streamReader = new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream("Capabilities.xml"));
            WmtsCapabilitiesSerializer serializer = new WmtsCapabilitiesSerializer();
            capabilities = serializer.read(WmtsCapabilities.class, streamReader, false);

        } catch (Exception ex) {
        }

        return capabilities;
    }

    @Test
    public void readCapabilities() {

        WmtsCapabilities capabilities = getCapabilities() ;
        Assert.assertNotNull(capabilities);
    }

    @Test
    public void testCapabilitiesContent() {
        WmtsCapabilities capabilities = getCapabilities() ;
        Assert.assertEquals(capabilities.getVersion(), "1.0.0");
        Assert.assertEquals(capabilities.getLayers().size(), 1);
        Assert.assertNull(capabilities.getLayer("0123456789"));
        Assert.assertNotNull(capabilities.getLayer("Vegetation_Mapping_Texas_Ecological_Mapping_Systems_Data"));
    }
}
