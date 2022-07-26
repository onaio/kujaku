package io.ona.kujaku.location.clients;

import android.location.Location;
import android.location.LocationListener;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.junit.Before;
import org.junit.Test;

import io.ona.kujaku.BaseTest;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 2019-11-25
 */

public class BaseLocationClientTest extends BaseTest {

    private TestLocationClient testLocationClient;

    @Before
    public void setUp() throws NoSuchFieldException, IllegalAccessException {
        testLocationClient = new TestLocationClient();
    }

    @Test
    public void isBetterLocationWhenGivenSignificantlyNewerLocationShouldReturnTrue() {
        int significantlyOlder = (2 * 60 * 1000) + 20;
        long timeNow = System.currentTimeMillis();

        Location newLocation = generateLocation(12, timeNow);
        Location currentLocation = generateLocation(3, timeNow - significantlyOlder);

        assertTrue(testLocationClient.isBetterLocation(newLocation, currentLocation));
    }

    @Test
    public void isBetterLocationWhenGivenSignificantlyOlderLocationShouldReturnFalse() {
        int significantlyOlder = (2 * 60 * 1000) + 20;
        long timeNow = System.currentTimeMillis();

        Location currentLocation = generateLocation(12, timeNow);
        Location newLocation = generateLocation(3, timeNow - significantlyOlder);

        assertFalse(testLocationClient.isBetterLocation(newLocation, currentLocation));
    }

    @Test
    public void setIsBetterLocationWhenGivenMoreAccurateLocationShouldReturnTrue() {
        int olderTime = 30 * 1000;
        long timeNow = System.currentTimeMillis();

        Location currentLocation = generateLocation(12, timeNow - olderTime);
        Location newLocation = generateLocation(3, timeNow);

        assertTrue(testLocationClient.isBetterLocation(newLocation, currentLocation));
    }

    @Test
    public void setIsBetterLocationWhenGivenNewerAndNotLessAccurateLocationShouldReturnTrue() {
        int olderTime = 30 * 1000;
        long timeNow = System.currentTimeMillis();

        Location currentLocation = generateLocation(12, timeNow - olderTime);
        Location newLocation = generateLocation(12, timeNow);

        assertTrue(testLocationClient.isBetterLocation(newLocation, currentLocation));
    }

    @Test
    public void setIsBetterLocationWhenGivenNewerAndNotSignificantlyLessAccurateLocationFromSameProviderShouldReturnTrue() {
        int olderTime = 30 * 1000;
        long timeNow = System.currentTimeMillis();

        Location currentLocation = generateLocation(12, timeNow - olderTime);
        Location newLocation = generateLocation(20, timeNow);

        assertTrue(testLocationClient.isBetterLocation(newLocation, currentLocation));
    }

    @Test
    public void setIsBetterLocationWhenGivenNewerAndSignificantlyLessAccurateLocationFromSameProviderShouldReturnFalse() {
        int olderTime = 30 * 1000;
        long timeNow = System.currentTimeMillis();

        Location currentLocation = generateLocation(12, timeNow - olderTime);
        Location newLocation = generateLocation(220, timeNow);

        testLocationClient.isBetterLocation(newLocation, currentLocation);
    }

    @Test
    public void setIsBetterLocationWhenGivenOlderAndSameAccuracyLocationFromSameProviderShouldReturnFalse() {
        int olderTime = 30 * 1000;
        long timeNow = System.currentTimeMillis();

        Location currentLocation = generateLocation(12, timeNow);
        Location newLocation = generateLocation(12, timeNow - olderTime);

        testLocationClient.isBetterLocation(newLocation, currentLocation);
    }

    public class TestLocationClient extends BaseLocationClient {

        @Override
        public void stopLocationUpdates() {
            // Do nothing
        }

        @Nullable
        @Override
        public Location getLastLocation() {
            return null;
        }

        @Override
        public void requestLocationUpdates(@NonNull LocationListener locationListener) {
            // Do nothing
        }

        @Override
        public void setUpdateIntervals(long updateInterval, long fastestUpdateInterval) {
            // Do nothing
        }

        @NonNull
        @Override
        public String getProvider() {
            return "test_provider";
        }
    }
}
