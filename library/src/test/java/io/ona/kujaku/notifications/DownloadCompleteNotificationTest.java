package io.ona.kujaku.notifications;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;

import org.junit.Test;
import org.robolectric.Shadows;
import org.robolectric.shadows.ShadowNotification;
import org.robolectric.shadows.ShadowNotificationManager;

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

        assertTrue(actualNotification != null);
        assertEquals(title, shadowNotification.getContentTitle());
        assertEquals(content, shadowNotification.getContentText());
    }

}
