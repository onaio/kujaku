package io.ona.kujaku.library;

import android.app.Activity;
import android.content.Intent;

import com.mapbox.mapboxsdk.Mapbox;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.ArrayList;

import io.ona.kujaku.BuildConfig;
import io.ona.kujaku.TestApplication;
import io.ona.kujaku.activities.MapActivity;
import io.ona.kujaku.helpers.ActivityLauncherHelper;

import static junit.framework.Assert.assertEquals;
import static org.robolectric.Shadows.shadowOf;

/**
 * @author Vincent Karuri
 */
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class
        , manifest = Config.NONE
        , application = TestApplication.class)
public class KujakuLibraryTest {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    private Activity activity;

    @Before
    public void setupBeforeTest() {
        activity = Robolectric.buildActivity(Activity.class).create().get();
    }

    @Test
    public void testMethodLaunchMapActivityShouldSuccessfullyLaunchMapActivity() throws InterruptedException {
            ActivityLauncherHelper.launchMapActivity(activity, Mapbox.getAccessToken(), new ArrayList<>(), true);
            Thread.sleep(5000l);

            Robolectric.getForegroundThreadScheduler().runOneTask(); // flush foreground job to allow AsyncTask's onPostExecute to run

            Intent expectedIntent = new Intent(activity, MapActivity.class);
            Intent actualIntent = shadowOf(RuntimeEnvironment.application).getNextStartedActivity();
            assertEquals(expectedIntent.getComponent(), actualIntent.getComponent());
    }
}
