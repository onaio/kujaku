package io.ona.kujaku.manager;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.robolectric.RobolectricTestRunner;

import io.ona.kujaku.BaseTest;


/**
 * Created by Emmanuel Otin - eo@novel-t.ch 24/06/19.
 */
@RunWith(RobolectricTestRunner.class)
public class KujakuCircleOptionsTest extends BaseTest {

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void createInstanceOfDrawingManager() {
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
}