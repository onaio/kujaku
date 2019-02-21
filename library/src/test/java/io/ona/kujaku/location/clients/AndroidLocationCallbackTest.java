package io.ona.kujaku.location.clients;

import android.location.LocationManager;

import org.junit.Before;
import org.junit.Test;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.util.ReflectionHelpers;

import io.ona.kujaku.BaseTest;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 21/02/2019
 */
public class AndroidLocationCallbackTest extends BaseTest {

    private AndroidLocationClient.AndroidLocationCallback androidLocationCallback;
    private String isSameProvider = "isSameProvider";
    private String isBetterLocation = "isBetterLocation";

    @Before
    public void setUp() throws NoSuchFieldException, IllegalAccessException {
        AndroidLocationClient androidLocationClient = new AndroidLocationClient(RuntimeEnvironment.application);
        androidLocationCallback = (AndroidLocationClient.AndroidLocationCallback) getValueInPrivateField(AndroidLocationClient.class
                , androidLocationClient
                , "androidLocationCallback");
    }

    @Test
    public void isSameProviderWhenGivenNonNullsShouldEvaluateCorrectly() {

        assertFalse(ReflectionHelpers.callInstanceMethod(androidLocationCallback, isSameProvider
                , ReflectionHelpers.ClassParameter.from(String.class, LocationManager.GPS_PROVIDER)
                , ReflectionHelpers.ClassParameter.from(String.class, LocationManager.NETWORK_PROVIDER)));


        assertTrue(ReflectionHelpers.callInstanceMethod(androidLocationCallback, isSameProvider
                , ReflectionHelpers.ClassParameter.from(String.class, LocationManager.NETWORK_PROVIDER)
                , ReflectionHelpers.ClassParameter.from(String.class, LocationManager.NETWORK_PROVIDER)));
    }

    @Test
    public void isSameProviderWhenGivenNullsShouldWorkOK() {
        String isSameProvider = "isSameProvider";

        assertTrue(ReflectionHelpers.callInstanceMethod(androidLocationCallback, isSameProvider
                , ReflectionHelpers.ClassParameter.from(String.class, null)
                , ReflectionHelpers.ClassParameter.from(String.class, null)));


        assertFalse(ReflectionHelpers.callInstanceMethod(androidLocationCallback, isSameProvider
                , ReflectionHelpers.ClassParameter.from(String.class, null)
                , ReflectionHelpers.ClassParameter.from(String.class, LocationManager.NETWORK_PROVIDER)));
    }

    @Test
    public void isBetterLocationWhenGivenSignificantlyNewerLocationShouldReturnTrue() {

    }
}
