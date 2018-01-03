package io.ona.kujaku.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;

import io.ona.kujaku.services.MapboxOfflineDownloaderService;
import io.ona.kujaku.utils.Constants;

/**
 * This receiver starts the {@link MapboxOfflineDownloaderService} a data connection (MOBILE or WIFI) is enabled.
 *
 * <p>
 *     <b>NOTE:</b> For >= API 24, Connectivity changes for Mobile Data Connections cannot be received
 *     automatically. {@link io.ona.kujaku.KujakuApplication} needs to be extended to register for
 *     the {@link ConnectivityManager#CONNECTIVITY_ACTION} events
 * </p>
 *
 * Created by Ephraim Kigamba - ekigamba@ona.io on 15/11/2017.
 */

public class KujakuNetworkChangeReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        if (activeNetwork != null) {
            resumeMapDownload(context);
         }
    }

    private void resumeMapDownload(Context context) {
        Intent mapService = new Intent(context, MapboxOfflineDownloaderService.class);
        mapService.putExtra(Constants.PARCELABLE_KEY_SERVICE_ACTION, Constants.SERVICE_ACTION.NETWORK_RESUME);
        context.startService(mapService);
    }

    /**
     * Registers for {@link ConnectivityManager#CONNECTIVITY_ACTION} events to be received by
     * {@link KujakuNetworkChangeReceiver} within the given Context
     *
     * @param context
     */
    public static void registerNetworkChangesBroadcastReceiver(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            KujakuNetworkChangeReceiver kujakuNetworkChangeReceiver = new KujakuNetworkChangeReceiver();
            IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
            context.registerReceiver(kujakuNetworkChangeReceiver, intentFilter);
        }
    }
}
