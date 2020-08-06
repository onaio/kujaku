package io.ona.kujaku.location.clients;

import android.location.GnssStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.util.ReflectionHelpers;

import io.ona.kujaku.BaseTest;
import io.ona.kujaku.KujakuLibrary;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 2019-11-25
 */

public class AndroidGpsLocationClientTest extends BaseTest {

    private AndroidGpsLocationClient androidGpsLocationClient;

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Before
    public void setUp() throws Exception {
        androidGpsLocationClient = Mockito.spy(new AndroidGpsLocationClient(RuntimeEnvironment.application));
    }

    @After
    public void tearDown() throws Exception {
        ReflectionHelpers.setStaticField(KujakuLibrary.class, "library", null);
    }

    @Test
    public void stopLocationUpdatesShouldSetListenerToNullWhenListenerIsCurrentlySet() {
        LocationListener locationListener = Mockito.mock(LocationListener.class);
        androidGpsLocationClient.addLocationListener(locationListener);

        Assert.assertTrue(androidGpsLocationClient.getLocationListeners().size() > 0);
        androidGpsLocationClient.stopLocationUpdates();

        Assert.assertEquals(0, androidGpsLocationClient.getLocationListeners().size());

        // do the same test using the deprecated setLocationListener and getLocationListener
        androidGpsLocationClient.setLocationListener(locationListener);
        Assert.assertNotNull(androidGpsLocationClient.getLocationListener());
        androidGpsLocationClient.stopLocationUpdates();
        Assert.assertNull(androidGpsLocationClient.getLocationListener());
    }


    @Test
    public void stopLocationUpdatesShouldCallLocationManagerRemoveUpdates() {
        LocationListener locationListener = Mockito.mock(LocationListener.class);
        LocationManager locationManager = Mockito.spy((LocationManager) ReflectionHelpers.getField(androidGpsLocationClient, "locationManager"));
        ReflectionHelpers.setField(androidGpsLocationClient, "locationManager", locationManager);
        androidGpsLocationClient.addLocationListener(locationListener);

        Assert.assertEquals(1, androidGpsLocationClient.getLocationListeners().size());
        androidGpsLocationClient.stopLocationUpdates();

        // Verify that the AndroidGPSLocationListener was also called
        Mockito.verify(locationManager, Mockito.times(2)).removeUpdates(Mockito.any(LocationListener.class));
    }

    @Test
    public void requestLocationUpdatesShouldCallLocationManagerRequestLocationUpdates() {
        LocationListener locationListener = Mockito.mock(LocationListener.class);
        LocationManager locationManager = Mockito.spy((LocationManager) ReflectionHelpers.getField(androidGpsLocationClient, "locationManager"));
        ReflectionHelpers.setField(androidGpsLocationClient, "locationManager", locationManager);

        // Mock call to KujakuLibrary.showToast
        KujakuLibrary kujakuLibrary = Mockito.mock(KujakuLibrary.class);
        ReflectionHelpers.setStaticField(KujakuLibrary.class, "library", kujakuLibrary);
        Mockito.doNothing().when(kujakuLibrary).showToast(Mockito.anyString());

        Location location = new Location(LocationManager.GPS_PROVIDER);
        location.setAccuracy(4f);
        location.setAltitude(23f);
        location.setLatitude(3d);
        location.setLongitude(1d);

        Mockito.doReturn(true).when(locationManager).registerGnssStatusCallback(Mockito.any(GnssStatus.Callback.class));
        Mockito.doReturn(location).when(locationManager).getLastKnownLocation(Mockito.eq(LocationManager.GPS_PROVIDER));
        Mockito.doReturn(true).when(locationManager).isProviderEnabled(Mockito.eq(LocationManager.GPS_PROVIDER));
        Mockito.doNothing().when(locationManager).requestLocationUpdates(Mockito.anyString(), Mockito.anyLong(), Mockito.anyFloat(), Mockito.any(LocationListener.class));
        androidGpsLocationClient.requestLocationUpdates(locationListener);

        Mockito.verify(locationManager, Mockito.times(1))
                .requestLocationUpdates(Mockito.eq(LocationManager.GPS_PROVIDER), Mockito.eq(1000L), Mockito.eq(0f), Mockito.any(LocationListener.class));
    }

    @Test
    public void androidGpsLocationClientOnLocationChangedShouldCallRegisteredLocationListenerAndUpdateLastLocation() {
        androidGpsLocationClient = new AndroidGpsLocationClient(RuntimeEnvironment.application);
        LocationListener locationListener = Mockito.mock(LocationListener.class);
        LocationListener locationListener2 = Mockito.mock(LocationListener.class);

        // Mock call to KujakuLibrary.showToast
        KujakuLibrary kujakuLibrary = Mockito.mock(KujakuLibrary.class);
        ReflectionHelpers.setStaticField(KujakuLibrary.class, "library", kujakuLibrary);
        Mockito.doNothing().when(kujakuLibrary).showToast(Mockito.anyString());

        // Create a mock location
        Location location = new Location(LocationManager.GPS_PROVIDER);
        location.setAccuracy(4f);
        location.setAltitude(23f);
        location.setLatitude(3d);
        location.setLongitude(1d);

        // Set the location listeners supposed to receive updates
        androidGpsLocationClient.addLocationListener(locationListener);
        androidGpsLocationClient.setLocationListener(locationListener2);

        // Retrieve the androidGpsLocationListener from the AndroidGpsLocationClient
        AndroidGpsLocationClient.AndroidGpsLocationListener androidGpsLocationListener = ReflectionHelpers.getField(androidGpsLocationClient, "androidGpsLocationListener");

        // Call onLocationChanged on the androidGpsLocationListener
        androidGpsLocationListener.onLocationChanged(location);

        Mockito.verify(locationListener).onLocationChanged(location);
        Mockito.verify(locationListener2).onLocationChanged(location);
        Assert.assertEquals(location, androidGpsLocationClient.getLastLocation());
    }


    @Test
    public void getLastLocationShouldRequestLastLocationFromLocationManager() {
        LocationManager locationManager = Mockito.spy((LocationManager) ReflectionHelpers.getField(androidGpsLocationClient, "locationManager"));
        ReflectionHelpers.setField(androidGpsLocationClient, "locationManager", locationManager);

        Location location = new Location(LocationManager.GPS_PROVIDER);
        location.setAccuracy(4f);
        location.setAltitude(23f);
        location.setLatitude(3d);
        location.setLongitude(1d);

        Mockito.doReturn(location).when(locationManager).getLastKnownLocation(LocationManager.GPS_PROVIDER);

        Location lastLocation = androidGpsLocationClient.getLastLocation();

        Assert.assertEquals(location, lastLocation);
        Mockito.verify(locationManager).getLastKnownLocation(LocationManager.GPS_PROVIDER);
    }


    @Test
    public void getLastLocationShouldReturnLatestLocationWhenCachedLocationAndLocatioManagerProvideLocations() {
        LocationManager locationManager = Mockito.spy((LocationManager) ReflectionHelpers.getField(androidGpsLocationClient, "locationManager"));
        ReflectionHelpers.setField(androidGpsLocationClient, "locationManager", locationManager);

        Location location = new Location(LocationManager.GPS_PROVIDER);
        location.setAccuracy(4f);
        location.setAltitude(23f);
        location.setLatitude(3d);
        location.setLongitude(1d);
        location.setTime(900);


        Location locationFromLocationManager = new Location(LocationManager.GPS_PROVIDER);
        locationFromLocationManager.setAccuracy(4f);
        locationFromLocationManager.setAltitude(23f);
        locationFromLocationManager.setLatitude(3d);
        locationFromLocationManager.setLongitude(1d);
        locationFromLocationManager.setTime(1000);

        Mockito.doReturn(locationFromLocationManager).when(locationManager).getLastKnownLocation(LocationManager.GPS_PROVIDER);
        ReflectionHelpers.setField(androidGpsLocationClient, "lastLocation", location);

        Location lastLocation = androidGpsLocationClient.getLastLocation();

        Assert.assertEquals(locationFromLocationManager, lastLocation);
        Mockito.verify(locationManager).getLastKnownLocation(LocationManager.GPS_PROVIDER);
    }

}
