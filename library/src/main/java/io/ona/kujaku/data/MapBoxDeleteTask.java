package io.ona.kujaku.data;

import androidx.annotation.NonNull;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.UUID;

import io.ona.kujaku.data.realm.objects.MapBoxOfflineQueueTask;
import io.realm.Realm;
import io.ona.kujaku.utils.exceptions.MalformedDataException;

/**
 * Holds an Offline Map delete task definition<br/>
 * Holds the:
 * <ul>
 *          <li>mapName - Unique name of the map</li>
 *          <li>mapBoxAccessToken - Token with which the download was made</li>
 * </ul>
 *
 * Created by Ephraim Kigamba - ekigamba@ona.io on 20/11/2017.
 */

public class MapBoxDeleteTask {

    private String mapName;
    private String mapBoxAccessToken;
    private static final String TAG = MapBoxDeleteTask.class.getSimpleName();

    public static final String MAP_NAME = "mapName"
            , MAP_BOX_ACCESS_TOKEN = "mapBoxAccessToken";

    public MapBoxDeleteTask() {}

    public MapBoxDeleteTask(@NonNull String mapName,@NonNull String mapBoxAccessToken) {
        this.mapName = mapName;
        this.mapBoxAccessToken = mapBoxAccessToken;
    }

    public MapBoxDeleteTask(JSONObject jsonObject) throws MalformedDataException {
        try {
            this.mapName = jsonObject.getString(MAP_NAME);
            this.mapBoxAccessToken = jsonObject.getString(MAP_BOX_ACCESS_TOKEN);
        } catch (JSONException e) {
            Log.e(TAG, Log.getStackTraceString(e));
            throw new MalformedDataException("Invalid Delete Task definition", e);
        }
    }

    public String getMapName() {
        return mapName;
    }

    public void setMapName(String mapName) {
        this.mapName = mapName;
    }

    public String getMapBoxAccessToken() {
        return mapBoxAccessToken;
    }

    public void setMapBoxAccessToken(String mapBoxAccessToken) {
        this.mapBoxAccessToken = mapBoxAccessToken;
    }

    /**
     * Generates the {@link JSONObject} of the task that will be saved in {@link MapBoxOfflineQueueTask#task}
     *
     * @return
     * @throws JSONException
     */
    public JSONObject getJSONObject() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(MAP_NAME, mapName);
        jsonObject.put(MAP_BOX_ACCESS_TOKEN, mapBoxAccessToken);

        return jsonObject;
    }

    /**
     * Creates a valid {@link MapBoxOfflineQueueTask} given a {@link MapBoxDeleteTask} with default
     * {@link MapBoxOfflineQueueTask#taskStatus} as {@link MapBoxOfflineQueueTask#TASK_STATUS_NOT_STARTED}
     * of type {@link MapBoxOfflineQueueTask#TASK_TYPE_DELETE}
     *
     * @param mapBoxDeleteTask
     * @return
     */
    public static MapBoxOfflineQueueTask constructMapBoxOfflineQueueTask(@NonNull MapBoxDeleteTask mapBoxDeleteTask) {
        Realm realm = Realm.getDefaultInstance();
        try {
            realm.beginTransaction();

            MapBoxOfflineQueueTask mapBoxOfflineQueueTask = realm.createObject(MapBoxOfflineQueueTask.class, UUID.randomUUID().toString());
            mapBoxOfflineQueueTask.setDateCreated(new Date());
            mapBoxOfflineQueueTask.setDateUpdated(new Date());
            mapBoxOfflineQueueTask.setTask(mapBoxDeleteTask.getJSONObject());
            mapBoxOfflineQueueTask.setTaskStatus(MapBoxOfflineQueueTask.TASK_STATUS_NOT_STARTED);
            mapBoxOfflineQueueTask.setTaskType(MapBoxOfflineQueueTask.TASK_TYPE_DELETE);

            realm.commitTransaction();

            return mapBoxOfflineQueueTask;
        } catch (Exception e){
            realm.cancelTransaction();
            return null;
        }
    }
}
