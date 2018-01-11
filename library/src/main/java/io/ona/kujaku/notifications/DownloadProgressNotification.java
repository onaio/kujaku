package io.ona.kujaku.notifications;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import java.text.DecimalFormat;

import io.ona.kujaku.R;
import io.ona.kujaku.data.realm.objects.MapBoxOfflineQueueTask;
import io.ona.kujaku.services.MapboxOfflineDownloaderService;
import utils.Constants;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 11/01/2018.
 */

public class DownloadProgressNotification extends KujakuNotification {

    public static final String CHANNEL_ID = "KUJAKU_DOWNLOAD_PROGRESS_CHANNEL";
    protected NotificationCompat.Builder notificationBuilder;
    private Intent stopDownloadIntent;

    public DownloadProgressNotification(Context context) {
        setContext(context);
        setSmallIcon(R.drawable.ic_stat_file_download);

        createNotificationChannel(NotificationManager.IMPORTANCE_DEFAULT, context.getString(R.string.download_progress_channel_name), CHANNEL_ID, context.getString(R.string.download_progress_channel_description));
    }

    public void createInitialNotification(String mapName, String mapBoxAccessToken, int requestCode, boolean showAction) {
        notificationBuilder = createNotification(String.format(context.getString(R.string.notification_download_progress_title), mapName));

        if (showAction) {
            stopDownloadIntent = createStopDownloadIntent(mapName, mapBoxAccessToken);

            PendingIntent stopDownloadPendingIntent = PendingIntent.getService(context, requestCode, stopDownloadIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            NotificationCompat.Action stopDownloadAction = new NotificationCompat.Action(R.drawable.ic_mapbox_download_stop, context.getString(R.string.stop_download), stopDownloadPendingIntent);

            notificationBuilder.mActions.clear();
            notificationBuilder.addAction(stopDownloadAction);
        }
    }

    public void updateNotification(double percentageProgress, String mapName, int requestId, boolean showAction) {
        if (percentageProgress == 0 && showAction) {
            updateNotificationWithNewMapDownload(mapName, requestId);
        }

        // Remove all previous actions if showAction is false
        if (!showAction) {
            clearActions();
        }

        updateNotificationWithDownloadProgress(percentageProgress);
    }

    public void displayForegroundNotification(NotificationCompat.Builder notificationBuilder, int notificationId) {
        ((Service) context).startForeground(notificationId, notificationBuilder.build());
    }

    public void displayForegroundNotification(int notificationId) {
        displayForegroundNotification(notificationBuilder, notificationId);
    }

    public void updateNotificationWithNewMapDownload(String mapName, int requestCode) {
        notificationBuilder.setContentTitle(String.format(context.getString(R.string.notification_download_progress_title), mapName));

        stopDownloadIntent.putExtra(Constants.PARCELABLE_KEY_MAP_UNIQUE_NAME, mapName);

        PendingIntent stopDownloadPendingIntent = PendingIntent.getService(context, requestCode, stopDownloadIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        NotificationCompat.Action stopDownloadAction =  new NotificationCompat.Action(R.drawable.ic_mapbox_download_stop, context.getString(R.string.stop_download), stopDownloadPendingIntent);

        notificationBuilder.mActions.clear();
        notificationBuilder.addAction(stopDownloadAction);
    }

    public void displayNotification(int notificationId) {
        displayNotification(notificationBuilder, notificationId);
    }

    public void updateNotificationWithDownloadProgress(double percentageProgress) {
        notificationBuilder.setContentText(String.format(context.getString(R.string.notification_download_progress_content), formatDecimal(percentageProgress)));
    }

    public void clearActions() {
        notificationBuilder.mActions.clear();
    }

    public Intent createStopDownloadIntent(String mapName, String mapBoxAccessToken) {
        Intent stopDownloadIntent = new Intent(context, MapboxOfflineDownloaderService.class);
        stopDownloadIntent.putExtra(Constants.PARCELABLE_KEY_SERVICE_ACTION, MapboxOfflineDownloaderService.SERVICE_ACTION.STOP_CURRENT_DOWNLOAD);
        stopDownloadIntent.putExtra(Constants.PARCELABLE_KEY_MAP_UNIQUE_NAME, mapName);
        stopDownloadIntent.putExtra(Constants.PARCELABLE_KEY_MAPBOX_ACCESS_TOKEN, mapBoxAccessToken);
        stopDownloadIntent.putExtra(Constants.PARCELABLE_KEY_DELETE_TASK_TYPE, MapBoxOfflineQueueTask.TASK_TYPE_DOWNLOAD);

        return stopDownloadIntent;
    }

    private String formatDecimal(double no) {
        java.text.DecimalFormat twoDForm = new DecimalFormat("0.##");
        return twoDForm.format(no);
    }
}
