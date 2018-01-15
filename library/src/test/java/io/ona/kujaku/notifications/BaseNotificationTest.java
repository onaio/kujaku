package io.ona.kujaku.notifications;

import android.content.Context;

import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import io.ona.kujaku.BuildConfig;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 16/01/2018.
 */
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class,manifest=Config.NONE)
public class BaseNotificationTest {

    protected Context context;

    @Before
    public void setup() {
        context = RuntimeEnvironment.application;
    }

}
