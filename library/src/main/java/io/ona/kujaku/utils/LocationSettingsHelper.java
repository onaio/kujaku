package io.ona.kujaku.utils;

import android.content.Context;

import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 31/10/2018
 */

public class LocationSettingsHelper {

    public static void checkLocationEnabled(Context context) {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(null);

        /*SettingsClient client = LocationServices. getSettingsClient(context);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());*/
    }
}
