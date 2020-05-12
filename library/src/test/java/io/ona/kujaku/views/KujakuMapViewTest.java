package io.ona.kujaku.views;

import android.location.LocationListener;
import android.util.AttributeSet;

import com.google.android.gms.location.LocationRequest;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.Layer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.util.ReflectionHelpers;

import java.util.ArrayList;

import io.ona.kujaku.BaseTest;
import io.ona.kujaku.interfaces.ILocationClient;
import io.ona.kujaku.listeners.LocationClientStartedCallback;
import io.ona.kujaku.location.clients.GoogleLocationClient;
import io.ona.kujaku.test.shadows.ShadowKujakuMapView;
import io.ona.kujaku.test.shadows.ShadowMapView;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 14/03/2019
 */

@Config(shadows = {ShadowKujakuMapView.class, ShadowMapView.class})
public class KujakuMapViewTest extends BaseTest {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Mock
    private Style style;
    private KujakuMapView kujakuMapView;

    @Before
    public void setUp() {
        kujakuMapView = new KujakuMapView(RuntimeEnvironment.application, (AttributeSet) null);
    }

    @Test
    public void addPrimaryGeoJsonSourceAndLayerToStyleShouldAddPrimaryGeoJsonSourceToStyleWhenSourceDoesNotExistOnTheStyle() {
        KujakuMapView spiedKujakuMapView = Mockito.spy(kujakuMapView);

        GeoJsonSource primaryGeoJsonSource = Mockito.mock(GeoJsonSource.class);

        Mockito.doReturn(primaryGeoJsonSource)
                .when(spiedKujakuMapView)
                .getPrimaryGeoJsonSource();

        Mockito.doReturn("the-id")
                .when(primaryGeoJsonSource)
                .getId();

        Mockito.doReturn(null)
                .when(style)
                .getSource(Mockito.anyString());

        ReflectionHelpers.callInstanceMethod(spiedKujakuMapView
                , "addPrimaryGeoJsonSourceAndLayerToStyle"
                , ReflectionHelpers.ClassParameter.from(Style.class, style));

        Mockito.verify(style, Mockito.times(1))
                .addSource(primaryGeoJsonSource);
    }

    @Test
    public void addPrimaryGeoJsonSourceAndLayerToStyleShouldAddLayerToStyleWhenLayerDoesNotExistOnStyle() {
        KujakuMapView spiedKujakuMapView = Mockito.spy(kujakuMapView);

        Layer primaryLayer = Mockito.mock(Layer.class);

        Mockito.doReturn(primaryLayer)
                .when(spiedKujakuMapView)
                .getPrimaryLayer();

        Mockito.doReturn("the-id")
                .when(primaryLayer)
                .getId();

        Mockito.doReturn(null)
                .when(style)
                .getLayer(Mockito.anyString());

        ReflectionHelpers.callInstanceMethod(spiedKujakuMapView
                , "addPrimaryGeoJsonSourceAndLayerToStyle"
                , ReflectionHelpers.ClassParameter.from(Style.class, style));

        Mockito.verify(style, Mockito.times(1))
                .addLayer(primaryLayer);
    }

    @Test
    public void addPrimaryGeoJsonSourceAndLayerToStyleShouldInitializeFeatureCollectionFromStyleWhenFlagIsTrue() {
        ReflectionHelpers.setField(kujakuMapView, "isFetchSourceFromStyle", true);
        assertTrue(ReflectionHelpers.getField(kujakuMapView, "isFetchSourceFromStyle"));


        ReflectionHelpers.callInstanceMethod(kujakuMapView
                , "addPrimaryGeoJsonSourceAndLayerToStyle"
                , ReflectionHelpers.ClassParameter.from(Style.class, style));

        assertFalse(ReflectionHelpers.getField(kujakuMapView, "isFetchSourceFromStyle"));
    }

    @Test
    public void changeLocationUpdatesShouldReturnTrueWhenGivenValidLocationUpdateParams() {
        KujakuMapView spiedKujakuMapView = Mockito.spy(kujakuMapView);

        GoogleLocationClient googleLocationClient = Mockito.mock(GoogleLocationClient.class);

        Mockito.doReturn(mockLocationListeners()).when(googleLocationClient).getLocationListeners();
        Mockito.doReturn(googleLocationClient)
                .when(spiedKujakuMapView)
                .getLocationClient();

        assertTrue(spiedKujakuMapView.changeLocationUpdates(5000
                , 2000
                , LocationRequest.PRIORITY_HIGH_ACCURACY));
    }

    @Test
    public void changeLocationUpdatesShouldReturnFalseWhenGivenValidLocationUpdateParams() {
        assertFalse(kujakuMapView.changeLocationUpdates(5000, 2000, 999));
    }

    @Test
    public void changeLocationUpdatesShouldCallLocationClientRequestingLocationUpdates() {
        KujakuMapView spiedKujakuMapView = Mockito.spy(kujakuMapView);
        GoogleLocationClient googleLocationClient = Mockito.mock(GoogleLocationClient.class);

        ArrayList<LocationListener> mockLocationListeners = mockLocationListeners();
        Mockito.doReturn(mockLocationListeners).when(googleLocationClient).getLocationListeners();
        Mockito.doReturn(googleLocationClient).when(spiedKujakuMapView).getLocationClient();

        spiedKujakuMapView.changeLocationUpdates(5000
                , 2000
                , LocationRequest.PRIORITY_HIGH_ACCURACY);
        Mockito.verify(googleLocationClient, Mockito.times(1))
                .requestLocationUpdates(Mockito.eq(mockLocationListeners.get(0)), Mockito.any(LocationRequest.class));
    }

    private ArrayList<LocationListener> mockLocationListeners() {
        LocationListener mockLocationListener = Mockito.mock(LocationListener.class);
        ArrayList<LocationListener> mockLocationListeners = new ArrayList<>();
        mockLocationListeners.add(mockLocationListener);
        return mockLocationListeners;
    }

    @Test
    public void getLocationClientShouldCallCallbackWhenLocationClientIsNotNull() {
        LocationClientStartedCallback locationClientStartedCallback = Mockito.mock(LocationClientStartedCallback.class);
        KujakuMapView spiedKujakuMapView = Mockito.spy(kujakuMapView);

        GoogleLocationClient googleLocationClient = Mockito.mock(GoogleLocationClient.class);
        Mockito.doReturn(googleLocationClient).when(spiedKujakuMapView).getLocationClient();
        spiedKujakuMapView.getLocationClient(locationClientStartedCallback);

        Mockito.verify(locationClientStartedCallback, Mockito.times(1))
                .onStarted(googleLocationClient);
    }

    @Test
    public void getLocationClientShouldCallCallbackWhenLocationClientIsNullAndWarmLocationIsCalled() {
        LocationClientStartedCallback locationClientStartedCallback = Mockito.mock(LocationClientStartedCallback.class);

        ReflectionHelpers.setField(kujakuMapView, "locationClientCallbacks", new ArrayList<>());
        kujakuMapView.getLocationClient(locationClientStartedCallback);

        Mockito.verify(locationClientStartedCallback, Mockito.times(0))
                .onStarted(Mockito.any(GoogleLocationClient.class));

        ArrayList<LocationClientStartedCallback> callbacks = ReflectionHelpers.getField(kujakuMapView, "locationClientCallbacks");
        assertEquals(1, callbacks.size());
        assertEquals(locationClientStartedCallback, callbacks.get(0));

        ReflectionHelpers.callInstanceMethod(kujakuMapView, "warmUpLocationServices");

        Mockito.verify(locationClientStartedCallback, Mockito.times(1))
                .onStarted(Mockito.any(ILocationClient.class));
        assertEquals(0, callbacks.size());
    }
}
