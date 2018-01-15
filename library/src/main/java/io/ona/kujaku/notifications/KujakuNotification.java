package io.ona.kujaku.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;

import io.ona.kujaku.KujakuApplication;
import io.ona.kujaku.R;
import io.ona.kujaku.services.MapboxOfflineDownloaderService;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 11/01/2018.
 */

abstract class KujakuNotification {

    protected NotificationChannel notificationChannel;
    public static final int NO_LED_COLOR = 0;
    private int smallIcon;
    protected Context context;

    public void createNotificationChannel(int importance, String channelName, String channelId) {
        createNotificationChannel(importance, channelName, channelId, null);
    }

    public void createNotificationChannel(int importance, String channelName, String channelId, @Nullable String description) {
        createNotificationChannel(importance, channelName, channelId, description, NO_LED_COLOR, null);
    }

    /**
     * Creates a notification channel for the app using the given importance, channel name, channel id, description, LED color & vibration pattern.
     * <br/>
     * <br/>
     * If you don't want an LED color, pass {@link KujakuNotification#NO_LED_COLOR} to ledColor parameter
     * <br/>
     * If you don't want vibration, pass {@code null} to vibrationPattern parameter
     * <p>
     * <br/>
     * <strong>This only works on API 26</strong>
     *
     * @param importance
     * @param channelName
     * @param channelId
     * @param description
     * @param ledColor
     * @param vibrationPattern
     */
    public void createNotificationChannel(int importance, String channelName, String channelId, @Nullable String description, @ColorInt int ledColor, @Nullable long[] vibrationPattern) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationChannel = new NotificationChannel(channelId, channelName, importance);
            if (description != null) {
                notificationChannel.setDescription(description);
            }

            if (ledColor != NO_LED_COLOR) {
                notificationChannel.enableLights(true);
                notificationChannel.setLightColor(ledColor);
            }

            if (vibrationPattern != null) {
                notificationChannel.enableVibration(true);
                notificationChannel.setVibrationPattern(vibrationPattern);
            }

            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void createNotificationChannel(int importance, String channelName, String channelId, @Nullable String description, @ColorInt int ledColor) {
        createNotificationChannel(importance, channelName, channelId, description, ledColor, null);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void createNotificationChannel(int importance, String channelName, String channelId, @Nullable String description, long[] vibrationPattern) {
        createNotificationChannel(importance, channelName, channelId, description, NO_LED_COLOR, vibrationPattern);
    }

    NotificationChannel getNotificationChannel() {
        return notificationChannel;
    }

    public NotificationCompat.Builder createNotification(String title, @Nullable String content) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, null)
                .setContentTitle(title)
                .setSmallIcon(smallIcon);

        if (content != null) {
            builder.setContentText(content);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(notificationChannel.getId());
        }

        return builder;
    }

    public NotificationCompat.Builder createNotification(String title) {
        return createNotification(title, null);
    }


    public void displayNotification(NotificationCompat.Builder notificationBuilder, int notificationId) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(notificationId, notificationBuilder.build());
    }

    public void setSmallIcon(int icon) {
        this.smallIcon = icon;
    }

    public void setContext(Context context) {
        this.context = context;
    }
}
