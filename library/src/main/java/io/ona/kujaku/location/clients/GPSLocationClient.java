package io.ona.kujaku.location.clients;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import io.ona.kujaku.R;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 03/10/2018
 */

public class GPSLocationClient extends BaseLocationClient implements LocationListener {

    private static final String TAG = GPSLocationClient.class.getName();
    private Location lastLocation;

    private long updateInterval;

    public GPSLocationClient(@NonNull Context context) {
        this.context = context;
        locationManager = (LocationManager) context.getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
    }

    @Override
    public void stopLocationUpdates() {
        if (isMonitoringLocation()) {
            lastLocation = null;
            setLocationListener(null);
            locationManager.removeUpdates(this);
        }
    }

    @Override
    public Location getLastLocation() {
        return lastLocation;
    }

    @Override
    public void requestLocationUpdates(@NonNull LocationListener locationListener) {
        setLocationListener(locationListener);
        if (isProviderEnabled()) {
            try {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, updateInterval, 0, GPSLocationClient.this);
                lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            } catch (SecurityException e) {
                Log.e(TAG, Log.getStackTraceString(e));
                Toast.makeText(context, R.string.location_disabled_location_permissions_not_granted, Toast.LENGTH_LONG)
                        .show();
            }
        } else {
            setLocationListener(null);
            Log.e(TAG, "The provider (" + getProvider() + ") is not enabled");
        }
    }

    @Override
    public void setUpdateIntervals(long updateInterval, long fastestUpdateInterval) {
        this.updateInterval = fastestUpdateInterval;
    }

    @Override
    public boolean isMonitoringLocation() {
        return getLocationListener() != null;
    }

    @Override
    public String getProvider() {
        return LocationManager.GPS_PROVIDER;
    }

    @Override
    public void onLocationChanged(Location location) {
        if (getLocationListener() != null) {
            getLocationListener().onLocationChanged(location);
        }

        // We should probably disable it if not monitoring the location
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // We can't do much - Probably show toasts
    }

    @Override
    public void onProviderEnabled(String provider) {
        // Nothing much: Probably show toasts
    }

    @Override
    public void onProviderDisabled(String provider) {
        // Nothing much: Probably show toasts
    }
}
