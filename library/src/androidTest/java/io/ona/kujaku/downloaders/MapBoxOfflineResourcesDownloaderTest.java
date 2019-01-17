package io.ona.kujaku.downloaders;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.annotation.UiThreadTest;
import android.support.test.rule.UiThreadTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.offline.OfflineManager;
import com.mapbox.mapboxsdk.offline.OfflineRegion;
import com.mapbox.mapboxsdk.offline.OfflineRegionDefinition;
import com.mapbox.mapboxsdk.offline.OfflineRegionStatus;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import io.ona.kujaku.BuildConfig;
import io.ona.kujaku.data.MapBoxDownloadTask;
import io.ona.kujaku.data.realm.objects.MapBoxOfflineQueueTask;
import io.ona.kujaku.listeners.IncompleteMapDownloadCallback;
import io.ona.kujaku.listeners.OnDownloadMapListener;
import io.ona.kujaku.utils.exceptions.OfflineMapDownloadException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 07/12/2017.
 */
@RunWith(AndroidJUnit4.class)
public class MapBoxOfflineResourcesDownloaderTest {

    private MapBoxOfflineResourcesDownloader mapBoxOfflineResourcesDownloader;
    private Context context;

    private String sampleMapName = UUID.randomUUID().toString();
    private CountDownLatch downLatch = new CountDownLatch(0);
    private ArrayList<Object> outputsFromCallbacks = new ArrayList<>();

    private ArrayList<byte[]> offlineRegionsMetadataList = new ArrayList<>();
    private ArrayList<OfflineRegion> offlineRegionsList = new ArrayList<>();
    private long lastId = 0;

    @Rule
    public UiThreadTestRule uiThreadTestRule = new UiThreadTestRule();

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setup() {
        context = InstrumentationRegistry.getTargetContext();
        MapBoxOfflineResourcesDownloader.instance = null;

    }

    @Test
    public void downloadMapShouldThrowExceptionWhenGivenNullContext() throws Throwable {
        expectedException.expect(OfflineMapDownloadException.class);
        expectedException.expectMessage("Context passed is nul");

        MapBoxDownloadTask invalidDownloadTask = createSampleDownloadTask();

        uiThreadTestRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mapBoxOfflineResourcesDownloader = MapBoxOfflineResourcesDownloader.getInstance(null, Mapbox.getInstance(context, BuildConfig.MAPBOX_SDK_ACCESS_TOKEN));
            }
        });
        mapBoxOfflineResourcesDownloader.downloadMap(invalidDownloadTask, null);
    }

    @Test
    public void downloadMapShouldThrowExceptionWhenGivenNullMapName() throws Throwable {
        expectedException.expect(OfflineMapDownloadException.class);
        expectedException.expectMessage("Invalid map name");

        MapBoxDownloadTask invalidDownloadTask = createSampleDownloadTask();
        invalidDownloadTask.setMapName(null);

        createMapboxOfflineResourcesDownloaderInstanceOnUIThread();
        mapBoxOfflineResourcesDownloader.downloadMap(invalidDownloadTask, null);
    }

    @Test
    public void downloadMapShouldThrowExceptionWhenGivenInvalidMapboxStyleURL() throws Throwable {
        expectedException.expect(OfflineMapDownloadException.class);
        expectedException.expectMessage("Invalid Style URL");

        MapBoxDownloadTask invalidDownloadTask = createSampleDownloadTask();
        invalidDownloadTask.setMapBoxStyleUrl("");

        createMapboxOfflineResourcesDownloaderInstanceOnUIThread();
        mapBoxOfflineResourcesDownloader.downloadMap(invalidDownloadTask, null);
    }

    @Test
    public void downloadMapShouldThrowExceptionWhenGivenInvalidMapboxStyleURL2() throws Throwable {
        expectedException.expect(OfflineMapDownloadException.class);
        expectedException.expectMessage("Invalid Style URL");

        MapBoxDownloadTask invalidDownloadTask = createSampleDownloadTask();
        invalidDownloadTask.setMapBoxStyleUrl("mapbox://tiles/kosi");

        createMapboxOfflineResourcesDownloaderInstanceOnUIThread();

        assertTrue(mapBoxOfflineResourcesDownloader.offlineManager != null);
        mapBoxOfflineResourcesDownloader.downloadMap(invalidDownloadTask, null);
    }

    @Test
    public void downloadMapShouldThrowExceptionWhenGivenInvalidMapboxStyleURL3() throws Throwable {
        expectedException.expect(OfflineMapDownloadException.class);
        expectedException.expectMessage("Invalid Style URL");

        MapBoxDownloadTask invalidDownloadTask = createSampleDownloadTask();
        invalidDownloadTask.setMapBoxStyleUrl("mapbox://styles/isdkl");

        createMapboxOfflineResourcesDownloaderInstanceOnUIThread();
        mapBoxOfflineResourcesDownloader.downloadMap(invalidDownloadTask, null);
    }

    @Test
    public void downloadMapShouldThrowExceptionWhenGivenInvalidMinMaxZoom() throws Throwable {
        expectedException.expect(OfflineMapDownloadException.class);
        expectedException.expectMessage("minZoom should be lower than maxZoom");

        MapBoxDownloadTask invalidDownloadTask = createSampleDownloadTask();
        invalidDownloadTask.setMinZoom(20);
        invalidDownloadTask.setMaxZoom(10);

        createMapboxOfflineResourcesDownloaderInstanceOnUIThread();
        mapBoxOfflineResourcesDownloader.downloadMap(invalidDownloadTask, null);
    }

    @Test
    public void downloadMapShouldThrowExceptionWhenGivenInvalidMinMaxZoom2() throws Throwable {
        expectedException.expect(OfflineMapDownloadException.class);
        expectedException.expectMessage("maxZoom & minZoom should be among 0-22");

        MapBoxDownloadTask invalidDownloadTask = createSampleDownloadTask();
        invalidDownloadTask.setMinZoom(30);
        invalidDownloadTask.setMaxZoom(10);

        createMapboxOfflineResourcesDownloaderInstanceOnUIThread();
        mapBoxOfflineResourcesDownloader.downloadMap(invalidDownloadTask, null);
    }

    @Test
    public void downloadMapShouldThrowExceptionWhenGivenInvalidMinMaxZoom3() throws Throwable {
        expectedException.expect(OfflineMapDownloadException.class);
        expectedException.expectMessage("maxZoom & minZoom should be among 0-22");

        MapBoxDownloadTask invalidDownloadTask = createSampleDownloadTask();
        invalidDownloadTask.setMinZoom(20);
        invalidDownloadTask.setMaxZoom(30);

        createMapboxOfflineResourcesDownloaderInstanceOnUIThread();
        mapBoxOfflineResourcesDownloader.downloadMap(invalidDownloadTask, null);
    }

    @Test
    public void downloadMapShouldThrowExceptionWhenGivenInvalidMinMaxZoom4() throws Throwable {
        expectedException.expect(OfflineMapDownloadException.class);
        expectedException.expectMessage("maxZoom & minZoom should be among 0-22");

        MapBoxDownloadTask invalidDownloadTask = createSampleDownloadTask();
        invalidDownloadTask.setMinZoom(-1);
        invalidDownloadTask.setMaxZoom(10);

        createMapboxOfflineResourcesDownloaderInstanceOnUIThread();
        mapBoxOfflineResourcesDownloader.downloadMap(invalidDownloadTask, null);
    }

    @Test
    public void downloadMapShouldThrowExceptionWhenGivenInvalidMinMaxZoom5() throws Throwable {
        expectedException.expect(OfflineMapDownloadException.class);
        expectedException.expectMessage("maxZoom & minZoom should be among 0-22");

        MapBoxDownloadTask invalidDownloadTask = createSampleDownloadTask();
        invalidDownloadTask.setMinZoom(20);
        invalidDownloadTask.setMaxZoom(-1);

        createMapboxOfflineResourcesDownloaderInstanceOnUIThread();
        mapBoxOfflineResourcesDownloader.downloadMap(invalidDownloadTask, null);
    }

    private void createMapboxOfflineResourcesDownloaderInstanceOnUIThread() throws Throwable {
        uiThreadTestRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mapBoxOfflineResourcesDownloader = MapBoxOfflineResourcesDownloader.getInstance(context, BuildConfig.MAPBOX_SDK_ACCESS_TOKEN);
            }
        });
    }

    @Test
    public void downloadMapShouldChangeOfflineRegionDownloadState() throws Throwable {

        createMapboxOfflineResourcesDownloaderInstanceOnUIThread();
        MapBoxDownloadTask invalidDownloadTask = createSampleDownloadTask();

        MapBoxOfflineResourcesDownloader spyMapBoxOfflineResourcesDownloader = Mockito.spy(mapBoxOfflineResourcesDownloader);

        //Mocked callback
        OnDownloadMapListener mockedOnDownloadMapListener = new OnDownloadMapListener() {
            @Override
            public void onStatusChanged(OfflineRegionStatus offlineRegionStatus, OfflineRegion offlineRegion) {
                // This should never be called

            }

            @Override
            public void onError(String errorReason) {
                // Do nothing here, not testing this here
            }

            @Override
            public void mapboxTileCountLimitExceeded(long limit) {
                // Do nothing here, not testing this here
            }
        };

        /*
        - Spy on the downloadMap method & return the mockedDownloadListener (NOT NEEDED RIGHT NOW)
        - Mock getMapStatus(OfflineRegion, int) call
        - Mock offlineManager.createOfflineRegion **
        - Mock offlineManager.listOfflineRegions **
        - Mock the OfflineRegion ->
                - getDefinition() &&
                - getMetadata()
        -
         */

        final OnDownloadMapListener spiedOnDownloadListener = Mockito.spy(mockedOnDownloadMapListener);

        // Replace the OfflineManager in the MapBoxOfflineResourcesDownloader with the Spied one
        OfflineManager spiedOfflineManager = Mockito.spy(mapBoxOfflineResourcesDownloader.offlineManager);
        mapBoxOfflineResourcesDownloader.offlineManager = spiedOfflineManager;

        // Mock OfflineManager.createOfflineRegion calls
        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {

                OfflineManager.CreateOfflineRegionCallback createOfflineRegionCallback = invocationOnMock.getArgument(2);

                //Should never be null --> CreateOfflineRegionCallback
                byte[] metadata = invocationOnMock.getArgument(1);
                OfflineRegionDefinition offlineRegionDefinition = invocationOnMock.getArgument(0);
                offlineRegionsMetadataList.add(metadata);

                OfflineRegion offlineRegion = createMockOfflineRegion(metadata, offlineRegionDefinition);

                offlineRegionsList.add(offlineRegion);
                createOfflineRegionCallback.onCreate(offlineRegion);

                // Reflect to get the download state value
                Class offlineRegionClass = Class.forName("com.mapbox.mapboxsdk.offline.OfflineRegion");
                Field stateField = offlineRegionClass.getDeclaredField("state");
                stateField.setAccessible(true);

                int state = stateField.getInt(offlineRegion);

                outputsFromCallbacks.add(state);

                return null;
            }
        }).when(spiedOfflineManager)
                .createOfflineRegion(Mockito.any(OfflineRegionDefinition.class), Mockito.any(byte[].class), Mockito.any(OfflineManager.CreateOfflineRegionCallback.class));

        // Mock OfflineManager.listOfflineRegions calls
        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                OfflineManager.ListOfflineRegionsCallback listOfflineRegionsCallback = invocationOnMock.getArgument(0);
                listOfflineRegionsCallback.onList(offlineRegionsList.toArray(new OfflineRegion[offlineRegionsList.size()]));

                return null;
            }
        }).when(spiedOfflineManager)
                .listOfflineRegions(Mockito.any(OfflineManager.ListOfflineRegionsCallback.class));

        mapBoxOfflineResourcesDownloader.downloadMap(invalidDownloadTask, spiedOnDownloadListener);

        assertEquals(OfflineRegion.STATE_ACTIVE, outputsFromCallbacks.get(0));
    }

    @Test
    public void deleteMapShouldCallErrorCallbackWhenGivenNullContext() throws Throwable {
        downLatch = new CountDownLatch(1);
        resetTestVariables();


        OfflineRegion.OfflineRegionDeleteCallback offlineRegionDeleteCallback = new OfflineRegion.OfflineRegionDeleteCallback() {
            @Override
            public void onDelete() {
                // This will not be called
                outputsFromCallbacks.add("");
                downLatch.countDown();
            }

            @Override
            public void onError(String error) {
                outputsFromCallbacks.add(error);
                downLatch.countDown();
            }
        };

        uiThreadTestRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mapBoxOfflineResourcesDownloader = MapBoxOfflineResourcesDownloader.getInstance(null, Mapbox.getInstance(context, BuildConfig.MAPBOX_SDK_ACCESS_TOKEN));
            }
        });
        mapBoxOfflineResourcesDownloader.deleteMap("Sample Map name", offlineRegionDeleteCallback);

        downLatch.await();

        assertEquals("Context passed is null", outputsFromCallbacks.get(0));
    }

    @Test
    public void deleteMapShouldCallErrorMethodWhenGiveNonExistentMapName() throws Throwable {
        downLatch = new CountDownLatch(1);
        resetTestVariables();

        uiThreadTestRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mapBoxOfflineResourcesDownloader = MapBoxOfflineResourcesDownloader.getInstance(context, Mapbox.getInstance(context, BuildConfig.MAPBOX_SDK_ACCESS_TOKEN));
            }
        });

        // Replace the OfflineManager in the MapBoxOfflineResourcesDownloader with the Spied one
        OfflineManager spiedOfflineManager = Mockito.spy(mapBoxOfflineResourcesDownloader.offlineManager);
        mapBoxOfflineResourcesDownloader.offlineManager = spiedOfflineManager;

        // Mock OfflineManager.listOfflineRegions calls
        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                OfflineManager.ListOfflineRegionsCallback listOfflineRegionsCallback = invocationOnMock.getArgument(0);
                listOfflineRegionsCallback.onList(offlineRegionsList.toArray(new OfflineRegion[offlineRegionsList.size()]));

                return null;
            }
        }).when(spiedOfflineManager)
                .listOfflineRegions(Mockito.any(OfflineManager.ListOfflineRegionsCallback.class));

        OfflineRegion.OfflineRegionDeleteCallback offlineRegionDeleteCallback = new OfflineRegion.OfflineRegionDeleteCallback() {
            @Override
            public void onDelete() {
                // This will not be called
                outputsFromCallbacks.add("");
                downLatch.countDown();
            }

            @Override
            public void onError(String error) {
                outputsFromCallbacks.add(error);
                downLatch.countDown();
            }
        };

        mapBoxOfflineResourcesDownloader.deleteMap("Sample Map name", offlineRegionDeleteCallback);

        downLatch.await();

        assertEquals("Map could not be found", outputsFromCallbacks.get(0));
    }

    @Test
    public void deleteMapShouldCallOnDeleteCallbackWhenGivenCorrectMapName() throws Throwable {
        downLatch = new CountDownLatch(1);
        resetTestVariables();

        createMapboxOfflineResourcesDownloaderInstanceOnUIThread();

        // Replace the OfflineManager in the MapBoxOfflineResourcesDownloader with the Spied one
        OfflineManager spiedOfflineManager = Mockito.spy(mapBoxOfflineResourcesDownloader.offlineManager);
        mapBoxOfflineResourcesDownloader.offlineManager = spiedOfflineManager;

        // Mock OfflineManager.createOfflineRegion calls
        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {

                OfflineManager.CreateOfflineRegionCallback createOfflineRegionCallback = invocationOnMock.getArgument(2);

                //Should never be null --> CreateOfflineRegionCallback
                byte[] metadata = invocationOnMock.getArgument(1);
                OfflineRegionDefinition offlineRegionDefinition = invocationOnMock.getArgument(0);
                offlineRegionsMetadataList.add(metadata);

                OfflineRegion offlineRegion = createMockOfflineRegion(metadata, offlineRegionDefinition);

                offlineRegionsList.add(offlineRegion);
                createOfflineRegionCallback.onCreate(offlineRegion);

                // Reflect to get the download state value
                Class offlineRegionClass = Class.forName("com.mapbox.mapboxsdk.offline.OfflineRegion");
                Field stateField = offlineRegionClass.getDeclaredField("state");
                stateField.setAccessible(true);

                int state = stateField.getInt(offlineRegion);

                assertEquals(OfflineRegion.STATE_ACTIVE, state);

                return null;
            }
        }).when(spiedOfflineManager)
                .createOfflineRegion(Mockito.any(OfflineRegionDefinition.class), Mockito.any(byte[].class), Mockito.any(OfflineManager.CreateOfflineRegionCallback.class));

        // Mock OfflineManager.listOfflineRegions calls
        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                OfflineManager.ListOfflineRegionsCallback listOfflineRegionsCallback = invocationOnMock.getArgument(0);
                listOfflineRegionsCallback.onList(offlineRegionsList.toArray(new OfflineRegion[offlineRegionsList.size()]));

                return null;
            }
        }).when(spiedOfflineManager)
                .listOfflineRegions(Mockito.any(OfflineManager.ListOfflineRegionsCallback.class));

        OfflineRegion.OfflineRegionDeleteCallback offlineRegionDeleteCallback = new OfflineRegion.OfflineRegionDeleteCallback() {
            @Override
            public void onDelete() {
                // This will not be called
                outputsFromCallbacks.add("Map deleted");
                downLatch.countDown();
            }

            @Override
            public void onError(String error) {
                outputsFromCallbacks.add(error);
                downLatch.countDown();
            }
        };

        MapBoxDownloadTask validMapBoxDownloadTask = createSampleDownloadTask();
        mapBoxOfflineResourcesDownloader.downloadMap(validMapBoxDownloadTask, null);

        mapBoxOfflineResourcesDownloader.deleteMap(validMapBoxDownloadTask.getMapName(), offlineRegionDeleteCallback);

        downLatch.await();

        assertEquals("Map deleted", outputsFromCallbacks.get(0));
    }

    @Test
    public void resumeDownloadShouldCallErrorCallbackWhenGivenNullContext() throws Throwable {
        downLatch = new CountDownLatch(1);
        resetTestVariables();

        OfflineRegion.OfflineRegionDeleteCallback offlineRegionDeleteCallback = new OfflineRegion.OfflineRegionDeleteCallback() {
            @Override
            public void onDelete() {
                outputsFromCallbacks.add("Map deleted");
                downLatch.countDown();
            }

            @Override
            public void onError(String error) {
                outputsFromCallbacks.add(error);
                downLatch.countDown();
            }
        };

        MapBoxDownloadTask invalidDownloadTask = createSampleDownloadTask();

        uiThreadTestRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mapBoxOfflineResourcesDownloader = MapBoxOfflineResourcesDownloader.getInstance(null, Mapbox.getInstance(context, BuildConfig.MAPBOX_SDK_ACCESS_TOKEN));
                mapBoxOfflineResourcesDownloader.deleteMap(invalidDownloadTask.getMapName(), offlineRegionDeleteCallback);
            }
        });

        downLatch.await();

        assertEquals("Context passed is null", outputsFromCallbacks.get(0));
    }

    @Test
    public void getIncompleteMapDownloadsShouldCallErrorCallbackWhenGivenNullContext() throws Throwable {
        resetTestVariables();
        downLatch = new CountDownLatch(1);

        IncompleteMapDownloadCallback incompleteMapDownloadCallback = new IncompleteMapDownloadCallback() {
            @Override
            public void incompleteMap(OfflineRegion offlineRegion, OfflineRegionStatus offlineRegionStatus) {
                outputsFromCallbacks.add("Incomplete maps gotten");
                downLatch.countDown();

            }

            @Override
            public void onError(String errorReason) {
                outputsFromCallbacks.add(errorReason);
                downLatch.countDown();
            }
        };

        uiThreadTestRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mapBoxOfflineResourcesDownloader = MapBoxOfflineResourcesDownloader.getInstance(null, Mapbox.getInstance(context, BuildConfig.MAPBOX_SDK_ACCESS_TOKEN));
            }
        });
        mapBoxOfflineResourcesDownloader.getIncompleteMapDownloads(incompleteMapDownloadCallback);

        assertEquals("Context passed is null", outputsFromCallbacks.get(0));
    }

    @Test
    public void deletePreviousOfflineMapDownloadsShouldDeleteMapAndReserveCurrentMap() throws Throwable {
        downLatch = new CountDownLatch(1);
        resetTestVariables();

        createMapboxOfflineResourcesDownloaderInstanceOnUIThread();

        // Replace the OfflineManager in the MapBoxOfflineResourcesDownloader with the Spied one
        OfflineManager spiedOfflineManager = Mockito.spy(mapBoxOfflineResourcesDownloader.offlineManager);
        mapBoxOfflineResourcesDownloader.offlineManager = spiedOfflineManager;

        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                OfflineManager.CreateOfflineRegionCallback createOfflineRegionCallback = invocationOnMock.getArgument(2);

                //Should never be null --> CreateOfflineRegionCallback
                byte[] metadata = invocationOnMock.getArgument(1);
                OfflineRegionDefinition offlineRegionDefinition = invocationOnMock.getArgument(0);
                offlineRegionsMetadataList.add(metadata);

                OfflineRegion offlineRegion = createMockOfflineRegion(metadata, offlineRegionDefinition, generateOfflineRegionId());

                offlineRegionsList.add(offlineRegion);
                createOfflineRegionCallback.onCreate(offlineRegion);

                // Reflect to get the download state value
                Class offlineRegionClass = Class.forName("com.mapbox.mapboxsdk.offline.OfflineRegion");
                Field stateField = offlineRegionClass.getDeclaredField("state");
                stateField.setAccessible(true);

                return null;
            }
        }).when(spiedOfflineManager)
                .createOfflineRegion(Mockito.any(OfflineRegionDefinition.class), Mockito.any(byte[].class), Mockito.any(OfflineManager.CreateOfflineRegionCallback.class));

        // Mock OfflineManager.listOfflineRegions calls
        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                OfflineManager.ListOfflineRegionsCallback listOfflineRegionsCallback = invocationOnMock.getArgument(0);
                listOfflineRegionsCallback.onList(offlineRegionsList.toArray(new OfflineRegion[offlineRegionsList.size()]));

                return null;
            }
        }).when(spiedOfflineManager)
                .listOfflineRegions(Mockito.any(OfflineManager.ListOfflineRegionsCallback.class));

        long idToExclude = 7;
        int iterations = 10;

        String myMapName = UUID.randomUUID().toString();
        ArrayList<Integer> indexesToGiveDifferentMapName = new ArrayList();
        indexesToGiveDifferentMapName.add(2);
        indexesToGiveDifferentMapName.add(5);
        indexesToGiveDifferentMapName.add(6);
        indexesToGiveDifferentMapName.add(9);

        for(int i = 0; i < iterations; i++) {
            MapBoxDownloadTask validMapBoxDownloadTask;

            if (indexesToGiveDifferentMapName.contains(i)) {
                validMapBoxDownloadTask = createSampleDownloadTask(UUID.randomUUID().toString());
            } else {
                validMapBoxDownloadTask = createSampleDownloadTask(myMapName);
            }

            mapBoxOfflineResourcesDownloader.downloadMap(validMapBoxDownloadTask, null);
        }

        mapBoxOfflineResourcesDownloader.deletePreviousOfflineMapDownloads(myMapName, idToExclude);

        mapBoxOfflineResourcesDownloader.offlineManager.listOfflineRegions(new OfflineManager.ListOfflineRegionsCallback() {
            @Override
            public void onList(OfflineRegion[] offlineRegions) {
                outputsFromCallbacks.add(offlineRegions.length);
                downLatch.countDown();
            }

            @Override
            public void onError(String error) {
                outputsFromCallbacks.add(-1);
                downLatch.countDown();
            }
        });

        assertEquals(5, outputsFromCallbacks.get(0));
    }

    /*
    |
    | HELPER METHODS
    |
    */

    private MapBoxDownloadTask createSampleDownloadTask(String mapName) {

        return new MapBoxDownloadTask(
                "kl",
                mapName,
                "mapbox://styles/ona/cj9jueph7034i2rphe0gp3o6m",
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

    private MapBoxDownloadTask createSampleDownloadTask() {
        return createSampleDownloadTask(sampleMapName);
    }

    private OfflineRegion createMockOfflineRegion(byte[] metadata, OfflineRegionDefinition offlineRegionDefinition, long id) {
        OfflineRegion offlineRegion = createMockOfflineRegion(metadata, offlineRegionDefinition);
        Mockito.when(offlineRegion.getID())
                .thenReturn(id);

        return offlineRegion;
    }

    private OfflineRegion createMockOfflineRegion(byte[] metadata, OfflineRegionDefinition offlineRegionDefinition) {
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
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {

                // Remove the offline region from the list
                offlineRegionsMetadataList.remove(offlineRegion.getMetadata());
                offlineRegionsList.remove(offlineRegion);

                // Just call the callback directly if gotten to this point i.e. The OfflineRegion is not null
                OfflineRegion.OfflineRegionDeleteCallback offlineRegionDeleteCallback = invocationOnMock.getArgument(0);
                offlineRegionDeleteCallback.onDelete();

                return null;
            }
        }).when(offlineRegion)
                .delete(Mockito.any(OfflineRegion.OfflineRegionDeleteCallback.class));

        return offlineRegion;
    }

    private void resetTestVariables() {
        outputsFromCallbacks.clear();

        offlineRegionsMetadataList.clear();
        offlineRegionsList.clear();
    }

    private long generateOfflineRegionId() {
        return lastId++;
    }

}