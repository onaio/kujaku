package io.ona.kujaku.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.support.v4.content.LocalBroadcastManager;

import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.offline.OfflineRegion;
import com.mapbox.mapboxsdk.offline.OfflineRegionStatus;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import io.ona.kujaku.BuildConfig;
import io.ona.kujaku.data.realm.objects.MapBoxOfflineQueueTask;
import io.realm.Realm;
import utils.Constants;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 03/01/2018.
 */


@RunWith(AndroidJUnit4.class)
public class MapboxOfflineDownloaderServiceInstrumentedTest {

    private String mapName = UUID.randomUUID().toString();
    private static final String TAG = MapboxOfflineDownloaderServiceInstrumentedTest.class.getSimpleName();

    private Context context;
    private MapboxOfflineDownloaderService mapboxOfflineDownloaderService;

    private String sampleValidMapboxStyleURL = "mapbox://styles/ona/90kiosdcIJ3d";
    private String mapboxAccessToken = BuildConfig.MAPBOX_SDK_ACCESS_TOKEN;
    private float minZoom = 22;
    private float maxZoom = 10;
    private LatLng topLeftBound = new LatLng(9.1, 9.1);
    private LatLng bottomRightBound = new LatLng(1.1, 20.5);

    private ArrayList<Object> resultsToCheck = new ArrayList<>();
    private CountDownLatch latch;
    private ArrayList<MapBoxOfflineQueueTask> offlineQueueTasks = new ArrayList<>();

    public MapboxOfflineDownloaderServiceInstrumentedTest() {
        Realm.init(InstrumentationRegistry.getTargetContext());
    }

    @Before
    public void setup() throws NoSuchFieldException, IllegalAccessException {
        context = InstrumentationRegistry.getTargetContext();
        mapboxOfflineDownloaderService = new MapboxOfflineDownloaderService();

        // Inject the context
        Field reflectedPrivateField = mapboxOfflineDownloaderService.getClass().getSuperclass().getSuperclass().getDeclaredField("mBase");
        reflectedPrivateField.setAccessible(true);
        reflectedPrivateField.set(mapboxOfflineDownloaderService, context);

        resultsToCheck.clear();
    }

    @After
    public void cleanup() {
        Realm realm = Realm.getDefaultInstance();

        realm.beginTransaction();
        Iterator<MapBoxOfflineQueueTask> iterator = offlineQueueTasks.iterator();
        while (iterator.hasNext()) {
            MapBoxOfflineQueueTask mapBoxOfflineQueueTask = iterator.next();
            if (mapBoxOfflineQueueTask.isValid()) {
                mapBoxOfflineQueueTask.deleteFromRealm();
                iterator.remove();
            }
        }

        realm.commitTransaction();
    }

    @Test
    public void onStatusChangedShouldShowDownloadCompleteNotificationWhenGivenCompletedOfflineRegion() throws Exception {
        latch = new CountDownLatch(1);
        OfflineRegionStatus completeOfflineRegionStatus = createOfflineRegion(OfflineRegion.STATE_ACTIVE, 300, 98923, 898, 230909, 300, true, true);

        // Create dummy download task & insert it into the service
        Intent sampleServiceIntent = createMapboxOfflineDownloaderServiceIntent();
        sampleServiceIntent = createSampleDownloadIntent(sampleServiceIntent);

        String mapName = sampleServiceIntent.getStringExtra(Constants.PARCELABLE_KEY_MAP_UNIQUE_NAME);

        mapboxOfflineDownloaderService.persistOfflineMapTask(sampleServiceIntent);

        MapBoxOfflineQueueTask mapBoxOfflineQueueTask = getTask(mapName);
        offlineQueueTasks.add(mapBoxOfflineQueueTask);

        insertValueInPrivateField(mapboxOfflineDownloaderService, "currentMapBoxTask", mapBoxOfflineQueueTask);

        setMapNameAndDownloadAction(mapName, MapboxOfflineDownloaderService.SERVICE_ACTION.DOWNLOAD_MAP);
        registerLocalBroadcastReceiverForDownloadServiceUpdates();


        mapboxOfflineDownloaderService.onStatusChanged(completeOfflineRegionStatus, null);
        latch.await();

        //1. Make sure broadcast is sent
        Intent intent = (Intent) resultsToCheck.get(0);
        assertBroadcastResults(intent, MapboxOfflineDownloaderService.SERVICE_ACTION_RESULT.SUCCESSFUL, mapName, "100.0", MapboxOfflineDownloaderService.SERVICE_ACTION.DOWNLOAD_MAP);

        //2. Make sure performNextTask() is called
        assertTrue(mapboxOfflineDownloaderService.performNextTaskCalled);

        //3. Make sure the new task status was persisted
        MapBoxOfflineQueueTask mapBoxOfflineQueueTask2 = (MapBoxOfflineQueueTask) getValueInPrivateField(mapboxOfflineDownloaderService, "currentMapBoxTask");
        assertEquals(MapBoxOfflineQueueTask.TASK_STATUS_DONE, mapBoxOfflineQueueTask2.getTaskStatus());

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

    private Intent createMapboxOfflineDownloaderServiceIntent() {
        Intent serviceIntent =
                new Intent(context,
                        MapboxOfflineDownloaderService.class);

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
        serviceIntent.putExtra(Constants.PARCELABLE_KEY_BOTTOM_RIGHT_BOUND, bottomRightBound);

        return serviceIntent;
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

    private void setMapNameAndDownloadAction(String mapName, MapboxOfflineDownloaderService.SERVICE_ACTION serviceAction) throws NoSuchFieldException, IllegalAccessException {
        insertValueInPrivateField(mapboxOfflineDownloaderService, "currentMapDownloadName", mapName);
        insertValueInPrivateField(mapboxOfflineDownloaderService, "currentServiceAction", serviceAction);
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

    private MapBoxOfflineQueueTask getTask(String mapName) {
        Realm realm = Realm.getDefaultInstance();

        MapBoxOfflineQueueTask mapBoxOfflineQueueTask = realm.where(MapBoxOfflineQueueTask.class)
                .equalTo("taskStatus", MapBoxOfflineQueueTask.TASK_STATUS_INCOMPLETE)
                .contains("task", mapName)
                .findFirst();

        return mapBoxOfflineQueueTask;
    }
}
