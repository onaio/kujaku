package io.ona.kujaku.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import org.junit.Test;
import org.robolectric.annotation.Config;

import java.util.UUID;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 17/01/2018.
 */
public class KujakuNotificationTest extends BaseNotificationTest {

    @Config(sdk = 26)
    @Test
    public void createNotificationShouldCreateValidNotificationBuilderWithTextAndChannelIdWhenGivenContent() throws NoSuchFieldException, IllegalAccessException {
        KujakuNotificationImplClass kujakuNotification = new KujakuNotificationImplClass();
        String title = "sample tiTle 4";
        String channelId = UUID.randomUUID().toString();
        String content = "This is some sample content";

        // Create the notification channel
        setSDKToAndroidOreo();
        kujakuNotification.setContext(context);
        kujakuNotification.createNotificationChannel(NotificationManager.IMPORTANCE_DEFAULT, "sample channel name", channelId);

        NotificationCompat.Builder builder = kujakuNotification.createNotification(title, content);

        assertNotificationBuilder(builder, title, content, null);
        assertEquals(channelId, getValueInPrivateField(NotificationCompat.Builder.class, builder, "mChannelId"));
    }

    @Config(sdk = 25)
    @Test
    public void createNotificationShouldCreateValidNotifcationBuilderWithTextOnly() throws NoSuchFieldException, IllegalAccessException {
        KujakuNotificationImplClass kujakuNotification = new KujakuNotificationImplClass();
        String title = "sample tiTle 4";
        String content = "This is some sample content for the notification";

        NotificationCompat.Builder builder = kujakuNotification.createNotification(title, content);

        assertNotificationBuilder(builder, title, content, null);
        assertEquals(null, getValueInPrivateField(NotificationCompat.Builder.class, builder, "mChannelId"));
    }

    @Config(sdk = 26)
    @Test
    public void createNotificationShouldCreateValidNotificationBuilderWithChannelIdOnly() throws NoSuchFieldException, IllegalAccessException {
        KujakuNotificationImplClass kujakuNotification = new KujakuNotificationImplClass();
        String title = "sample tiTle 4";
        String channelId = UUID.randomUUID().toString();

        // Create the notification channel
        setSDKToAndroidOreo();
        kujakuNotification.setContext(context);
        kujakuNotification.createNotificationChannel(NotificationManager.IMPORTANCE_DEFAULT, "sample channel name", channelId);

        NotificationCompat.Builder builder = kujakuNotification.createNotification(title);

        assertNotificationBuilder(builder, title, null, null);
        assertEquals(channelId, getValueInPrivateField(NotificationCompat.Builder.class, builder, "mChannelId"));
    }

    @Config(sdk = 25)
    @Test
    public void createNotificationShouldCreateValidNotificationBuilderWithoutTextOrChannelId() throws NoSuchFieldException, IllegalAccessException {
        KujakuNotificationImplClass kujakuNotification = new KujakuNotificationImplClass();

        String title = "sample tiTle 4";
        NotificationCompat.Builder builder = kujakuNotification.createNotification(title);

        assertNotificationBuilder(builder, title, null, null);
        assertEquals(null, getValueInPrivateField(NotificationCompat.Builder.class, builder, "mChannelId"));
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @Test
    public void createNotificationChannelShouldCreateValidNotificationChannel() {
        KujakuNotificationImplClass kujakuNotificationImplClass = new KujakuNotificationImplClass();
        kujakuNotificationImplClass.setContext(context);
        String channelName = "my channel";
        String channelDescription = "This is a test channel";
        String channelId = UUID.randomUUID().toString();
        int ledColor = Color.RED;
        long[] vibrationPattern = new long[]{200, 700, 200, 700};
        kujakuNotificationImplClass.createNotificationChannel(NotificationManager.IMPORTANCE_HIGH, channelName, channelId, channelDescription, ledColor, vibrationPattern);

        channelIdsAdded.add(channelId);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel notificationChannel = notificationManager.getNotificationChannel(channelId);

        assertNotNull(notificationChannel);
        assertEquals(channelName, notificationChannel.getName());
        assertEquals(channelDescription, notificationChannel.getDescription());
        assertEquals(true, notificationChannel.shouldVibrate());
        assertEquals(true, notificationChannel.shouldShowLights());
        assertArrayEquals(vibrationPattern, notificationChannel.getVibrationPattern());
        assertEquals(ledColor, notificationChannel.getLightColor());
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @Test
    public void createNotificationChannelShouldCreateValidNotificationChannelWithoutLights() {
        KujakuNotificationImplClass kujakuNotificationImplClass = new KujakuNotificationImplClass();
        kujakuNotificationImplClass.setContext(context);
        String channelName = "my channel";
        String channelDescription = "This is a test channel";
        String channelId = UUID.randomUUID().toString();
        long[] vibrationPattern = new long[]{200, 700, 200, 700};
        kujakuNotificationImplClass.createNotificationChannel(NotificationManager.IMPORTANCE_HIGH, channelName, channelId, channelDescription, vibrationPattern);

        channelIdsAdded.add(channelId);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel notificationChannel = notificationManager.getNotificationChannel(channelId);

        assertNotNull(notificationChannel);
        assertEquals(channelName, notificationChannel.getName());
        assertEquals(channelDescription, notificationChannel.getDescription());
        assertEquals(true, notificationChannel.shouldVibrate());
        assertEquals(false, notificationChannel.shouldShowLights());
        assertArrayEquals(vibrationPattern, notificationChannel.getVibrationPattern());
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @Test
    public void createNotificationChannelShouldCreateValidNotificationChannelWithoutVibration() {
        KujakuNotificationImplClass kujakuNotificationImplClass = new KujakuNotificationImplClass();
        kujakuNotificationImplClass.setContext(context);
        String channelName = "my channel";
        String channelDescription = "This is a test channel";
        String channelId = UUID.randomUUID().toString();
        int ledColor = Color.RED;
        kujakuNotificationImplClass.createNotificationChannel(NotificationManager.IMPORTANCE_HIGH, channelName, channelId, channelDescription, ledColor);

        channelIdsAdded.add(channelId);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel notificationChannel = notificationManager.getNotificationChannel(channelId);

        assertNotNull(notificationChannel);
        assertEquals(channelName, notificationChannel.getName());
        assertEquals(channelDescription, notificationChannel.getDescription());
        assertEquals(false, notificationChannel.shouldVibrate());
        assertEquals(true, notificationChannel.shouldShowLights());
        assertEquals(ledColor, notificationChannel.getLightColor());
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @Test
    public void createNotificationChannelShouldCreateValidNotificationChannelWithoutLightsOrVibration() {
        KujakuNotificationImplClass kujakuNotificationImplClass = new KujakuNotificationImplClass();
        kujakuNotificationImplClass.setContext(context);
        String channelName = "my channel";
        String channelDescription = "This is a test channel";
        String channelId = UUID.randomUUID().toString();
        kujakuNotificationImplClass.createNotificationChannel(NotificationManager.IMPORTANCE_HIGH, channelName, channelId, channelDescription);

        channelIdsAdded.add(channelId);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel notificationChannel = notificationManager.getNotificationChannel(channelId);

        assertNotNull(notificationChannel);
        assertEquals(channelName, notificationChannel.getName());
        assertEquals(channelDescription, notificationChannel.getDescription());
        assertEquals(false, notificationChannel.shouldVibrate());
        assertEquals(false, notificationChannel.shouldShowLights());
    }
}
