package io.ona.kujaku.notifications;

import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.runner.RunWith;

import java.lang.reflect.Field;
import java.util.ArrayList;

/**
 * Tests extended by this class should run on API 26 devices i.e. Android Oreo
 *
 * Created by Ephraim Kigamba - ekigamba@ona.io on 16/01/2018.
 */
@RunWith(AndroidJUnit4.class)
public abstract class BaseNotificationInstrumentedTest {

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

}
