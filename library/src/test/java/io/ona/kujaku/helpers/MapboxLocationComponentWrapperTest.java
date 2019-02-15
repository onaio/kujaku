package io.ona.kujaku.helpers;

import org.junit.Test;

import io.ona.kujaku.exceptions.LocationComponentInitializationException;

/**
 * @author Vincent Karuri
 */
public class MapboxLocationComponentWrapperTest {
    @Test(expected = LocationComponentInitializationException.class)
    public void testExecptionIsThrownIfGetInstanceMethodIsCalledBeforeInit() {
        MapboxLocationComponentWrapper.getInstance();
    }
}
