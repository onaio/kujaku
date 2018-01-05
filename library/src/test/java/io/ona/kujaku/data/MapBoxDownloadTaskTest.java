package io.ona.kujaku.data;

import com.mapbox.mapboxsdk.geometry.LatLng;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import io.ona.kujaku.BuildConfig;
import utils.exceptions.MalformedDataException;

import static org.junit.Assert.*;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 05/12/2017.
 */
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class,manifest=Config.NONE)
public class MapBoxDownloadTaskTest {

    @Test
    public void constructorShouldCreateValidObject() {
        String mapName = "sample map name";
        String mapboxAccessToken = "90sd09jio(#@";
        String packageName = "package.com.io";
        String mapboxStyleUrl = "mapbox://styles/klj/oiuu0oklsd";
        double minZoom = 20.5;
        double maxZoom = 2.3;
        LatLng topLeftBound = new LatLng();
        topLeftBound.setLongitude(Math.random() * 90);
        topLeftBound.setLatitude(Math.random() * 90);

        LatLng bottomRightBound = new LatLng();
        bottomRightBound.setLatitude(Math.random() * 90);
        bottomRightBound.setLongitude(Math.random() * 90);

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(MapBoxDeleteTask.MAP_NAME, mapName);
            jsonObject.put(MapBoxDeleteTask.MAP_BOX_ACCESS_TOKEN, mapboxAccessToken);

            MapBoxDownloadTask mapBoxDownloadTask = new MapBoxDownloadTask(jsonObject);

            assertEquals(mapName, mapBoxDownloadTask.getMapName());
            assertEquals(mapboxAccessToken, mapBoxDownloadTask.getMapBoxAccessToken());
            assertEquals(packageName, mapBoxDownloadTask.getPackageName());
            assertEquals(mapboxStyleUrl, mapBoxDownloadTask.getMapBoxStyleUrl());
            assertEquals(minZoom, mapBoxDownloadTask.getMinZoom(), 0.0);
            assertEquals(maxZoom, mapBoxDownloadTask.getMaxZoom(), 0.0);
            assertEquals(bottomRightBound, mapBoxDownloadTask.getBottomRightBound());
            assertEquals(topLeftBound, mapBoxDownloadTask.getTopLeftBound());
        } catch (JSONException | MalformedDataException e) {
            e.printStackTrace();
        }
    }

}