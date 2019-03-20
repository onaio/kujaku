package io.ona.kujaku.services;


import android.content.Context;
import android.location.Location;
import android.location.LocationManager;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.controller.ServiceController;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLocationManager;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import io.ona.kujaku.BuildConfig;
import io.ona.kujaku.activities.MapActivity;
import io.ona.kujaku.listeners.TrackingServiceListener;
import io.ona.kujaku.services.options.TrackingServiceHighAccuracyOptions;
import io.ona.kujaku.services.options.TrackingServiceSaveBatteryOptions;

import static android.location.LocationManager.GPS_PROVIDER;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.robolectric.Shadows.shadowOf;

/**
 * 
 * Created by Emmanuel Otin - eo@novel-t.ch 03/20/19.
 */
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class,
        manifest = Config.NONE)
public class TrackingServiceTest {

    private static final String TAG = TrackingServiceTest.class.getSimpleName();
    private Context context;

    private ServiceController<TrackingService> controller;

    private Location location1;
    private Location location2;
    private Location location3;

    @Before
    public void setUp() {
        context = RuntimeEnvironment.application;

        location1 = new Location(GPS_PROVIDER);
        location1.setAccuracy(20);
        location1.setLongitude(6.055484);
        location1.setLatitude(46.2182335);

        location2 = new Location(GPS_PROVIDER);
        location2.setAccuracy(19);
        location2.setLongitude(6.055485);
        location2.setLatitude(46.2182335);

        location3 = new Location(GPS_PROVIDER);
        location3.setAccuracy(20);
        location3.setLongitude(1.1);
        location3.setLatitude(1.1);

    }

    @After
    public void tearDown() {
        controller.destroy();
    }

    @Test
    public void testTrackingServiceStatusAtCreation() {
        controller = Robolectric.buildService(TrackingService.class,
                TrackingService.getIntent(context, MapActivity.class, new TrackingServiceHighAccuracyOptions()));

        assertEquals(TrackingService.TrackingServiceStatus.STOPPED, TrackingService.getTrackingServiceStatus());
        assertFalse(TrackingService.isRunning());
        assertNull(null, controller.get().getRecordedLocations());
    }

    @Test
    public void testStartingServiceWithoutGpsProvider() {
        controller = Robolectric.buildService(TrackingService.class,
                TrackingService.getIntent(context, MapActivity.class, new TrackingServiceHighAccuracyOptions()));

        controller.create().startCommand(0,0);
        assertEquals(TrackingService.TrackingServiceStatus.STOPPED_GPS, TrackingService.getTrackingServiceStatus());

    }

    @Test
    public void testStartingServiceWithGpsProvider() {
        controller = Robolectric.buildService(TrackingService.class,
                TrackingService.getIntent(context, MapActivity.class, new TrackingServiceHighAccuracyOptions()));

        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        ShadowLocationManager shadowLocationManager = shadowOf(locationManager);
        shadowLocationManager.setProviderEnabled(GPS_PROVIDER, true);

        controller.create().startCommand(0,0);

        assertEquals(TrackingService.TrackingServiceStatus.WAITING_FIRST_FIX, TrackingService.getTrackingServiceStatus());
        assertTrue(TrackingService.isRunning());
    }

    @Test
    public void testStartingServiceHighAccuracy() throws InterruptedException  {
        controller = Robolectric.buildService(TrackingService.class,
                TrackingService.getIntent(context, MapActivity.class, new TrackingServiceHighAccuracyOptions()));

        simulateLocations();
    }

    @Test
    public void testStartingServiceSaveBattery() throws InterruptedException {
        controller = Robolectric.buildService(TrackingService.class,
                TrackingService.getIntent(context, MapActivity.class, new TrackingServiceSaveBatteryOptions()));

        simulateLocations();
    }

    private void simulateLocations() throws InterruptedException {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        ShadowLocationManager shadowLocationManager = shadowOf(locationManager);
        shadowLocationManager.setProviderEnabled(GPS_PROVIDER, true);

        CountDownLatch latch1 = new CountDownLatch(1);
        CountDownLatch latch2 = new CountDownLatch(1);
        CountDownLatch latch3 = new CountDownLatch(1);

        List<Location> locations;

        controller.get().registerTrackingServiceListener(new TrackingServiceListener() {
            @Override
            public void onFirstLocationReceived(Location location) {
                Assert.assertEquals(location.getLatitude(), location1.getLatitude());
                Assert.assertEquals(location.getLongitude(), location1.getLongitude());
                latch1.countDown();
            }

            @Override
            public void onNewLocationReceived(Location location) {
                Assert.assertNotNull(location);
                latch2.countDown();
            }

            @Override
            public void onCloseToDepartureLocation(Location location) {
                Assert.assertEquals(location.getLatitude(), location1.getLatitude());
                Assert.assertEquals(location.getLongitude(), location1.getLongitude());
                latch3.countDown();
            }

            @Override
            public void onServiceConnected(TrackingService service) {

            }

            @Override
            public void onServiceDisconnected() {

            }
        });

        controller.create().startCommand(0,0);

        Thread.sleep(2000); // TaskService thread waiting for running

        location1.setTime(System.currentTimeMillis());
        shadowLocationManager.simulateLocation(location1);
        latch1.await();

        Thread.sleep(1000);
        location2.setTime(System.currentTimeMillis());   // Too close from first point
        shadowLocationManager.simulateLocation(location2);

        Thread.sleep(1000);
        location3.setTime(System.currentTimeMillis());     // register location1
        shadowLocationManager.simulateLocation(location3);
        latch2.await();

        Thread.sleep(1000);
        location1.setTime(System.currentTimeMillis());  // register location3 & location1
        shadowLocationManager.simulateLocation(location1);
        latch3.await();

        Thread.sleep(1000);
        locations = controller.get().getRecordedLocations();

        assertEquals(locations.size(), 3);
    }
}