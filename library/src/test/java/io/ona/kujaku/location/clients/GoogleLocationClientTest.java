package io.ona.kujaku.location.clients;

import android.location.LocationListener;

import com.google.android.gms.location.LocationRequest;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.robolectric.RuntimeEnvironment;

import io.ona.kujaku.BaseTest;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 2019-11-25
 */

public class GoogleLocationClientTest extends BaseTest {

    private GoogleLocationClient googleLocationClient;

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Before
    public void setUp() throws Exception {
        googleLocationClient = Mockito.spy(new GoogleLocationClient(RuntimeEnvironment.application));
    }

    @Test
    public void stopLocationUpdatesShouldSetListenerToNullWhenListenerIsCurrentlySet() {
        LocationListener locationListener = Mockito.mock(LocationListener.class);

        googleLocationClient.addLocationListener(locationListener);


        Assert.assertTrue(googleLocationClient.getLocationListeners().size() > 0);
        googleLocationClient.stopLocationUpdates();

        Assert.assertEquals(0, googleLocationClient.getLocationListeners().size());
    }

    @Test
    public void requestLocationUpdatesShouldDefaultCallToHighAccuracy() {
        LocationListener locationListener = Mockito.mock(LocationListener.class);
        ArgumentCaptor<LocationRequest> locationRequestCaptor = ArgumentCaptor.forClass(LocationRequest.class);

        Mockito.doNothing().when(googleLocationClient).requestLocationUpdates(Mockito.eq(locationListener), Mockito.any(LocationRequest.class));
        googleLocationClient.requestLocationUpdates(locationListener);

        Mockito.verify(googleLocationClient, Mockito.times(1))
                .requestLocationUpdates(Mockito.eq(locationListener), locationRequestCaptor.capture());
        Assert.assertEquals(LocationRequest.PRIORITY_HIGH_ACCURACY, locationRequestCaptor.getValue().getPriority());
    }


}
