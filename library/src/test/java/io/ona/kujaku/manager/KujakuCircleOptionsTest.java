package io.ona.kujaku.manager;

import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.plugins.annotation.Circle;
import com.mapbox.mapboxsdk.plugins.annotation.CircleOptions;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.robolectric.RobolectricTestRunner;

import io.ona.kujaku.BaseTest;


/**
 * Created by Emmanuel Otin - eo@novel-t.ch 24/06/19.
 */
@RunWith(RobolectricTestRunner.class)
public class KujakuCircleOptionsTest extends BaseTest {

    @Test
    public void testKujakuCircleOptions() {
        KujakuCircleOptions options = DrawingManager.getKujakuCircleOptions();
        KujakuCircleOptions optionsMiddle = DrawingManager.getKujakuCircleMiddleOptions();
        KujakuCircleOptions optionsDraggable = DrawingManager.getKujakuCircleDraggableOptions();

        // Draggable
        Assert.assertTrue(optionsDraggable.getDraggable());
        Assert.assertFalse(options.getDraggable());
        Assert.assertFalse(optionsMiddle.getDraggable());

        // Middle
        Assert.assertTrue(optionsMiddle.getMiddleCircle());
        Assert.assertFalse(options.getMiddleCircle());
        Assert.assertFalse(optionsDraggable.getMiddleCircle());
    }

    @Test
    public void testCustomKujakuCircleOptions() {

        KujakuCircleOptions kujakuOptions = new KujakuCircleOptions()
                .withCircleBlur(10f)
                .withCircleOpacity(5f)
                .withCircleStrokeWidth(4f)
                .withCircleColor("red")
                .withCircleStrokeOpacity(2f)
                .withGeometry(Point.fromLngLat(1,2));


        CircleOptions option = (CircleOptions) kujakuOptions;

        Assert.assertEquals(option.getCircleBlur(), kujakuOptions.getCircleBlur());
        Assert.assertEquals(option.getCircleOpacity(), kujakuOptions.getCircleOpacity());
        Assert.assertEquals(option.getCircleStrokeWidth(), kujakuOptions.getCircleStrokeWidth());
        Assert.assertEquals(option.getCircleColor(), kujakuOptions.getCircleColor());
        Assert.assertEquals(option.getCircleStrokeOpacity(), kujakuOptions.getCircleStrokeOpacity());
    }
}