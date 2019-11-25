package io.ona.kujaku.location.clients;

import android.location.Location;
import android.location.LocationManager;

import com.google.android.gms.location.LocationResult;

import org.junit.Before;
import org.junit.Test;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.util.ReflectionHelpers;

import java.util.ArrayList;

import io.ona.kujaku.BaseTest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 21/02/2019
 */
public class GoogleLocationCallbackTest extends BaseTest {

    private GoogleLocationClient.GoogleLocationCallback googleLocationCallback;
    private String isSameProvider = "isSameProvider";
    private GoogleLocationClient googleLocationClient;

    @Before
    public void setUp() throws NoSuchFieldException, IllegalAccessException {
        googleLocationClient = new GoogleLocationClient(RuntimeEnvironment.application);
        googleLocationCallback = (GoogleLocationClient.GoogleLocationCallback) getValueInPrivateField(GoogleLocationClient.class
                , googleLocationClient
                , "googleLocationCallback");
    }

    @Test
    public void isSameProviderWhenGivenSameProviderShouldReturnTrue() {
        assertTrue(ReflectionHelpers.callInstanceMethod(googleLocationClient, isSameProvider
                , ReflectionHelpers.ClassParameter.from(String.class, LocationManager.NETWORK_PROVIDER)
                , ReflectionHelpers.ClassParameter.from(String.class, LocationManager.NETWORK_PROVIDER)));
    }

    @Test
    public void isSameProviderWhenGivenDifferentProvidersShouldReturnFalse() {
        assertFalse(ReflectionHelpers.callInstanceMethod(googleLocationClient, isSameProvider
                , ReflectionHelpers.ClassParameter.from(String.class, LocationManager.GPS_PROVIDER)
                , ReflectionHelpers.ClassParameter.from(String.class, LocationManager.NETWORK_PROVIDER)));
    }

    @Test
    public void isSameProviderWhenGivenNullsShouldReturnTrue() {
        assertTrue(ReflectionHelpers.callInstanceMethod(googleLocationClient, isSameProvider
                , ReflectionHelpers.ClassParameter.from(String.class, null)
                , ReflectionHelpers.ClassParameter.from(String.class, null)));
    }

    @Test
    public void isSameProviderWhenGivenProviderAndNullShouldReturnFalse() {
        assertFalse(ReflectionHelpers.callInstanceMethod(googleLocationClient, isSameProvider
                , ReflectionHelpers.ClassParameter.from(String.class, null)
                , ReflectionHelpers.ClassParameter.from(String.class, LocationManager.NETWORK_PROVIDER)));
    }

    @Test
    public void onLocationResultWhenGivenMultipleLocationsShouldPickTheMostAccurateWhileNotSignificantlyOlder() throws NoSuchFieldException, IllegalAccessException {
        int slightlyOlder = 60 * 1000;

        long timeNow = System.currentTimeMillis();

        ArrayList<Location> locations = new ArrayList<>();
        locations.add(generateLocation(65, timeNow));
        locations.add(generateLocation(30, timeNow));
        locations.add(generateLocation(7, timeNow - slightlyOlder));
        locations.add(generateLocation(20, timeNow - slightlyOlder));

        googleLocationCallback.onLocationResult(LocationResult.create(locations));

        Location location = (Location) getValueInPrivateField(GoogleLocationClient.class, googleLocationClient, "lastLocation");
        assertEquals(7f, location.getAccuracy(), 0f);
    }

}
