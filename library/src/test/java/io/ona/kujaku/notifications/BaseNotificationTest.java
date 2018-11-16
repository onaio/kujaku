package io.ona.kujaku.notifications;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowPendingIntent;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import io.ona.kujaku.BaseTest;
import io.ona.kujaku.BuildConfig;
import io.ona.kujaku.R;
import io.ona.kujaku.test.shadows.ShadowNotificationChannel;

import static org.junit.Assert.assertEquals;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 16/01/2018.
 */
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, manifest = Config.NONE, shadows = {ShadowNotificationChannel.class})
public abstract class BaseNotificationTest extends BaseTest {

    protected Context context;

    @Before
    public void setup() {
        context = RuntimeEnvironment.application;
    }

    protected void assertNotificationBuilder(NotificationCompat.Builder builder, String title, String content, @Nullable NotificationCompat.Action action) throws NoSuchFieldException, IllegalAccessException {
        String actualTitle = (String) getValueInPrivateField(NotificationCompat.Builder.class, builder, "mContentTitle");
        String actualContent = (String) getValueInPrivateField(NotificationCompat.Builder.class, builder, "mContentText");

        assertEquals(title, actualTitle);
        assertEquals(content, actualContent);

        if (action != null) {
            NotificationCompat.Action actualAction = builder.mActions.get(0);
            assertEquals(action.getIcon(), actualAction.getIcon());
            assertEquals(action.getTitle(), actualAction.getTitle());
        }
    }

    protected void compareIntent(Intent expected, Intent actual) {
        Bundle expectedBundle = expected.getExtras();
        Bundle actualBundle = actual.getExtras();

        for (String key : expectedBundle.keySet()) {
            Object expectedValue = expectedBundle.get(key);
            Object actualValue = actualBundle.get(key);

            assertEquals(expectedValue, actualValue);
        }
    }

    protected Intent getIntent(PendingIntent pendingIntent) throws IllegalStateException {
        ShadowPendingIntent shadowPendingIntent = Shadows.shadowOf(pendingIntent);

        return shadowPendingIntent.getSavedIntent();
    }

    protected void assertNotificationBuilder(NotificationCompat.Builder builder, String mapName) throws NoSuchFieldException, IllegalAccessException {
        assertNotificationBuilder(builder, getNotificationProgressTitle(mapName), null, null);
    }

    protected void assertNotificationBuilder(NotificationCompat.Builder builder, String mapName, double percentageProgress) throws NoSuchFieldException, IllegalAccessException {
        assertNotificationBuilder(builder, getNotificationProgressTitle(mapName), getNotificationProgressContent(percentageProgress), null);
    }

    protected String getNotificationProgressTitle(String mapName) {
        return String.format(context.getString(R.string.notification_download_progress_title), mapName);
    }

    protected String getNotificationProgressContent(double percentageProgress) {
        return String.format(context.getString(R.string.notification_download_progress_content), DownloadProgressNotification.formatDecimal(percentageProgress));
    }

    protected void setSDKToAndroidOreo() throws NoSuchFieldException, IllegalAccessException {
        setFinalStatic(Build.VERSION.class.getField("SDK_INT"), Build.VERSION_CODES.O);
    }

}
