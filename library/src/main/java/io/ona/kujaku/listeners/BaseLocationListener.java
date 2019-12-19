package io.ona.kujaku.listeners;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.mapbox.android.core.location.LocationEngineCallback;
import com.mapbox.android.core.location.LocationEngineResult;

import timber.log.Timber;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 03/10/2018
 */

public class BaseLocationListener implements LocationListener, LocationEngineCallback<LocationEngineResult> {

    @Override
    public void onLocationChanged(Location location) {
        // Do nothing
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // Do nothing
    }

    @Override
    public void onProviderEnabled(String provider) {
        // Do nothing
    }

    @Override
    public void onProviderDisabled(String provider) {
        // Do nothing
    }

    @Override
    public void onSuccess(LocationEngineResult result) {
        Location location = result.getLastLocation();
        if (location != null) {
            onLocationChanged(location);
        }
    }

    @Override
    public void onFailure(@NonNull Exception exception) {
        Timber.e(exception);
    }
}
