package io.ona.kujaku.location.clients;

import android.location.Location;
import android.location.LocationListener;
import android.support.annotation.NonNull;

import io.ona.kujaku.interfaces.ILocationClient;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 03/10/2018
 */

public class GPSLocationClient extends BaseLocationClient {

    @Override
    public void stopLocationUpdates() {

    }

    @Override
    public Location getLastLocation() {
        return null;
    }

    @Override
    public void requestLocationUpdates(@NonNull LocationListener locationListener) {

    }

    @Override
    public void setUpdateIntervals(long updateInterval, long fastestUpdateInterval) {

    }

    @Override
    public boolean isMonitoringLocation() {
        return false;
    }
}
