package io.ona.kujaku.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 15/01/2018.
 */
@Ignore
@RunWith(AndroidJUnit4.class)
public class KujakuNotificationInstrumentedUnitTest extends BaseNotificationInstrumentedTest {

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

        assertTrue(notificationChannel != null);
        assertEquals(channelName, notificationChannel.getName());
        assertEquals(channelDescription, notificationChannel.getDescription());
        assertEquals(true, notificationChannel.shouldVibrate());
        assertEquals(true, notificationChannel.shouldShowLights());
        assertTrue(Arrays.equals(vibrationPattern, notificationChannel.getVibrationPattern()));
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

        assertTrue(notificationChannel != null);
        assertEquals(channelName, notificationChannel.getName());
        assertEquals(channelDescription, notificationChannel.getDescription());
        assertEquals(true, notificationChannel.shouldVibrate());
        assertEquals(false, notificationChannel.shouldShowLights());
        assertTrue(Arrays.equals(vibrationPattern, notificationChannel.getVibrationPattern()));
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

        assertTrue(notificationChannel != null);
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

        assertTrue(notificationChannel != null);
        assertEquals(channelName, notificationChannel.getName());
        assertEquals(channelDescription, notificationChannel.getDescription());
        assertEquals(false, notificationChannel.shouldVibrate());
        assertEquals(false, notificationChannel.shouldShowLights());
    }
}
