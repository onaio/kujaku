package io.ona.kujaku.library;

import android.content.Intent;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.ArrayList;

import io.ona.kujaku.BaseKujakuApplication;
import io.ona.kujaku.BuildConfig;
import io.ona.kujaku.KujakuLibrary;
import io.ona.kujaku.TestApplication;
import io.ona.kujaku.activities.MapActivity;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
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

    @Mock
    private BaseKujakuApplication application;

    private KujakuLibrary library;

    @Before
    public void setupBeforeTest() {
        library = KujakuLibrary.getInstance();
        library.setHostApplication(application);
    }

    @Test
    public void testProcessFeatureJSONIsCalledOnKujakuApplication() {
        library.sendFeatureJSONToHostApp(new JSONObject());
        verify(application, times(1)).processFeatureJSON(anyObject());
    }

    @Test
    public void testMethodLaunchMapActivityShouldSuccessfullyLaunchMapActivity() throws InterruptedException {
            library.setHostApplication((BaseKujakuApplication) RuntimeEnvironment.application);
            library.launchMapActivity(new ArrayList<>());
            Thread.sleep(5000l);

            Robolectric.getForegroundThreadScheduler().runOneTask(); // flush foreground job to allow AsyncTask's onPostExecute to run

            Intent expectedIntent = new Intent(library.getHostApplication(), MapActivity.class);
            Intent actualIntent = shadowOf(RuntimeEnvironment.application).getNextStartedActivity();
            assertEquals(expectedIntent.getComponent(), actualIntent.getComponent());
    }
}
