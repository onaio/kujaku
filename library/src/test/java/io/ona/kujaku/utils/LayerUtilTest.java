package io.ona.kujaku.utils;

import com.mapbox.mapboxsdk.style.layers.Layer;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import io.ona.kujaku.BuildConfig;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 2019-05-20
 */
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class,
        manifest = Config.NONE)
public class LayerUtilTest {

    private LayerUtil layerUtil;

    @Before
    public void setUp() {
        layerUtil = new LayerUtil();
    }


    @Test
    public void testGetLayer() {
        Layer layer = layerUtil.getLayer("{\n" +
                "      \"id\": \"background\",\n" +
                "      \"type\": \"background\",\n" +
                "      \"layout\": {},\n" +
                "      \"paint\": {\n" +
                "        \"background-color\": \"rgb(4,7,14)\"\n" +
                "      }\n" +
                "    }");

        Assert.assertNull(layer);
    }

}
