package io.ona.kujaku.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.offline.OfflineManager;
import com.mapbox.mapboxsdk.offline.OfflineRegion;
import com.mapbox.mapboxsdk.offline.OfflineRegionDefinition;
import com.mapbox.mapboxsdk.offline.OfflineRegionStatus;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLooper;
import org.robolectric.shadows.ShadowNotification;
import org.robolectric.shadows.ShadowNotificationManager;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import io.ona.kujaku.BuildConfig;
import io.ona.kujaku.data.MapBoxDeleteTask;
import io.ona.kujaku.data.MapBoxDownloadTask;
import io.ona.kujaku.data.realm.RealmDatabase;
import io.ona.kujaku.data.realm.objects.MapBoxOfflineQueueTask;
import io.ona.kujaku.downloaders.MapBoxOfflineResourcesDownloader;
import io.ona.kujaku.listeners.OfflineRegionStatusCallback;
import io.ona.kujaku.test.shadows.ShadowConnectivityReceiver;
import io.ona.kujaku.test.shadows.ShadowLibraryLoader;
import io.ona.kujaku.test.shadows.ShadowMapBoxDeleteTask;
import io.ona.kujaku.test.shadows.ShadowMapBoxDownloadTask;
import io.ona.kujaku.test.shadows.ShadowOfflineManager;
import io.ona.kujaku.test.shadows.ShadowRealm;
import io.ona.kujaku.test.shadows.ShadowRealmDatabase;
import io.ona.kujaku.test.shadows.implementations.RealmDbTestImplementation;
import io.ona.kujaku.utils.Constants;
import io.ona.kujaku.utils.NumberFormatter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

/**
 *
 *
 * Created by Ephraim Kigamba - ekigamba@ona.io on 05/12/2017.
 */
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class,
        manifest = Config.NONE,
        shadows = {
                ShadowMapBoxDeleteTask.class,
                ShadowMapBoxDownloadTask.class,
                ShadowConnectivityReceiver.class,
                ShadowRealm.class,
                ShadowOfflineManager.class,
                ShadowRealmDatabase.class,
                ShadowLibraryLoader.class
})
public class MapboxOfflineDownloaderServiceTest {

    private String mapName = UUID.randomUUID().toString();
    private static final String TAG = MapboxOfflineDownloaderServiceTest.class.getSimpleName();

    private Context context;
    private MapboxOfflineDownloaderService mapboxOfflineDownloaderService;

    private String sampleValidMapboxStyleURL = "mapbox://styles/ona/90kiosdcIJ3d";
    private String mapboxAccessToken = BuildConfig.MAPBOX_SDK_ACCESS_TOKEN;
    private float minZoom = 22;
    private float maxZoom = 10;
    private LatLng topLeftBound = new LatLng(9.1, 9.1);
    private LatLng topRightBound = new LatLng(9.1, 20.5);
    private LatLng bottomRightBound = new LatLng(1.1, 20.5);
    private LatLng bottomLeftBound = new LatLng(9.1, 1.1);

    private CountDownLatch latch;
    private ArrayList<Object> resultsToCheck = new ArrayList<>();

    @Before
    public void setUp() throws Exception {
        context = RuntimeEnvironment.application;
        RealmDbTestImplementation.resetDb();
        mapboxOfflineDownloaderService = Robolectric.buildService(MapboxOfflineDownloaderService.class)
                .get();

        resultsToCheck.clear();
    }

    @After
    public void tearDown() {
        mapboxOfflineDownloaderService = null;
    }

    @Test
    public void persistOfflineMapTaskShouldReturnFalseWhenGivenNullIntent() {
        Intent sampleExtra = null;
        assertEquals(false, mapboxOfflineDownloaderService.persistOfflineMapTask(sampleExtra));
    }

    @Test
    public void persistOfflineMapTaskShouldReturnFalseWhenGivenNullIntentExtras() {
        Intent sampleExtra = new Intent();
        assertEquals(false, mapboxOfflineDownloaderService.persistOfflineMapTask(sampleExtra));

        sampleExtra.putExtra("SOME EXTRA", "");
        assertEquals(false, mapboxOfflineDownloaderService.persistOfflineMapTask(sampleExtra));
    }

    @Test
    public void persistOfflineMapTaskShouldReturnTrueWhenGivenValidDeleteTask() {
        Intent sampleServiceIntent = createMapboxOfflineDownloaderServiceIntent();
        sampleServiceIntent = createSampleDeleteIntent(sampleServiceIntent);

        assertEquals(true, mapboxOfflineDownloaderService.persistOfflineMapTask(sampleServiceIntent));
    }

    @Test
    public void persistOfflineMapTaskShouldReturnTrueWhenGivenValidDownloadTask() throws NoSuchFieldException, IllegalAccessException {
        Intent sampleServiceIntent = createMapboxOfflineDownloaderServiceIntent();
        sampleServiceIntent = createSampleDownloadIntent(sampleServiceIntent);

        insertValueInPrivateField(mapboxOfflineDownloaderService, "realmDatabase", RealmDatabase.init(context));
        assertEquals(true, mapboxOfflineDownloaderService.persistOfflineMapTask(sampleServiceIntent));
    }

    @Test
    public void persistOfflineMapTaskShouldReturnFalseWhenGivenInvalidDownloadTask() {
        Intent sampleServiceIntent = createMapboxOfflineDownloaderServiceIntent();
        sampleServiceIntent = createSampleDownloadIntent(sampleServiceIntent);

        Intent sampleInvalidIntent = (Intent) sampleServiceIntent.clone();

        sampleInvalidIntent.removeExtra(Constants.PARCELABLE_KEY_STYLE_URL);
        assertEquals(false, mapboxOfflineDownloaderService.persistOfflineMapTask(sampleInvalidIntent));

        sampleInvalidIntent = (Intent) sampleInvalidIntent.clone();
        sampleInvalidIntent.removeExtra(Constants.PARCELABLE_KEY_MIN_ZOOM);
        assertEquals(false, mapboxOfflineDownloaderService.persistOfflineMapTask(sampleInvalidIntent));

        sampleInvalidIntent = (Intent) sampleInvalidIntent.clone();
        sampleInvalidIntent.removeExtra(Constants.PARCELABLE_KEY_MAX_ZOOM);
        assertEquals(false, mapboxOfflineDownloaderService.persistOfflineMapTask(sampleInvalidIntent));

        sampleInvalidIntent = (Intent) sampleInvalidIntent.clone();
        sampleInvalidIntent.removeExtra(Constants.PARCELABLE_KEY_TOP_LEFT_BOUND);
        assertEquals(false, mapboxOfflineDownloaderService.persistOfflineMapTask(sampleInvalidIntent));

        sampleInvalidIntent = (Intent) sampleInvalidIntent.clone();
        sampleInvalidIntent.removeExtra(Constants.PARCELABLE_KEY_BOTTOM_RIGHT_BOUND);
        assertEquals(false, mapboxOfflineDownloaderService.persistOfflineMapTask(sampleInvalidIntent));
    }

    @Test
    public void persistOfflineMapTaskShouldSaveQueueTaskWhenGivenValidDeleteTask() throws NoSuchFieldException, IllegalAccessException {
        Intent sampleServiceIntent = createMapboxOfflineDownloaderServiceIntent();
        sampleServiceIntent = createSampleDeleteIntent(sampleServiceIntent);

        Calendar calendar = Calendar.getInstance();
        insertValueInPrivateField(mapboxOfflineDownloaderService, "realmDatabase", RealmDatabase.init(context));
        assertEquals(true, mapboxOfflineDownloaderService.persistOfflineMapTask(sampleServiceIntent));

        MapBoxOfflineQueueTask task = (MapBoxOfflineQueueTask) RealmDbTestImplementation.first();

        assertEquals(MapBoxOfflineQueueTask.TASK_TYPE_DELETE, task.getTaskType());
        assertEquals(MapBoxOfflineQueueTask.TASK_STATUS_NOT_STARTED, task.getTaskStatus());
        assertTrue((calendar.getTimeInMillis() - task.getDateCreated().getTime()) < 1000);
        assertTrue((calendar.getTimeInMillis() - task.getDateUpdated().getTime()) < 1000);

        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(MapBoxDeleteTask.MAP_NAME, mapName);
            jsonObject.put(MapBoxDeleteTask.MAP_BOX_ACCESS_TOKEN, mapboxAccessToken);

            assertEquals(jsonObject.toString(), task.getTask().toString());
        } catch (JSONException e) {
            Log.e(TAG, Log.getStackTraceString(e));
            fail();
        }

    }

    @Test
    public void persistOfflineMapTaskShouldSaveQueueTaskWhenGivenValidDownloadTask() throws NoSuchFieldException, IllegalAccessException {
        Intent sampleServiceIntent = createMapboxOfflineDownloaderServiceIntent();
        sampleServiceIntent = createSampleDownloadIntent(sampleServiceIntent);

        Calendar calendar = Calendar.getInstance();
        insertValueInPrivateField(mapboxOfflineDownloaderService, "realmDatabase", RealmDatabase.init(context));
        assertEquals(true, mapboxOfflineDownloaderService.persistOfflineMapTask(sampleServiceIntent));

        MapBoxOfflineQueueTask task = (MapBoxOfflineQueueTask) RealmDbTestImplementation.first();

        assertEquals(MapBoxOfflineQueueTask.TASK_TYPE_DOWNLOAD, task.getTaskType());
        assertEquals(MapBoxOfflineQueueTask.TASK_STATUS_NOT_STARTED, task.getTaskStatus());
        assertTrue((calendar.getTimeInMillis() - task.getDateCreated().getTime()) < 1000);
        assertTrue((calendar.getTimeInMillis() - task.getDateUpdated().getTime()) < 1000);

        try {
            JSONObject jsonObject = new JSONObject();

            jsonObject.put(MapBoxDownloadTask.PACKAGE_NAME, "kl");
            jsonObject.put(MapBoxDownloadTask.MAP_NAME, mapName);
            jsonObject.put(MapBoxDownloadTask.MAPBOX_STYLE_URL, sampleValidMapboxStyleURL);
            jsonObject.put(MapBoxDownloadTask.MAPBOX_ACCESS_TOKEN, mapboxAccessToken);
            jsonObject.put(MapBoxDownloadTask.MIN_ZOOM, minZoom);
            jsonObject.put(MapBoxDownloadTask.MAX_ZOOM, maxZoom);
            jsonObject.put(MapBoxDownloadTask.TOP_LEFT_BOUND, MapBoxDownloadTask.constructLatLngJSONObject(topLeftBound));
            jsonObject.put(MapBoxDownloadTask.TOP_RIGHT_BOUND, MapBoxDownloadTask.constructLatLngJSONObject(topRightBound));
            jsonObject.put(MapBoxDownloadTask.BOTTOM_RIGHT_BOUND, MapBoxDownloadTask.constructLatLngJSONObject(bottomRightBound));
            jsonObject.put(MapBoxDownloadTask.BOTTOM_LEFT_BOUND, MapBoxDownloadTask.constructLatLngJSONObject(bottomLeftBound));

            assertEquals(jsonObject.toString(), task.getTask().toString());
        } catch (JSONException e) {
            Log.e(TAG, Log.getStackTraceString(e));
            fail();
        }
    }

    @Test
    public void sendBroadcastShouldProduceValidIntentWhenGivenDownloadUpdate() {
        assertValidBroadcastCreatedWhenSendBroadcastIsCalled(MapboxOfflineDownloaderService.SERVICE_ACTION_RESULT.SUCCESSFUL, mapName, "9.0%", MapboxOfflineDownloaderService.SERVICE_ACTION.DOWNLOAD_MAP);
    }

    @Test
    public void sendBroadcast2ShouldProduceValidIntentWhenGivenDownloadUpdate() {
        assertValidBroadcastCreatedWhenSendBroadcast2IsCalled(MapboxOfflineDownloaderService.SERVICE_ACTION_RESULT.SUCCESSFUL, mapName, MapboxOfflineDownloaderService.SERVICE_ACTION.DELETE_MAP);
    }

    @Test
    public void mapboxTileLimitExceededShouldCreateValidBroadcast() throws InterruptedException, NoSuchFieldException, IllegalAccessException {
        latch = new CountDownLatch(1);

        registerLocalBroadcastReceiverForDownloadServiceUpdates();

        setMapNameAndDownloadAction(mapName, MapboxOfflineDownloaderService.SERVICE_ACTION.DOWNLOAD_MAP);
        mapboxOfflineDownloaderService.mapboxTileCountLimitExceeded(60000);

        latch.await();

        Intent intent = (Intent) resultsToCheck.get(0);
        assertBroadcastResults(intent, MapboxOfflineDownloaderService.SERVICE_ACTION_RESULT.FAILED, mapName, "MapBox Tile Count limit exceeded 60000 while Downloading " + mapName, MapboxOfflineDownloaderService.SERVICE_ACTION.DOWNLOAD_MAP);
    }

    @Test
    public void onErrorShouldCreateValidBroadcastWhenGivenNonEmptyReasonAndMessage() throws NoSuchFieldException, IllegalAccessException {
        latch = new CountDownLatch(1);

        String reason = "Some reason here";
        String message = "Some error message here";

        registerLocalBroadcastReceiverForDownloadServiceUpdates();
        setMapNameAndDownloadAction(mapName, MapboxOfflineDownloaderService.SERVICE_ACTION.DELETE_MAP);

        mapboxOfflineDownloaderService.onError(reason, message);

        Intent intent = (Intent) resultsToCheck.get(0);
        assertBroadcastResults(intent, MapboxOfflineDownloaderService.SERVICE_ACTION_RESULT.FAILED, mapName, "REASON : " + reason + "\nMESSAGE: " + message, MapboxOfflineDownloaderService.SERVICE_ACTION.DELETE_MAP);
    }

    @Test
    public void onErrorShouldCreateValidBroadcastWhenGivenNonEmptyReasonAndEmptyMessage() throws NoSuchFieldException, IllegalAccessException, InterruptedException {
        latch = new CountDownLatch(1);

        String reason = "Some reason here";
        String message = "";

        registerLocalBroadcastReceiverForDownloadServiceUpdates();
        setMapNameAndDownloadAction(mapName, MapboxOfflineDownloaderService.SERVICE_ACTION.DELETE_MAP);

        mapboxOfflineDownloaderService.onError(reason, message);

        latch.await();
        Intent intent = (Intent) resultsToCheck.get(0);
        assertBroadcastResults(intent, MapboxOfflineDownloaderService.SERVICE_ACTION_RESULT.FAILED, mapName, "REASON : " + reason, MapboxOfflineDownloaderService.SERVICE_ACTION.DELETE_MAP);
    }

    @Test
    public void onStatusChangedShouldShowProgressNotificationWhenGivenIncompleteOfflineRegionStatus() throws NoSuchFieldException, IllegalAccessException, InterruptedException, NoSuchMethodException, InvocationTargetException {
        latch = new CountDownLatch(1);
        OfflineRegionStatus incompleteOfflineRegionStatus = createOfflineRegion(OfflineRegion.STATE_ACTIVE, 200, 98923, 898, 230909, 300, true, false);

        setMapNameAndDownloadAction(mapName, MapboxOfflineDownloaderService.SERVICE_ACTION.DOWNLOAD_MAP);
        registerLocalBroadcastReceiverForDownloadServiceUpdates();

        insertValueInPrivateField(mapboxOfflineDownloaderService, "serviceHandler", new Handler(mapboxOfflineDownloaderService.getApplication().getMainLooper()));

        Method method = mapboxOfflineDownloaderService.getClass().getDeclaredMethod("startDownloadProgressUpdater");
        method.setAccessible(true);
        method.invoke(mapboxOfflineDownloaderService);

        mapboxOfflineDownloaderService.onStatusChanged(incompleteOfflineRegionStatus, null);
        latch.await();

        double percentage = 100.0 * 200l/300l;
        String contentTitle = "Offline Map Download Progress: " + mapName;
        String contentText = "Downloading: " + NumberFormatter.formatDecimal(percentage) + " %";

        Intent intent = (Intent) resultsToCheck.get(0);
        assertBroadcastResults(intent, MapboxOfflineDownloaderService.SERVICE_ACTION_RESULT.SUCCESSFUL, mapName, String.valueOf(percentage), MapboxOfflineDownloaderService.SERVICE_ACTION.DOWNLOAD_MAP );

        ShadowNotificationManager shadowNotificationManager = Shadows.shadowOf((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE));
        Notification notification = shadowNotificationManager.getNotification(mapboxOfflineDownloaderService.PROGRESS_NOTIFICATION_ID);

        assertEquals(null, notification);

        Thread.sleep((Long) getValueInPrivateField(mapboxOfflineDownloaderService, "timeBetweenUpdates") * 2);

        ShadowLooper.runUiThreadTasks();

        notification = shadowNotificationManager.getNotification(mapboxOfflineDownloaderService.PROGRESS_NOTIFICATION_ID);

        ShadowNotification shadowNotification = Shadows.shadowOf(notification);

        assertEquals(contentTitle, shadowNotification.getContentTitle());
        assertEquals(contentText, shadowNotification.getContentText());

        method = mapboxOfflineDownloaderService.getClass().getDeclaredMethod("stopDownloadProgressUpdater");
        method.setAccessible(true);
        method.invoke(mapboxOfflineDownloaderService);

    }

    @Test
    public void observeOfflineRegionShouldChangeOfflineRegionStatus() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, NoSuchFieldException, ClassNotFoundException {
        OfflineRegion offlineRegion = createMockOfflineRegion(null, null, OfflineRegion.STATE_INACTIVE);

        Method method = mapboxOfflineDownloaderService.getClass().getDeclaredMethod("observeOfflineRegion", OfflineRegion.class);
        method.setAccessible(true);
        method.invoke(mapboxOfflineDownloaderService, offlineRegion);

        Field field = Class.forName("com.mapbox.mapboxsdk.offline.OfflineRegion").getDeclaredField("state");
        field.setAccessible(true);

        assertEquals(OfflineRegion.STATE_ACTIVE, field.getInt(offlineRegion));
        assertTrue(mapboxOfflineDownloaderService.observeOfflineRegionCalled);
    }

    @Test
    public void showDownloadCompleteNotificationShouldCreateValidNotification() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        String title = "Some title";
        String description = "Some description";

        Method method = mapboxOfflineDownloaderService.getClass().getDeclaredMethod("showDownloadCompleteNotification", String.class, String.class);
        method.setAccessible(true);
        method.invoke(mapboxOfflineDownloaderService, title, description);

        ShadowNotificationManager shadowNotificationManager = Shadows.shadowOf((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE));
        Notification notification = shadowNotificationManager.getNotification(mapboxOfflineDownloaderService.LAST_DOWNLOAD_COMPLETE_NOTIFICATION_ID);
        ShadowNotification shadowNotification = Shadows.shadowOf(notification);

        assertEquals(title, shadowNotification.getContentTitle());
        assertEquals(description, shadowNotification.getContentText());
    }

    @Test
    public void getTaskStatusShouldUpdateCurrentDownloadMapNameWhenGivenValidDownloadQueueTask() throws InvocationTargetException, IllegalAccessException, NoSuchMethodException, NoSuchFieldException {
        String expectedMapName = UUID.randomUUID().toString();
        MapBoxOfflineQueueTask mapBoxOfflineQueueTask = MapBoxDownloadTask.constructMapBoxOfflineQueueTask(createSampleDownloadTask("kl", expectedMapName, sampleValidMapboxStyleURL));

        // Set the offlineManager to null
        MapBoxOfflineResourcesDownloader mapBoxOfflineResourcesDownloader = MapBoxOfflineResourcesDownloader.getInstance(context, "");
        OfflineManager offlineManager = (OfflineManager) getValueInPrivateField(mapBoxOfflineResourcesDownloader, "offlineManager");

        insertValueInPrivateField(mapBoxOfflineResourcesDownloader, "offlineManager", null);

        Method method = mapboxOfflineDownloaderService.getClass().getDeclaredMethod("getTaskStatus", MapBoxOfflineQueueTask.class, String.class, OfflineRegionStatusCallback.class);
        method.setAccessible(true);
        method.invoke(mapboxOfflineDownloaderService, mapBoxOfflineQueueTask, BuildConfig.MAPBOX_SDK_ACCESS_TOKEN, null);

        insertValueInPrivateField(mapBoxOfflineResourcesDownloader, "offlineManager", offlineManager);

        assertEquals(expectedMapName, (String) getValueInPrivateField(mapboxOfflineDownloaderService, "currentMapDownloadName"));
    }

    @Test
    public void getTaskStatusShouldUpdateCurrentDownloadMapNameWhenGivenValidDeleteQueueTask() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, NoSuchFieldException {
        String expectedMapName = UUID.randomUUID().toString();
        MapBoxDeleteTask mapBoxDeleteTask = new MapBoxDeleteTask(
                expectedMapName,
                mapboxAccessToken
        );
        MapBoxOfflineQueueTask mapBoxOfflineQueueTask = MapBoxDeleteTask.constructMapBoxOfflineQueueTask(mapBoxDeleteTask);

        // Set the offlineManager to null
        MapBoxOfflineResourcesDownloader mapBoxOfflineResourcesDownloader = MapBoxOfflineResourcesDownloader.getInstance(context, "");
        OfflineManager offlineManager = (OfflineManager) getValueInPrivateField(mapBoxOfflineResourcesDownloader, "offlineManager");

        insertValueInPrivateField(mapBoxOfflineResourcesDownloader, "offlineManager", null);

        Method method = mapboxOfflineDownloaderService.getClass().getDeclaredMethod("getTaskStatus", MapBoxOfflineQueueTask.class, String.class, OfflineRegionStatusCallback.class);
        method.setAccessible(true);
        method.invoke(mapboxOfflineDownloaderService, mapBoxOfflineQueueTask, BuildConfig.MAPBOX_SDK_ACCESS_TOKEN, null);

        insertValueInPrivateField(mapBoxOfflineResourcesDownloader, "offlineManager", offlineManager);

        assertEquals(expectedMapName, (String) getValueInPrivateField(mapboxOfflineDownloaderService, "currentMapDownloadName"));
    }

    @Test
    public synchronized void onStatusChangedShouldShowDownloadCompleteNotificationWhenGivenCompletedOfflineRegion() throws Throwable {
        latch = new CountDownLatch(1);
        OfflineRegionStatus completeOfflineRegionStatus = createOfflineRegion(OfflineRegion.STATE_ACTIVE, 300, 98923, 898, 230909, 300, true, true);

        // Create dummy download task & insert it into the service
        Intent sampleServiceIntent = createMapboxOfflineDownloaderServiceIntent();
        sampleServiceIntent = createSampleDownloadIntent(sampleServiceIntent);

        String mapName = sampleServiceIntent.getStringExtra(Constants.PARCELABLE_KEY_MAP_UNIQUE_NAME);

        RealmDatabase realmDatabase = RealmDatabase.init(context);
        insertValueInPrivateField(mapboxOfflineDownloaderService, "realmDatabase", realmDatabase);

        mapboxOfflineDownloaderService.persistOfflineMapTask(sampleServiceIntent);

        setMapNameAndDownloadAction(mapName, MapboxOfflineDownloaderService.SERVICE_ACTION.DOWNLOAD_MAP);
        registerLocalBroadcastReceiverForDownloadServiceUpdates();


        mapboxOfflineDownloaderService.onStatusChanged(completeOfflineRegionStatus, null);
        latch.await();

        //1. Make sure broadcast is sent
        Intent intent = (Intent) resultsToCheck.get(0);
        assertBroadcastResults(intent, MapboxOfflineDownloaderService.SERVICE_ACTION_RESULT.SUCCESSFUL, mapName, "100.0", MapboxOfflineDownloaderService.SERVICE_ACTION.DOWNLOAD_MAP);

        //2. Make sure performNextTask() is called
        assertTrue(mapboxOfflineDownloaderService.performNextTaskCalled);
    }

    /*
    --------------------------
    --------------------------

    HELPER METHODS FOR TESTING

    --------------------------
    --------------------------
     */

    private void assertValidBroadcastCreatedWhenSendBroadcastIsCalled(MapboxOfflineDownloaderService.SERVICE_ACTION_RESULT serviceActionResult, String mapName, String resultMessage, MapboxOfflineDownloaderService.SERVICE_ACTION parentServiceAction) {
        latch = new CountDownLatch(1);
        try {

            registerLocalBroadcastReceiverForDownloadServiceUpdates();
            invokeSendBroadcast(serviceActionResult, mapName, resultMessage, parentServiceAction);
            latch.await();

            Intent intent = (Intent) resultsToCheck.get(0);
            assertBroadcastResults(intent, serviceActionResult, mapName, resultMessage, parentServiceAction);

        } catch (NoSuchMethodException e) {
            System.out.println(TAG + ": " + Log.getStackTraceString(e));
            fail();
        } catch (IllegalAccessException e) {
            System.out.println(TAG + ": " + Log.getStackTraceString(e));
            fail();
        } catch (InvocationTargetException e) {
            System.out.println(TAG + ": " + Log.getStackTraceString(e));
            fail();
        } catch (InterruptedException e) {
            System.out.println(TAG + ": " + Log.getStackTraceString(e));
            fail();
        }
    }

    private void registerLocalBroadcastReceiverForDownloadServiceUpdates() {
        LocalBroadcastManager.getInstance(context)
                .registerReceiver(new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        resultsToCheck.add(intent);
                        latch.countDown();
                    }
                }, new IntentFilter(Constants.INTENT_ACTION_MAP_DOWNLOAD_SERVICE_STATUS_UPDATES));
    }

    private void invokeSendBroadcast(MapboxOfflineDownloaderService.SERVICE_ACTION_RESULT serviceActionResult, String mapName, String resultMessage, MapboxOfflineDownloaderService.SERVICE_ACTION parentServiceAction) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Method sendBroadcastMethod = mapboxOfflineDownloaderService.getClass().getDeclaredMethod(
                "sendBroadcast",
                MapboxOfflineDownloaderService.SERVICE_ACTION_RESULT.class,
                String.class,
                MapboxOfflineDownloaderService.SERVICE_ACTION.class,
                String.class);
        sendBroadcastMethod.setAccessible(true);
        sendBroadcastMethod.invoke(mapboxOfflineDownloaderService,
                serviceActionResult,
                mapName,
                parentServiceAction,
                resultMessage);
    }

    private void invokeSendBroadcast(MapboxOfflineDownloaderService.SERVICE_ACTION_RESULT serviceActionResult, String mapName, MapboxOfflineDownloaderService.SERVICE_ACTION parentServiceAction) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        invokeSendBroadcast(serviceActionResult, mapName, "", parentServiceAction);
    }

    private void assertBroadcastResults(Intent intent, MapboxOfflineDownloaderService.SERVICE_ACTION_RESULT serviceActionResult, String mapName, String resultMessage, MapboxOfflineDownloaderService.SERVICE_ACTION parentServiceAction) {
        assertTrue(intent.hasExtra(MapboxOfflineDownloaderService.KEY_RESULT_STATUS));
        assertTrue(intent.hasExtra(MapboxOfflineDownloaderService.KEY_RESULT_MESSAGE));
        assertTrue(intent.hasExtra(MapboxOfflineDownloaderService.KEY_RESULTS_PARENT_ACTION));
        assertTrue(intent.hasExtra(Constants.PARCELABLE_KEY_MAP_UNIQUE_NAME));

        assertEquals(serviceActionResult.name(), intent.getStringExtra(MapboxOfflineDownloaderService.KEY_RESULT_STATUS));
        assertEquals(resultMessage, intent.getStringExtra(MapboxOfflineDownloaderService.KEY_RESULT_MESSAGE));
        assertEquals(parentServiceAction, intent.getSerializableExtra(MapboxOfflineDownloaderService.KEY_RESULTS_PARENT_ACTION));
        assertEquals(mapName, intent.getStringExtra(Constants.PARCELABLE_KEY_MAP_UNIQUE_NAME));
    }

    private void assertBroadcastResults(Intent intent, MapboxOfflineDownloaderService.SERVICE_ACTION_RESULT serviceActionResult, String mapName, MapboxOfflineDownloaderService.SERVICE_ACTION parentServiceAction) {
        assertBroadcastResults(intent, serviceActionResult, mapName, "", parentServiceAction);
    }

    private void assertValidBroadcastCreatedWhenSendBroadcast2IsCalled(MapboxOfflineDownloaderService.SERVICE_ACTION_RESULT serviceActionResult, String mapName, MapboxOfflineDownloaderService.SERVICE_ACTION parentServiceAction) {
        latch = new CountDownLatch(1);

        try {
            registerLocalBroadcastReceiverForDownloadServiceUpdates();
            invokeSendBroadcast(serviceActionResult, mapName, parentServiceAction);

            latch.await();

            Intent intent = (Intent) resultsToCheck.get(0);
            assertBroadcastResults(intent, serviceActionResult, mapName, parentServiceAction);
        } catch (NoSuchMethodException e) {
            System.out.println(TAG + ": " + Log.getStackTraceString(e));
            fail();
        } catch (IllegalAccessException e) {
            System.out.println(TAG + ": " + Log.getStackTraceString(e));
            fail();
        } catch (InvocationTargetException e) {
            System.out.println(TAG + ": " + Log.getStackTraceString(e));
            fail();
        } catch (InterruptedException e) {
            System.out.println(TAG + ": " + Log.getStackTraceString(e));
            fail();
        }
    }

    private void setMapNameAndDownloadAction(String mapName, MapboxOfflineDownloaderService.SERVICE_ACTION serviceAction) throws NoSuchFieldException, IllegalAccessException {
        insertValueInPrivateField(mapboxOfflineDownloaderService, "currentMapDownloadName", mapName);
        insertValueInPrivateField(mapboxOfflineDownloaderService, "currentServiceAction", serviceAction);
    }

    private void insertValueInPrivateField(Object object, String fieldName, Object value) throws NoSuchFieldException, IllegalAccessException {
        Field reflectedPrivateField = object.getClass().getDeclaredField(fieldName);
        reflectedPrivateField.setAccessible(true);
        reflectedPrivateField.set(object, value);
    }

    private Object getValueInPrivateField(Object object, String fieldName) throws IllegalAccessException, NoSuchFieldException {
        Field reflectedPrivateField = object.getClass().getDeclaredField(fieldName);
        reflectedPrivateField.setAccessible(true);
        return reflectedPrivateField.get(object);
    }

    private Intent createMapboxOfflineDownloaderServiceIntent() {
        Intent serviceIntent =
                new Intent(context,
                        MapboxOfflineDownloaderService.class);

        return serviceIntent;
    }

    private Intent createSampleDeleteIntent(Intent serviceIntent) {

        // Data can be passed to the service via the Intent.
        serviceIntent.putExtra(Constants.PARCELABLE_KEY_SERVICE_ACTION, MapboxOfflineDownloaderService.SERVICE_ACTION.DELETE_MAP);
        serviceIntent.putExtra(Constants.PARCELABLE_KEY_MAP_UNIQUE_NAME, mapName);
        serviceIntent.putExtra(Constants.PARCELABLE_KEY_MAPBOX_ACCESS_TOKEN, mapboxAccessToken);

        return serviceIntent;
    }

    private Intent createSampleDownloadIntent(Intent serviceIntent) {
        // Data can be passed to the service via the Intent.
        serviceIntent.putExtra(Constants.PARCELABLE_KEY_SERVICE_ACTION, MapboxOfflineDownloaderService.SERVICE_ACTION.DOWNLOAD_MAP);
        serviceIntent.putExtra(Constants.PARCELABLE_KEY_MAP_UNIQUE_NAME, mapName);
        serviceIntent.putExtra(Constants.PARCELABLE_KEY_MAPBOX_ACCESS_TOKEN, mapboxAccessToken);
        serviceIntent.putExtra(Constants.PARCELABLE_KEY_STYLE_URL, sampleValidMapboxStyleURL);
        serviceIntent.putExtra(Constants.PARCELABLE_KEY_MAX_ZOOM, maxZoom);
        serviceIntent.putExtra(Constants.PARCELABLE_KEY_MIN_ZOOM, minZoom);
        serviceIntent.putExtra(Constants.PARCELABLE_KEY_TOP_LEFT_BOUND, topLeftBound);
        serviceIntent.putExtra(Constants.PARCELABLE_KEY_TOP_RIGHT_BOUND, topRightBound);
        serviceIntent.putExtra(Constants.PARCELABLE_KEY_BOTTOM_RIGHT_BOUND, bottomRightBound);
        serviceIntent.putExtra(Constants.PARCELABLE_KEY_BOTTOM_LEFT_BOUND, bottomLeftBound);

        return serviceIntent;
    }

    private OfflineRegionStatus createOfflineRegion(int downloadState, long completedResourceCount,
                                                    long completedResourceSize, long completedTileCount,
                                                    long completedTileSize, long requiredResourceCount,
                                                    boolean requiredResourceCountIsPrecise, boolean isDownloadComplete) {

        OfflineRegionStatus offlineRegionStatus = mock(OfflineRegionStatus.class);

        // Mock OfflineRegion.setMetadata
        Mockito.when(offlineRegionStatus.getDownloadState())
                .thenReturn(downloadState);

        Mockito.when(offlineRegionStatus.getCompletedResourceCount())
                .thenReturn(completedResourceCount);

        Mockito.when(offlineRegionStatus.getCompletedResourceSize())
                .thenReturn(completedResourceSize);

        Mockito.when(offlineRegionStatus.getCompletedTileCount())
                .thenReturn(completedTileCount);

        Mockito.when(offlineRegionStatus.getCompletedTileSize())
                .thenReturn(completedTileSize);

        Mockito.when(offlineRegionStatus.getRequiredResourceCount())
                .thenReturn(requiredResourceCount);

        Mockito.when(offlineRegionStatus.isRequiredResourceCountPrecise())
                .thenReturn(requiredResourceCountIsPrecise);

        Mockito.when(offlineRegionStatus.isComplete())
                .thenReturn(isDownloadComplete);

        return offlineRegionStatus;
    }

    private OfflineRegion createMockOfflineRegion(byte[] metadata, OfflineRegionDefinition offlineRegionDefinition, int downloadState ) {
        final OfflineRegion offlineRegion = mock(OfflineRegion.class);

        // Mock OfflineRegion.setMetadata
        Mockito.when(offlineRegion.getDefinition())
                .thenReturn(offlineRegionDefinition);

        Mockito.when(offlineRegion.getMetadata())
                .thenReturn(metadata);

        // Mock OfflineRegion.setDownloadState
        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                Class offlineRegionClass = Class.forName("com.mapbox.mapboxsdk.offline.OfflineRegion");
                Field stateField = offlineRegionClass.getDeclaredField("state");
                stateField.setAccessible(true);

                int passedState = invocationOnMock.getArgument(0);
                stateField.set(offlineRegion, passedState);

                return null;
            }
        }).when(offlineRegion)
                .setDownloadState(Mockito.anyInt());

        // Mock calling setObserver
        Mockito.doNothing()
                .when(offlineRegion)
                .setObserver(Mockito.any(OfflineRegion.OfflineRegionObserver.class));

        // Mock the OfflineRegion.delete(OfflineRegionDeleteCallback)
        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                OfflineRegion.OfflineRegionDeleteCallback offlineRegionDeleteCallback = invocation.getArgument(0);
                offlineRegionDeleteCallback.onDelete();

                return null;
            }
        }).when(offlineRegion)
                .delete(Mockito.any(OfflineRegion.OfflineRegionDeleteCallback.class));

        offlineRegion.setDownloadState(downloadState);

        return offlineRegion;
    }

    private MapBoxDownloadTask createSampleDownloadTask(String packageName, String mapName, String mapBoxStyleURL) {

        return new MapBoxDownloadTask(
                packageName,
                mapName,
                mapBoxStyleURL,
                10d,
                12d,
                new LatLng(
                        -17.854564,
                        25.854782
                ),
                new LatLng(
                        -17.854564,
                        25.876589
                ),
                new LatLng(
                        -17.875469,
                        25.876589
                ),
                new LatLng(
                        -17.875469,
                        25.854782
                ),
                BuildConfig.MAPBOX_SDK_ACCESS_TOKEN
        );
    }


}