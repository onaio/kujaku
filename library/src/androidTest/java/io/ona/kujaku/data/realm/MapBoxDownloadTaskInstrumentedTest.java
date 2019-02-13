package io.ona.kujaku.data.realm;

import com.mapbox.mapboxsdk.geometry.LatLng;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import io.ona.kujaku.data.MapBoxDownloadTask;
import io.ona.kujaku.data.realm.objects.MapBoxOfflineQueueTask;
import io.realm.Realm;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 15/01/2018.
 */

public class MapBoxDownloadTaskInstrumentedTest extends RealmRelatedInstrumentedTest {

    @Override
    public void setUp() throws Exception {
        super.setUp();
        Realm.init(context);
    }

    @Test
    public void constructMapBoxOfflineQueueTaskShouldSaveInRealm() throws JSONException {
        String mapName = UUID.randomUUID().toString();
        String mapBoxAccessToken = "sample_token";
        String packageName = "com.sample";
        String mapBoxStyleUrl = "mapbox://styles/user/sampleStyle";
        double minZoom = 10d;
        double maxZoom = 20d;
        LatLng bottomRightBound = new LatLng(
                -17.875469,
                25.876589
        );

        LatLng bottomLeftBound = new LatLng(
                -17.875469,
                25.854782
        );
        LatLng topLeftBound = new LatLng(
                -17.854564,
                25.854782
        );
        LatLng topRightBound = new LatLng(
                -17.854564,
                25.876589
        );
        MapBoxDownloadTask mapBoxDownloadTask = new MapBoxDownloadTask(packageName, mapName, mapBoxStyleUrl, minZoom, maxZoom, topLeftBound, topRightBound, bottomRightBound, bottomLeftBound, mapBoxAccessToken);

        Date timeNow = Calendar.getInstance().getTime();
        MapBoxOfflineQueueTask mapBoxOfflineQueueTask = MapBoxDownloadTask.constructMapBoxOfflineQueueTask(mapBoxDownloadTask);
        addedRecords.add(mapBoxOfflineQueueTask);

        Realm realm = Realm.getDefaultInstance();

        MapBoxOfflineQueueTask queryResultTask = realm.where(MapBoxOfflineQueueTask.class)
                .contains("task", mapName)
                .equalTo("taskStatus", MapBoxOfflineQueueTask.TASK_STATUS_NOT_STARTED)
                .equalTo("taskType", MapBoxOfflineQueueTask.TASK_TYPE_DOWNLOAD)
                .findFirst();

        Assert.assertTrue(queryResultTask != null);
        Assert.assertTrue((queryResultTask.getDateCreated().getTime() - timeNow.getTime()) < 1000);
        Assert.assertTrue((queryResultTask.getDateUpdated().getTime() - timeNow.getTime()) < 1000);

        JSONObject mapBoxDownloadTaskJSON = queryResultTask.getTask();

        Assert.assertEquals(packageName, mapBoxDownloadTaskJSON.getString("packageName"));
        Assert.assertEquals(mapName, mapBoxDownloadTaskJSON.getString("mapName"));
        Assert.assertEquals(mapBoxStyleUrl, mapBoxDownloadTaskJSON.getString("mapBoxStyleUrl"));
        Assert.assertEquals(mapBoxAccessToken, mapBoxDownloadTaskJSON.getString("mapBoxAccessToken"));
        Assert.assertEquals(minZoom, mapBoxDownloadTaskJSON.getDouble("minZoom"), 0);
        Assert.assertEquals(maxZoom, mapBoxDownloadTaskJSON.getDouble("maxZoom"), 0);

        JSONObject bottomRightBoundJSON = mapBoxDownloadTaskJSON.getJSONObject("bottomRightBound");
        JSONObject topLeftBoundJSON = mapBoxDownloadTaskJSON.getJSONObject("topLeftBound");

        Assert.assertEquals(bottomRightBound.getLatitude(), bottomRightBoundJSON.getDouble("lat"), 0);
        Assert.assertEquals(bottomRightBound.getLongitude(), bottomRightBoundJSON.getDouble("lng"), 0);
        Assert.assertEquals(topLeftBound.getLatitude(), topLeftBoundJSON.getDouble("lat"), 0);
        Assert.assertEquals(topLeftBound.getLongitude(), topLeftBoundJSON.getDouble("lng"), 0);

    }
}
