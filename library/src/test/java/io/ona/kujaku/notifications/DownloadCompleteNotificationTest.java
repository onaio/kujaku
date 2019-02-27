package io.ona.kujaku.notifications;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.support.annotation.RequiresApi;

import org.junit.Test;
import org.robolectric.Shadows;
import org.robolectric.shadows.ShadowNotification;
import org.robolectric.shadows.ShadowNotificationManager;

import io.ona.kujaku.R;

import static org.junit.Assert.*;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 16/01/2018.
 */

public class DownloadCompleteNotificationTest extends BaseNotificationTest {

    @Test
    public void displayNotificationShouldShowValidNotificationWhenGivenTitleAndContent() {
        DownloadCompleteNotification downloadCompleteNotification = new DownloadCompleteNotification(context);

        String title = "This is a sample title";
        String content = "content text";
        int notificationId = 209234;

        downloadCompleteNotification.displayNotification(title, content, notificationId);

        ShadowNotificationManager shadowNotificationManager = Shadows.shadowOf((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE));
        Notification actualNotification = shadowNotificationManager.getNotification(notificationId);
        ShadowNotification shadowNotification = Shadows.shadowOf(actualNotification);

        assertNotNull(actualNotification);
        assertEquals(title, shadowNotification.getContentTitle());
        assertEquals(content, shadowNotification.getContentText());
    }

    @RequiresApi(26)
    @Test
    public void constructorShouldInitialiseVariables() throws NoSuchFieldException, IllegalAccessException {
        DownloadCompleteNotification downloadCompleteNotification = new DownloadCompleteNotification(context);

        assertEquals(context, downloadCompleteNotification.context);
        assertNotNull(getValueInPrivateField(DownloadProgressNotification.class.getSuperclass(), downloadCompleteNotification, "smallIcon"));

        channelIdsAdded.add(downloadCompleteNotification.CHANNEL_ID);
    }

    @RequiresApi(26)
    @Test
    public void constructorShouldCreateValidNotificationAndCreateNotificationChannel() {
        DownloadCompleteNotification downloadCompleteNotification = new DownloadCompleteNotification(context);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel notificationChannel = notificationManager.getNotificationChannel(DownloadCompleteNotification.CHANNEL_ID);

        channelIdsAdded.add(downloadCompleteNotification.CHANNEL_ID);

        assertEquals(context.getString(R.string.download_complete_channel_name), notificationChannel.getName());
        assertEquals(context.getString(R.string.download_complete_channel_description), notificationChannel.getDescription());
        assertTrue(notificationChannel.shouldVibrate());
        assertTrue(notificationChannel.shouldShowLights());
        assertEquals(NotificationManager.IMPORTANCE_DEFAULT, notificationChannel.getImportance());
        assertEquals(DownloadCompleteNotification.NOTIFICATION_COLOR, notificationChannel.getLightColor());
    }

}
