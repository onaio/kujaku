package io.ona.kujaku.data.realm;

import com.mapbox.mapboxsdk.geometry.LatLng;

import org.json.JSONException;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

import io.ona.kujaku.BuildConfig;
import io.ona.kujaku.data.MapBoxDeleteTask;
import io.ona.kujaku.data.MapBoxDownloadTask;
import io.ona.kujaku.data.realm.objects.MapBoxOfflineQueueTask;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 29/12/2017.
 */
public class RealmDatabaseTest extends RealmRelatedInstrumentedTest {

    private String sampleMapBoxStyleURL = "mapbox://styles/user/i89lkjscd";

    @Override
    public void setUp() throws Exception {
        super.setUp();
        insertValueInPrivateStaticField(RealmDatabase.class, "realmDatabase", null);
    }

    @Test
    public void initShouldCreateNewInstance() throws NoSuchFieldException, IllegalAccessException {
        Object object = getValueInPrivateField(RealmDatabase.class, null, "realmDatabase");
        assertEquals(null, object);

        RealmDatabase realmDatabase = RealmDatabase.init(context);

        Object newValue = getValueInPrivateField(RealmDatabase.class, null, "realmDatabase");
        assertTrue(newValue != null);
        assertTrue(realmDatabase != null);
    }

    @Test
    public void initShouldNotCreateNewInstanceWhenSingletonAlreadyExists() throws NoSuchFieldException, IllegalAccessException {
        RealmDatabase expectedRealmDatabase = RealmDatabase.init(context);

        Object object = getValueInPrivateField(RealmDatabase.class, null, "realmDatabase");
        assertTrue(expectedRealmDatabase != null);
        assertEquals(object, expectedRealmDatabase);

        RealmDatabase realmDatabase = RealmDatabase.init(context);
        assertEquals(expectedRealmDatabase, realmDatabase);
    }

    @Test
    public void constructorShouldCreateValidConfigurationWhenGivenContext() {
        RealmDatabase.init(context);

        Realm realm = Realm.getDefaultInstance();
        RealmConfiguration realmConfiguration = realm.getConfiguration();

        assertEquals(RealmDatabase.NAME, realmConfiguration.getRealmFileName());
        assertEquals(RealmDatabase.VERSION, realmConfiguration.getSchemaVersion());
    }

    @Test
    public void getTasksShouldReturnCurrentRecordsWithAddedRecords() throws JSONException {
        Realm.init(context);

        int downloadAndDeleteTasksLen = 30;
        MapBoxOfflineQueueTask[] tasks = new MapBoxOfflineQueueTask[downloadAndDeleteTasksLen];
        String packageName = "com.sample.sub";

        for(int i = 0; i < downloadAndDeleteTasksLen; i++) {
            boolean isDownload = ((int) (Math.random() * 2)) == 1;

            String mapName = UUID.randomUUID().toString();

            MapBoxOfflineQueueTask mapBoxOfflineQueueTask;
            if (isDownload) {
                MapBoxDownloadTask mapBoxDownloadTask = createSampleDownloadTask(packageName, mapName, sampleMapBoxStyleURL);
                mapBoxOfflineQueueTask = MapBoxDownloadTask.constructMapBoxOfflineQueueTask(mapBoxDownloadTask);
            } else {
                MapBoxDeleteTask mapBoxDeleteTask = new MapBoxDeleteTask(mapName, BuildConfig.MAPBOX_SDK_ACCESS_TOKEN);
                mapBoxOfflineQueueTask = MapBoxDeleteTask.constructMapBoxOfflineQueueTask(mapBoxDeleteTask);
            }

            addedRecords.add(mapBoxOfflineQueueTask);
            tasks[i] = mapBoxOfflineQueueTask;
        }

        RealmResults<MapBoxOfflineQueueTask> realmResults = RealmDatabase.init(context)
                .getTasks();

        for(int i = 0; i < downloadAndDeleteTasksLen; i++) {
            MapBoxOfflineQueueTask task = tasks[i];

            boolean found = false;
            for(MapBoxOfflineQueueTask mapBoxOfflineQueueTask: realmResults) {
                if (mapBoxOfflineQueueTask.getTask().toString().equals(task.getTask().toString())) {

                    assertEquals(task.getDateCreated().getTime(), mapBoxOfflineQueueTask.getDateCreated().getTime());
                    assertEquals(task.getDateUpdated().getTime(), mapBoxOfflineQueueTask.getDateUpdated().getTime());
                    assertEquals(task.getId(), mapBoxOfflineQueueTask.getId());
                    assertEquals(task.getTaskStatus(), mapBoxOfflineQueueTask.getTaskStatus());
                    assertEquals(task.getTaskType(), mapBoxOfflineQueueTask.getTaskType());

                    found = true;
                    break;
                }
            }

            assertTrue(found);
        }
    }

    @Test
    public void deleteTaskShouldReturnFalseWhenGivenNonExistentTask() {
        RealmDatabase realmDatabase = RealmDatabase.init(context);

        boolean isDeleted = realmDatabase.deleteTask("klskdf", false);
        assertFalse(isDeleted);

        isDeleted = realmDatabase.deleteTask("klkjsdf", true);
        assertFalse(isDeleted);
    }

    @Test
    public void deleteTaskShouldReturnFalseWhenGivenWrongTaskTypeWithExistentTask() {
        RealmDatabase realmDatabase = RealmDatabase.init(context);

        String downloadMapName = UUID.randomUUID().toString();
        String deleteMapName = UUID.randomUUID().toString();
        MapBoxDownloadTask mapBoxDownloadTask = createSampleDownloadTask("kl", downloadMapName, sampleMapBoxStyleURL);
        addedRecords.add(MapBoxDownloadTask.constructMapBoxOfflineQueueTask(mapBoxDownloadTask));

        MapBoxDeleteTask mapBoxDeleteTask = new MapBoxDeleteTask(deleteMapName, sampleMapBoxStyleURL);
        addedRecords.add(MapBoxDeleteTask.constructMapBoxOfflineQueueTask(mapBoxDeleteTask));

        assertFalse(realmDatabase.deleteTask(downloadMapName, false));
        assertFalse(realmDatabase.deleteTask(deleteMapName, true));
    }

    @Test
    public void deleteTaskShouldShouldReturnTrueWhenGivenExistentTasks() {
        RealmDatabase realmDatabase = RealmDatabase.init(context);

        String downloadMapName = UUID.randomUUID().toString();
        String deleteMapName = UUID.randomUUID().toString();
        MapBoxDownloadTask mapBoxDownloadTask = createSampleDownloadTask("kl", downloadMapName, sampleMapBoxStyleURL);
        MapBoxDownloadTask.constructMapBoxOfflineQueueTask(mapBoxDownloadTask);

        MapBoxDeleteTask mapBoxDeleteTask = new MapBoxDeleteTask(deleteMapName, sampleMapBoxStyleURL);
        MapBoxDeleteTask.constructMapBoxOfflineQueueTask(mapBoxDeleteTask);

        assertTrue(realmDatabase.deleteTask(downloadMapName, true));
        assertFalse(realmDatabase.deleteTask(downloadMapName, true));

        assertTrue(realmDatabase.deleteTask(deleteMapName, false));
        assertFalse(realmDatabase.deleteTask(deleteMapName, false));
    }

    @Test
    public void persistCompletedStatus() {
        RealmDatabase realmDatabase = RealmDatabase.init(context);

        String downloadMapName = UUID.randomUUID().toString();

        MapBoxDownloadTask mapBoxDownloadTask = createSampleDownloadTask("kl", downloadMapName, sampleMapBoxStyleURL);
        MapBoxOfflineQueueTask mapBoxOfflineQueueTask = MapBoxDownloadTask.constructMapBoxOfflineQueueTask(mapBoxDownloadTask);
        addedRecords.add(mapBoxOfflineQueueTask);

        realmDatabase.persistCompletedStatus(mapBoxOfflineQueueTask);

        assertEquals(MapBoxOfflineQueueTask.TASK_STATUS_DONE, mapBoxOfflineQueueTask.getTaskStatus());
    }

    @Test
    public void persistDownloadStartedStatus() {
        RealmDatabase realmDatabase = RealmDatabase.init(context);

        String downloadMapName = UUID.randomUUID().toString();

        MapBoxDownloadTask mapBoxDownloadTask = createSampleDownloadTask("kl", downloadMapName, sampleMapBoxStyleURL);
        MapBoxOfflineQueueTask mapBoxOfflineQueueTask = MapBoxDownloadTask.constructMapBoxOfflineQueueTask(mapBoxDownloadTask);
        addedRecords.add(mapBoxOfflineQueueTask);

        realmDatabase.persistDownloadStartedStatus(mapBoxOfflineQueueTask);

        assertEquals(MapBoxOfflineQueueTask.TASK_STATUS_STARTED, mapBoxOfflineQueueTask.getTaskStatus());
    }

    @Test
    public void getPendingOfflineMapDownloadsWithSimilarNames() {
        RealmDatabase realmDatabase = RealmDatabase.init(context);

        String downloadMapName = UUID.randomUUID().toString();

        MapBoxDownloadTask mapBoxDownloadTask = createSampleDownloadTask("kl", downloadMapName, sampleMapBoxStyleURL);
        MapBoxOfflineQueueTask mapBoxOfflineQueueTask = MapBoxDownloadTask.constructMapBoxOfflineQueueTask(mapBoxDownloadTask);

        MapBoxDownloadTask mapBoxDownloadTask1 = createSampleDownloadTask("kl", downloadMapName, sampleMapBoxStyleURL);
        MapBoxOfflineQueueTask mapBoxOfflineQueueTask1 = MapBoxDownloadTask.constructMapBoxOfflineQueueTask(mapBoxDownloadTask1);

        addedRecords.add(mapBoxOfflineQueueTask);
        addedRecords.add(mapBoxOfflineQueueTask1);

        RealmResults<MapBoxOfflineQueueTask> realmResults = realmDatabase.getPendingOfflineMapDownloadsWithSimilarNames(downloadMapName);

        assertEquals(2, realmResults.size());
    }

    @Test
    public void deletePendingOfflineMapDownloadsWithSimilarNames() {
        RealmDatabase realmDatabase = RealmDatabase.init(context);

        String downloadMapName = UUID.randomUUID().toString();

        MapBoxDownloadTask mapBoxDownloadTask = createSampleDownloadTask("kl", downloadMapName, sampleMapBoxStyleURL);
        MapBoxOfflineQueueTask mapBoxOfflineQueueTask = MapBoxDownloadTask.constructMapBoxOfflineQueueTask(mapBoxDownloadTask);

        MapBoxDownloadTask mapBoxDownloadTask1 = createSampleDownloadTask("kl", downloadMapName, sampleMapBoxStyleURL);
        MapBoxOfflineQueueTask mapBoxOfflineQueueTask1 = MapBoxDownloadTask.constructMapBoxOfflineQueueTask(mapBoxDownloadTask1);

        MapBoxDownloadTask mapBoxDownloadTask2 = createSampleDownloadTask("kl", downloadMapName, sampleMapBoxStyleURL);
        MapBoxOfflineQueueTask mapBoxOfflineQueueTask2 = MapBoxDownloadTask.constructMapBoxOfflineQueueTask(mapBoxDownloadTask2);

        MapBoxDownloadTask mapBoxDownloadTask3 = createSampleDownloadTask("kl", "Market curve", sampleMapBoxStyleURL);
        MapBoxOfflineQueueTask mapBoxOfflineQueueTask3 = MapBoxDownloadTask.constructMapBoxOfflineQueueTask(mapBoxDownloadTask3);

        realmDatabase.persistDownloadStartedStatus(mapBoxOfflineQueueTask1);

        addedRecords.add(mapBoxOfflineQueueTask);
        addedRecords.add(mapBoxOfflineQueueTask1);
        addedRecords.add(mapBoxOfflineQueueTask2);
        addedRecords.add(mapBoxOfflineQueueTask3);

        String id1 = mapBoxOfflineQueueTask.getId();
        String id2 = mapBoxOfflineQueueTask2.getId();

        boolean isDeleted = realmDatabase.deletePendingOfflineMapDownloadsWithSimilarNames(downloadMapName);

        assertTrue(isDeleted);

        Realm realm = Realm.getDefaultInstance();

        RealmResults<MapBoxOfflineQueueTask> realmResults = realm.where(MapBoxOfflineQueueTask.class)
                .equalTo("id", id1)
                .or()
                .equalTo("id", id2)
                .findAll();

        assertEquals(0, realmResults.size());

        realmResults = realm.where(MapBoxOfflineQueueTask.class)
                .equalTo("id", mapBoxOfflineQueueTask1.getId())
                .or()
                .equalTo("id", mapBoxOfflineQueueTask3.getId())
                .findAll();

        assertEquals(2, realmResults.size());
    }

    @Test
    public void getNextTaskShouldReturnValidTask() throws JSONException {
        RealmDatabase realmDatabase = RealmDatabase.init(context);

        // First change the status for the current next tasks unit there is not other
        ArrayList<Object[]> currentNotStartedTasks = new ArrayList();
        Realm realm = Realm.getDefaultInstance();

        while (true) {
            MapBoxOfflineQueueTask nextTask = realmDatabase.getNextTask();

            if (nextTask == null) {
                break;
            } else {
                currentNotStartedTasks.add(new Object[]{nextTask.getTaskStatus(), nextTask.getId(), nextTask});

                realm.beginTransaction();
                nextTask.setTaskStatus(MapBoxOfflineQueueTask.TASK_STATUS_DONE);
                realm.commitTransaction();
            }
        }

        MapBoxOfflineQueueTask deleteTask = MapBoxDeleteTask.constructMapBoxOfflineQueueTask(new MapBoxDeleteTask(UUID.randomUUID().toString(), UUID.randomUUID().toString()));
        realm.beginTransaction();
        deleteTask.setTaskStatus(MapBoxOfflineQueueTask.TASK_STATUS_STARTED);
        realm.commitTransaction();
        addedRecords.add(deleteTask);

        String mapName = UUID.randomUUID().toString();
        MapBoxDownloadTask mapBoxDownloadTask = createSampleDownloadTask("com.android.developer", mapName, sampleMapBoxStyleURL);
        MapBoxOfflineQueueTask expectedNextTask = MapBoxDownloadTask.constructMapBoxOfflineQueueTask(mapBoxDownloadTask);

        MapBoxOfflineQueueTask completedTask = MapBoxDownloadTask.constructMapBoxOfflineQueueTask(createSampleDownloadTask("com.android.developer", UUID.randomUUID().toString(), sampleMapBoxStyleURL));
        MapBoxOfflineQueueTask startedTask = MapBoxDownloadTask.constructMapBoxOfflineQueueTask(createSampleDownloadTask("com.android.developer", UUID.randomUUID().toString(), sampleMapBoxStyleURL));

        addedRecords.add(expectedNextTask);
        addedRecords.add(completedTask);
        addedRecords.add(startedTask);

        realm.beginTransaction();

        completedTask.setTaskStatus(MapBoxOfflineQueueTask.TASK_STATUS_DONE);
        startedTask.setTaskStatus(MapBoxOfflineQueueTask.TASK_STATUS_STARTED);

        realm.commitTransaction();

        MapBoxOfflineQueueTask nextTask = realmDatabase.getNextTask();

        assertEquals(expectedNextTask.getId(), nextTask.getId());
        assertEquals(expectedNextTask.getTask().toString(), nextTask.getTask().toString());
        assertEqualsDate(expectedNextTask.getDateCreated(), nextTask.getDateCreated());
        assertEqualsDate(expectedNextTask.getDateUpdated(), expectedNextTask.getDateUpdated());

        //Revert the task status
        for(Object[] taskSummary: currentNotStartedTasks) {
            int taskStatus = (int) taskSummary[0];

            MapBoxOfflineQueueTask task = (MapBoxOfflineQueueTask) taskSummary[2];
            realm.beginTransaction();
            task.setTaskStatus(taskStatus);
            realm.commitTransaction();
        }

    }

    /*
    ---------------------------

    HELPER METHODS

    ---------------------------
     */

    private void assertEqualsDate(Date expectedDate, Date actualDate) {
        assertEquals(expectedDate.getTime(), actualDate.getTime());
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
                sampleMapBoxStyleURL
        );
    }
}