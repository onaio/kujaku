package io.ona.kujaku.sample.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.mapbox.mapboxsdk.Mapbox;

import io.ona.kujaku.sample.BuildConfig;
import io.ona.kujaku.sample.R;

public abstract class BaseNavigationDrawerActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private NavigationView navigationView;
    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // This should ideally be in the Application class so that it's not easily forgotten when using
        // the KujakuMapView
        Mapbox.getInstance(this, BuildConfig.MAPBOX_SDK_ACCESS_TOKEN);

        setContentView(getContentView());
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.ic_menu);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();


        navigationView = getNavigationView();
        navigationView.setNavigationItemSelectedListener(this);

        // Set activity title
        setActivityTitleFromNavItem();
    }

    private void setActivityTitleFromNavItem() {
        if (getSelectedNavigationItem() != 0) {
            MenuItem menuItem = navigationView.getMenu().findItem(getSelectedNavigationItem());
            if (menuItem != null) {
                setTitle(menuItem.getTitle());
            }
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.action_settings:
                return true;

            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.

        switch (item.getItemId()) {
            case R.id.nav_low_level_manual_add_point:
                startActivity(new Intent(this, LowLevelManualAddPointMapView.class));
                drawerLayout.closeDrawer(GravityCompat.START);
                finish();
                return true;

            case R.id.nav_low_level_location_add_point:
                startActivity(new Intent(this, LowLevelLocationAddPointMapView.class));
                drawerLayout.closeDrawer(GravityCompat.START);
                finish();
                return true;

            case R.id.nav_high_level_add_point:
                startActivity(new Intent(this, HighLevelMapView.class));
                drawerLayout.closeDrawer(GravityCompat.START);
                finish();
                return true;

            case R.id.nav_offline_regions:
                startActivity(new Intent(this, OfflineRegionsActivity.class));
                finish();
                return true;

            case R.id.nav_task_queue:
                startActivity(new Intent(this, TaskQueueActivity.class));
                finish();
                return true;

            case R.id.nav_main_activity:
                startActivity(new Intent(this, MainActivity.class));
                finish();
                return true;

            case R.id.nav_high_level_location_add_point:
                startActivity(new Intent(this, HighLevelLocationAddPointMapView.class));
                finish();
                return true;

            case R.id.nav_card_activity:
                startActivity(new Intent(this, CardActivity.class));
                finish();
                return true;

            case R.id.nav_low_level_add_point_custom_marker:
                startActivity(new Intent(this, CustomMarkerLowLevelAddPoint.class));
                finish();
                return true;

            case R.id.nav_wmts_activity:
                startActivity(new Intent(this, WmtsActivity.class));
                finish();
                return true;

            case R.id.nav_add_update_activity:
                startActivity(new Intent(this, AddUpdatePropertiesActivity.class));
                finish();
                return true;

            case R.id.nav_feature_click_status:
                startActivity(new Intent(this, FeatureClickStatusActivity.class));
                finish();
                return true;

            case R.id.nav_bounding_box_listener:
                startActivity(new Intent(this, BoundsChangeListenerActivity.class));
                finish();
                return true;

            case R.id.nav_bounds_aware_activity:
                startActivity(new Intent(this, BoundsAwareActivity.class));
                finish();
                return true;

            case R.id.nav_feature_click_listener:
                startActivity(new Intent(this, FeatureClickListenerActivity.class));
                finish();
                return true;

            case R.id.nav_padded_bbox_calculator:
                startActivity(new Intent(this, PaddedBboxCalculatorActivity.class));
                finish();
                return true;

            case R.id.nav_configurable_circle:
                startActivity(new Intent(this, ConfigurableLocationCircleActivity.class));
                finish();
                return true;

            case R.id.nav_one_to_one_case_relationship_activity:
                startActivity(new Intent(this, OneToOneCaseRelationshipActivity.class));
                finish();
                return true;

            case R.id.nav_one_to_many_case_relationship_activity:
                startActivity(new Intent(this, OneToManyCaseRelationshipActivity.class));
                finish();
                return true;

            case R.id.nav_foci_boundary:
                startActivity(new Intent(this, FociBoundaryActivity.class));
                finish();
                return true;

            case R.id.nav_passive_record_object:
                startActivity(new Intent(this, PassiveRecordObjectActivity.class));
                finish();
                return true;

            case R.id.nav_base_layer_switcher_plugin:
                startActivity(new Intent(this, BaseLayerSwitcherActivity.class));
                finish();
                return true;

            case R.id.nav_drawing_boundaries:
                startActivity(new Intent(this, DrawingBoundariesActivity.class));
                finish();
                return true;

            case R.id.nav_splitting_polygon:
                startActivity(new Intent(this, SplittingPolygonActivity.class));
                finish();
                return true;

            default:
                break;
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        NavigationView navigationView = getNavigationView();
        setSelectedNavigationItem(getSelectedNavigationItem());
    }

    abstract protected int getContentView();

    abstract protected int getSelectedNavigationItem();

    protected NavigationView getNavigationView() {
        return findViewById(R.id.nav_view);
    }

    protected void setSelectedNavigationItem(@IdRes int navigationItem) {
        if (navigationView == null) {
            navigationView = getNavigationView();
        }
        navigationView.setCheckedItem(navigationItem);
    }
}
