package io.ona.kujaku.location.clients;

import android.location.LocationListener;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import io.ona.kujaku.interfaces.ILocationClient;
import io.ona.kujaku.listeners.LocationClientListener;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 03/10/2018
 */

public abstract class BaseLocationClient implements ILocationClient {

    private LocationClientListener locationClientListener;
    private LocationListener locationListener;

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
}
