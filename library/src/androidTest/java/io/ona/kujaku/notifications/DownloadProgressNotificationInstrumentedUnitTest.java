package io.ona.kujaku.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.support.annotation.RequiresApi;

import org.junit.Test;

import io.ona.kujaku.R;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 15/01/2018.
 */
public class DownloadProgressNotificationInstrumentedUnitTest extends BaseNotificationInstrumentedTest {

    @Test
    public void constructorShouldInitialiseVariables() throws NoSuchFieldException, IllegalAccessException {
        DownloadProgressNotification downloadProgressNotification = new DownloadProgressNotification(context);

        assertEquals(context, downloadProgressNotification.context);
        assertTrue(getValueInPrivateField(DownloadProgressNotification.class.getSuperclass(), downloadProgressNotification, "smallIcon") != null);

        channelIdsAdded.add(downloadProgressNotification.CHANNEL_ID);
    }

    @RequiresApi(26)
    @Test
    public void constructorShouldCreateValidNotificationChannel() {
        DownloadProgressNotification downloadProgressNotification = new DownloadProgressNotification(context);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel notificationChannel = notificationManager.getNotificationChannel(DownloadProgressNotification.CHANNEL_ID);

        channelIdsAdded.add(downloadProgressNotification.CHANNEL_ID);

        assertEquals(context.getString(R.string.download_progress_channel_name), notificationChannel.getName());
        assertEquals(context.getString(R.string.download_progress_channel_description), notificationChannel.getDescription());
        assertFalse(notificationChannel.shouldVibrate());
        assertFalse(notificationChannel.shouldShowLights());
        assertEquals(NotificationManager.IMPORTANCE_LOW, notificationChannel.getImportance());
    }

}
