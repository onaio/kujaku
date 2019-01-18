package io.ona.kujaku.data.realm;

import org.junit.Assert;
import org.junit.Test;

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import io.ona.kujaku.data.MapBoxDeleteTask;
import io.ona.kujaku.data.realm.objects.MapBoxOfflineQueueTask;
import io.realm.Realm;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 15/01/2018.
 */

public class MapBoxDeleteTaskInstrumentedTest extends RealmRelatedInstrumentedTest {

    @Override
    public void setUp() throws Exception {
        super.setUp();
        Realm.init(context);
    }

    @Test
    public void constructMapBoxOfflineQueueTaskShouldSaveInRealm() {
        String mapName = UUID.randomUUID().toString();
        String mapboxAccessToken = "sample_token";
        MapBoxDeleteTask mapBoxDeleteTask = new MapBoxDeleteTask(mapName, mapboxAccessToken);

        Date timeNow = Calendar.getInstance().getTime();
        MapBoxOfflineQueueTask mapBoxOfflineQueueTask = MapBoxDeleteTask.constructMapBoxOfflineQueueTask(mapBoxDeleteTask);
        addedRecords.add(mapBoxOfflineQueueTask);

        Realm realm = Realm.getDefaultInstance();

        MapBoxOfflineQueueTask queryResultTask = realm.where(MapBoxOfflineQueueTask.class)
                .contains("task", mapboxAccessToken)
                .contains("task", mapName)
                .equalTo("taskStatus", MapBoxOfflineQueueTask.TASK_STATUS_NOT_STARTED)
                .equalTo("taskType", MapBoxOfflineQueueTask.TASK_TYPE_DELETE)
                .findFirst();

        Assert.assertTrue(queryResultTask != null);
        Assert.assertTrue((queryResultTask.getDateCreated().getTime() - timeNow.getTime()) < 1000);
        Assert.assertTrue((queryResultTask.getDateUpdated().getTime() - timeNow.getTime()) < 1000);
    }
}
