package io.ona.kujaku.notifications;

import android.app.ActivityManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.support.v4.app.NotificationCompat;

import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import utils.Constants;

import static org.junit.Assert.*;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 16/01/2018.
 */
@RunWith(AndroidJUnit4.class)
public class BaseNotificationsTest {

    protected Context context;
    protected ArrayList<String> channelIdsAdded = new ArrayList<>();

    @Before
    public void setup() throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
        context = InstrumentationRegistry.getTargetContext();
    }

    @After
    public void tearDown() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            for (String channelId : channelIdsAdded) {
                notificationManager.deleteNotificationChannel(channelId);
            }

            channelIdsAdded.clear();
        }
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

            Intent actualIntent = getIntent(actualAction.getActionIntent());

        }
    }

    protected void compareIntent(Intent expected, Intent actual) {
        Bundle expectedBundle = expected.getExtras();
        Bundle actualBundle = actual.getExtras();

        for(String key: expectedBundle.keySet()) {
            Object expectedValue = expectedBundle.get(key);
            Object actualValue = actualBundle.get(key);

            assertEquals(expectedValue, actualValue);
        }
    }

    protected void insertValueInPrivateField(Class classWithField, Object instance, String fieldName, Object newValue) throws IllegalAccessException, NoSuchFieldException {
        Field instanceField = classWithField.getDeclaredField(fieldName);
        if (!instanceField.isAccessible()) {
            instanceField.setAccessible(true);
        }

        instanceField.set(instance, newValue);
    }

    protected void insertValueInPrivateStaticField(Class classWithField, String fieldName, Object newValue) throws NoSuchFieldException, IllegalAccessException {
        insertValueInPrivateField(classWithField, null, fieldName, newValue);
    }

    protected Object getValueInPrivateField(Class classWithField, Object instance, String fieldName) throws IllegalAccessException, NoSuchFieldException {
        Field instanceField = classWithField.getDeclaredField(fieldName);
        if (!instanceField.isAccessible()) {
            instanceField.setAccessible(true);
        }

        return instanceField.get(instance);
    }

    /**
     * Return the Intent for PendingIntent.
     * Return null in case of some (impossible) errors: see Android source.
     * @throws IllegalStateException in case of something goes wrong.
     * See {@link Throwable#getCause()} for more details.
     */
    public Intent getIntent(PendingIntent pendingIntent) throws IllegalStateException {
        try {
            Method getIntent = PendingIntent.class.getDeclaredMethod("getIntent");
            getIntent.setAccessible(true);
            return (Intent) getIntent.invoke(pendingIntent);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(e);
        } catch (InvocationTargetException e) {
            throw new IllegalStateException(e);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

}
