package io.ona.kujaku.notifications;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import java.text.DecimalFormat;

import io.ona.kujaku.R;
import io.ona.kujaku.data.realm.objects.MapBoxOfflineQueueTask;
import io.ona.kujaku.services.MapboxOfflineDownloaderService;
import io.ona.kujaku.utils.Constants;

/**
 * Creates the appropriate channel for download progress notifications. Displays download progress
 * notification or updates the current one with a valid stop button that stops the current offline map download
 * <p>
 * Created by Ephraim Kigamba - ekigamba@ona.io on 11/01/2018.
 */
public class DownloadProgressNotification extends KujakuNotification {

    public static final String CHANNEL_ID = "KUJAKU_DOWNLOAD_PROGRESS_CHANNEL";
    protected NotificationCompat.Builder notificationBuilder;
    private Intent stopDownloadIntent;

    public DownloadProgressNotification(Context context) {
        setContext(context);
        setSmallIcon(R.drawable.ic_stat_file_download);

        createNotificationChannel(NotificationManager.IMPORTANCE_LOW, context.getString(R.string.download_progress_channel_name), CHANNEL_ID, context.getString(R.string.download_progress_channel_description));
    }

    /**
     * Creates the initial progress notification with a Stop Download action to stop the current download when clicked. It however does not display the created notification
     *
     * @param mapName
     * @param mapBoxAccessToken
     * @param requestCode
     * @param showAction
     */
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

    /**
     * Updates the download progress notification by changing the map name, updating the {@link PendingIntent}
     * for the STOP DOWNLOAD action & updating the download progress percentage
     *
     * @param percentageProgress
     * @param mapName
     * @param requestCode
     * @param showAction
     */
    public void updateNotification(double percentageProgress, String mapName, int requestCode, boolean showAction) {
        if (percentageProgress == 0 && showAction) {
            updateNotificationWithNewMapDownload(mapName, requestCode);
        }

        // Remove all previous actions if showAction is false
        if (!showAction) {
            clearActions();
        }

        updateNotificationWithDownloadProgress(percentageProgress);
    }

    /**
     * Displays a foreground notification using the {@link Context} from the calling {@link Service} passed
     *
     * @param notificationBuilder
     * @param notificationId
     */
    public void displayForegroundNotification(NotificationCompat.Builder notificationBuilder, int notificationId) {
        ((Service) context).startForeground(notificationId, notificationBuilder.build());
    }

    /**
     * Displays foreground notification using the {@link Context} from the calling {@link Service} passed on instantiation
     *
     * @param notificationId
     */
    public void displayForegroundNotification(int notificationId) {
        displayForegroundNotification(notificationBuilder, notificationId);
    }

    /**
     * Changes the map name on the {@link NotificationCompat.Builder} title and replaces {@link PendingIntent} for the STOP DOWNLOAD action
     *
     * @param mapName
     * @param requestCode
     */
    public void updateNotificationWithNewMapDownload(String mapName, int requestCode) {
        notificationBuilder.setContentTitle(String.format(context.getString(R.string.notification_download_progress_title), mapName));

        stopDownloadIntent.putExtra(Constants.PARCELABLE_KEY_MAP_UNIQUE_NAME, mapName);

        PendingIntent stopDownloadPendingIntent = PendingIntent.getService(context, requestCode, stopDownloadIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        NotificationCompat.Action stopDownloadAction = new NotificationCompat.Action(R.drawable.ic_mapbox_download_stop, context.getString(R.string.stop_download), stopDownloadPendingIntent);

        notificationBuilder.mActions.clear();
        notificationBuilder.addAction(stopDownloadAction);
    }

    public void displayNotification(int notificationId) {
        displayNotification(notificationBuilder, notificationId);
    }

    /**
     * It updates the notification download progress by updating the text for the {@link NotificationCompat.Builder}
     *
     * @param percentageProgress
     */
    public void updateNotificationWithDownloadProgress(double percentageProgress) {
        notificationBuilder.setContentText(String.format(context.getString(R.string.notification_download_progress_content), formatDecimal(percentageProgress)));
    }

    /**
     * It clears the {@link NotificationCompat.Builder} actions
     */
    public void clearActions() {
        notificationBuilder.mActions.clear();
    }

    /**
     * Creates a valid service intent with {@link MapboxOfflineDownloaderService.SERVICE_ACTION#STOP_CURRENT_DOWNLOAD}
     * {@link io.ona.kujaku.services.MapboxOfflineDownloaderService.SERVICE_ACTION} to be sent to the
     * {@Link MapboxOfflineDownloaderService} given the map name & Mapbox Access Token
     *
     * @param mapName
     * @param mapBoxAccessToken
     * @return
     */
    public Intent createStopDownloadIntent(String mapName, String mapBoxAccessToken) {
        Intent stopDownloadIntent = new Intent(context, MapboxOfflineDownloaderService.class);
        stopDownloadIntent.putExtra(Constants.PARCELABLE_KEY_SERVICE_ACTION, MapboxOfflineDownloaderService.SERVICE_ACTION.STOP_CURRENT_DOWNLOAD);
        stopDownloadIntent.putExtra(Constants.PARCELABLE_KEY_MAP_UNIQUE_NAME, mapName);
        stopDownloadIntent.putExtra(Constants.PARCELABLE_KEY_MAPBOX_ACCESS_TOKEN, mapBoxAccessToken);
        stopDownloadIntent.putExtra(Constants.PARCELABLE_KEY_DELETE_TASK_TYPE, MapBoxOfflineQueueTask.TASK_TYPE_DOWNLOAD);

        return stopDownloadIntent;
    }

    /**
     * Formats a decimal to between 0-2 decimal places depending on the decimal places of the double passed
     *
     * @param no
     * @return
     */
    protected static String formatDecimal(double no) {
        DecimalFormat twoDForm = new DecimalFormat("0.##");
        return twoDForm.format(no);
    }
}
