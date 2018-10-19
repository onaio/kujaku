package io.ona.kujaku;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

import org.json.JSONObject;

import io.ona.kujaku.data.realm.RealmDatabase;
import io.ona.kujaku.receivers.KujakuNetworkChangeReceiver;
import io.ona.kujaku.services.MapboxOfflineDownloaderService;
import io.ona.kujaku.utils.Constants;

/**
 * This application class should be extended for all apps especially if you expect your app to be
 * used in Android Nougat(7) API 24 so that the SDK can receive Connectivity State changes and resume
 * download of incomplete maps
 *
 * Created by Ephraim Kigamba and Vincent Karuri
*/

public abstract class BaseKujakuApplication extends Application {

    private boolean enableMapDownloadResume;
    private BaseKujakuApplication hostApplication;

    protected void init(BaseKujakuApplication application) {
        KujakuLibrary.getInstance().setHostApplication(application);
        setHostApplication(application);

        RealmDatabase.init(getHostApplication());
        if (isEnableMapDownloadResume()) {
            KujakuNetworkChangeReceiver.registerNetworkChangesBroadcastReceiver(getHostApplication());
            resumeMapDownload(getHostApplication());
        }
    }

    private void resumeMapDownload(Context context) {
        Intent mapService = new Intent(context, MapboxOfflineDownloaderService.class);
        mapService.putExtra(Constants.PARCELABLE_KEY_SERVICE_ACTION, MapboxOfflineDownloaderService.SERVICE_ACTION.NETWORK_RESUME);

        PendingIntent pendingIntent = PendingIntent.getService(getHostApplication(), Constants.MAP_DOWNLOAD_SERVICE_ALARM_REQUEST_CODE, mapService, 0);
        //Add the alarm here
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.setInexactRepeating(
                AlarmManager.ELAPSED_REALTIME
                , SystemClock.elapsedRealtime() + Constants.MAP_DOWNLOAD_SERVICE_ALARM_INTERVAL
                , Constants.MAP_DOWNLOAD_SERVICE_ALARM_INTERVAL
                , pendingIntent);
    }

    public abstract void processFeatureJSON(JSONObject points);

    public boolean isEnableMapDownloadResume() {
        return enableMapDownloadResume;
    }

    public void setEnableMapDownloadResume(boolean enableMapDownloadResume) {
        this.enableMapDownloadResume = enableMapDownloadResume;
    }

    protected BaseKujakuApplication getHostApplication() {
        return hostApplication;
    }

    protected void setHostApplication(BaseKujakuApplication hostApplication) {
        this.hostApplication = hostApplication;
    }
}
