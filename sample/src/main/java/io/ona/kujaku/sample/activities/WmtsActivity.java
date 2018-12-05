package io.ona.kujaku.sample.activities;

import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.view.Menu;
import android.view.MenuItem;

import com.mapbox.mapboxsdk.Mapbox;

import io.ona.kujaku.listeners.WmtsCapabilitiesListener;
import io.ona.kujaku.sample.BuildConfig;
import io.ona.kujaku.sample.R;
import io.ona.kujaku.services.WmtsCapabilitiesService;
import io.ona.kujaku.utils.wmts.model.WmtsCapabilities;
import io.ona.kujaku.views.KujakuMapView;

public class WmtsActivity extends BaseNavigationDrawerActivity {

    private static final String TAG = WmtsActivity.class.getName();

    private KujakuMapView kujakuMapView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, BuildConfig.MAPBOX_SDK_ACCESS_TOKEN);

        kujakuMapView = findViewById(R.id.wmts_mapView);

        WmtsCapabilitiesService wmtsService = new WmtsCapabilitiesService();
        wmtsService.setCapabilitiesUrl("https://tpwd.texas.gov/arcgis/rest/services/Vegetation_Mapping/Texas_Ecological_Mapping_Systems_Data/mapserver/WMTS/1.0.0/WMTSCapabilities.xml");
        wmtsService.requestData();
        wmtsService.setListener(new WmtsCapabilitiesListener() {
            @Override
            public void onCapabilitiesReceived(WmtsCapabilities capabilities) {
                try {
                    kujakuMapView.addWmtsLayer(capabilities);
                }
                catch (Exception ex) {
                    //throw ex;
                }
            }
        });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        return super.onNavigationItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (kujakuMapView != null) kujakuMapView.onResume();
    }

    @Override
    protected int getContentView() {
        return R.layout.wmts_map_view;
    }

    @Override
    protected int getSelectedNavigationItem() {
        return R.id.nav_wmts;
    }

    @Override
    protected NavigationView getNavigationView() {
        return super.getNavigationView();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (kujakuMapView != null) kujakuMapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (kujakuMapView != null) kujakuMapView.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (kujakuMapView != null) kujakuMapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (kujakuMapView != null) kujakuMapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (kujakuMapView != null) kujakuMapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (kujakuMapView != null) kujakuMapView.onLowMemory();
    }

    @Override
    protected void setSelectedNavigationItem(int navigationItem) {
        super.setSelectedNavigationItem(navigationItem);
    }
}
