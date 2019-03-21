package io.ona.kujaku.helpers;

import android.content.Context;
import android.content.Intent;

import com.mapbox.mapboxsdk.geometry.LatLng;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;

import io.ona.kujaku.BaseTest;
import io.ona.kujaku.data.realm.objects.MapBoxOfflineQueueTask;
import io.ona.kujaku.services.MapboxOfflineDownloaderService;
import io.ona.kujaku.utils.Constants;

import static org.junit.Assert.*;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 21/03/2019
 */

public class OfflineServiceHelperTest extends BaseTest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private Context context;

    @Test
    public void requestOfflineMapDownloadShouldCreateValidIntentAndCallStartService() {
        String mapName = "kampala-map";
        String mapboxStyleUrl = "mapbox://some-map";
        String mapboxAccessToken = "access-token-here";

        LatLng topLeftBound = Mockito.mock(LatLng.class);
        LatLng topRightBound = Mockito.mock(LatLng.class);
        LatLng bottomRightBound = Mockito.mock(LatLng.class);
        LatLng bottomLeftBound = Mockito.mock(LatLng.class);

        double minZoom = 20;
        double maxZoom = 12;

        OfflineServiceHelper.ZoomRange zoomRange = new OfflineServiceHelper.ZoomRange(minZoom, maxZoom);

        ArrayList<Object> results = new ArrayList<>();

        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Intent intent = invocation.getArgumentAt(0, Intent.class);

                results.add(intent.getStringExtra(Constants.PARCELABLE_KEY_MAP_UNIQUE_NAME));
                results.add(intent.getStringExtra(Constants.PARCELABLE_KEY_STYLE_URL));
                results.add(intent.getStringExtra(Constants.PARCELABLE_KEY_MAPBOX_ACCESS_TOKEN));
                results.add(intent.getDoubleExtra(Constants.PARCELABLE_KEY_MIN_ZOOM, 0d));
                results.add(intent.getDoubleExtra(Constants.PARCELABLE_KEY_MAX_ZOOM, 0d));
                results.add((MapboxOfflineDownloaderService.SERVICE_ACTION) intent.getExtras().get(Constants.PARCELABLE_KEY_SERVICE_ACTION));
                results.add(intent.getParcelableExtra(Constants.PARCELABLE_KEY_BOTTOM_LEFT_BOUND));
                results.add(intent.getParcelableExtra(Constants.PARCELABLE_KEY_BOTTOM_RIGHT_BOUND));
                results.add(intent.getParcelableExtra(Constants.PARCELABLE_KEY_TOP_RIGHT_BOUND));
                results.add(intent.getParcelableExtra(Constants.PARCELABLE_KEY_TOP_LEFT_BOUND));

                return null;
            }
        })
                .when(context)
                .startService(Mockito.any(Intent.class));

        OfflineServiceHelper.requestOfflineMapDownload(context, mapName, mapboxStyleUrl, mapboxAccessToken
                , topLeftBound, topRightBound, bottomRightBound, bottomLeftBound, zoomRange);

        Mockito.verify(context, Mockito.times(1))
                .startService(Mockito.any(Intent.class));

        assertEquals(mapName, results.get(0));
        assertEquals(mapboxStyleUrl, results.get(1));
        assertEquals(mapboxAccessToken, results.get(2));
        assertEquals(minZoom, (double) results.get(3), 0d);
        assertEquals(maxZoom, (double) results.get(4), 0d);
        assertEquals(MapboxOfflineDownloaderService.SERVICE_ACTION.DOWNLOAD_MAP, results.get(5));
        assertEquals(bottomLeftBound, results.get(6));
        assertEquals(bottomRightBound, results.get(7));
        assertEquals(topRightBound, results.get(8));
        assertEquals(topLeftBound, results.get(9));
    }

    @Test
    public void deleteOfflineMapShouldCreateValidIntentAndCallStartService() {
        String mapName = "kampala-map";
        String mapboxAccessToken = "access-token-here";
        ArrayList<Object> results = new ArrayList<>();

        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Intent intent = invocation.getArgumentAt(0, Intent.class);

                results.add(intent.getStringExtra(Constants.PARCELABLE_KEY_MAP_UNIQUE_NAME));
                results.add(intent.getStringExtra(Constants.PARCELABLE_KEY_MAPBOX_ACCESS_TOKEN));
                results.add((MapboxOfflineDownloaderService.SERVICE_ACTION) intent.getExtras().get(Constants.PARCELABLE_KEY_SERVICE_ACTION));
                return null;
            }
        })
                .when(context)
                .startService(Mockito.any(Intent.class));

        OfflineServiceHelper.deleteOfflineMap(context, mapName, mapboxAccessToken);

        Mockito.verify(context, Mockito.times(1))
                .startService(Mockito.any(Intent.class));

        assertEquals(mapName, results.get(0));
        assertEquals(mapboxAccessToken, results.get(1));
        assertEquals(MapboxOfflineDownloaderService.SERVICE_ACTION.DELETE_MAP, results.get(2));
    }

    @Test
    public void stopMapDownloadShouldCreateValidIntentAndCallStartService() {
        String mapName = "kampala-map";
        String mapboxAccessToken = "access-token-here";
        ArrayList<Object> results = new ArrayList<>();

        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Intent intent = invocation.getArgumentAt(0, Intent.class);

                results.add(intent.getStringExtra(Constants.PARCELABLE_KEY_MAP_UNIQUE_NAME));
                results.add(intent.getStringExtra(Constants.PARCELABLE_KEY_MAPBOX_ACCESS_TOKEN));
                results.add(intent.getExtras().get(Constants.PARCELABLE_KEY_SERVICE_ACTION));
                results.add(intent.getExtras().get(Constants.PARCELABLE_KEY_DELETE_TASK_TYPE));

                return null;
            }
        })
                .when(context)
                .startService(Mockito.any(Intent.class));

        OfflineServiceHelper.stopMapDownload(context, mapName, mapboxAccessToken);

        Mockito.verify(context, Mockito.times(1))
                .startService(Mockito.any(Intent.class));

        assertEquals(mapName, results.get(0));
        assertEquals(mapboxAccessToken, results.get(1));
        assertEquals(MapboxOfflineDownloaderService.SERVICE_ACTION.STOP_CURRENT_DOWNLOAD, results.get(2));
        assertEquals(MapBoxOfflineQueueTask.TASK_TYPE_DOWNLOAD, results.get(3));
    }
}