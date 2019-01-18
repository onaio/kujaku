package io.ona.kujaku.notifications;

import android.app.NotificationManager;
import android.support.v4.app.NotificationCompat;

import org.junit.Test;
import org.robolectric.annotation.Config;

import java.util.UUID;

import static org.junit.Assert.assertEquals;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 17/01/2018.
 */
public class KujakuNotificationTest extends BaseNotificationTest {

    @Config(sdk = 26)
    @Test
    public void createNotificationShouldCreateValidNotificationBuilderWithTextAndChannelIdWhenGivenContent() throws NoSuchFieldException, IllegalAccessException {
        KujakuNotificationImplClass2 kujakuNotification = new KujakuNotificationImplClass2();
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
        KujakuNotificationImplClass2 kujakuNotification = new KujakuNotificationImplClass2();
        String title = "sample tiTle 4";
        String content = "This is some sample content for the notification";

        NotificationCompat.Builder builder = kujakuNotification.createNotification(title, content);

        assertNotificationBuilder(builder, title, content, null);
        assertEquals(null, getValueInPrivateField(NotificationCompat.Builder.class, builder, "mChannelId"));
    }

    @Config(sdk = 26)
    @Test
    public void createNotificationShouldCreateValidNotificationBuilderWithChannelIdOnly() throws NoSuchFieldException, IllegalAccessException {
        KujakuNotificationImplClass2 kujakuNotification = new KujakuNotificationImplClass2();
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
        KujakuNotificationImplClass2 kujakuNotification = new KujakuNotificationImplClass2();

        String title = "sample tiTle 4";
        NotificationCompat.Builder builder = kujakuNotification.createNotification(title);

        assertNotificationBuilder(builder, title, null, null);
        assertEquals(null, getValueInPrivateField(NotificationCompat.Builder.class, builder, "mChannelId"));
    }
}
