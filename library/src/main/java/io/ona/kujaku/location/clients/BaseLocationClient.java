package io.ona.kujaku.location.clients;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.annotation.Nullable;

import io.ona.kujaku.interfaces.ILocationClient;
import io.ona.kujaku.listeners.LocationClientListener;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 03/10/2018
 */

public abstract class BaseLocationClient implements ILocationClient {

    private LocationClientListener locationClientListener;
    private LocationListener locationListener;
    protected LocationManager locationManager;
    protected Context context;

    private static final int TWO_MINUTES = 1000 * 60 * 2;

    @Override
    public void setListener(LocationClientListener locationClientListener) {
        this.locationClientListener = locationClientListener;
    }

    @Nullable
    @Override
    public LocationClientListener getListener() {
        return locationClientListener;
    }

    @Override
    public void setLocationListener(@Nullable LocationListener locationListener) {
        this.locationListener = locationListener;
    }

    @Nullable
    @Override
    public LocationListener getLocationListener() {
        return locationListener;
    }

    @Override
    public boolean isProviderEnabled() {
        return locationManager != null && locationManager.isProviderEnabled(getProvider());
    }

    @Override
    public void close() {
        locationManager = null;
        context = null;
    }

    /**
     * Determines whether one Location reading is better than the current Location fix
     *
     * @param location            The new Location that you want to evaluate
     * @param currentBestLocation The current Location fix, to which you want to compare the new one
     */
    protected boolean isBetterLocation(Location location, Location currentBestLocation) {
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

    /**
     * Checks whether two providers are the same
     */
    protected boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }
}
