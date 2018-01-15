package io.ona.kujaku.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;

import org.junit.Test;

import java.util.UUID;

import io.ona.kujaku.R;
import io.ona.kujaku.data.realm.objects.MapBoxOfflineQueueTask;
import io.ona.kujaku.services.MapboxOfflineDownloaderService;
import utils.Constants;

import static org.junit.Assert.*;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 15/01/2018.
 */
public class DownloadProgressNotificationTest extends BaseNotificationsTest {

    @Test
    public void constructorShouldInitialiseVariables() throws NoSuchFieldException, IllegalAccessException {
        DownloadProgressNotification downloadProgressNotification = new DownloadProgressNotification(context);

        assertEquals(context, downloadProgressNotification.context);
        assertTrue(getValueInPrivateField(DownloadProgressNotification.class.getSuperclass(), downloadProgressNotification, "smallIcon") != null);

        channelIdsAdded.add(downloadProgressNotification.CHANNEL_ID);
    }

    @RequiresApi(26)
    @Test
    public void constructorShouldCreateValidNotification() {
        DownloadProgressNotification downloadProgressNotification = new DownloadProgressNotification(context);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel notificationChannel = notificationManager.getNotificationChannel(DownloadProgressNotification.CHANNEL_ID);

        channelIdsAdded.add(downloadProgressNotification.CHANNEL_ID);

        assertEquals(context.getString(R.string.download_progress_channel_name), notificationChannel.getName());
        assertEquals(context.getString(R.string.download_progress_channel_description) ,notificationChannel.getDescription());
        assertFalse(notificationChannel.shouldVibrate());
        assertFalse(notificationChannel.shouldShowLights());
        assertEquals(NotificationManager.IMPORTANCE_LOW, notificationChannel.getImportance());
    }

    @Test
    public void createInitialNotificationShouldCreateNotificationBuilderWithoutAction() throws NoSuchFieldException, IllegalAccessException {
        DownloadProgressNotification downloadProgressNotification = new DownloadProgressNotification(context);

        channelIdsAdded.add(DownloadProgressNotification.CHANNEL_ID);

        String mapName = UUID.randomUUID().toString();
        String mapBoxAccessToken = "sample_access_token";
        int requestCode = 89230923;

        downloadProgressNotification.createInitialNotification(mapName, mapBoxAccessToken, requestCode, false);

        NotificationCompat.Builder builder = downloadProgressNotification.notificationBuilder;

        assertNotificationBuilder(builder, mapName);
    }

    // Fix this test which also fails due to the same permission error
    @Test
    public void createInitialNotificationShouldCreateNotificationBuilderWithStopDownloadAction() throws NoSuchFieldException, IllegalAccessException {
        DownloadProgressNotification downloadProgressNotification = new DownloadProgressNotification(context);

        channelIdsAdded.add(DownloadProgressNotification.CHANNEL_ID);

        String mapName = UUID.randomUUID().toString();
        String mapBoxAccessToken = "sample_access_token";
        int requestCode = 89230923;

        downloadProgressNotification.createInitialNotification(mapName, mapBoxAccessToken, requestCode, true);

        NotificationCompat.Builder builder = downloadProgressNotification.notificationBuilder;

        assertNotificationBuilder(builder, mapName);

        // Check the action contents
        Intent stopDownloadIntent = new Intent(context, MapboxOfflineDownloaderService.class);
        stopDownloadIntent.putExtra(Constants.PARCELABLE_KEY_SERVICE_ACTION, MapboxOfflineDownloaderService.SERVICE_ACTION.STOP_CURRENT_DOWNLOAD);
        stopDownloadIntent.putExtra(Constants.PARCELABLE_KEY_MAP_UNIQUE_NAME, mapName);
        stopDownloadIntent.putExtra(Constants.PARCELABLE_KEY_MAPBOX_ACCESS_TOKEN, mapBoxAccessToken);
        stopDownloadIntent.putExtra(Constants.PARCELABLE_KEY_DELETE_TASK_TYPE, MapBoxOfflineQueueTask.TASK_TYPE_DOWNLOAD);

        NotificationCompat.Action actualAction = builder.mActions.get(0);
        compareIntent(stopDownloadIntent, getIntent(actualAction.getActionIntent()));
    }

    @Test
    public void updateNotificationShouldNotUpdateNotificationWithNewMapName() throws NoSuchFieldException, IllegalAccessException {
        DownloadProgressNotification downloadProgressNotification = new DownloadProgressNotification(context);

        channelIdsAdded.add(DownloadProgressNotification.CHANNEL_ID);

        String mapName = UUID.randomUUID().toString();
        String mapBoxAccessToken = "sample_access_token";
        int requestCode = 89238087;
        double percentageProgress = 45d;

        downloadProgressNotification.createInitialNotification(mapName, mapBoxAccessToken, requestCode, false);

        downloadProgressNotification.updateNotification(percentageProgress, UUID.randomUUID().toString(), requestCode, false);
        assertNotificationBuilder(downloadProgressNotification.notificationBuilder, mapName, percentageProgress);
    }
/*
    @Test
    public void displayForegroundNotificationShouldStartForegroundNotification() {
        DownloadProgressNotification downloadProgressNotification = new DownloadProgressNotification(context);

        channelIdsAdded.add(DownloadProgressNotification.CHANNEL_ID);

        String mapName = UUID.randomUUID().toString();
        String mapBoxAccessToken = "sample_access_token";
        int requestCode = 89238087;
        double percentageProgress = 45d;

        downloadProgressNotification.createInitialNotification(mapName, mapBoxAccessToken, requestCode, false);
        downloadProgressNotification.updateNotification(percentageProgress, UUID.randomUUID().toString(), requestCode, false);

    }*/

    /*@Test
    public void displayForegroundNotificationShouldDisplayNotificationFromCurrentNotificationBuilder() {}*/

    // Fix this test which fails due to permission error when trying to get the Intent from PendingIntent
    @Test
    public void updateNotificationWithNewMapDownload() throws NoSuchFieldException, IllegalAccessException {
        DownloadProgressNotification downloadProgressNotification = new DownloadProgressNotification(context);

        channelIdsAdded.add(DownloadProgressNotification.CHANNEL_ID);

        String mapName = UUID.randomUUID().toString();
        String mapBoxAccessToken = "sample_access_token";
        int requestCode = 89238087;

        downloadProgressNotification.createInitialNotification(mapName, mapBoxAccessToken, requestCode, true);
        assertNotificationBuilder(downloadProgressNotification.notificationBuilder, mapName);

        String newMapName = "notification channel";
        downloadProgressNotification.updateNotificationWithNewMapDownload(newMapName, requestCode);
        assertNotificationBuilder(downloadProgressNotification.notificationBuilder, newMapName);

        Intent stopDownloadIntent = new Intent(context, MapboxOfflineDownloaderService.class);
        stopDownloadIntent.putExtra(Constants.PARCELABLE_KEY_SERVICE_ACTION, MapboxOfflineDownloaderService.SERVICE_ACTION.STOP_CURRENT_DOWNLOAD);
        stopDownloadIntent.putExtra(Constants.PARCELABLE_KEY_MAP_UNIQUE_NAME, mapName);
        stopDownloadIntent.putExtra(Constants.PARCELABLE_KEY_MAPBOX_ACCESS_TOKEN, mapBoxAccessToken);
        stopDownloadIntent.putExtra(Constants.PARCELABLE_KEY_DELETE_TASK_TYPE, MapBoxOfflineQueueTask.TASK_TYPE_DOWNLOAD);

        assertEquals(1, downloadProgressNotification.notificationBuilder.mActions.size());
        compareIntent(stopDownloadIntent, getIntent(downloadProgressNotification.notificationBuilder.mActions.get(0).getActionIntent()));
    }

    /*@Test
    public void displayNotificationShouldDisplayNotificationFromTheCurrentNotificationBuilder() throws NoSuchFieldException, IllegalAccessException {
        DownloadProgressNotification downloadProgressNotification = new DownloadProgressNotification(context);

        channelIdsAdded.add(DownloadProgressNotification.CHANNEL_ID);

        String mapName = UUID.randomUUID().toString();
        String mapBoxAccessToken = "sample_access_token";
        int requestCode = 89238087;
        int notificationId

        downloadProgressNotification.createInitialNotification(mapName, mapBoxAccessToken, requestCode, false);
        assertNotificationBuilder(downloadProgressNotification.notificationBuilder, mapName);

        downloadProgressNotification.displayNotification();
    }*/

    @Test
    public void updateNotificationWithDownloadProgress() throws NoSuchFieldException, IllegalAccessException {
        DownloadProgressNotification downloadProgressNotification = new DownloadProgressNotification(context);

        channelIdsAdded.add(DownloadProgressNotification.CHANNEL_ID);

        String mapName = UUID.randomUUID().toString();
        String mapBoxAccessToken = "sample_access_token";
        int requestCode = 89238087;
        double progressPercentage = 89d;

        downloadProgressNotification.createInitialNotification(mapName, mapBoxAccessToken, requestCode, false);
        downloadProgressNotification.updateNotificationWithDownloadProgress(2.434d);
        assertNotificationBuilder(downloadProgressNotification.notificationBuilder, mapName, 2.434d);

        downloadProgressNotification.updateNotificationWithDownloadProgress(progressPercentage);

        assertNotificationBuilder(downloadProgressNotification.notificationBuilder, mapName, progressPercentage);
    }

    @Test
    public void clearActions() {
        DownloadProgressNotification downloadProgressNotification = new DownloadProgressNotification(context);

        channelIdsAdded.add(DownloadProgressNotification.CHANNEL_ID);

        String mapName = UUID.randomUUID().toString();
        String mapBoxAccessToken = "sample_access_token";
        int requestCode = 89238087;

        downloadProgressNotification.createInitialNotification(mapName, mapBoxAccessToken, requestCode, true);
        assertEquals(1, downloadProgressNotification.notificationBuilder.mActions.size());

        downloadProgressNotification.clearActions();
        assertEquals(0, downloadProgressNotification.notificationBuilder.mActions.size());
    }

    @Test
    public void createStopDownloadIntentShouldCreateValidIntent() {
        DownloadProgressNotification downloadProgressNotification = new DownloadProgressNotification(context);

        channelIdsAdded.add(DownloadProgressNotification.CHANNEL_ID);
        String mapName = "my map yea";
        String mapBoxAccessToken = "sample_access_token";

        Intent stopDownloadIntent = downloadProgressNotification.createStopDownloadIntent(mapName, mapBoxAccessToken);

        assertEquals(mapName, stopDownloadIntent.getStringExtra(Constants.PARCELABLE_KEY_MAP_UNIQUE_NAME));
        assertEquals(mapBoxAccessToken, stopDownloadIntent.getStringExtra(Constants.PARCELABLE_KEY_MAPBOX_ACCESS_TOKEN));
        assertEquals(MapboxOfflineDownloaderService.SERVICE_ACTION.STOP_CURRENT_DOWNLOAD, stopDownloadIntent.getExtras().get(Constants.PARCELABLE_KEY_SERVICE_ACTION));
        assertEquals(MapBoxOfflineQueueTask.TASK_TYPE_DOWNLOAD, stopDownloadIntent.getExtras().get(Constants.PARCELABLE_KEY_DELETE_TASK_TYPE));
    }

    @Test
    public void formatDecimal() {
        String[] formatedDecimals = new String[]{"4", "78.1", "56.34", "56.34", "78.1"};
        double[] decimals = new double[]{4, 78.1, 56.34, 56.3409, 78.10};

        for(int i = 0; i < formatedDecimals.length; i++) {
            assertEquals(formatedDecimals[i], DownloadProgressNotification.formatDecimal(decimals[i]));
        }
    }

    protected void assertNotificationBuilder(NotificationCompat.Builder builder, String mapName) throws NoSuchFieldException, IllegalAccessException {
        assertNotificationBuilder(builder, String.format(context.getString(R.string.notification_download_progress_title), mapName), null, null);
    }

    protected void assertNotificationBuilder(NotificationCompat.Builder builder, String mapName, double percentageProgress) throws NoSuchFieldException, IllegalAccessException {
        assertNotificationBuilder(builder, String.format(context.getString(R.string.notification_download_progress_title), mapName), String.format(context.getString(R.string.notification_download_progress_content), DownloadProgressNotification.formatDecimal(percentageProgress)), null);
    }

}
