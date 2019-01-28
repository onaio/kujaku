package io.ona.kujaku.utils;

import android.app.Activity;
import android.content.IntentSender;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 31/10/2018
 */

public class LocationSettingsHelper {

    private static final String TAG = LocationSettingsHelper.class.getName();

    /**
     * Checks if location is currently enabled & if the location settings are at high accuracy for
     * use in {@link io.ona.kujaku.views.KujakuMapView}
     *
     * @param activity
     */
    public static void checkLocationEnabled(Activity activity, ResultCallback<LocationSettingsResult> resultCallback) {
        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(activity)
                .addApi(LocationServices.API).build();
        googleApiClient.connect();

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(10000 / 2);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
        result.setResultCallback(resultCallback);
    }
}
