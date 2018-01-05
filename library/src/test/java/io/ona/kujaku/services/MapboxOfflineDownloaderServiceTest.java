package io.ona.kujaku.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.mapbox.mapboxsdk.geometry.LatLng;
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
import io.ona.kujaku.data.realm.objects.MapBoxOfflineQueueTask;
import io.ona.kujaku.listeners.OfflineRegionStatusCallback;
import io.ona.kujaku.shadows.ShadowConnectivityReceiver;
import io.ona.kujaku.shadows.ShadowMapBoxDeleteTask;
import io.ona.kujaku.shadows.ShadowMapBoxDownloadTask;
import io.ona.kujaku.shadows.ShadowOfflineManager;
import io.ona.kujaku.shadows.ShadowRealm;
import io.ona.kujaku.shadows.implementations.RealmDbTestImplementation;
import io.ona.kujaku.utils.NumberFormatter;
import utils.Constants;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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
                ShadowOfflineManager.class
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
    private LatLng bottomRightBound = new LatLng(1.1, 20.5);

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
    public void persistsOfflineMapTaskShouldReturnFalseWhenGivenNullIntent() {
        Intent sampleExtra = null;
        assertEquals(false, mapboxOfflineDownloaderService.persistOfflineMapTask(sampleExtra));
    }

    @Test
    public void persistsOfflineMapTaskShouldReturnFalseWhenGivenNullIntentExtras() {
        Intent sampleExtra = new Intent();
        assertEquals(false, mapboxOfflineDownloaderService.persistOfflineMapTask(sampleExtra));

        sampleExtra.putExtra("SOME EXTRA", "");
        assertEquals(false, mapboxOfflineDownloaderService.persistOfflineMapTask(sampleExtra));
    }

    @Test
    public void persistsOfflineMapTaskShouldReturnTrueWhenGivenValidDeleteTask() {
        Intent sampleServiceIntent = createMapboxOfflineDownloaderServiceIntent();
        sampleServiceIntent = createSampleDeleteIntent(sampleServiceIntent);

        assertEquals(true, mapboxOfflineDownloaderService.persistOfflineMapTask(sampleServiceIntent));
    }

    @Test
    public void persistsOfflineMapTaskShouldReturnTrueWhenGivenValidDownloadTask() {
        Intent sampleServiceIntent = createMapboxOfflineDownloaderServiceIntent();
        sampleServiceIntent = createSampleDownloadIntent(sampleServiceIntent);

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
    public void persistOfflineMapTaskShouldSaveQueueTaskWhenGivenValidDeleteTask() {
        Intent sampleServiceIntent = createMapboxOfflineDownloaderServiceIntent();
        sampleServiceIntent = createSampleDeleteIntent(sampleServiceIntent);

        Calendar calendar = Calendar.getInstance();
        assertEquals(true, mapboxOfflineDownloaderService.persistOfflineMapTask(sampleServiceIntent));

        MapBoxOfflineQueueTask task = (MapBoxOfflineQueueTask) RealmDbTestImplementation.first();

        assertEquals(MapBoxOfflineQueueTask.TASK_TYPE_DELETE, task.getTaskType());
        assertEquals(MapBoxOfflineQueueTask.TASK_STATUS_INCOMPLETE, task.getTaskStatus());
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
    public void persistOfflineMapTaskShouldSaveQueueTaskWhenGivenValidDownloadTask() {
        Intent sampleServiceIntent = createMapboxOfflineDownloaderServiceIntent();
        sampleServiceIntent = createSampleDownloadIntent(sampleServiceIntent);

        Calendar calendar = Calendar.getInstance();
        assertEquals(true, mapboxOfflineDownloaderService.persistOfflineMapTask(sampleServiceIntent));

        MapBoxOfflineQueueTask task = (MapBoxOfflineQueueTask) RealmDbTestImplementation.first();

        assertEquals(MapBoxOfflineQueueTask.TASK_TYPE_DOWNLOAD, task.getTaskType());
        assertEquals(MapBoxOfflineQueueTask.TASK_STATUS_INCOMPLETE, task.getTaskStatus());
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
            jsonObject.put(MapBoxDownloadTask.BOTTOM_RIGHT_BOUND, MapBoxDownloadTask.constructLatLngJSONObject(bottomRightBound));

            assertEquals(jsonObject.toString(), task.getTask().toString());
        } catch (JSONException e) {
            Log.e(TAG, Log.getStackTraceString(e));
            fail();
        }
    }

    @Test
    public void sendBroadcastShouldProduceValidIntentWhenGivenDownloadUpdate() {
        assertValidBroadcastCreatedWhenSendBroadcastIsCalled(MapboxOfflineDownloaderService.SERVICE_ACTION_RESULT.SUCCESSFUL, mapName, "9.0%", Constants.SERVICE_ACTION.DOWNLOAD_MAP);
    }

    @Test
    public void sendBroadcast2ShouldProduceValidIntentWhenGivenDownloadUpdate() {
        assertValidBroadcastCreatedWhenSendBroadcast2IsCalled(MapboxOfflineDownloaderService.SERVICE_ACTION_RESULT.SUCCESSFUL, mapName, Constants.SERVICE_ACTION.DELETE_MAP);
    }

    @Test
    public void mapboxTileLimitExceededShouldCreateValidBroadcast() throws InterruptedException, NoSuchFieldException, IllegalAccessException {
        latch = new CountDownLatch(1);

        registerLocalBroadcastReceiverForDownloadServiceUpdates();

        setMapNameAndDownloadAction(mapName, Constants.SERVICE_ACTION.DOWNLOAD_MAP);
        mapboxOfflineDownloaderService.mapboxTileCountLimitExceeded(60000);

        latch.await();

        Intent intent = (Intent) resultsToCheck.get(0);
        assertBroadcastResults(intent, MapboxOfflineDownloaderService.SERVICE_ACTION_RESULT.FAILED, mapName, "MapBox Tile Count limit exceeded : 60000 while Downloading " + mapName, Constants.SERVICE_ACTION.DOWNLOAD_MAP);
    }

    @Test
    public void onErrorShouldCreateValidBroadcastWhenGivenNonEmptyReasonAndMessage() throws NoSuchFieldException, IllegalAccessException {
        latch = new CountDownLatch(1);

        String reason = "Some reason here";
        String message = "Some error message here";

        registerLocalBroadcastReceiverForDownloadServiceUpdates();
        setMapNameAndDownloadAction(mapName, Constants.SERVICE_ACTION.DELETE_MAP);

        mapboxOfflineDownloaderService.onError(reason, message);

        Intent intent = (Intent) resultsToCheck.get(0);
        assertBroadcastResults(intent, MapboxOfflineDownloaderService.SERVICE_ACTION_RESULT.FAILED, mapName, reason + ": " + message, Constants.SERVICE_ACTION.DELETE_MAP);
    }

    @Test
    public void onErrorShouldCreateValidBroadcastWhenGivenNonEmptyReasonAndEmptyMessage() throws NoSuchFieldException, IllegalAccessException, InterruptedException {
        latch = new CountDownLatch(1);

        String reason = "Some reason here";
        String message = "";

        registerLocalBroadcastReceiverForDownloadServiceUpdates();
        setMapNameAndDownloadAction(mapName, Constants.SERVICE_ACTION.DELETE_MAP);

        mapboxOfflineDownloaderService.onError(reason, message);

        latch.await();
        Intent intent = (Intent) resultsToCheck.get(0);
        assertBroadcastResults(intent, MapboxOfflineDownloaderService.SERVICE_ACTION_RESULT.FAILED, mapName, reason, Constants.SERVICE_ACTION.DELETE_MAP);
    }

    @Test
    public void onStatusChangedShouldShowProgressNotificationWhenGivenIncompleteOfflineRegionStatus() throws NoSuchFieldException, IllegalAccessException, InterruptedException {
        latch = new CountDownLatch(1);
        OfflineRegionStatus completeOfflineRegionStatus = createOfflineRegion(OfflineRegion.STATE_ACTIVE, 200, 98923, 898, 230909, 300, true, false);

        setMapNameAndDownloadAction(mapName, Constants.SERVICE_ACTION.DOWNLOAD_MAP);
        registerLocalBroadcastReceiverForDownloadServiceUpdates();

        mapboxOfflineDownloaderService.onStatusChanged(completeOfflineRegionStatus, null);

        latch.await();

        double percentage = 100.0 * 200l/300l;
        String contentTitle = "Offline Map Download Progress: " + mapName;
        String contentText = "Downloading: " + NumberFormatter.formatDecimal(percentage) + " %";

        Intent intent = (Intent) resultsToCheck.get(0);
        assertBroadcastResults(intent, MapboxOfflineDownloaderService.SERVICE_ACTION_RESULT.SUCCESSFUL, mapName, String.valueOf(percentage), Constants.SERVICE_ACTION.DOWNLOAD_MAP );

        ShadowNotificationManager shadowNotificationManager = Shadows.shadowOf((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE));
        Notification notification = shadowNotificationManager.getNotification(mapboxOfflineDownloaderService.PROGRESS_NOTIFICATION_ID);
        ShadowNotification shadowNotification = Shadows.shadowOf(notification);

        assertEquals(contentTitle, shadowNotification.getContentTitle());
        assertEquals(contentText, shadowNotification.getContentText());
    }

    @Test
    public void isNetworkConnectionPreferred() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method = mapboxOfflineDownloaderService.getClass().getDeclaredMethod("isNetworkConnectionPreferred", int.class);
        method.setAccessible(true);
        boolean isPreferred = (boolean) method.invoke(mapboxOfflineDownloaderService, 2);

        assertFalse(isPreferred);
        assertTrue((boolean) method.invoke(mapboxOfflineDownloaderService, ConnectivityManager.TYPE_WIFI));
        assertTrue((boolean) method.invoke(mapboxOfflineDownloaderService, ConnectivityManager.TYPE_MOBILE));
        assertFalse((boolean) method.invoke(mapboxOfflineDownloaderService, ConnectivityManager.TYPE_MOBILE_DUN));
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

        Method method = mapboxOfflineDownloaderService.getClass().getDeclaredMethod("getTaskStatus", MapBoxOfflineQueueTask.class, String.class, OfflineRegionStatusCallback.class);
        method.setAccessible(true);
        method.invoke(mapboxOfflineDownloaderService, mapBoxOfflineQueueTask, BuildConfig.MAPBOX_SDK_ACCESS_TOKEN, null);

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

        Method method = mapboxOfflineDownloaderService.getClass().getDeclaredMethod("getTaskStatus", MapBoxOfflineQueueTask.class, String.class, OfflineRegionStatusCallback.class);
        method.setAccessible(true);
        method.invoke(mapboxOfflineDownloaderService, mapBoxOfflineQueueTask, BuildConfig.MAPBOX_SDK_ACCESS_TOKEN, null);

        assertEquals(expectedMapName, (String) getValueInPrivateField(mapboxOfflineDownloaderService, "currentMapDownloadName"));
    }

    /*
    --------------------------
    --------------------------

    HELPER METHODS FOR TESTING

    --------------------------
    --------------------------
     */

    private void assertValidBroadcastCreatedWhenSendBroadcastIsCalled(MapboxOfflineDownloaderService.SERVICE_ACTION_RESULT serviceActionResult, String mapName, String resultMessage, Constants.SERVICE_ACTION parentServiceAction) {
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

    private void invokeSendBroadcast(MapboxOfflineDownloaderService.SERVICE_ACTION_RESULT serviceActionResult, String mapName, String resultMessage, Constants.SERVICE_ACTION parentServiceAction) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Method sendBroadcastMethod = mapboxOfflineDownloaderService.getClass().getDeclaredMethod(
                "sendBroadcast",
                MapboxOfflineDownloaderService.SERVICE_ACTION_RESULT.class,
                String.class,
                Constants.SERVICE_ACTION.class,
                String.class);
        sendBroadcastMethod.setAccessible(true);
        sendBroadcastMethod.invoke(mapboxOfflineDownloaderService,
                serviceActionResult,
                mapName,
                parentServiceAction,
                resultMessage);
    }

    private void invokeSendBroadcast(MapboxOfflineDownloaderService.SERVICE_ACTION_RESULT serviceActionResult, String mapName, Constants.SERVICE_ACTION parentServiceAction) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        invokeSendBroadcast(serviceActionResult, mapName, "", parentServiceAction);
    }

    private void assertBroadcastResults(Intent intent, MapboxOfflineDownloaderService.SERVICE_ACTION_RESULT serviceActionResult, String mapName, String resultMessage, Constants.SERVICE_ACTION parentServiceAction) {
        assertTrue(intent.hasExtra(MapboxOfflineDownloaderService.RESULT_STATUS));
        assertTrue(intent.hasExtra(MapboxOfflineDownloaderService.RESULT_MESSAGE));
        assertTrue(intent.hasExtra(MapboxOfflineDownloaderService.RESULTS_PARENT_ACTION));
        assertTrue(intent.hasExtra(Constants.PARCELABLE_KEY_MAP_UNIQUE_NAME));

        assertEquals(serviceActionResult.name(), intent.getStringExtra(MapboxOfflineDownloaderService.RESULT_STATUS));
        assertEquals(resultMessage, intent.getStringExtra(MapboxOfflineDownloaderService.RESULT_MESSAGE));
        assertEquals(parentServiceAction, intent.getSerializableExtra(MapboxOfflineDownloaderService.RESULTS_PARENT_ACTION));
        assertEquals(mapName, intent.getStringExtra(Constants.PARCELABLE_KEY_MAP_UNIQUE_NAME));
    }

    private void assertBroadcastResults(Intent intent, MapboxOfflineDownloaderService.SERVICE_ACTION_RESULT serviceActionResult, String mapName, Constants.SERVICE_ACTION parentServiceAction) {
        assertBroadcastResults(intent, serviceActionResult, mapName, "", parentServiceAction);
    }

    private void assertValidBroadcastCreatedWhenSendBroadcast2IsCalled(MapboxOfflineDownloaderService.SERVICE_ACTION_RESULT serviceActionResult, String mapName, Constants.SERVICE_ACTION parentServiceAction) {
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

    private void setMapNameAndDownloadAction(String mapName, Constants.SERVICE_ACTION serviceAction) throws NoSuchFieldException, IllegalAccessException {
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
        serviceIntent.putExtra(Constants.PARCELABLE_KEY_SERVICE_ACTION, Constants.SERVICE_ACTION.DELETE_MAP);
        serviceIntent.putExtra(Constants.PARCELABLE_KEY_MAP_UNIQUE_NAME, mapName);
        serviceIntent.putExtra(Constants.PARCELABLE_KEY_MAPBOX_ACCESS_TOKEN, mapboxAccessToken);

        return serviceIntent;
    }

    private Intent createSampleDownloadIntent(Intent serviceIntent) {
        // Data can be passed to the service via the Intent.
        serviceIntent.putExtra(Constants.PARCELABLE_KEY_SERVICE_ACTION, Constants.SERVICE_ACTION.DOWNLOAD_MAP);
        serviceIntent.putExtra(Constants.PARCELABLE_KEY_MAP_UNIQUE_NAME, mapName);
        serviceIntent.putExtra(Constants.PARCELABLE_KEY_MAPBOX_ACCESS_TOKEN, mapboxAccessToken);
        serviceIntent.putExtra(Constants.PARCELABLE_KEY_STYLE_URL, sampleValidMapboxStyleURL);
        serviceIntent.putExtra(Constants.PARCELABLE_KEY_MAX_ZOOM, maxZoom);
        serviceIntent.putExtra(Constants.PARCELABLE_KEY_MIN_ZOOM, minZoom);
        serviceIntent.putExtra(Constants.PARCELABLE_KEY_TOP_LEFT_BOUND, topLeftBound);
        serviceIntent.putExtra(Constants.PARCELABLE_KEY_BOTTOM_RIGHT_BOUND, bottomRightBound);

        return serviceIntent;
    }

    private OfflineRegionStatus createOfflineRegion(int downloadState, long completedResourceCount,
                                                    long completedResourceSize, long completedTileCount,
                                                    long completedTileSize, long requiredResourceCount,
                                                    boolean requiredResourceCountIsPrecise, boolean isDownloadComplete) {

        OfflineRegionStatus offlineRegionStatus = Mockito.mock(OfflineRegionStatus.class);

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
        final OfflineRegion offlineRegion = Mockito.mock(OfflineRegion.class);

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

                int passedState = invocationOnMock.getArgumentAt(0, int.class);
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
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {

                // Just call the callback directly if gotten to this point i.e. The OfflineRegion is not null
                OfflineRegion.OfflineRegionDeleteCallback offlineRegionDeleteCallback = invocationOnMock.getArgumentAt(0, OfflineRegion.OfflineRegionDeleteCallback.class);
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
                        -17.875469,
                        25.876589
                ),
                BuildConfig.MAPBOX_SDK_ACCESS_TOKEN
        );
    }


}