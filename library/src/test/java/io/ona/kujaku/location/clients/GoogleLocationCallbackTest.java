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
    private String isBetterLocation = "isBetterLocation";
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
        assertTrue(ReflectionHelpers.callInstanceMethod(googleLocationCallback, isSameProvider
                , ReflectionHelpers.ClassParameter.from(String.class, LocationManager.NETWORK_PROVIDER)
                , ReflectionHelpers.ClassParameter.from(String.class, LocationManager.NETWORK_PROVIDER)));
    }

    @Test
    public void isSameProviderWhenGivenDifferentProvidersShouldReturnFalse() {
        assertFalse(ReflectionHelpers.callInstanceMethod(googleLocationCallback, isSameProvider
                , ReflectionHelpers.ClassParameter.from(String.class, LocationManager.GPS_PROVIDER)
                , ReflectionHelpers.ClassParameter.from(String.class, LocationManager.NETWORK_PROVIDER)));
    }

    @Test
    public void isSameProviderWhenGivenNullsShouldReturnTrue() {
        assertTrue(ReflectionHelpers.callInstanceMethod(googleLocationCallback, isSameProvider
                , ReflectionHelpers.ClassParameter.from(String.class, null)
                , ReflectionHelpers.ClassParameter.from(String.class, null)));
    }

    @Test
    public void isSameProviderWhenGivenProviderAndNullShouldReturnFalse() {
        assertFalse(ReflectionHelpers.callInstanceMethod(googleLocationCallback, isSameProvider
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

    @Test
    public void isBetterLocationWhenGivenSignificantlyNewerLocationShouldReturnTrue() {
        int significantlyOlder = (2 * 60 * 1000) + 20;
        long timeNow = System.currentTimeMillis();

        Location newLocation = generateLocation(12, timeNow);
        Location currentLocation = generateLocation(3, timeNow - significantlyOlder);

        assertTrue(ReflectionHelpers.callInstanceMethod(GoogleLocationClient.GoogleLocationCallback.class, googleLocationCallback
                , isBetterLocation, ReflectionHelpers.ClassParameter.from(Location.class, newLocation)
                , ReflectionHelpers.ClassParameter.from(Location.class, currentLocation)));
    }

    @Test
    public void isBetterLocationWhenGivenSignificantlyOlderLocationShouldReturnFalse() {
        int significantlyOlder = (2 * 60 * 1000) + 20;
        long timeNow = System.currentTimeMillis();

        Location currentLocation = generateLocation(12, timeNow);
        Location newLocation = generateLocation(3, timeNow - significantlyOlder);

        assertFalse(ReflectionHelpers.callInstanceMethod(GoogleLocationClient.GoogleLocationCallback.class, googleLocationCallback
                , isBetterLocation, ReflectionHelpers.ClassParameter.from(Location.class, newLocation)
                , ReflectionHelpers.ClassParameter.from(Location.class, currentLocation)));
    }

    @Test
    public void setIsBetterLocationWhenGivenMoreAccurateLocationShouldReturnTrue() {
        int olderTime = 30 * 1000;
        long timeNow = System.currentTimeMillis();

        Location currentLocation = generateLocation(12, timeNow - olderTime);
        Location newLocation = generateLocation(3, timeNow);

        assertTrue(ReflectionHelpers.callInstanceMethod(GoogleLocationClient.GoogleLocationCallback.class, googleLocationCallback
                , isBetterLocation, ReflectionHelpers.ClassParameter.from(Location.class, newLocation)
                , ReflectionHelpers.ClassParameter.from(Location.class, currentLocation)));
    }

    @Test
    public void setIsBetterLocationWhenGivenNewerAndNotLessAccurateLocationShouldReturnTrue() {
        int olderTime = 30 * 1000;
        long timeNow = System.currentTimeMillis();

        Location currentLocation = generateLocation(12, timeNow - olderTime);
        Location newLocation = generateLocation(12, timeNow);

        assertTrue(ReflectionHelpers.callInstanceMethod(GoogleLocationClient.GoogleLocationCallback.class, googleLocationCallback
                , isBetterLocation, ReflectionHelpers.ClassParameter.from(Location.class, newLocation)
                , ReflectionHelpers.ClassParameter.from(Location.class, currentLocation)));
    }

    @Test
    public void setIsBetterLocationWhenGivenNewerAndNotSignificantlyLessAccurateLocationFromSameProviderShouldReturnTrue() {
        int olderTime = 30 * 1000;
        long timeNow = System.currentTimeMillis();

        Location currentLocation = generateLocation(12, timeNow - olderTime);
        Location newLocation = generateLocation(20, timeNow);

        assertTrue(ReflectionHelpers.callInstanceMethod(GoogleLocationClient.GoogleLocationCallback.class, googleLocationCallback
                , isBetterLocation, ReflectionHelpers.ClassParameter.from(Location.class, newLocation)
                , ReflectionHelpers.ClassParameter.from(Location.class, currentLocation)));
    }

    @Test
    public void setIsBetterLocationWhenGivenNewerAndSignificantlyLessAccurateLocationFromSameProviderShouldReturnFalse() {
        int olderTime = 30 * 1000;
        long timeNow = System.currentTimeMillis();

        Location currentLocation = generateLocation(12, timeNow - olderTime);
        Location newLocation = generateLocation(220, timeNow);

        assertFalse(ReflectionHelpers.callInstanceMethod(GoogleLocationClient.GoogleLocationCallback.class, googleLocationCallback
                , isBetterLocation, ReflectionHelpers.ClassParameter.from(Location.class, newLocation)
                , ReflectionHelpers.ClassParameter.from(Location.class, currentLocation)));
    }

    @Test
    public void setIsBetterLocationWhenGivenOlderAndSameAccuracyLocationFromSameProviderShouldReturnFalse() {
        int olderTime = 30 * 1000;
        long timeNow = System.currentTimeMillis();

        Location currentLocation = generateLocation(12, timeNow);
        Location newLocation = generateLocation(12, timeNow - olderTime);

        assertFalse(ReflectionHelpers.callInstanceMethod(GoogleLocationClient.GoogleLocationCallback.class, googleLocationCallback
                , isBetterLocation, ReflectionHelpers.ClassParameter.from(Location.class, newLocation)
                , ReflectionHelpers.ClassParameter.from(Location.class, currentLocation)));
    }

    private Location generateLocation(float horizontalAccuracy, long time) {
        Location location = generateRandomLocation();
        location.setAccuracy(horizontalAccuracy);
        location.setTime(time);

        return location;
    }

    private Location generateRandomLocation() {
        double minLat = -30d;
        double maxLat = 60d;
        double minLon = -30d;
        double maxLon = 60d;

        Location location = new Location("test_provider");
        location.setLatitude((Math.random() * (maxLat - minLat)) + minLat);
        location.setLongitude((Math.random() * (maxLon - minLon)) + minLon);

        return location;
    }
}
