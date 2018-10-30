package io.ona.kujaku.location;

import android.content.Context;
import android.location.Location;
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
 * Created by Ephraim Kigamba - ekigamba@ona.io on 06/02/2018.
 */

public class GoogleLocationClient implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private GoogleApiClient googleApiClient;
    private Location lastLocation;

    private static final String TAG = GoogleLocationClient.class.getName();

    private Context context;
    private LocationListener locationListener;

    private long locationRequestInterval = 5000;
    private long locationRequestFastestInterval = 1000;

    public GoogleLocationClient(@NonNull Context context, LocationListener locationListener) {
        this.context = context;
        this.locationListener = locationListener;
    }

    public void initGoogleApiClient() {
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(context)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }

        googleApiClient.connect();
    }

    public void disconnectGoogleApiClient() {
        if (googleApiClient != null) {
            googleApiClient.disconnect();
        }
    }

    // GPS - Location Stuff
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(locationRequestInterval);
        locationRequest.setFastestInterval(locationRequestFastestInterval);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        try {
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
            lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        } catch (SecurityException e) {
            Log.e(TAG, Log.getStackTraceString(e));
            Toast.makeText(context, "Sorry but we could not get your location since the app does not have permissions to access your Location", Toast.LENGTH_LONG)
                    .show();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Toast.makeText(context, R.string.msg_location_retrieval_taking_longer_than_expected, Toast.LENGTH_LONG)
                .show();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(context, R.string.msg_could_not_find_your_location, Toast.LENGTH_LONG)
                .show();
    }

    @Override
    public void onLocationChanged(Location location) {
        lastLocation = location;

        if (locationListener != null) {
            locationListener.onLocationChanged(location);
        }
    }

    public void setLocationRequestInterval(long locationRequestInterval) {
        this.locationRequestInterval = locationRequestInterval;
    }

    public void setLocationRequestFastestInterval(long locationRequestFastestInterval) {
        this.locationRequestFastestInterval = locationRequestFastestInterval;
    }

    public Location getLastLocation() {
        return lastLocation;
    }
}
