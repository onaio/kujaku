package io.ona.kujaku.helpers;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import com.mapbox.mapboxsdk.geometry.LatLng;

import io.ona.kujaku.data.realm.objects.MapBoxOfflineQueueTask;
import io.ona.kujaku.services.MapboxOfflineDownloaderService;
import io.ona.kujaku.utils.Constants;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 05/12/2018
 */

public abstract class OfflineServiceHelper {

    /**
     * Requests an offline map download for you from the {@link MapboxOfflineDownloaderService}.
     * You should register a {@link android.content.BroadcastReceiver} with {@link android.content.IntentFilter} action
     * {@link io.ona.kujaku.utils.Constants#INTENT_ACTION_MAP_DOWNLOAD_SERVICE_STATUS_UPDATES}.
     * See {@link MapboxOfflineDownloaderService} for more on the available updates
     *
     * @param context
     * @param mapName
     * @param mapboxStyleUrl
     * @param mapBoxAccessToken
     * @param topLeftBound
     * @param topRightBound
     * @param bottomRightBound
     * @param bottomLeftBound
     * @param zoomRange
     */
    public static void requestOfflineMapDownload(@NonNull Context context, @NonNull String mapName,
                                          @NonNull String mapboxStyleUrl, @NonNull String mapBoxAccessToken,
                                          @NonNull LatLng topLeftBound, @NonNull LatLng topRightBound,
                                          @NonNull LatLng bottomRightBound, @NonNull LatLng bottomLeftBound,
                                          @NonNull ZoomRange zoomRange) {
        requestOfflineMapDownload(context, mapName, mapboxStyleUrl, mapBoxAccessToken, topLeftBound, topRightBound, bottomRightBound, bottomLeftBound, zoomRange,6000l);
    }


    /**
     * Requests an offline map download for you from the {@link MapboxOfflineDownloaderService}.
     * You should register a {@link android.content.BroadcastReceiver} with {@link android.content.IntentFilter} action
     * {@link io.ona.kujaku.utils.Constants#INTENT_ACTION_MAP_DOWNLOAD_SERVICE_STATUS_UPDATES}.
     * See {@link MapboxOfflineDownloaderService} for more on the available updates
     *
     * @param context
     * @param mapName
     * @param mapboxStyleUrl
     * @param mapBoxAccessToken
     * @param topLeftBound
     * @param topRightBound
     * @param bottomRightBound
     * @param bottomLeftBound
     * @param zoomRange
     * @param tileDownloadLimit the tile download limit
     */
    public static void requestOfflineMapDownload(@NonNull Context context, @NonNull String mapName,
                                                 @NonNull String mapboxStyleUrl, @NonNull String mapBoxAccessToken,
                                                 @NonNull LatLng topLeftBound, @NonNull LatLng topRightBound,
                                                 @NonNull LatLng bottomRightBound, @NonNull LatLng bottomLeftBound,
                                                 @NonNull ZoomRange zoomRange, @NonNull Long tileDownloadLimit) {
        Intent intent = new Intent(context, MapboxOfflineDownloaderService.class);
        intent.putExtra(Constants.PARCELABLE_KEY_SERVICE_ACTION, MapboxOfflineDownloaderService.SERVICE_ACTION.DOWNLOAD_MAP);
        intent.putExtra(Constants.PARCELABLE_KEY_STYLE_URL, mapboxStyleUrl);
        intent.putExtra(Constants.PARCELABLE_KEY_MAP_UNIQUE_NAME, mapName);
        intent.putExtra(Constants.PARCELABLE_KEY_MAPBOX_ACCESS_TOKEN, mapBoxAccessToken);
        intent.putExtra(Constants.PARCELABLE_KEY_TOP_LEFT_BOUND, topLeftBound);
        intent.putExtra(Constants.PARCELABLE_KEY_TOP_RIGHT_BOUND, topRightBound);
        intent.putExtra(Constants.PARCELABLE_KEY_BOTTOM_RIGHT_BOUND, bottomRightBound);
        intent.putExtra(Constants.PARCELABLE_KEY_BOTTOM_LEFT_BOUND, bottomLeftBound);
        intent.putExtra(Constants.PARCELABLE_KEY_MIN_ZOOM, zoomRange.getMinZoom());
        intent.putExtra(Constants.PARCELABLE_KEY_MAX_ZOOM, zoomRange.getMaxZoom());
        intent.putExtra(Constants.PARCELABLE_KEY_MAPBOX_DOWNLOAD_TILE_LIMIT,tileDownloadLimit);

        context.startService(intent);
    }

    /**
     * Requests the deletion of an offline map from {@Link MapboxOfflineDownloaderService}. This is
     * an offline map already downloaded or queued to download using the {@link MapboxOfflineDownloaderService}
     *
     * @param context
     * @param mapName
     * @param mapBoxAccessToken
     */
    public static void deleteOfflineMap(@NonNull Context context, @NonNull String mapName, @NonNull String mapBoxAccessToken) {
        Intent intent = new Intent(context, MapboxOfflineDownloaderService.class);
        intent.putExtra(Constants.PARCELABLE_KEY_SERVICE_ACTION, MapboxOfflineDownloaderService.SERVICE_ACTION.DELETE_MAP);
        intent.putExtra(Constants.PARCELABLE_KEY_MAP_UNIQUE_NAME, mapName);
        intent.putExtra(Constants.PARCELABLE_KEY_MAPBOX_ACCESS_TOKEN, mapBoxAccessToken);

        context.startService(intent);
    }

    /**
     * Requests the {@Link MapboxOfflineDownloaderService} to stop downloading the current map referenced
     * by the {@code mapName} passed. This will also download all the resources of the
     * {@link com.mapbox.mapboxsdk.offline.OfflineRegion} downloaded that are not shared with other {@link com.mapbox.mapboxsdk.offline.OfflineRegion}
     *
     * @param context
     * @param mapName
     * @param mapBoxAccessToken
     */
    public static void stopMapDownload(@NonNull Context context, @NonNull String mapName, @NonNull String mapBoxAccessToken) {
        Intent stopDownloadIntent = new Intent(context, MapboxOfflineDownloaderService.class);
        stopDownloadIntent.putExtra(Constants.PARCELABLE_KEY_SERVICE_ACTION, MapboxOfflineDownloaderService.SERVICE_ACTION.STOP_CURRENT_DOWNLOAD);
        stopDownloadIntent.putExtra(Constants.PARCELABLE_KEY_MAP_UNIQUE_NAME, mapName);
        stopDownloadIntent.putExtra(Constants.PARCELABLE_KEY_MAPBOX_ACCESS_TOKEN, mapBoxAccessToken);
        stopDownloadIntent.putExtra(Constants.PARCELABLE_KEY_DELETE_TASK_TYPE, MapBoxOfflineQueueTask.TASK_TYPE_DOWNLOAD);

        context.startService(stopDownloadIntent);
    }

    public static class ZoomRange {
        private double minZoom;
        private double maxZoom;

        public ZoomRange(double minZoom, double maxZoom) {
            this.minZoom = minZoom;
            this.maxZoom = maxZoom;
        }

        public double getMinZoom() {
            return minZoom;
        }

        public double getMaxZoom() {
            return maxZoom;
        }
    }


}
