package io.ona.kujaku.shadows;

import android.support.annotation.NonNull;
import android.util.Log;

import org.json.JSONException;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

import java.util.Date;

import io.ona.kujaku.data.MapBoxDownloadTask;
import io.ona.kujaku.data.realm.objects.MapBoxOfflineQueueTask;
import io.ona.kujaku.shadows.implementations.RealmDbTestImplementation;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 22/12/2017.
 */

@Implements(MapBoxDownloadTask.class)
public class ShadowMapBoxDownloadTask {

    @Implementation
    public static MapBoxOfflineQueueTask constructMapBoxOfflineQueueTask(@NonNull MapBoxDownloadTask mapBoxDownloadTask) {
        try {
            MapBoxOfflineQueueTask mapBoxOfflineQueueTask = new MapBoxOfflineQueueTask();
            mapBoxOfflineQueueTask.setDateCreated(new Date());
            mapBoxOfflineQueueTask.setDateUpdated(new Date());
            mapBoxOfflineQueueTask.setTask(mapBoxDownloadTask.getJSONObject());
            mapBoxOfflineQueueTask.setTaskStatus(MapBoxOfflineQueueTask.TASK_STATUS_INCOMPLETE);
            mapBoxOfflineQueueTask.setTaskType(MapBoxOfflineQueueTask.TASK_TYPE_DOWNLOAD);

            RealmDbTestImplementation.add(mapBoxOfflineQueueTask.getId(), mapBoxOfflineQueueTask);

            return mapBoxOfflineQueueTask;
        } catch (JSONException e) {
            Log.e("MapBoxDownloadTaskShado", Log.getStackTraceString(e));

            return null;
        }
    }
}
