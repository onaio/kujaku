package io.ona.kujaku.location.clients;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

import io.ona.kujaku.interfaces.ILocationClient;
import io.ona.kujaku.listeners.LocationClientListener;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 03/10/2018
 */

public abstract class BaseLocationClient implements ILocationClient {

    private LocationClientListener locationClientListener;
    private ArrayList<LocationListener> locationListeners = new ArrayList<>();
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

    /**
     * @deprecated use {@code addLocationListener} instead
     */
    @Deprecated
    @Override
    public void setLocationListener(@Nullable LocationListener locationListener) {
        locationListeners.add(0, locationListener);
    }

    /**
     * @deprecated use {@code getLocationListeners} instead
     */
    @Deprecated
    @Nullable
    @Override
    public LocationListener getLocationListener() {
        if (locationListeners.size() > 0) {
            return locationListeners.get(0);
        }
        return null;
    }

    @Override
    public boolean addLocationListener(@NonNull LocationListener locationListener) {
        if (!locationListeners.contains(locationListener)) {
            return locationListeners.add(locationListener);
        }
        return false;
    }

    @Override
    public ArrayList<LocationListener> getLocationListeners() {
        return locationListeners;
    }


    @Override
    public boolean removeLocationListener(@NonNull LocationListener locationListener) {
        return getLocationListeners().remove(locationListener);
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
        int accuracyDelta = (int)(location.getAccuracy() - currentBestLocation.getAccuracy());
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

    @Override
    public boolean isMonitoringLocation() {
        return getLocationListeners().size() > 0;
    }

    @Override
    public void clearLocationListeners() {
        getLocationListeners().clear();
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
