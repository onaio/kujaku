package io.ona.kujaku.location.clients;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import io.ona.kujaku.R;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 03/10/2018
 */

public class AndroidLocationClient extends BaseLocationClient implements LocationListener {

    private Location lastLocation;
    private FusedLocationProviderClient fusedLocationClient;
    private AndroidLocationCallback androidLocationCallback;

    private long updateInterval = 5000;
    private long fastestUpdateInterval = 1000;

    private static final String TAG = AndroidLocationClient.class.getName();

    public AndroidLocationClient(@NonNull Context context) {
        this.context = context;
        locationManager = (LocationManager) context.getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
        androidLocationCallback = new AndroidLocationCallback();
    }

    @Override
    public void stopLocationUpdates() {
        if (isMonitoringLocation()) {
            lastLocation = null;
            setLocationListener(null);
            fusedLocationClient.removeLocationUpdates(androidLocationCallback);
        }
    }

    @Override
    public Location getLastLocation() {
        return lastLocation;
    }

    @Override
    public void onLocationChanged(Location location) {
        lastLocation = location;

        if (getLocationListener() != null) {
            getLocationListener().onLocationChanged(location);
        }
    }

    @Override
    public void requestLocationUpdates(@NonNull android.location.LocationListener locationListener) {
        setLocationListener(locationListener);
        if (isProviderEnabled()) {
            LocationRequest locationRequest = new LocationRequest();
            locationRequest.setInterval(updateInterval);
            locationRequest.setFastestInterval(fastestUpdateInterval);
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

            try {
                fusedLocationClient.getLastLocation()
                        .addOnSuccessListener((Activity) context, new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                if (lastLocation == null || location.getTime() > lastLocation.getTime()) {
                                    lastLocation = location;
                                }
                            }
                        });

                fusedLocationClient.requestLocationUpdates(locationRequest, androidLocationCallback, null);
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
        this.updateInterval = updateInterval;
        this.fastestUpdateInterval = fastestUpdateInterval;
    }

    @Override
    public boolean isMonitoringLocation() {
        return getLocationListener() != null;
    }

    @Override
    public String getProvider() {
        // TODO: This requires a bit more research and testing
        return LocationManager.NETWORK_PROVIDER;
    }

    @Override
    public boolean isProviderEnabled() {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) &&
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    @Override
    public void close() {
        stopLocationUpdates();
        super.close();
    }

    private class AndroidLocationCallback extends LocationCallback {

        @Override
        public void onLocationResult(LocationResult locationResult) {
            if (locationResult == null) {
                return;
            }

            Location latestLocation = null;
            for (Location location : locationResult.getLocations()) {
                if (latestLocation == null || location.getTime() > latestLocation.getTime()) {
                    latestLocation = location;
                }
            }

            if (latestLocation != null) {
                lastLocation = latestLocation;
                if (getLocationListener() != null) {
                    getLocationListener().onLocationChanged(lastLocation);
                }
            }
        }
    }
}
