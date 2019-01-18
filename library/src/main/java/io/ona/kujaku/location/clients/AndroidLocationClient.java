package io.ona.kujaku.location.clients;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import io.ona.kujaku.R;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 03/10/2018
 */

public class AndroidLocationClient extends BaseLocationClient implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private GoogleApiClient googleApiClient;
    private Location lastLocation;

    private long updateInterval = 5000;
    private long fastestUpdateInterval = 1000;

    private boolean waitingForConnection = false;

    private static final String TAG = AndroidLocationClient.class.getName();

    public AndroidLocationClient(@NonNull Context context) {
        this.context = context;
        locationManager = (LocationManager) context.getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        initGoogleApiClient();
    }

    @Override
    public void stopLocationUpdates() {
        if (isMonitoringLocation()) {
            waitingForConnection = false;
            lastLocation = null;
            setLocationListener(null);
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);

            // Probably not a good idea, but works for now until a solution around this
            disconnectGoogleApiClient();
        }
    }

    @Override
    public Location getLastLocation() {
        return lastLocation;
    }


    private void initGoogleApiClient() {
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(context)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }

        googleApiClient.connect();
    }

    private void disconnectGoogleApiClient() {
        if (googleApiClient != null) {
            googleApiClient.disconnect();
        }
    }
    
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (waitingForConnection) {
            waitingForConnection = false;
            if (getLocationListener() != null) {
                requestLocationUpdates(getLocationListener());
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Toast.makeText(context, R.string.msg_location_retrieval_taking_longer_than_expected, Toast.LENGTH_LONG)
                .show();

        stopLocationUpdates();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(context, R.string.msg_could_not_find_your_location, Toast.LENGTH_LONG)
                .show();
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
            if (!googleApiClient.isConnected() || googleApiClient.isConnecting()) {
                if (!googleApiClient.isConnected() && !googleApiClient.isConnecting()) {
                    initGoogleApiClient();
                }

                waitingForConnection = true;
            } else {
                LocationRequest locationRequest = new LocationRequest();
                locationRequest.setInterval(updateInterval);
                locationRequest.setFastestInterval(fastestUpdateInterval);
                locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

                try {
                    LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
                    lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
                } catch (SecurityException e) {
                    Log.e(TAG, Log.getStackTraceString(e));
                    Toast.makeText(context, R.string.location_disabled_location_permissions_not_granted, Toast.LENGTH_LONG)
                            .show();
                }
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
        disconnectGoogleApiClient();

        super.close();
    }
}
