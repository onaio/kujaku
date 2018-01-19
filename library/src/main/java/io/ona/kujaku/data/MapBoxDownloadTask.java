package io.ona.kujaku.data;

import android.support.annotation.NonNull;
import android.util.Log;

import com.mapbox.mapboxsdk.geometry.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.UUID;

import io.ona.kujaku.data.realm.objects.MapBoxOfflineQueueTask;
import io.realm.Realm;
import utils.exceptions.MalformedDataException;

/**
 * Stores/Carries data on Offline MapBox Style
 *
 * Created by Ephraim Kigamba - ekigamba@ona.io on 20/11/2017.
 * Created by Jason Rogena - jrogena@ona.io on 20/11/2017.
 */

public class MapBoxDownloadTask {
    private String packageName;
    private String mapName;
    private String mapBoxStyleUrl;
    private double minZoom;
    private double maxZoom;
    private LatLng topLeftBound;
    private LatLng bottomRightBound;
    private String mapBoxAccessToken;

    private JSONObject jsonObject;
    private static final String TAG = MapBoxDownloadTask.class.getSimpleName();

    public static final String PACKAGE_NAME = "packageName"
            , MAP_NAME = "mapName"
            , MAPBOX_STYLE_URL = "mapBoxStyleUrl"
            , MIN_ZOOM = "minZoom"
            , MAX_ZOOM = "maxZoom"
            , TOP_LEFT_BOUND = "topLeftBound"
            , BOTTOM_RIGHT_BOUND = "bottomRightBound"
            , MAPBOX_ACCESS_TOKEN = "mapBoxAccessToken";

    public static final String BOUND_LATITUDE = "lat"
            , BOUND_LONGITUDE = "lng";


    public MapBoxDownloadTask() {
    }

    public MapBoxDownloadTask(@NonNull String packageName,
                              @NonNull String mapName,
                              @NonNull String mapBoxStyleUrl,
                              double minZoom, double maxZoom,
                              @NonNull LatLng topLeftBound,
                              @NonNull LatLng bottomRightBound,
                              @NonNull String mapBoxAccessToken) {
        this.packageName = packageName;
        this.mapName = mapName;
        this.mapBoxStyleUrl = mapBoxStyleUrl;
        this.minZoom = minZoom;
        this.maxZoom = maxZoom;
        this.topLeftBound = topLeftBound;
        this.bottomRightBound = bottomRightBound;
        this.mapBoxAccessToken = mapBoxAccessToken;
    }

    public MapBoxDownloadTask(@NonNull JSONObject jsonObject) throws MalformedDataException {
        this.jsonObject = jsonObject;

        try {
            packageName = jsonObject.getString(PACKAGE_NAME);
            mapName = jsonObject.getString(MAP_NAME);
            mapBoxStyleUrl = jsonObject.getString(MAPBOX_STYLE_URL);
            mapBoxAccessToken = jsonObject.getString(MAPBOX_ACCESS_TOKEN);
            minZoom = jsonObject.getDouble(MIN_ZOOM);
            maxZoom = jsonObject.getDouble(MAX_ZOOM);

            topLeftBound = constructLatLng(jsonObject.getJSONObject(TOP_LEFT_BOUND));
            bottomRightBound = constructLatLng(jsonObject.getJSONObject(BOTTOM_RIGHT_BOUND));

        } catch (JSONException e) {
            Log.e(TAG, Log.getStackTraceString(e));
            throw new MalformedDataException("Invalid Download Task definition", e);
        }

    }

    /**
     * Generates the {@link JSONObject} of the task that will be saved in {@link MapBoxOfflineQueueTask#task}
     *
     * @return
     * @throws JSONException
     */
    public JSONObject getJSONObject() throws JSONException {
        if (jsonObject == null) {
            jsonObject = new JSONObject();
        }

        jsonObject.put(PACKAGE_NAME, packageName);
        jsonObject.put(MAP_NAME, mapName);
        jsonObject.put(MAPBOX_STYLE_URL, mapBoxStyleUrl);
        jsonObject.put(MAPBOX_ACCESS_TOKEN, mapBoxAccessToken);
        jsonObject.put(MIN_ZOOM, minZoom);
        jsonObject.put(MAX_ZOOM, maxZoom);
        jsonObject.put(TOP_LEFT_BOUND, constructLatLngJSONObject(topLeftBound));
        jsonObject.put(BOTTOM_RIGHT_BOUND, constructLatLngJSONObject(bottomRightBound));

        return jsonObject;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getMapName() {
        return mapName;
    }

    public void setMapName(String mapName) {
        this.mapName = mapName;
    }

    public String getMapBoxStyleUrl() {
        return mapBoxStyleUrl;
    }

    public void setMapBoxStyleUrl(String mapBoxStyleUrl) {
        this.mapBoxStyleUrl = mapBoxStyleUrl;
    }

    public double getMinZoom() {
        return minZoom;
    }

    public void setMinZoom(double minZoom) {
        this.minZoom = minZoom;
    }

    public double getMaxZoom() {
        return maxZoom;
    }

    public void setMaxZoom(double maxZoom) {
        this.maxZoom = maxZoom;
    }

    public LatLng getTopLeftBound() {
        return topLeftBound;
    }

    public void setTopLeftBound(LatLng topLeftBound) {
        this.topLeftBound = topLeftBound;
    }

    public LatLng getBottomRightBound() {
        return bottomRightBound;
    }

    public void setBottomRightBound(LatLng bottomRightBound) {
        this.bottomRightBound = bottomRightBound;
    }

    public String getMapBoxAccessToken() {
        return mapBoxAccessToken;
    }

    public void setMapBoxAccessToken(String mapBoxAccessToken) {
        this.mapBoxAccessToken = mapBoxAccessToken;
    }

    /**
     * Converts {@link LatLng} to JSONObject so that it can be stored in
     * {@link MapBoxDownloadTask#bottomRightBound} & {@link MapBoxDownloadTask#topLeftBound}
     *
     * @param latLng to convert to {@link JSONObject}
     * @return {@link JSONObject} with the latitude & longitude from {@link LatLng}
     * @throws JSONException
     */
    public static JSONObject constructLatLngJSONObject(LatLng latLng) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(BOUND_LATITUDE, latLng.getLatitude());
        jsonObject.put(BOUND_LONGITUDE, latLng.getLongitude());

        return jsonObject;
    }

    /**
     * Converts {@link JSONObject} to {@link LatLng}. The LatLng is used as {@link JSONObject}
     * inside {@link MapBoxDownloadTask#topLeftBound} & {@link MapBoxDownloadTask#bottomRightBound}
     *
     * @param jsonObject {@link JSONObject} to convert
     * @return similar {@link LatLng} to {@code jsonObject} passed
     * @throws JSONException
     */
    public static LatLng constructLatLng(JSONObject jsonObject) throws JSONException {
        return new LatLng(jsonObject.getDouble(BOUND_LATITUDE), jsonObject.getDouble(BOUND_LONGITUDE));
    }

    /**
     * Creates a valid {@link MapBoxOfflineQueueTask} given a {@link MapBoxDownloadTask} with default
     * {@link MapBoxOfflineQueueTask#taskStatus} = {@link MapBoxOfflineQueueTask#TASK_STATUS_NOT_STARTED}
     * & adds it to the queue
     *
     * @param mapBoxDownloadTask to add to the queue
     * @return the Queued MapBox Task {@link MapBoxOfflineQueueTask}
     */
    public static MapBoxOfflineQueueTask constructMapBoxOfflineQueueTask(@NonNull MapBoxDownloadTask mapBoxDownloadTask) {

        Realm realm = Realm.getDefaultInstance();
        try {
            realm.beginTransaction();

            MapBoxOfflineQueueTask mapBoxOfflineQueueTask = realm.createObject(MapBoxOfflineQueueTask.class, UUID.randomUUID().toString());
            mapBoxOfflineQueueTask.setDateCreated(new Date());
            mapBoxOfflineQueueTask.setDateUpdated(new Date());
            mapBoxOfflineQueueTask.setTask(mapBoxDownloadTask.getJSONObject());
            mapBoxOfflineQueueTask.setTaskStatus(MapBoxOfflineQueueTask.TASK_STATUS_NOT_STARTED);
            mapBoxOfflineQueueTask.setTaskType(MapBoxOfflineQueueTask.TASK_TYPE_DOWNLOAD);

            realm.commitTransaction();

            return mapBoxOfflineQueueTask;
        } catch (Exception e){
            Log.e(TAG, Log.getStackTraceString(e));
            realm.cancelTransaction();
            return null;
        }
    }
}
