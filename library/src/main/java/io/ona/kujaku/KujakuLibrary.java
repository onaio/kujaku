package io.ona.kujaku;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

import java.util.List;

import io.ona.kujaku.data.realm.RealmDatabase;
import io.ona.kujaku.domain.Point;
import io.ona.kujaku.exceptions.KujakuLibraryInitializationException;
import io.ona.kujaku.helpers.ActivityLauncherHelper;
import io.ona.kujaku.receivers.KujakuNetworkChangeReceiver;
import io.ona.kujaku.services.MapboxOfflineDownloaderService;
import io.ona.kujaku.utils.Constants;

import static android.content.Context.ALARM_SERVICE;

/**
 * @author Vincent Karuri
 */
public class KujakuLibrary {

    private static boolean enableMapDownloadResume;

    private static KujakuLibrary library;

    private KujakuLibrary() {}

    public static KujakuLibrary getInstance() {
        if (library == null) {
            throw new KujakuLibraryInitializationException("KujakuLibrary was not initialized! Please call KujakuLibrary's init method " +
                    "in your application's onCreate method before attempting to access the library instance.");
        }
        return library;
    }

    public static void init(Context context) {
        RealmDatabase.init(context);

        if (isEnableMapDownloadResume()) {
            KujakuNetworkChangeReceiver.registerNetworkChangesBroadcastReceiver(context);
            resumeMapDownload(context);
        }
        library = new KujakuLibrary();
    }

    private static void resumeMapDownload(Context context) {
        Intent mapService = new Intent(context, MapboxOfflineDownloaderService.class);
        mapService.putExtra(Constants.PARCELABLE_KEY_SERVICE_ACTION, MapboxOfflineDownloaderService.SERVICE_ACTION.NETWORK_RESUME);

        PendingIntent pendingIntent = PendingIntent.getService(context, Constants.MAP_DOWNLOAD_SERVICE_ALARM_REQUEST_CODE, mapService, 0);
        //Add the alarm here
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        alarmManager.setInexactRepeating(
                AlarmManager.ELAPSED_REALTIME
                , SystemClock.elapsedRealtime() + Constants.MAP_DOWNLOAD_SERVICE_ALARM_INTERVAL
                , Constants.MAP_DOWNLOAD_SERVICE_ALARM_INTERVAL
                , pendingIntent);
    }

    public static boolean isEnableMapDownloadResume() {
        return enableMapDownloadResume;
    }

    public void setEnableMapDownloadResume(boolean enableMapDownloadResume) {
        this.enableMapDownloadResume = enableMapDownloadResume;
    }


    public void launchMapActivity(Activity hostActivity, List<Point> points, boolean enableDropPoint) {
        ActivityLauncherHelper.launchMapActivity(hostActivity, points, enableDropPoint);
    }
}
