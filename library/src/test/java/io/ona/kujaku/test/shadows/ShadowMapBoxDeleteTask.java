package io.ona.kujaku.test.shadows;

import android.support.annotation.NonNull;
import android.util.Log;

import org.json.JSONException;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

import java.util.Date;

import io.ona.kujaku.data.MapBoxDeleteTask;
import io.ona.kujaku.data.realm.objects.MapBoxOfflineQueueTask;
import io.ona.kujaku.test.shadows.implementations.RealmDbTestImplementation;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 22/12/2017.
 */

@Implements(MapBoxDeleteTask.class)
public class ShadowMapBoxDeleteTask {

    @Implementation
    public static MapBoxOfflineQueueTask constructMapBoxOfflineQueueTask(@NonNull MapBoxDeleteTask mapBoxDeleteTask) {
        try {
            MapBoxOfflineQueueTask mapBoxOfflineQueueTask = new MapBoxOfflineQueueTask();
            mapBoxOfflineQueueTask.setDateCreated(new Date());
            mapBoxOfflineQueueTask.setDateUpdated(new Date());
            mapBoxOfflineQueueTask.setTask(mapBoxDeleteTask.getJSONObject());
            mapBoxOfflineQueueTask.setTaskStatus(MapBoxOfflineQueueTask.TASK_STATUS_NOT_STARTED);
            mapBoxOfflineQueueTask.setTaskType(MapBoxOfflineQueueTask.TASK_TYPE_DELETE);

            RealmDbTestImplementation.add(mapBoxOfflineQueueTask.getId(), mapBoxOfflineQueueTask);

            return mapBoxOfflineQueueTask;
        } catch (JSONException e) {
            Log.e("ShadowMapBoxDeleteTask", Log.getStackTraceString(e));

            return null;
        }
    }
}
