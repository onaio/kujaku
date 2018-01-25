package io.ona.kujaku.test.shadows;

import android.app.NotificationChannel;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 17/01/2018.
 */
@Implements(NotificationChannel.class)
public class ShadowNotificationChannel {

    @Implementation
    public void setDescription(String description) {
        // Do nothing
    }

    @Implementation
    public void enableLights(boolean lights) {
        // Do nothing
    }

    @Implementation
    public void setLightColor(int argb) {
        // Do nothing
    }

    @Implementation
    public void enableVibration(boolean vibration) {
        // Do nothing
    }

    @Implementation
    public void setVibrationPattern(long[] vibrationPattern) {
        // Do nothing
    }

}
