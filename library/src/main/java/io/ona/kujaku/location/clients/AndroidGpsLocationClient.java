package io.ona.kujaku.location.clients;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.location.GnssStatus;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.widget.Toast;

import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import java.util.ArrayList;

import io.ona.kujaku.R;
import io.ona.kujaku.listeners.BaseLocationListener;
import io.ona.kujaku.utils.LocationSettingsHelper;
import timber.log.Timber;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 2019-11-22
 */

public class AndroidGpsLocationClient extends BaseLocationClient {

    private Location lastLocation;

    private long updateInterval = 5000;
    private long fastestUpdateInterval = 1000;
    private Object gpsStatusCallback;
    private AndroidGpsLocationListener androidGpsLocationListener;


    public AndroidGpsLocationClient(@NonNull Context context) {
        this.context = context;
        locationManager = (LocationManager) context.getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        androidGpsLocationListener = new AndroidGpsLocationListener();
    }

    @Override
    public void stopLocationUpdates() {
        if (isMonitoringLocation()) {
            lastLocation = null;

            locationManager.removeUpdates(androidGpsLocationListener);

            for (LocationListener locationListener : getLocationListeners()) {
                locationManager.removeUpdates(locationListener);
            }
            clearLocationListeners();
        }

        unregisterForGpsStoppedEvent();
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

    @SuppressLint("MissingPermission")
    @Nullable
    @Override
    public Location getLastLocation() {
        Location lastLocationFromProvider = locationManager.getLastKnownLocation(getProvider());
        return lastLocationFromProvider != null && (lastLocation == null || lastLocationFromProvider.getTime() > lastLocation.getTime()) ? lastLocationFromProvider : lastLocation;
    }

    @Override
    public void requestLocationUpdates(@NonNull LocationListener locationListener) {
        addLocationListener(locationListener);
        if (isProviderEnabled()) {
            try {
                Location location = locationManager.getLastKnownLocation(getProvider());
                if (location != null && (lastLocation == null || (location != null && isBetterLocation(location, lastLocation)))) {
                    lastLocation = location;
                    locationListener.onLocationChanged(lastLocation);
                } else {
                    // Enable snackbar in the activity
                    Toast.makeText(context, R.string.retrieving_gps_location_couple_of_minutes, Toast.LENGTH_SHORT).show();
                }

                locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER
                        , fastestUpdateInterval
                        , 0f
                        , androidGpsLocationListener);

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

    @Override
    public void setUpdateIntervals(long updateInterval, long fastestUpdateInterval) {
        this.updateInterval = updateInterval;
        this.fastestUpdateInterval = fastestUpdateInterval;
    }

    @NonNull
    @Override
    public String getProvider() {
        return LocationManager.GPS_PROVIDER;
    }

    @Override
    public void close() {
        stopLocationUpdates();
        super.close();
    }

    @VisibleForTesting
    protected class AndroidGpsLocationListener extends BaseLocationListener {

        @Override
        public void onLocationChanged(Location location) {
            lastLocation = location;

            ArrayList<LocationListener> locationListeners = getLocationListeners();
            if (locationListeners != null) {
                for(LocationListener locationListener: locationListeners) {
                    locationListener.onLocationChanged(location);
                }
            }
        }

    }
}
