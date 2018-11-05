package io.ona.kujaku.views;

import android.content.Context;
import android.location.Location;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;

import org.json.JSONObject;
import org.junit.Before;
import static org.junit.Assert.*;

import org.junit.Test;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.HashMap;

import io.ona.kujaku.BaseTest;
import io.ona.kujaku.R;
import io.ona.kujaku.callbacks.AddPointCallback;
import io.ona.kujaku.listeners.OnLocationChanged;
import io.ona.kujaku.test.shadows.ShadowGeoJsonSource;
import io.ona.kujaku.test.shadows.implementations.KujakuMapTestView;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 05/11/2018
 */

@Config(shadows = {ShadowGeoJsonSource.class})
public class KujakuMapViewTest extends BaseTest {

    private Context context;
    private KujakuMapTestView kujakuMapView;
    private MapboxMap mapboxMap;

    @Before
    public void setup() {
        context = RuntimeEnvironment.application;
        mapboxMap = null;
        kujakuMapView = new KujakuMapTestView(context);
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

            }
        };

        kujakuMapView.enableAddPoint(true, onLocationChanged);
        assertFalse((boolean) getValueInPrivateField(KujakuMapView.class, kujakuMapView, isMapScrollingVariableName));
        assertEquals(onLocationChanged, getValueInPrivateField(KujakuMapView.class, kujakuMapView, "onLocationChanged"));
        assertTrue((boolean) getValueInPrivateField(KujakuMapView.class, kujakuMapView, "updateUserLocationOnMap"));
    }

    @Test
    public void enableAddPointShouldShowLatestPositionWhenGivenOnLocationChangedAndTrue() throws NoSuchFieldException, IllegalAccessException, InterruptedException {
        OnLocationChanged onLocationChanged = new OnLocationChanged() {
            @Override
            public void onLocationChanged(Location location) {

            }
        };

        LatLng latLng = new LatLng(14d, 23d);

        insertValueInPrivateField(KujakuMapView.class, kujakuMapView, "latestLocation", latLng);
        setFinalStatic(KujakuMapView.class.getDeclaredField("ANIMATE_TO_LOCATION_DURATION"), 0);
        kujakuMapView.enableAddPoint(true, onLocationChanged);

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

            }

            @Override
            public void onCancel() {

            }
        });

        assertEquals(View.VISIBLE, addBtn.getVisibility());
        assertEquals(View.GONE, buttonsLayout.getVisibility());
    }

    @Test
    public void addPointShouldPerformActionsOnClick() {
        ImageButton addBtn = kujakuMapView.findViewById(R.id.imgBtn_mapview_locationAdditionBtn);
        ImageButton cancelBtn = kujakuMapView.findViewById(R.id.imgBtn_mapview_locationAdditionBtn);
        Button doneAddingPoint = kujakuMapView.findViewById(R.id.btn_mapview_locationSelectionBtn);;
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
    public void focusOnUserLocationShouldChangeTargetIconWhenGivenCalled() throws NoSuchFieldException, IllegalAccessException {
        ImageButton currentLocationBtn = kujakuMapView.findViewById(R.id.ib_mapview_focusOnMyLocationIcon);
        String updateUserLocationOnMap = "updateUserLocationOnMap";

        kujakuMapView.focusOnUserLocation(true);
        assertTrue((boolean) getValueInPrivateField(KujakuMapView.class, kujakuMapView, updateUserLocationOnMap));

        kujakuMapView.focusOnUserLocation(false);
        assertFalse((boolean) getValueInPrivateField(KujakuMapView.class, kujakuMapView, updateUserLocationOnMap));
    }
}
