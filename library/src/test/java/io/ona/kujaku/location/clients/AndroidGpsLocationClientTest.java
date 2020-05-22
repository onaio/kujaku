package io.ona.kujaku.location.clients;

import android.location.GnssStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;

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
        ReflectionHelpers.setStaticField(KujakuLibrary.class, "library", null);
    }


}
