package io.ona.kujaku.interfaces;

import android.location.Location;
import android.location.LocationListener;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import io.ona.kujaku.listeners.LocationClientListener;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 03/10/2018
 */

public interface ILocationClient {

    void stopLocationUpdates();

    void setListener(@Nullable LocationClientListener locationClientListener);

    @Nullable LocationClientListener getListener();

    void setLocationListener(@NonNull LocationListener locationListener);

    @Nullable LocationListener getLocationListener();

    @Nullable Location getLastLocation();

    void requestLocationUpdates(@NonNull LocationListener locationListener);

    void setUpdateIntervals(long updateInterval, long fastestUpdateInterval);

    boolean isMonitoringLocation();

    String getProvider();

    boolean isProviderEnabled();

    /**
     * This removes references to contexts from the class and LocationManager
     *
     */
    void close();
}
