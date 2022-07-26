package io.ona.kujaku.interfaces;

import android.location.Location;
import android.location.LocationListener;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

import io.ona.kujaku.listeners.LocationClientListener;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 03/10/2018
 */

public interface ILocationClient {

    void stopLocationUpdates();

    void setListener(@Nullable LocationClientListener locationClientListener);

    @Nullable LocationClientListener getListener();

    /**
     * @deprecated use {@code addLocationListener} instead
     */
    @Deprecated
    void setLocationListener(@NonNull LocationListener locationListener);

    /**
     *
     * @param locationListener required listener which needs to receive the location updates
     * @return <tt>true</tt> if {@param locationListener} was added as a result of this call
     */
    boolean addLocationListener(@NonNull LocationListener locationListener);

    /**
     * @deprecated use {@code getLocationListeners} instead
     */
    @Deprecated
    @Nullable LocationListener getLocationListener();

    boolean removeLocationListener(@NonNull LocationListener locationListener);

    ArrayList<LocationListener> getLocationListeners();

    void clearLocationListeners();

    @Nullable Location getLastLocation();

    void requestLocationUpdates(@NonNull LocationListener locationListener);

    void setUpdateIntervals(long updateInterval, long fastestUpdateInterval);

    boolean isMonitoringLocation();

    @NonNull
    String getProvider();

    boolean isProviderEnabled();

    /**
     * This removes references to contexts from the class and LocationManager
     *
     */
    void close();
}
