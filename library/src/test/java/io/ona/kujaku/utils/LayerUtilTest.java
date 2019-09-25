package io.ona.kujaku.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;

import com.mapbox.mapboxsdk.style.layers.BackgroundLayer;
import com.mapbox.mapboxsdk.style.layers.Layer;
import com.mapbox.mapboxsdk.style.layers.RasterLayer;
import com.mapbox.mapboxsdk.utils.ThreadUtils;

import junit.framework.Assert;

import org.json.JSONArray;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadow.api.Shadow;

import java.io.IOException;

import io.ona.kujaku.BaseTest;
import io.ona.kujaku.test.shadows.ShadowBackgroundLayer;
import io.ona.kujaku.test.shadows.ShadowLayer;
import io.ona.kujaku.test.shadows.ShadowRasterLayer;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 2019-05-22
 */

@Config(shadows = {ShadowLayer.class, ShadowBackgroundLayer.class, ShadowRasterLayer.class})
public class LayerUtilTest extends BaseTest {

    private Context context;
    private LayerUtil layerUtil;

    @Before
    public void setUp() {
        context = RuntimeEnvironment.application;
        layerUtil = new LayerUtil();
        context.getApplicationInfo().flags = ApplicationInfo.FLAG_DEBUGGABLE;
        ThreadUtils.init(context);
    }

    @Test
    public void getLayerShouldParseBackgroundLayer() throws JSONException, IOException {
            JSONArray jsonArray = new JSONArray(
                    IOUtil.readInputStreamAsString(context.getAssets().open("streets-base-layers.json"))
            );

        Layer backgroundLayer = layerUtil.getLayer(jsonArray.get(0).toString());

        Assert.assertTrue(backgroundLayer instanceof BackgroundLayer);
        ShadowLayer shadowLayer = (ShadowLayer) Shadow.extract(backgroundLayer);
        Assert.assertTrue(shadowLayer.getPropertyValues().containsKey("background-color"));
    }

    @Test
    public void getLayerShouldParseRasterLayer() {
        String rasterLayerJson = "{\n" +
                "      \"id\": \"mapbox-mapbox-satellite\",\n" +
                "      \"type\": \"raster\",\n" +
                "      \"source\": \"mapbox://mapbox.satellite\",\n" +
                "      \"layout\": {},\n" +
                "      \"paint\": {}\n" +
                "    }";

        Layer rasterLayer = layerUtil.getLayer(rasterLayerJson);
        Assert.assertTrue(rasterLayer instanceof RasterLayer);
        ShadowLayer shadowLayer = (ShadowLayer) Shadow.extract(rasterLayer);
        Assert.assertEquals(0, shadowLayer.getPropertyValues().size());
    }

    @Test
    public void getFloatArray() throws JSONException {
        JSONArray jsonArray = new JSONArray("[7, 90, 78, 45, 858, 86, 958]");

        Float[] result = layerUtil.getFloatArray(jsonArray);

        Assert.assertEquals(7, result.length);
        Assert.assertEquals(7f, result[0]);
        Assert.assertEquals(90f, result[1]);
        Assert.assertEquals(958f, result[6]);
    }
}