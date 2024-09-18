package io.ona.kujaku.library;

import static junit.framework.Assert.assertEquals;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;

import androidx.test.core.app.ApplicationProvider;

import com.mapbox.mapboxsdk.Mapbox;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.Shadows;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;

import java.util.ArrayList;

import io.ona.kujaku.TestApplication;
import io.ona.kujaku.activities.MapActivity;
import io.ona.kujaku.helpers.ActivityLauncherHelper;
import io.ona.kujaku.test.shadows.ShadowConnectivityReceiver;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE
        , application = TestApplication.class,
        shadows = {ShadowConnectivityReceiver.class})
public class KujakuLibraryTest {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    private Activity activity;
    private ActivityController<Activity> activityController;

    @Before
    public void setupBeforeTest() {
        Mapbox.getInstance(ApplicationProvider.getApplicationContext(), "some-access-token");
        activityController = Robolectric.buildActivity(Activity.class);
        activity = activityController.create().get();
    }

    @After
    public void tearDown() {
        activityController.close();
    }

    @Test
    public void testMethodLaunchMapActivityShouldSuccessfullyLaunchMapActivity() {
        ActivityLauncherHelper.launchMapActivity(activity, Mapbox.getAccessToken(), new ArrayList<>(), true);

        Robolectric.flushForegroundThreadScheduler(); // flush foreground job to allow AsyncTask's onPostExecute to run

        Intent expectedIntent = new Intent(activity, MapActivity.class);

        Intent actualIntent = Shadows.shadowOf((Application) ApplicationProvider.getApplicationContext()).getNextStartedActivity();
        assertEquals(expectedIntent.getComponent(), actualIntent.getComponent());
    }
}
