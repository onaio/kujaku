package io.ona.kujaku.location.clients;

import android.content.Context;
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
}
