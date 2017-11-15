package io.ona.kujaku;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

import io.ona.kujaku.receivers.KujakuNetworkChangeReceiver;
import io.ona.kujaku.services.MapboxOfflineDownloaderService;
import io.ona.kujaku.utils.Constants;

/**
 * This application class should be extended for all apps especially if you expect your app to be
 * used in Android Nougat(7) API 24 so that the SDK can receive Connectivity State changes and resume
 * download of incomplete maps
 *
 * Created by Ephraim Kigamba - ekigamba@ona.io on 15/11/2017.
 */

public class KujakuApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        KujakuNetworkChangeReceiver.registerNetworkChangesBroadcastReceiver(getApplicationContext());
        resumeMapDownload(this);
    }

    private void resumeMapDownload(Context context) {
        Intent mapService = new Intent(context, MapboxOfflineDownloaderService.class);
        mapService.putExtra(Constants.PARCELABLE_KEY_SERVICE_ACTION, Constants.SERVICE_ACTION.NETWORK_RESUME);

        PendingIntent pendingIntent = PendingIntent.getService(this, Constants.MAP_DOWNLOAD_SERVICE_ALARM_REQUEST_CODE, mapService, 0);
        //Add the alarm here
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.setInexactRepeating(
                AlarmManager.ELAPSED_REALTIME
                , SystemClock.elapsedRealtime() + Constants.MAP_DOWNLOAD_SERVICE_ALARM_INTERVAL
                , Constants.MAP_DOWNLOAD_SERVICE_ALARM_INTERVAL
                , pendingIntent);
    }
}
