package io.ona.kujaku.notifications;

import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Color;
import androidx.core.app.NotificationCompat;

import io.ona.kujaku.R;

/**
 * Creates the appropriate channel for download error notifications.
 * This notification displays critical download errors. Such errors include 404s and Mapbox tile
 * count limit exceeded errors
 * <p>
 * Created by Ephraim Kigamba - ekigamba@ona.io on 04/12/2018
 */

public class CriticalDownloadErrorNotification extends KujakuNotification {

    public static final String CHANNEL_ID = "KUJAKU_DOWNLOAD_ERROR_CHANNEL";
    public static final int NOTIFICATION_COLOR = Color.RED;
    public static final long[] VIBRATION_PATTERN = new long[]{200, 600, 300, 600};


    public CriticalDownloadErrorNotification(Context context) {
        setContext(context);
        setSmallIcon(R.drawable.ic_stat_file_download);

        createNotificationChannel(NotificationManager.IMPORTANCE_HIGH, context.getString(R.string.download_error_channel_name)
                , CHANNEL_ID, context.getString(R.string.download_error_channel_description), NOTIFICATION_COLOR, VIBRATION_PATTERN);
    }

    /**
     * Displays a download error notification given the title, content and notificationId
     *
     * @param title
     * @param content
     * @param notificationId
     */
    public void displayNotification(String title, String content, int notificationId) {
        NotificationCompat.Builder notificationBuilder = createNotification(title, content);
        notificationBuilder.setSmallIcon(android.R.drawable.ic_dialog_info);
        displayNotification(notificationBuilder, notificationId);
    }
}
