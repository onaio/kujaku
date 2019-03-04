package io.ona.kujaku.layers;

import android.support.annotation.NonNull;

import com.mapbox.geojson.Point;

import org.robolectric.annotation.Config;

import io.ona.kujaku.BaseTest;
import io.ona.kujaku.test.shadows.ShadowGeoJsonSource;
import io.ona.kujaku.test.shadows.ShadowLayer;
import io.ona.kujaku.test.shadows.ShadowLineLayer;
import io.ona.kujaku.test.shadows.ShadowSymbolLayer;

import static org.junit.Assert.assertEquals;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 22/02/2019
 */
@Config(shadows = {ShadowGeoJsonSource.class, ShadowSymbolLayer.class, ShadowLineLayer.class, ShadowLayer.class})
public abstract class BaseKujakuLayerTest extends BaseTest {

    protected void assertPointEquals(@NonNull Point expected, @NonNull Point actual) {
        assertEquals(expected.latitude(), actual.latitude(), 0d);
        assertEquals(expected.longitude(), actual.longitude(), 0d);
    }

}
