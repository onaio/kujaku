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

import io.ona.kujaku.sample.R;

public abstract class BaseNavigationDrawerActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private NavigationView navigationView;
    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
