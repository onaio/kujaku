package io.ona.kujaku.services;


import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.location.Location;
import android.location.LocationManager;
import android.os.IBinder;
import android.os.Parcel;
import android.view.Gravity;

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
import io.ona.kujaku.R;
import io.ona.kujaku.activities.MapActivity;
import io.ona.kujaku.listeners.TrackingServiceListener;
import io.ona.kujaku.services.configurations.TrackingServiceDefaultUIConfiguration;
import io.ona.kujaku.services.options.TrackingServiceHighAccuracyOptions;
import io.ona.kujaku.services.options.TrackingServiceSaveBatteryOptions;

import static android.location.LocationManager.GPS_PROVIDER;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.robolectric.Shadows.shadowOf;

/**
 * Created by Emmanuel Otin - eo@novel-t.ch 03/20/19.
 */
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class,
        manifest = Config.NONE)
public class TrackingServiceTest {

    private Context context;

    private ServiceController<TrackingService> controller;

    private String connectionStatus = "";

    private Location locationDeparture;
    private Location location_1;
    private Location location_2;
    private Location location_3;

    @Before
    public void setUp() {
        context = RuntimeEnvironment.application;

        locationDeparture = new Location(GPS_PROVIDER);
        locationDeparture.setAccuracy(20);
        locationDeparture.setLongitude(6.054989);
        locationDeparture.setLatitude(46.218049);

        location_1 = new Location(GPS_PROVIDER);
        location_1.setAccuracy(18);
        location_1.setLongitude(6.055042);
        location_1.setLatitude(46.218084);

        location_2 = new Location(GPS_PROVIDER);
        location_2.setAccuracy(20);
        location_2.setLongitude(6.055096);
        location_2.setLatitude(46.218119);

        location_3 = new Location(GPS_PROVIDER);
        location_3.setAccuracy(20);
        location_3.setLongitude(6.055697);
        location_3.setLatitude(46.218561);
    }

    @After
    public void tearDown() {
        if (controller != null) {
            controller.destroy();
        }
    }

    @Test
    public void testTrackingServiceStatusAtCreation() {
        controller = Robolectric.buildService(TrackingService.class,
                TrackingService.getIntent(context, MapActivity.class, new TrackingServiceHighAccuracyOptions()));

        assertEquals(TrackingService.TrackingServiceStatus.STOPPED, TrackingService.getTrackingServiceStatus());
        assertFalse(TrackingService.isRunning());
        assertNull(null, controller.get().getRecordedKujakuLocations());

        controller.destroy();
    }

    @Test
    public void testStartingServiceWithoutGpsProvider() {
        controller = Robolectric.buildService(TrackingService.class,
                TrackingService.getIntent(context, MapActivity.class, new TrackingServiceHighAccuracyOptions()));

        controller.create().startCommand(0,0);
        assertEquals(TrackingService.TrackingServiceStatus.STOPPED_GPS, TrackingService.getTrackingServiceStatus());

        controller.destroy();
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

        controller.destroy();
    }

    @Test
    public void testParcelableTrackingServiceHighAccuracyOptions() {
        TrackingServiceHighAccuracyOptions options = new TrackingServiceHighAccuracyOptions();
        Parcel parcel = Parcel.obtain();
        options.writeToParcel(parcel, options.describeContents());
        parcel.setDataPosition(0);

        TrackingServiceHighAccuracyOptions createdFromParcel = TrackingServiceHighAccuracyOptions.CREATOR.createFromParcel(parcel);
        assertEquals(createdFromParcel.getDistanceFromDeparture(), 10);
        assertEquals(createdFromParcel.getGpsMinDistance(), 0);
        assertEquals(createdFromParcel.getMinAccuracy(), 50);
        assertEquals(createdFromParcel.getMinDistance(), 5);
        assertEquals(createdFromParcel.getMinTime(), 0);
        assertEquals(createdFromParcel.getToleranceIntervalDistance(), 1);

    }

    @Test
    public void testParcelableTrackingServiceSaveBatteryOptions() {
        TrackingServiceSaveBatteryOptions options = new TrackingServiceSaveBatteryOptions();
        Parcel parcel = Parcel.obtain();
        options.writeToParcel(parcel, options.describeContents());
        parcel.setDataPosition(0);

        TrackingServiceSaveBatteryOptions createdFromParcel = TrackingServiceSaveBatteryOptions.CREATOR.createFromParcel(parcel);
        assertEquals(createdFromParcel.getDistanceFromDeparture(), 10);
        assertEquals(createdFromParcel.getGpsMinDistance(), 5);
        assertEquals(createdFromParcel.getMinAccuracy(), 50);
        assertEquals(createdFromParcel.getMinDistance(), 5);
        assertEquals(createdFromParcel.getMinTime(), 0);
        assertEquals(createdFromParcel.getToleranceIntervalDistance(), 1);

    }

    @Test
    public void testTrackingServiceDefaultUiConfiguration() {
       TrackingServiceDefaultUIConfiguration uiTrackingService = new TrackingServiceDefaultUIConfiguration();
       assertTrue(uiTrackingService.displayIcons());

       assertEquals(uiTrackingService.getBackgroundDrawable(), R.drawable.circle_button_black_border);
       assertEquals(uiTrackingService.getRecordingDrawable(), R.drawable.ic_recording_red);
       assertEquals(uiTrackingService.getStoppedDrawable(), R.drawable.ic_recording_gray);

       assertEquals(uiTrackingService.getLayoutWidth(), R.dimen.tracking_service_location_dimen);
       assertEquals(uiTrackingService.getLayoutHeight(), R.dimen.tracking_service_location_dimen);

       assertEquals(uiTrackingService.getLayoutMarginLeft(), R.dimen.tracking_service_location_margin);
       assertEquals(uiTrackingService.getLayoutMarginTop(), R.dimen.tracking_service_location_margin);
       assertEquals(uiTrackingService.getLayoutMarginRight(), R.dimen.tracking_service_location_margin);
       assertEquals(uiTrackingService.getLayoutMarginBottom(), R.dimen.tracking_service_location_margin);

       assertEquals(uiTrackingService.getPadding(), R.dimen.tracking_service_location_padding);
       assertEquals(uiTrackingService.getLayoutGravity(), Gravity.TOP | Gravity.LEFT);
    }

    @Test
    public void testStartAndBindService() throws InterruptedException {
        CountDownLatch latch1 = new CountDownLatch(1);
        CountDownLatch latch2 = new CountDownLatch(1);

        ServiceConnection connection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                connectionStatus = "connected";
                latch1.countDown();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                connectionStatus = "disconnected";
                latch2.countDown();
            }
        };

        assertEquals(connectionStatus, "");
        TrackingService.startAndBindService(context, MapActivity.class, connection, new TrackingServiceHighAccuracyOptions());
        latch1.await();
        assertEquals(connectionStatus, "connected");

        TrackingService.stopAndUnbindService(context, connection);
        latch2.await();
        assertEquals(connectionStatus, "disconnected");
    }

    @Test
    public void testServiceWithLocationInDistanceTolerance() throws InterruptedException {
        controller = Robolectric.buildService(TrackingService.class,
                TrackingService.getIntent(context, MapActivity.class, new TrackingServiceHighAccuracyOptions()));

        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        ShadowLocationManager shadowLocationManager = shadowOf(locationManager);
        shadowLocationManager.setProviderEnabled(GPS_PROVIDER, true);

        float distance = locationDeparture.distanceTo(location_1);
        assertEquals(distance, 5, 1); // 5 meters +-1 meter

        distance = location_1.distanceTo(location_2);
        assertEquals(distance, 5, 1); // 5 meters +-1 meter

        CountDownLatch latch1 = new CountDownLatch(1);
        CountDownLatch latch2 = new CountDownLatch(1);

        controller.get().registerTrackingServiceListener(new TrackingServiceListener() {
            @Override
            public void onFirstLocationReceived(Location location) {
                assertEquals(location.getLatitude(), locationDeparture.getLatitude(),0);
                assertEquals(location.getLongitude(), locationDeparture.getLongitude(),0);
                latch1.countDown();
            }

            @Override
            public void onNewLocationReceived(Location location) {
                assertNotNull(location);
                latch2.countDown();
            }

            @Override
            public void onCloseToDepartureLocation(Location location) {
                assertNotNull(location);
            }


            @Override
            public void onServiceConnected(TrackingService service) {
                // Empty body
            }

            @Override
            public void onServiceDisconnected() {
                // Empty body
            }
        });

        controller.create().startCommand(0,0);

        Thread.sleep(2000); // TaskService thread waiting for running

        locationDeparture.setTime(System.currentTimeMillis());
        shadowLocationManager.simulateLocation(locationDeparture);
        latch1.await();

        Thread.sleep(1000);
        location_1.setTime(System.currentTimeMillis());
        shadowLocationManager.simulateLocation(location_1);

        Thread.sleep(1000);
        location_2.setTime(System.currentTimeMillis());     // register location_1
        shadowLocationManager.simulateLocation(location_2);
        latch2.await();
        Thread.sleep(1000);

        location_3.setTime(System.currentTimeMillis());     // register location_2
        shadowLocationManager.simulateLocation(location_3);
        Thread.sleep(1000);

        locationDeparture.setTime(System.currentTimeMillis()); // register location_3 and location departure
        shadowLocationManager.simulateLocation(locationDeparture);

        List<Location> list = controller.get().getRecordedKujakuLocations();
        assertEquals(list.size(), 4);

        assertEquals(list.get(0).getLatitude(), location_1.getLatitude(),0);
        assertEquals(list.get(0).getLongitude(), location_1.getLongitude(),0);

        assertEquals(list.get(1).getLatitude(), location_2.getLatitude(),0);
        assertEquals(list.get(1).getLongitude(), location_2.getLongitude(),0);

        assertEquals(list.get(2).getLatitude(), location_3.getLatitude(),0);
        assertEquals(list.get(2).getLongitude(), location_3.getLongitude(),0);

        assertEquals(list.get(3).getLatitude(), locationDeparture.getLatitude(),0);
        assertEquals(list.get(3).getLongitude(), locationDeparture.getLongitude(),0);

        assertEquals(list.size(), TrackingService.getCurrentRecordedKujakuLocations().size());
        controller.startCommand(0,0);
        assertEquals(list.size(), TrackingService.getPreviousRecordedKujakuLocations().size());

        controller.destroy();
    }
}