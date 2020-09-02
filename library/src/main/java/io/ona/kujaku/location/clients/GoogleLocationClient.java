package io.ona.kujaku.location.clients;

import android.app.Activity;
import android.content.Context;
import android.location.GnssStatus;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.widget.Toast;

import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.OnSuccessListener;

import io.ona.kujaku.R;
import io.ona.kujaku.utils.LocationSettingsHelper;
import timber.log.Timber;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 03/10/2018
 */

public class GoogleLocationClient extends BaseLocationClient implements LocationListener {

    private Location lastLocation;
    private FusedLocationProviderClient fusedLocationClient;
    private GoogleLocationCallback googleLocationCallback;

    private long updateInterval = 5000;
    private long fastestUpdateInterval = 1000;

    private Object gpsStatusCallback;

    public GoogleLocationClient(@NonNull Context context) {
        this.context = context;
        locationManager = (LocationManager) context.getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
        googleLocationCallback = new GoogleLocationCallback();
    }

    @Override
    public void stopLocationUpdates() {
        if (isMonitoringLocation()) {
            lastLocation = null;
            clearLocationListeners();
            fusedLocationClient.removeLocationUpdates(googleLocationCallback);
        }

        unregisterForGpsStoppedEvent();
    }

    @Override
    @Nullable
    public Location getLastLocation() {
        return lastLocation;
    }

    @Override
    public void onLocationChanged(Location location) {
        lastLocation = location;

        for (android.location.LocationListener locationListener : getLocationListeners()) {
            locationListener.onLocationChanged(location);
        }
    }

    @Override
    public void requestLocationUpdates(@NonNull android.location.LocationListener locationListener) {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(updateInterval);
        locationRequest.setFastestInterval(fastestUpdateInterval);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        requestLocationUpdates(locationListener, locationRequest);
    }

    public void requestLocationUpdates(@NonNull android.location.LocationListener locationListener
            , @NonNull LocationRequest locationRequest) {
        addLocationListener(locationListener);
        if (isProviderEnabled()) {
            try {
                fusedLocationClient.getLastLocation()
                        .addOnSuccessListener((Activity) context, new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                if (lastLocation == null || (location != null && isBetterLocation(location, lastLocation))) {
                                    lastLocation = location;
                                }
                            }
                        });

                fusedLocationClient.requestLocationUpdates(locationRequest, googleLocationCallback, null);

                // This method protects itself from multiple calls
                registerForGpsStoppedEvent();
            } catch (SecurityException e) {
                Timber.e(e);
                Toast.makeText(context, R.string.location_disabled_location_permissions_not_granted, Toast.LENGTH_LONG)
                        .show();
            }
        } else {
            removeLocationListener(locationListener);
            Timber.e(new Exception(), "The provider (" + getProvider() + ") is not enabled");
        }
    }

    private void registerForGpsStoppedEvent() {
        try {
            // Multiple calls to this method are possible
            // Therefore, we need to prevent multiple registration of the listener by only registering if it's null
            if (gpsStatusCallback == null) {
                if (Build.VERSION.SDK_INT > 23) {
                    gpsStatusCallback = new GnssStatus.Callback() {
                        @Override
                        public void onStopped() {
                            resetLastLocationIfLocationServiceIsOff();
                        }
                    };
                    locationManager.registerGnssStatusCallback((GnssStatus.Callback) gpsStatusCallback);
                } else {
                    gpsStatusCallback = new GpsStatus.Listener() {
                        @Override
                        public void onGpsStatusChanged(int event) {
                            if (event == GpsStatus.GPS_EVENT_STOPPED) {
                                // Check if location is still enabled
                                resetLastLocationIfLocationServiceIsOff();
                            }
                        }
                    };
                    locationManager.addGpsStatusListener((GpsStatus.Listener) gpsStatusCallback);
                }
            }
        } catch (SecurityException ex) {
            Timber.e(ex);
        }
    }

    private void unregisterForGpsStoppedEvent() {
        if (gpsStatusCallback != null) {
            try {
                if (Build.VERSION.SDK_INT > 23) {
                    locationManager.unregisterGnssStatusCallback((GnssStatus.Callback) gpsStatusCallback);
                } else {
                    locationManager.removeGpsStatusListener((GpsStatus.Listener) gpsStatusCallback);
                }

                gpsStatusCallback = null;
            } catch (SecurityException ex) {
                Timber.e(ex);
            }
        }
    }

    private void resetLastLocationIfLocationServiceIsOff() {
        if (context instanceof Activity) {
            LocationSettingsHelper.checkLocationEnabled((Activity) context, new ResultCallback<LocationSettingsResult>() {
                @Override
                public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {
                    int statusCode = locationSettingsResult.getStatus().getStatusCode();

                    if (statusCode == LocationSettingsStatusCodes.RESOLUTION_REQUIRED
                            || statusCode == LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE) {
                        lastLocation = null;
                    }
                }
            });
        }
    }

    @Override
    public void setUpdateIntervals(long updateInterval, long fastestUpdateInterval) {
        this.updateInterval = updateInterval;
        this.fastestUpdateInterval = fastestUpdateInterval;
    }

    @NonNull
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

    @Override
    public void locationManager() {
        if (locationManager == null) {
            locationManager = (LocationManager) context.getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }

    @VisibleForTesting
    protected class GoogleLocationCallback extends LocationCallback {

        @Override
        public void onLocationResult(LocationResult locationResult) {
            if (locationResult == null) {
                return;
            }

            Location latestLocation = getLastLocation();
            for (Location location : locationResult.getLocations()) {
                if (latestLocation == null || (location != null && isBetterLocation(location, latestLocation))) {
                    latestLocation = location;
                }
            }

            if (latestLocation != null) {
                lastLocation = latestLocation;
                for (android.location.LocationListener locationListener : getLocationListeners()) {
                    locationListener.onLocationChanged(lastLocation);
                }
            }
        }
    }

    public FusedLocationProviderClient getFusedLocationClient() {
        return fusedLocationClient;
    }
}
