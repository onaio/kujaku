package io.ona.kujaku.layers;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import androidx.annotation.NonNull;

import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.utils.ThreadUtils;

import org.junit.Before;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import io.ona.kujaku.BaseTest;
import io.ona.kujaku.test.shadows.ShadowCircleLayer;
import io.ona.kujaku.test.shadows.ShadowFillLayer;
import io.ona.kujaku.test.shadows.ShadowGeoJsonSource;
import io.ona.kujaku.test.shadows.ShadowLayer;
import io.ona.kujaku.test.shadows.ShadowLineLayer;
import io.ona.kujaku.test.shadows.ShadowSymbolLayer;

import static org.junit.Assert.assertEquals;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 22/02/2019
 */
@Config(shadows = {ShadowGeoJsonSource.class,
        ShadowSymbolLayer.class,
        ShadowLineLayer.class,
        ShadowFillLayer.class,
        ShadowLayer.class,
        ShadowCircleLayer.class})
public abstract class BaseKujakuLayerTest extends BaseTest {

    @Before
    public void setUp() {
        Context context = RuntimeEnvironment.application;
        context.getApplicationInfo().flags = ApplicationInfo.FLAG_DEBUGGABLE;
        ThreadUtils.init(RuntimeEnvironment.application);
    }

    protected void assertPointEquals(@NonNull Point expected, @NonNull Point actual) {
        assertEquals(expected.latitude(), actual.latitude(), 0d);
        assertEquals(expected.longitude(), actual.longitude(), 0d);
    }

}
