package io.ona.kujaku.data.realm;

import android.content.Context;
import android.support.annotation.NonNull;

import com.mapbox.mapboxsdk.offline.OfflineRegion;

import io.ona.kujaku.data.realm.objects.MapBoxOfflineQueueTask;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

/**
 * Created by Jason Rogena - jrogena@ona.io on 11/20/17.
 */
public class RealmDatabase {
    protected static final long VERSION = 1l;
    protected static final String NAME = "kujaku.realm";
    private static RealmDatabase realmDatabase;
    private final Context context;

    public static RealmDatabase init(@NonNull Context context) {
        if (realmDatabase == null) {
            realmDatabase = new RealmDatabase(context);
        }

        return realmDatabase;
    }

    private RealmDatabase(Context context) {
        this.context = context;
        Realm.init(context.getApplicationContext());
        RealmConfiguration configuration = new RealmConfiguration.Builder()
                .name(NAME)
                .schemaVersion(VERSION)
                .build();
        Realm.setDefaultConfiguration(configuration);
    }

    public boolean deleteTask(@NonNull String mapName, boolean isDownloadTask) {
        Realm realm = Realm.getDefaultInstance();
        MapBoxOfflineQueueTask taskToDelete;
        String taskType = isDownloadTask ? MapBoxOfflineQueueTask.TASK_TYPE_DOWNLOAD : MapBoxOfflineQueueTask.TASK_TYPE_DELETE;

        taskToDelete = realm.where(MapBoxOfflineQueueTask.class)
                .equalTo("taskType", taskType)
                .contains("task", mapName)
                .findFirst();

        if (taskToDelete != null) {
            realm.beginTransaction();
            taskToDelete.deleteFromRealm();
            realm.commitTransaction();

            realm.close();

            return true;
        } else {
            return false;
        }
    }

    /**
     * Saves a {@link MapBoxOfflineQueueTask} as {@link MapBoxOfflineQueueTask#TASK_STATUS_DONE}<br/>
     * This means the {@link OfflineRegion} can no longer be resumed if it was incomplete.<br/>
     * This also means that a {@link OfflineRegion} will still be in storage if it was not successfully deleted
     *
     * @param mapBoxOfflineQueueTask
     */
    public void persistCompletedStatus(@NonNull MapBoxOfflineQueueTask mapBoxOfflineQueueTask) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();

        mapBoxOfflineQueueTask.setTaskStatus(MapBoxOfflineQueueTask.TASK_STATUS_DONE);
        realm.commitTransaction();
    }

    /**
     * Updates the {@link MapBoxOfflineQueueTask} status as {@link MapBoxOfflineQueueTask#TASK_STATUS_NOT_STARTED}<br/>
     * This means that subsequent Offline Region download calls with a similar map name will not result in this task being deleted.
     *
     * @param mapBoxOfflineQueueTask
     */
    public void persistDownloadStartedStatus(@NonNull MapBoxOfflineQueueTask mapBoxOfflineQueueTask) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        mapBoxOfflineQueueTask.setTaskStatus(MapBoxOfflineQueueTask.TASK_STATUS_STARTED);

        realm.commitTransaction();
    }

    /**
     * Retrieves the {@link MapBoxOfflineQueueTask}s of type {@link MapBoxOfflineQueueTask#TASK_TYPE_DOWNLOAD}
     * which are pending i.e. their downloads have not started. Their status is {@link MapBoxOfflineQueueTask#TASK_STATUS_NOT_STARTED}
     *
     * @param mapName
     * @return
     */
    protected RealmResults<MapBoxOfflineQueueTask> getPendingOfflineMapDownloadsWithSimilarNames(String mapName) {
        Realm realm = Realm.getDefaultInstance();

        RealmResults<MapBoxOfflineQueueTask> mapBoxOfflineQueueTaskRealmResults = realm.where(MapBoxOfflineQueueTask.class)
                .equalTo("taskType", MapBoxOfflineQueueTask.TASK_TYPE_DOWNLOAD)
                .equalTo("taskStatus", MapBoxOfflineQueueTask.TASK_STATUS_NOT_STARTED)
                .findAll();

        return mapBoxOfflineQueueTaskRealmResults;
    }

    /**
     * Deletes all {@link MapBoxOfflineQueueTask}s of type {@link MapBoxOfflineQueueTask#TASK_TYPE_DOWNLOAD}
     * whose download has not started
     *
     * @param mapName
     * @return
     */
    public boolean deletePendingOfflineMapDownloadsWithSimilarNames(String mapName) {
        RealmResults<MapBoxOfflineQueueTask> realmResults = getPendingOfflineMapDownloadsWithSimilarNames(mapName);

        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();

        boolean isDeleted = realmResults.deleteAllFromRealm();

        realm.commitTransaction();

        return isDeleted;
    }
}
