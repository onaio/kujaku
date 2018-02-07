package io.ona.kujaku.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.support.annotation.RequiresApi;

import org.junit.Ignore;
import org.junit.Test;

import io.ona.kujaku.R;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 16/01/2018.
 */
@Ignore
public class DownloadCompleteNotificationInstrumentedUnitTest extends BaseNotificationInstrumentedTest {

    @RequiresApi(26)
    @Test
    public void constructorShouldInitialiseVariables() throws NoSuchFieldException, IllegalAccessException {
        DownloadCompleteNotification downloadCompleteNotification = new DownloadCompleteNotification(context);

        assertEquals(context, downloadCompleteNotification.context);
        assertTrue(getValueInPrivateField(DownloadProgressNotification.class.getSuperclass(), downloadCompleteNotification, "smallIcon") != null);

        channelIdsAdded.add(downloadCompleteNotification.CHANNEL_ID);
    }

    @RequiresApi(26)
    @Test
    public void constructorShouldCreateValidNotification() {
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
