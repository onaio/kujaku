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
    private static final int TWO_MINUTES = 1000 * 60 * 2;

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
                if (latestLocation == null || (location != null && isBetterLocation(location, latestLocation))) {
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

        /** Determines whether one Location reading is better than the current Location fix
         * @param location  The new Location that you want to evaluate
         * @param currentBestLocation  The current Location fix, to which you want to compare the new one
         */
        private boolean isBetterLocation(Location location, Location currentBestLocation) {
            if (currentBestLocation == null) {
                // A new location is always better than no location
                return true;
            }

            // Check whether the new location fix is newer or older
            long timeDelta = location.getTime() - currentBestLocation.getTime();
            boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
            boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
            boolean isNewer = timeDelta > 0;

            // If it's been more than two minutes since the current location, use the new location
            // because the user has likely moved
            if (isSignificantlyNewer) {
                return true;
                // If the new location is more than two minutes older, it must be worse
            } else if (isSignificantlyOlder) {
                return false;
            }

            // Check whether the new location fix is more or less accurate
            int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
            boolean isLessAccurate = accuracyDelta > 0;
            boolean isMoreAccurate = accuracyDelta < 0;
            boolean isSignificantlyLessAccurate = accuracyDelta > 200;

            // Check if the old and new location are from the same provider
            boolean isFromSameProvider = isSameProvider(location.getProvider(),
                    currentBestLocation.getProvider());

            // Determine location quality using a combination of timeliness and accuracy
            if (isMoreAccurate) {
                return true;
            } else if (isNewer && !isLessAccurate) {
                return true;
            } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
                return true;
            }

            return false;
        }

        /** Checks whether two providers are the same */
        private boolean isSameProvider(String provider1, String provider2) {
            if (provider1 == null) {
                return provider2 == null;
            }
            return provider1.equals(provider2);
        }
    }

    public FusedLocationProviderClient getFusedLocationClient() {
        return fusedLocationClient;
    }
}
