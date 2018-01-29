package io.ona.kujaku.notifications;

import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Color;
import android.support.v4.app.NotificationCompat;

import io.ona.kujaku.R;

/**
 * Creates a valid {@link android.app.NotificationChannel} for download complete notifications. It
 * also displays download complete notifications.
 * <p>
 * Created by Ephraim Kigamba - ekigamba@ona.io on 11/01/2018.
 */

public class DownloadCompleteNotification extends KujakuNotification {
    public static final String CHANNEL_ID = "KUJAKU_DOWNLOAD_COMPLETE_CHANNEL";
    public static final int NOTIFICATION_COLOR = Color.BLUE;
    public static final long[] VIBRATION_PATTERN = new long[]{200, 600, 300, 600};

    public DownloadCompleteNotification(Context context) {
        setContext(context);
        setSmallIcon(R.drawable.ic_stat_file_download);

        createNotificationChannel(NotificationManager.IMPORTANCE_DEFAULT, context.getString(R.string.download_complete_channel_name), CHANNEL_ID, context.getString(R.string.download_complete_channel_description), NOTIFICATION_COLOR, VIBRATION_PATTERN);
    }

    /**
     * Displays a download complete notification given the title, content and notificationId
     *
     * @param title
     * @param content
     * @param notificationId
     */
    public void displayNotification(String title, String content, int notificationId) {
        NotificationCompat.Builder notificationBuilder = createNotification(title, content);
        displayNotification(notificationBuilder, notificationId);
    }
}
