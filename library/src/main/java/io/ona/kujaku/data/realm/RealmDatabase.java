package io.ona.kujaku.data.realm;

import android.content.Context;
import android.support.annotation.NonNull;

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

    protected RealmResults<MapBoxOfflineQueueTask> getPendingOfflineMapDownloadsWithSimilarNames(String mapName) {
        Realm realm = Realm.getDefaultInstance();

        RealmResults<MapBoxOfflineQueueTask> mapBoxOfflineQueueTaskRealmResults = realm.where(MapBoxOfflineQueueTask.class)
                .equalTo("taskType", MapBoxOfflineQueueTask.TASK_TYPE_DOWNLOAD)
                .equalTo("taskStatus", MapBoxOfflineQueueTask.TASK_STATUS_NOT_STARTED)
                .findAll();

        return mapBoxOfflineQueueTaskRealmResults;
    }

    public boolean deletePendingOfflineMapDownloadsWithSimilarNames(String mapName) {
        RealmResults<MapBoxOfflineQueueTask> realmResults = getPendingOfflineMapDownloadsWithSimilarNames(mapName);

        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();

        boolean isDeleted = realmResults.deleteAllFromRealm();

        realm.commitTransaction();

        return isDeleted;
    }
}
