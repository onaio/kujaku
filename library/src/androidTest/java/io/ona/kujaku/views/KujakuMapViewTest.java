package io.ona.kujaku.views;

import android.content.Context;
import android.location.Location;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.UiThreadTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.VisibleRegion;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import io.ona.kujaku.BaseTest;
import io.ona.kujaku.R;
import io.ona.kujaku.callbacks.AddPointCallback;
import io.ona.kujaku.exceptions.WmtsCapabilitiesException;
import io.ona.kujaku.listeners.BoundsChangeListener;
import io.ona.kujaku.listeners.OnLocationChanged;
import io.ona.kujaku.wmts.serializer.WmtsCapabilitiesSerializer;
import io.ona.kujaku.wmts.model.WmtsCapabilities;
import io.ona.kujaku.wmts.model.WmtsLayer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 05/11/2018
 */

@RunWith(AndroidJUnit4.class)
public class KujakuMapViewTest extends BaseTest {

    private KujakuMapTestView kujakuMapView;
    @Rule
    public UiThreadTestRule uiThreadTestRule = new UiThreadTestRule();

    @Before
    public void setUp() throws Throwable {
        Context context = InstrumentationRegistry.getTargetContext();

        uiThreadTestRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Mapbox.getInstance(context, "sample_token");
                kujakuMapView = new KujakuMapTestView(context);
            }
        });
    }

    @Test
    public void enableAddPointShouldShowMarkerLayoutWhenPassedTrue() throws NoSuchFieldException, IllegalAccessException {
        assertEquals(View.GONE, kujakuMapView.findViewById(R.id.iv_mapview_locationSelectionMarker).getVisibility());

        kujakuMapView.enableAddPoint(true);

        assertTrue((boolean) getValueInPrivateField(KujakuMapView.class, kujakuMapView, "canAddPoint"));
        assertEquals(View.VISIBLE, kujakuMapView.findViewById(R.id.iv_mapview_locationSelectionMarker).getVisibility());

    }

    @Test
    public void enableAddPointShouldHideMarkerLayoutWhenPassedFalse() throws NoSuchFieldException, IllegalAccessException {
        assertEquals(View.GONE, kujakuMapView.findViewById(R.id.iv_mapview_locationSelectionMarker).getVisibility());

        kujakuMapView.enableAddPoint(false);

        assertFalse((boolean) getValueInPrivateField(KujakuMapView.class, kujakuMapView, "canAddPoint"));
        assertEquals(View.GONE, kujakuMapView.findViewById(R.id.iv_mapview_locationSelectionMarker).getVisibility());

        kujakuMapView.enableAddPoint(true);
        assertEquals(View.VISIBLE, kujakuMapView.findViewById(R.id.iv_mapview_locationSelectionMarker).getVisibility());

        kujakuMapView.enableAddPoint(false);

        assertFalse((boolean) getValueInPrivateField(KujakuMapView.class, kujakuMapView, "canAddPoint"));
        assertEquals(View.GONE, kujakuMapView.findViewById(R.id.iv_mapview_locationSelectionMarker).getVisibility());

    }

    @Test
    public void enableAddPointShouldEnableLocationUpdatesWhenGivenOnLocationChangedAndTrue() throws NoSuchFieldException, IllegalAccessException {
        String isMapScrollingVariableName = "isMapScrolled";
        insertValueInPrivateField(KujakuMapView.class, kujakuMapView, isMapScrollingVariableName, true);

        OnLocationChanged onLocationChanged = new OnLocationChanged() {
            @Override
            public void onLocationChanged(Location location) {
                // Do nothing
            }
        };

        kujakuMapView.enableAddPoint(true, onLocationChanged);
        assertFalse((boolean) getValueInPrivateField(KujakuMapView.class, kujakuMapView, isMapScrollingVariableName));
        assertEquals(onLocationChanged, getValueInPrivateField(KujakuMapView.class, kujakuMapView, "onLocationChangedListener"));
        assertTrue((boolean) getValueInPrivateField(KujakuMapView.class, kujakuMapView, "updateUserLocationOnMap"));
    }

    @Test
    public void enableAddPointShouldShowLatestPositionWhenGivenOnLocationChangedAndTrue() throws NoSuchFieldException, IllegalAccessException, InterruptedException, Throwable {
        OnLocationChanged onLocationChanged = new OnLocationChanged() {
            @Override
            public void onLocationChanged(Location location) {
                // Do nothing
            }
        };

        LatLng latLng = new LatLng(14d, 23d);

        insertValueInPrivateField(KujakuMapView.class, kujakuMapView, "latestLocation", latLng);
        uiThreadTestRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                kujakuMapView.enableAddPoint(true, onLocationChanged);
            }
        });

        // Check if the two circle layers were added
        // Check if geojson for current user position was added
        // Todo: this can use a drawable icon instead of having two extra layers

        assertNotNull(getValueInPrivateField(KujakuMapView.class, kujakuMapView, "pointsSource"));

        //Make sure the map centers on the location
        assertTrue(kujakuMapView.isMapCentered);
    }

    @Test
    public void dropPointShouldReturnNullWhenAddPointModeHasNotBeenEnabled() {
        assertNull(kujakuMapView.dropPoint());
    }

    @Test
    public void dropPointShouldReturnNull() {
        assertNull(kujakuMapView.dropPoint(new LatLng(45d, 45d)));
    }

    @Test
    public void addPointShouldMakeAddViewVisible() {
        ImageButton addBtn = kujakuMapView.findViewById(R.id.imgBtn_mapview_locationAdditionBtn);
        LinearLayout buttonsLayout = kujakuMapView.findViewById(R.id.ll_mapview_locationSelectionBtns);

        assertEquals(View.GONE, addBtn.getVisibility());
        kujakuMapView.addPoint(false, new AddPointCallback() {
            @Override
            public void onPointAdd(JSONObject jsonObject) {
                // Do nothing
            }

            @Override
            public void onCancel() {
                // Do nothing
            }
        });

        assertEquals(View.VISIBLE, addBtn.getVisibility());
        assertEquals(View.GONE, buttonsLayout.getVisibility());
    }

    @Test
    public void addPointShouldPerformActionsOnClick() {
        ImageButton addBtn = kujakuMapView.findViewById(R.id.imgBtn_mapview_locationAdditionBtn);
        Button doneAddingPoint = kujakuMapView.findViewById(R.id.btn_mapview_locationSelectionBtn);
        LinearLayout buttonsLayout = kujakuMapView.findViewById(R.id.ll_mapview_locationSelectionBtns);

        String isPointAddCalled = "isPointAddCalled";

        HashMap<String, Object> states = new HashMap<>();
        states.put(isPointAddCalled, false);

        kujakuMapView.addPoint(false, new AddPointCallback() {
            @Override
            public void onPointAdd(JSONObject jsonObject) {
                states.put(isPointAddCalled, true);
            }

            @Override
            public void onCancel() {
                // Do nothing
            }
        });

        assertEquals(View.GONE, buttonsLayout.getVisibility());

        addBtn.callOnClick();

        assertEquals(View.VISIBLE, buttonsLayout.getVisibility());
        assertEquals(View.GONE, addBtn.getVisibility());
        assertTrue(kujakuMapView.isCanAddPoint());

        doneAddingPoint.callOnClick();

        assertTrue((boolean) states.get(isPointAddCalled));
        assertEquals(View.GONE, buttonsLayout.getVisibility());
        assertEquals(View.VISIBLE, addBtn.getVisibility());
        assertFalse(kujakuMapView.isCanAddPoint());
    }

    @Test
    public void showCurrentLocationBtnShouldChangeVisibleWhenCalled() {
        ImageButton currentLocationBtn = kujakuMapView.findViewById(R.id.ib_mapview_focusOnMyLocationIcon);
        assertEquals(View.GONE, currentLocationBtn.getVisibility());

        kujakuMapView.showCurrentLocationBtn(true);
        assertEquals(View.VISIBLE, currentLocationBtn.getVisibility());

        kujakuMapView.showCurrentLocationBtn(false);
        assertEquals(View.GONE, currentLocationBtn.getVisibility());
    }

    @Test
    public void focusOnUserLocationShouldChangeTargetIconWhenCalled() throws NoSuchFieldException, IllegalAccessException {
        String updateUserLocationOnMap = "updateUserLocationOnMap";

        kujakuMapView.focusOnUserLocation(true);
        assertTrue((boolean) getValueInPrivateField(KujakuMapView.class, kujakuMapView, updateUserLocationOnMap));
        ImageButton imageButton = kujakuMapView.findViewById(R.id.ib_mapview_focusOnMyLocationIcon);

        int drawableResId = (int) getValueInPrivateField(ImageView.class, imageButton, "mResource");
        assertEquals(R.drawable.ic_cross_hair_blue, drawableResId);


        kujakuMapView.focusOnUserLocation(false);
        assertFalse((boolean) getValueInPrivateField(KujakuMapView.class, kujakuMapView, updateUserLocationOnMap));

        drawableResId = (int) getValueInPrivateField(ImageView.class, imageButton, "mResource");
        assertEquals(R.drawable.ic_cross_hair, drawableResId);
    }

    @Test
    public void addNullWmtsLayers() {
        assertEquals(0, kujakuMapView.getWmtsLayers().size());
        try {
            kujakuMapView.addWmtsLayer(null);
        }
        catch (WmtsCapabilitiesException ex) {
            assertEquals(ex.getMessage(), "capabilities object is null or empty");
        }
        assertEquals(0, kujakuMapView.getWmtsLayers().size());
    }

    @Test
    public void addFirstWmtsLayers() throws Exception {
        assertEquals(0, kujakuMapView.getWmtsLayers().size());

        InputStreamReader streamReader = new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream("Capabilities.xml"));
        WmtsCapabilitiesSerializer serializer = new WmtsCapabilitiesSerializer();
        WmtsCapabilities capabilities = serializer.read(WmtsCapabilities.class, streamReader, false);

        kujakuMapView.addWmtsLayer(capabilities);

        assertEquals(1, kujakuMapView.getWmtsLayers().size());
        assertEquals("Vegetation_Mapping_Texas_Ecological_Mapping_Systems_Data", ((WmtsLayer)kujakuMapView.getWmtsLayers().toArray()[0]).getIdentifier());
    }

    @Test
    public void addUnknowWmtsLayers() throws Exception {
        assertEquals(0, kujakuMapView.getWmtsLayers().size());

        InputStreamReader streamReader = new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream("Capabilities.xml"));
        WmtsCapabilitiesSerializer serializer = new WmtsCapabilitiesSerializer();
        WmtsCapabilities capabilities = serializer.read(WmtsCapabilities.class, streamReader, false);

        try {
            kujakuMapView.addWmtsLayer(capabilities, "unknownLayer", "unknownStyle", "unknownTileMatrix");
        } catch (WmtsCapabilitiesException ex) {
            assertEquals(ex.getMessage(), String.format("Layer with identifier %1$s is unknown", "unknownLayer"));
        }

        assertEquals(0, kujakuMapView.getWmtsLayers().size());
    }

    @Test
    public void addKnwonWmtsLayersAndTestMaximumAndMinimumZooms() throws Exception {
        assertEquals(0, kujakuMapView.getWmtsLayers().size());

        InputStreamReader streamReader = new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream("Capabilities.xml"));
        WmtsCapabilitiesSerializer serializer = new WmtsCapabilitiesSerializer();
        WmtsCapabilities capabilities = serializer.read(WmtsCapabilities.class, streamReader, false);

        kujakuMapView.addWmtsLayer(capabilities, "Vegetation_Mapping_Texas_Ecological_Mapping_Systems_Data", "default", "GoogleMapsCompatible");

        assertEquals(1, kujakuMapView.getWmtsLayers().size());

        WmtsLayer layer = (WmtsLayer)kujakuMapView.getWmtsLayers().toArray()[0];
        assertEquals(layer.getMaximumZoom(), 18);
        assertEquals(layer.getMinimumZoom(), 0);
    }

    @Test
    public void setBoundsChangeListener() throws NoSuchFieldException, IllegalAccessException {
        String fieldName = "boundsChangeListener";
        BoundsChangeListener boundsChangeListener = new BoundsChangeListener() {
            @Override
            public void onBoundsChanged(LatLng topLeft, LatLng topRight, LatLng bottomRight, LatLng bottomLeft) {
                // do nothing for now
            }
        };

        insertValueInPrivateField(KujakuMapView.class, kujakuMapView, fieldName, null);
        assertNull(getValueInPrivateField(KujakuMapView.class, kujakuMapView, fieldName));
        kujakuMapView.setBoundsChangeListener(boundsChangeListener);

        assertEquals(boundsChangeListener, getValueInPrivateField(KujakuMapView.class, kujakuMapView, fieldName));
    }


    @Test
    public void setBoundsChangeListenerShouldCallListenerFirstTime() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        ArrayList<Boolean> results = new ArrayList<>();

        BoundsChangeListener boundsChangeListener = new BoundsChangeListener() {
            @Override
            public void onBoundsChanged(LatLng topLeft, LatLng topRight, LatLng bottomRight, LatLng bottomLeft) {
                countDownLatch.countDown();
                results.add(true);
            }
        };

        kujakuMapView.setVisibleRegion(new VisibleRegion(new LatLng(16, 16), new LatLng(15, 15),
                new LatLng(14, 14), new LatLng(13, 13), null));
        kujakuMapView.setBoundsChangeListener(boundsChangeListener);

        countDownLatch.await(3, TimeUnit.SECONDS);
        assertEquals(1, results.size());
    }
}
