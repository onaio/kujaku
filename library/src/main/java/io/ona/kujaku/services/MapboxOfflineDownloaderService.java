package io.ona.kujaku.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.mapbox.mapboxsdk.offline.OfflineRegion;
import com.mapbox.mapboxsdk.offline.OfflineRegionStatus;

import io.ona.kujaku.downloaders.MapBoxOfflineResourcesDownloader;
import io.ona.kujaku.exceptions.OfflineMapDownloadException;
import io.ona.kujaku.listeners.IncompleteMapDownloadCallback;
import io.ona.kujaku.listeners.OnDownloadMapListener;
import io.ona.kujaku.parcelables.LatLngParcelable;
import io.ona.kujaku.utils.Constants;

/**
 * Service performs Offline Map Download, Offline Map Deletion & Offline Map Download Resumption
 * <p>
 *     You need to pass the following in the Intent Extras:
 *          - {@link Constants#PARCELABLE_KEY_SERVICE_ACTION}
 *          - Optional {@link Constants#PARCELABLE_KEY_NETWORK_STATE} - Required for {@link io.ona.kujaku.utils.Constants.SERVICE_ACTION#NETWORK_RESUME}
 *          - Optional {@link Constants#PARCELABLE_KEY_MAP_UNIQUE_NAME} - Required for {@link io.ona.kujaku.utils.Constants.SERVICE_ACTION#DELETE_MAP} & {@link io.ona.kujaku.utils.Constants.SERVICE_ACTION#DOWNLOAD_MAP}
 *          - Optional {@link Constants#PARCELABLE_KEY_MAPBOX_ACCESS_TOKEN} - Required for {@link io.ona.kujaku.utils.Constants.SERVICE_ACTION#DELETE_MAP} & {@link io.ona.kujaku.utils.Constants.SERVICE_ACTION#DOWNLOAD_MAP}
 *          - Optional {@link Constants#PARCELABLE_KEY_STYLE_URL} - Required for {@link io.ona.kujaku.utils.Constants.SERVICE_ACTION#DOWNLOAD_MAP}
 *          - Optional {@link Constants#PARCELABLE_KEY_MAX_ZOOM} - Required for {@link io.ona.kujaku.utils.Constants.SERVICE_ACTION#DOWNLOAD_MAP}
 *          - Optional {@link Constants#PARCELABLE_KEY_MIN_ZOOM} - Required for {@link io.ona.kujaku.utils.Constants.SERVICE_ACTION#DOWNLOAD_MAP}
 *          - Optional {@link Constants#PARCELABLE_KEY_TOP_LEFT_BOUND} - Required for {@link io.ona.kujaku.utils.Constants.SERVICE_ACTION#DOWNLOAD_MAP}
 *          - Optional {@link Constants#PARCELABLE_KEY_BOTTOM_RIGHT_BOUND} - Required for {@link io.ona.kujaku.utils.Constants.SERVICE_ACTION#DOWNLOAD_MAP}
 * </p>
 *
 * <p>
 *     The service posts updates through a Local Broadcast with action {@link Constants#INTENT_ACTION_MAP_DOWNLOAD_SERVICE_STATUS_UPDATES}
 * </p>
 *
 *
 * Created by Ephraim Kigamba - ekigamba@ona.io on 13/11/2017.
 */

public class MapboxOfflineDownloaderService extends Service {


    private enum SERVICE_ACTION_RESULT {
        SUCCESSFUL,
        FAILED
    }
    private static final String RESULT_STATUS = "RESULT_STATUS";
    private static final String RESULT_MESSAGE = "RESULT_MESSAGE";
    private static final String RESULTS_PARENT_ACTION = "RESULTS_PARENT_ACTION";
    private static final String TAG = MapboxOfflineDownloaderService.class.getSimpleName();
    private static final int[] PREFERRED_DOWNLOAD_NETWORKS = {
            ConnectivityManager.TYPE_WIFI,
            ConnectivityManager.TYPE_MOBILE
    };

    private static final String MY_PREFERENCES = "KUJAKU PREFERENCES";
    private static final String PREFERENCE_MAPBOX_ACCESS_TOKEN = "MAPBOX ACCESS TOKEN";

    public MapboxOfflineDownloaderService() {
        super();
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        onHandleIntent(intent);
        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent == null) {
            return;
        }

        Bundle extras = intent.getExtras();
        if (extras != null
                && extras.containsKey(Constants.PARCELABLE_KEY_SERVICE_ACTION)) {

            final Constants.SERVICE_ACTION serviceAction = (Constants.SERVICE_ACTION) extras.get(Constants.PARCELABLE_KEY_SERVICE_ACTION);

            if (serviceAction == Constants.SERVICE_ACTION.NETWORK_RESUME && extras.containsKey(Constants.PARCELABLE_KEY_NETWORK_STATE)) {

                int activeNetworkState = getConnectionType();
                onNetworkResume(activeNetworkState);
            } else if (extras.containsKey(Constants.PARCELABLE_KEY_MAP_UNIQUE_NAME)
                    && extras.containsKey(Constants.PARCELABLE_KEY_MAPBOX_ACCESS_TOKEN)) {

                final String mapUniqueName = extras.getString(Constants.PARCELABLE_KEY_MAP_UNIQUE_NAME);
                String mapboxAccessToken = extras.getString(Constants.PARCELABLE_KEY_MAPBOX_ACCESS_TOKEN);
                saveAccessToken(mapboxAccessToken);

                MapBoxOfflineResourcesDownloader mapBoxOfflineResourcesDownloader = MapBoxOfflineResourcesDownloader.getInstance(getApplicationContext(), mapboxAccessToken);

                if (serviceAction == Constants.SERVICE_ACTION.DOWNLOAD_MAP) {
                    if (extras.containsKey(Constants.PARCELABLE_KEY_STYLE_URL)
                            && extras.containsKey(Constants.PARCELABLE_KEY_MAX_ZOOM)
                            && extras.containsKey(Constants.PARCELABLE_KEY_MIN_ZOOM)
                            && extras.containsKey(Constants.PARCELABLE_KEY_TOP_LEFT_BOUND)
                            && extras.containsKey(Constants.PARCELABLE_KEY_BOTTOM_RIGHT_BOUND)) {

                        String styleUrl = extras.getString(Constants.PARCELABLE_KEY_STYLE_URL);
                        double maxZoom = extras.getDouble(Constants.PARCELABLE_KEY_MAX_ZOOM);
                        double minZoom = extras.getDouble(Constants.PARCELABLE_KEY_MIN_ZOOM);
                        LatLngParcelable topLeftBound = extras.getParcelable(Constants.PARCELABLE_KEY_TOP_LEFT_BOUND);
                        LatLngParcelable bottomRightBound = extras.getParcelable(Constants.PARCELABLE_KEY_BOTTOM_RIGHT_BOUND);

                        try {
                            mapBoxOfflineResourcesDownloader.downloadMap(mapUniqueName, styleUrl, topLeftBound, bottomRightBound, minZoom, maxZoom, new OnDownloadMapListener() {
                                @Override
                                public void onStatusChanged(OfflineRegionStatus offlineRegionStatus) {
                                    double percentageDownload = (offlineRegionStatus.getRequiredResourceCount() >= 0) ? 100.0 * offlineRegionStatus.getCompletedResourceCount() / offlineRegionStatus.getRequiredResourceCount() : 0.0;
                                    sendBroadcast(SERVICE_ACTION_RESULT.SUCCESSFUL, mapUniqueName, serviceAction, String.valueOf(percentageDownload));
                                }

                                @Override
                                public void onError(String errorReason, String errorMessage) {
                                    sendBroadcast(SERVICE_ACTION_RESULT.FAILED, mapUniqueName, serviceAction, errorMessage + " - " + errorMessage);
                                }
                            });
                        } catch (OfflineMapDownloadException e) {
                            Log.e(TAG, Log.getStackTraceString(e));
                            sendBroadcast(SERVICE_ACTION_RESULT.FAILED, mapUniqueName, serviceAction, e.getMessage());
                        }
                    }
                } else {
                    // DELETE MAP Service Action
                    mapBoxOfflineResourcesDownloader.deleteMap(mapUniqueName, new OfflineRegion.OfflineRegionDeleteCallback() {
                        @Override
                        public void onDelete() {
                            sendBroadcast(SERVICE_ACTION_RESULT.SUCCESSFUL, mapUniqueName, serviceAction);
                        }

                        @Override
                        public void onError(String error) {
                            sendBroadcast(SERVICE_ACTION_RESULT.FAILED, mapUniqueName, serviceAction, error);
                        }
                    });
                }
            }

        }
    }

    /**
     * Sends a local broadcast with the result of a service operation & mapName except for {@code io.ona.kujaku.utils.Constants.SERVICE_ACTION.DELETE_MAP}
     * 
     * 
     * @param serviceActionResult {@link SERVICE_ACTION_RESULT#SUCCESSFUL} or {@link SERVICE_ACTION_RESULT#FAILED}
     * @param mapName Unique name of the map
     * @param message Additional message/information about the result eg. For a {@link SERVICE_ACTION_RESULT#FAILED} result
     */
    private void sendBroadcast(SERVICE_ACTION_RESULT serviceActionResult, String mapName, Constants.SERVICE_ACTION serviceAction, String message) {
        Intent intent = new Intent();
        intent.setAction(Constants.INTENT_ACTION_MAP_DOWNLOAD_SERVICE_STATUS_UPDATES);
        intent.putExtra(RESULT_STATUS, serviceActionResult.name());
        intent.putExtra(RESULT_MESSAGE, message);
        intent.putExtra(Constants.PARCELABLE_KEY_MAP_UNIQUE_NAME, mapName);
        intent.putExtra(RESULTS_PARENT_ACTION, serviceAction);

        LocalBroadcastManager.getInstance(this)
                .sendBroadcast(intent);
    }

    /**
     * Sends a local broadcast with the result of a service operation & mapName except for {@code io.ona.kujaku.utils.Constants.SERVICE_ACTION.DELETE_MAP}
     *
     *
     * @param serviceActionResult {@link SERVICE_ACTION_RESULT#SUCCESSFUL} or {@link SERVICE_ACTION_RESULT#FAILED}
     * @param mapName Unique name of the map
     */
    private void sendBroadcast(SERVICE_ACTION_RESULT serviceActionResult, String mapName, Constants.SERVICE_ACTION serviceAction) {
        sendBroadcast(serviceActionResult, mapName, serviceAction, "");
    }

    private void onNetworkResume(int networkType) {
        if (isNetworkConnectionPreferred(networkType)) {
            String mapBoxAccessToken = getSavedAccessToken();
            if (!mapBoxAccessToken.isEmpty()) {
                final MapBoxOfflineResourcesDownloader mapBoxOfflineResourcesDownloader = MapBoxOfflineResourcesDownloader.getInstance(getApplicationContext(), mapBoxAccessToken);
                mapBoxOfflineResourcesDownloader.getIncompleteMapDownloads(new IncompleteMapDownloadCallback() {
                    @Override
                    public void incompleteMap(OfflineRegion offlineRegion, OfflineRegionStatus offlineRegionStatus) {
                        if (offlineRegionStatus.getDownloadState() != OfflineRegion.STATE_ACTIVE) {
                            mapBoxOfflineResourcesDownloader.resumeMapDownload(offlineRegion, null);
                            Log.i(TAG, "Resuming Map Download ID: " + offlineRegion.getID());
                        }
                    }

                    @Override
                    public void onError(String errorReason, String errorMessage) {
                        // We cant do much for now
                        Log.e(TAG, errorReason + "\n" + errorMessage);
                    }
                });
            }
        }
    }

    private int getConnectionType() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        return activeNetwork.getType();
    }

    private boolean isNetworkConnectionPreferred(int connectionType) {
        for(int preferredNetwork: PREFERRED_DOWNLOAD_NETWORKS) {
            if (preferredNetwork == connectionType) {
                return true;
            }
        }

        return false;
    }

    private String getSavedAccessToken() {
        SharedPreferences sharedPreferences = getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE);
        return sharedPreferences.getString(PREFERENCE_MAPBOX_ACCESS_TOKEN, "");
    }

    private boolean saveAccessToken(String mapBoxAccessToken) {
        SharedPreferences sharedPreferences = getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString(PREFERENCE_MAPBOX_ACCESS_TOKEN, mapBoxAccessToken);
        return editor.commit();
    }

}