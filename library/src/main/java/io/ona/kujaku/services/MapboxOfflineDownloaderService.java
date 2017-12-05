package io.ona.kujaku.services;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.text.format.Formatter;
import android.util.Log;

import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.offline.OfflineRegion;
import com.mapbox.mapboxsdk.offline.OfflineRegionError;
import com.mapbox.mapboxsdk.offline.OfflineRegionStatus;

import org.json.JSONException;

import java.text.DecimalFormat;

import io.ona.kujaku.R;
import io.ona.kujaku.data.MapBoxDeleteTask;
import io.ona.kujaku.data.MapBoxDownloadTask;
import io.ona.kujaku.downloaders.MapBoxOfflineResourcesDownloader;
import io.ona.kujaku.listeners.IncompleteMapDownloadCallback;
import io.ona.kujaku.listeners.OfflineRegionObserver;
import io.ona.kujaku.listeners.OfflineRegionStatusCallback;
import io.ona.kujaku.listeners.OnDownloadMapListener;

import io.ona.kujaku.data.realm.objects.MapBoxOfflineQueueTask;
import io.ona.kujaku.utils.NumberFormatter;
import io.realm.Realm;
import utils.Constants;
import utils.exceptions.MalformedDataException;
import utils.exceptions.OfflineMapDownloadException;

/**
 * Service performs Offline Map Download, Offline Map Deletion & Offline Map Download Resumption
 * <p>
 *     You need to pass the following in the Intent Extras:
 *          - {@link Constants#PARCELABLE_KEY_SERVICE_ACTION}
 *          - Optional {@link Constants#PARCELABLE_KEY_NETWORK_STATE} - Required for {@link Constants.SERVICE_ACTION#NETWORK_RESUME}
 *          - Optional {@link Constants#PARCELABLE_KEY_MAP_UNIQUE_NAME} - Required for {@link Constants.SERVICE_ACTION#DELETE_MAP} & {@link Constants.SERVICE_ACTION#DOWNLOAD_MAP}
 *          - Optional {@link Constants#PARCELABLE_KEY_MAPBOX_ACCESS_TOKEN} - Required for {@link Constants.SERVICE_ACTION#DELETE_MAP} & {@link Constants.SERVICE_ACTION#DOWNLOAD_MAP}
 *          - Optional {@link Constants#PARCELABLE_KEY_STYLE_URL} - Required for {@link Constants.SERVICE_ACTION#DOWNLOAD_MAP}
 *          - Optional {@link Constants#PARCELABLE_KEY_MAX_ZOOM} - Required for {@link Constants.SERVICE_ACTION#DOWNLOAD_MAP}
 *          - Optional {@link Constants#PARCELABLE_KEY_MIN_ZOOM} - Required for {@link Constants.SERVICE_ACTION#DOWNLOAD_MAP}
 *          - Optional {@link Constants#PARCELABLE_KEY_TOP_LEFT_BOUND} - Required for {@link Constants.SERVICE_ACTION#DOWNLOAD_MAP}
 *          - Optional {@link Constants#PARCELABLE_KEY_BOTTOM_RIGHT_BOUND} - Required for {@link Constants.SERVICE_ACTION#DOWNLOAD_MAP}
 * </p>
 *
 * <p>
 *     The service posts updates through a Local Broadcast with action {@link Constants#INTENT_ACTION_MAP_DOWNLOAD_SERVICE_STATUS_UPDATES}<br/>
 *     The Broadcasts are classified into two:
 *     <ol>
 *          <li>SUCCESS messages
 *              <p>These messages either signal a download progress updated, delete map operation success or a download map</p>
 *          </li>
 *          <li>FAILURE message
 *              <p>These messages signal a map download failure or delete map operation failure.
 *                  <br/>A map download failure can be caused by:
 *                  <ol>
 *                      <li>Map name being already in use</li>
 *                      <li>Invalid map definitions passed</li>
 *                      <li>Tile map count limit being exceeded {@see https://www.mapbox.com/help/mobile-offline/#tile-ceiling--limits}</li>
 *                  </ol><br/>
 *                  A map delete failure can be caused by:
 *                  <ol>
 *                      <li>Map referenced, by name, does not exist</li>
 *                      <li>An error caused on MapBox SDK while deleting the map</li>
 *                  </ol>
 *              </p>
 *          </li>
 *     </ol>
 *     Any broadcast has the:
 *     <ul>
 *         <li>{@link MapboxOfflineDownloaderService#RESULT_STATUS} - Can be either a {@link MapboxOfflineDownloaderService.SERVICE_ACTION_RESULT#SUCCESSFUL} or {@link MapboxOfflineDownloaderService.SERVICE_ACTION_RESULT#FAILED}</li>
 *         <li>{@link MapboxOfflineDownloaderService#RESULT_MESSAGE} - The error or success message</li>
 *         <li>{@link MapboxOfflineDownloaderService#RESULTS_PARENT_ACTION} - Can either be {@link utils.Constants.SERVICE_ACTION#DOWNLOAD_MAP} or {@link utils.Constants.SERVICE_ACTION#DELETE_MAP}</li>
 * </p>
 *
 *
 * Created by Ephraim Kigamba - ekigamba@ona.io on 13/11/2017.
 */

public class MapboxOfflineDownloaderService extends Service implements OfflineRegionObserver, OnDownloadMapListener {

    public enum SERVICE_ACTION_RESULT {
        SUCCESSFUL,
        FAILED
    }
    public static final String RESULT_STATUS = "RESULT_STATUS";
    public static final String RESULT_MESSAGE = "RESULT_MESSAGE";
    public static final String RESULTS_PARENT_ACTION = "RESULTS_PARENT_ACTION";
    private static final String TAG = MapboxOfflineDownloaderService.class.getSimpleName();
    private static final int[] PREFERRED_DOWNLOAD_NETWORKS = {
            ConnectivityManager.TYPE_WIFI,
            ConnectivityManager.TYPE_MOBILE
    };

    public static final String MY_PREFERENCES = "KUJAKU PREFERENCES";
    public static final String PREFERENCE_MAPBOX_ACCESS_TOKEN = "MAPBOX ACCESS TOKEN";

    private String mapBoxAccessToken = "";
    private String currentMapDownloadName = "";
    private Constants.SERVICE_ACTION currentServiceAction;
    private MapBoxOfflineQueueTask currentMapBoxTask;

    private NotificationCompat.Builder progressNotificationBuilder;
    public static final int PROGRESS_NOTIFICATION_ID = 85;
    public int LAST_DOWNLOAD_COMPLETE_NOTIFICATION_ID = 87;

    public MapboxOfflineDownloaderService() {
        super();
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        persistOfflineMapTask(intent);
        performNextTask();
        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     *
     * @param intent Intent passed when the service was called {@link Context#startService(Intent)}
     * @return {@code TRUE} if the OfflineMapTask was successfully saved
     *          {@code FALSE} if the OfflineMapTask could not be saved
     */
    private boolean persistOfflineMapTask(@Nullable Intent intent) {
        if (intent == null) {
            return false;
        }

        Bundle extras = intent.getExtras();
        if (extras != null
                && extras.containsKey(Constants.PARCELABLE_KEY_SERVICE_ACTION)) {
            final Constants.SERVICE_ACTION serviceAction = (Constants.SERVICE_ACTION) extras.get(Constants.PARCELABLE_KEY_SERVICE_ACTION);

            if (extras.containsKey(Constants.PARCELABLE_KEY_MAP_UNIQUE_NAME)
                    && extras.containsKey(Constants.PARCELABLE_KEY_MAPBOX_ACCESS_TOKEN)) {
                String mapUniqueName = extras.getString(Constants.PARCELABLE_KEY_MAP_UNIQUE_NAME);
                mapBoxAccessToken = extras.getString(Constants.PARCELABLE_KEY_MAPBOX_ACCESS_TOKEN);
                //saveAccessToken(mapboxAccessToken);

                MapBoxDownloadTask downloadTask = new MapBoxDownloadTask();
                downloadTask.setMapName(mapUniqueName);
                downloadTask.setMapBoxAccessToken(mapBoxAccessToken);

                if (serviceAction == Constants.SERVICE_ACTION.DOWNLOAD_MAP) {
                    if (extras.containsKey(Constants.PARCELABLE_KEY_STYLE_URL)
                            && extras.containsKey(Constants.PARCELABLE_KEY_MAX_ZOOM)
                            && extras.containsKey(Constants.PARCELABLE_KEY_MIN_ZOOM)
                            && extras.containsKey(Constants.PARCELABLE_KEY_TOP_LEFT_BOUND)
                            && extras.containsKey(Constants.PARCELABLE_KEY_BOTTOM_RIGHT_BOUND)) {

                        downloadTask.setPackageName("kl");
                        downloadTask.setMapBoxStyleUrl(extras.getString(Constants.PARCELABLE_KEY_STYLE_URL));
                        downloadTask.setMaxZoom(extras.getDouble(Constants.PARCELABLE_KEY_MAX_ZOOM));
                        downloadTask.setMinZoom(extras.getDouble(Constants.PARCELABLE_KEY_MIN_ZOOM));
                        downloadTask.setTopLeftBound((LatLng) extras.getParcelable(Constants.PARCELABLE_KEY_TOP_LEFT_BOUND));
                        downloadTask.setBottomRightBound((LatLng) extras.getParcelable(Constants.PARCELABLE_KEY_BOTTOM_RIGHT_BOUND));

                        MapBoxDownloadTask.constructMapBoxOfflineQueueTask(downloadTask);

                        return true;
                    }
                } else {
                    MapBoxDeleteTask deleteTask = new MapBoxDeleteTask();
                    deleteTask.setMapBoxAccessToken(mapBoxAccessToken);
                    deleteTask.setMapName(mapUniqueName);

                    MapBoxDeleteTask.constructMapBoxOfflineQueueTask(deleteTask);

                    return true;
                }

            }

        }

        return false;
    }

    /**
     * Offline Map tasks such as {@link MapBoxOfflineQueueTask#TASK_TYPE_DELETE} &
     * {@link MapBoxOfflineQueueTask#TASK_TYPE_DOWNLOAD} are performed here.
     * <p>
     * A {@link MapBoxOfflineQueueTask#TASK_TYPE_DELETE} will only be performed if the Offline
     * Region with the given name exists.
     * <p>
     * A {@link MapBoxOfflineQueueTask#TASK_TYPE_DOWNLOAD} will either be RESUMED, OBSERVED if RUNNING
     * , IGNORED(thus FAILING if it does) or DOWNLOADED.
     */
    private void performNextTask() {
        final MapBoxOfflineQueueTask mapBoxOfflineQueueTask = getNextTask();

        if (mapBoxOfflineQueueTask != null) {
            getTaskStatus(mapBoxOfflineQueueTask, mapBoxAccessToken, new OfflineRegionStatusCallback() {
                @Override
                public void onStatus(OfflineRegionStatus status, OfflineRegion offlineRegion) {
                    if (MapBoxOfflineQueueTask.TASK_TYPE_DELETE.equals(mapBoxOfflineQueueTask.getTaskType())) {
                        MapBoxOfflineResourcesDownloader.getInstance(MapboxOfflineDownloaderService.this, mapBoxAccessToken)
                                .deleteMap(currentMapDownloadName, new OfflineRegion.OfflineRegionDeleteCallback() {
                                    @Override
                                    public void onDelete() {
                                        sendBroadcast(SERVICE_ACTION_RESULT.SUCCESSFUL, currentMapDownloadName, Constants.SERVICE_ACTION.DELETE_MAP, "Map deleted successfully!");
                                        persistCompletedStatus(mapBoxOfflineQueueTask);
                                        performNextTask();
                                    }

                                    @Override
                                    public void onError(String error) {
                                        MapboxOfflineDownloaderService.this.onError(error, null);
                                        // An error means this cannot be solved even at a later time THUS persist the task as DONE
                                        persistCompletedStatus(mapBoxOfflineQueueTask);
                                        performNextTask();
                                    }
                                });
                        return;
                    }

                    if (status.getDownloadState() == OfflineRegion.STATE_ACTIVE) {
                        // TASK IS RUNNING
                        currentServiceAction = Constants.SERVICE_ACTION.DOWNLOAD_MAP;
                        currentMapBoxTask = mapBoxOfflineQueueTask;
                        observeOfflineRegion(offlineRegion);
                    } else {
                        if (!status.isComplete()) {
                            // TASK IS NOT RUNNING
                            currentServiceAction = Constants.SERVICE_ACTION.DOWNLOAD_MAP;
                            currentMapBoxTask = mapBoxOfflineQueueTask;
                            MapBoxOfflineResourcesDownloader.getInstance(MapboxOfflineDownloaderService.this, mapBoxAccessToken)
                                    .resumeMapDownload(offlineRegion, MapboxOfflineDownloaderService.this);
                            // Set the progress notification
                            showProgressNotification(currentMapDownloadName, 0.0);
                        } else {
                            // IGNORE IT AND SEND A BROADCAST HERE
                            persistCompletedStatus(mapBoxOfflineQueueTask);
                            MapboxOfflineDownloaderService.this.onError("Similar map with the name exists & has already been downloaded", null);
                        }
                    }
                }

                @Override
                public void onError(String error) {
                    if (error.contains("Map could not be found") && MapBoxOfflineQueueTask.TASK_TYPE_DOWNLOAD.equals(mapBoxOfflineQueueTask.getTaskType())) {
                        currentServiceAction = Constants.SERVICE_ACTION.DOWNLOAD_MAP;
                        currentMapBoxTask = mapBoxOfflineQueueTask;
                        try {
                            MapBoxOfflineResourcesDownloader.getInstance(MapboxOfflineDownloaderService.this, mapBoxAccessToken)
                                    .downloadMap(new MapBoxDownloadTask(mapBoxOfflineQueueTask.getTask()), MapboxOfflineDownloaderService.this);

                            //Set the progress notification
                            showProgressNotification(currentMapDownloadName, 0.0);
                        } catch (MalformedDataException | JSONException | OfflineMapDownloadException e) {
                            Log.e(TAG, Log.getStackTraceString(e));
                        }
                    } else {
                        MapboxOfflineDownloaderService.this.onError(error, null);
                    }
                }
            });
        } else {
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.cancel(PROGRESS_NOTIFICATION_ID);
            stopSelf();
        }
    }

    /**
     * Sends a local broadcast with the result of a service operation & mapName. To capture the
     * local broadcast messages, you need to use the {@link LocalBroadcastManager} to register a
     * {@link android.content.BroadcastReceiver} for action {@link Constants#INTENT_ACTION_MAP_DOWNLOAD_SERVICE_STATUS_UPDATES}
     *
     * The broadcast has the following extras:
     * <ol>
     *     <li>{@link MapboxOfflineDownloaderService#RESULT_STATUS} - Either SUCCESSFUL or FAILED. Type {@link MapboxOfflineDownloaderService.SERVICE_ACTION_RESULT}</li>
     *     <li>{@link MapboxOfflineDownloaderService#RESULT_MESSAGE} - User-friendly and/or descriptive message of the result. Type {@link String}</li>
     *     <li>{@link Constants#PARCELABLE_KEY_MAP_UNIQUE_NAME} - The Map's unique name. Type {@code {@link String}}</li>
     *     <li>{@link MapboxOfflineDownloaderService#RESULTS_PARENT_ACTION} - The action that was performed to produce this result {@link MapboxOfflineDownloaderService.SERVICE_ACTION_RESULT}</li>
     * </ol>
     *
     * <p>
     *     <h3>Sample Usage</h3>
     *     {@code LocalBroadcastManager.getInstance(context).registerReceiver(myBroadcastReceiver, new IntentFilter(utils.Constants.INTENT_ACTION_MAP_DOWNLOAD_SERVICE_STATUS_UPDATES)); }
     * </p>
     * 
     * 
     * @param serviceActionResult {@link SERVICE_ACTION_RESULT#SUCCESSFUL} or {@link SERVICE_ACTION_RESULT#FAILED}
     * @param mapName Unique name of the map
     * @param message Additional message/information about the result eg. For a {@link SERVICE_ACTION_RESULT#FAILED} result
     */
    private void sendBroadcast(@NonNull SERVICE_ACTION_RESULT serviceActionResult,@NonNull String mapName,@NonNull Constants.SERVICE_ACTION serviceAction,@NonNull String message) {
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
     *Sends a local broadcast with the result of a service operation & mapName. To capture the
     * local broadcast messages, you need to use the {@link LocalBroadcastManager} to register a
     * {@link android.content.BroadcastReceiver} for action {@link Constants#INTENT_ACTION_MAP_DOWNLOAD_SERVICE_STATUS_UPDATES}
     *
     * The broadcast has the following extras:
     * <ol>
     *     <li>{@link MapboxOfflineDownloaderService#RESULT_STATUS} - Either SUCCESSFUL or FAILED. Type {@link MapboxOfflineDownloaderService.SERVICE_ACTION_RESULT}</li>
     *     <li>{@link MapboxOfflineDownloaderService#RESULT_MESSAGE} - User-friendly and/or descriptive message of the result. Type {@link String}</li>
     *     <li>{@link Constants#PARCELABLE_KEY_MAP_UNIQUE_NAME} - The Map's unique name. Type {@code {@link String}}</li>
     *     <li>{@link MapboxOfflineDownloaderService#RESULTS_PARENT_ACTION} - The action that was performed to produce this result {@link MapboxOfflineDownloaderService.SERVICE_ACTION_RESULT}</li>
     * </ol>
     *
     * <p>
     *     <h3>Sample Usage</h3>
     *     {@code LocalBroadcastManager.getInstance(context).registerReceiver(myBroadcastReceiver, new IntentFilter(utils.Constants.INTENT_ACTION_MAP_DOWNLOAD_SERVICE_STATUS_UPDATES)); }
     * </p>
     *
     * @see #sendBroadcast(SERVICE_ACTION_RESULT, String, Constants.SERVICE_ACTION)
     *
     *
     * @param serviceActionResult {@link SERVICE_ACTION_RESULT#SUCCESSFUL} or {@link SERVICE_ACTION_RESULT#FAILED}
     * @param mapName Unique name of the map
     */
    private void sendBroadcast(@NonNull SERVICE_ACTION_RESULT serviceActionResult,@NonNull String mapName,@NonNull Constants.SERVICE_ACTION serviceAction) {
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

    /**
     * Returns the next {@link MapBoxOfflineQueueTask#TASK_STATUS_INCOMPLETE} {@link MapBoxOfflineQueueTask}
     *
     * @return
     */
    private MapBoxOfflineQueueTask getNextTask() {
        Realm realm = Realm.getDefaultInstance();

        MapBoxOfflineQueueTask mapBoxOfflineQueueTask = realm.where(MapBoxOfflineQueueTask.class)
                .equalTo("taskStatus", MapBoxOfflineQueueTask.TASK_STATUS_INCOMPLETE)
                .findFirst();

        return mapBoxOfflineQueueTask;
    }

    /**
     * Asynchronously retrieves the referenced {@link OfflineRegion}'s {@link OfflineRegionStatus} which provides
     * information about the download progress & if currently downloading
     *
     * @param mapBoxOfflineQueueTask the QueueTask with the {@link OfflineRegion} definition data
     * @param mapBoxAccessToken the MapBox Access Token with which to download the map OR the map was downloaded
     * @param offlineRegionStatusCallback the callback to call once the {@link OfflineRegionStatus} is retrieved
     */
    private void getTaskStatus(@NonNull MapBoxOfflineQueueTask mapBoxOfflineQueueTask,@NonNull String mapBoxAccessToken, OfflineRegionStatusCallback offlineRegionStatusCallback) {
        String mapName = "";

        try {
            if (mapBoxOfflineQueueTask.getTaskType().equals(MapBoxOfflineQueueTask.TASK_TYPE_DELETE)) {
                MapBoxDeleteTask mapBoxDeleteTask = new MapBoxDeleteTask(mapBoxOfflineQueueTask.getTask());
                mapName = mapBoxDeleteTask.getMapName();
            } else {
                MapBoxDownloadTask mapBoxDownloadTask = new MapBoxDownloadTask(mapBoxOfflineQueueTask.getTask());
                mapName = mapBoxDownloadTask.getMapName();
            }

            currentMapDownloadName = mapName;
            MapBoxOfflineResourcesDownloader.getInstance(this, mapBoxAccessToken)
                    .getMapStatus(mapName, offlineRegionStatusCallback);
        } catch (MalformedDataException | JSONException e) {
            Log.e(TAG, Log.getStackTraceString(e));
            if (offlineRegionStatusCallback != null) {
                offlineRegionStatusCallback.onError(e.getMessage());
            }
        }
    }

    /**
     * Shows a non-removable progress notification with a default download icon, the Map Name & percentage
     * progress rounded of to 2 decimal places.
     *
     * @param mapName the unique map name
     * @param percentageProgress Download progress usually between 0-100%
     */
    private void showProgressNotification(@NonNull String mapName, double percentageProgress) {
        if (progressNotificationBuilder == null) {
            progressNotificationBuilder = new NotificationCompat.Builder(MapboxOfflineDownloaderService.this)
                    .setContentTitle("Offline Map Download Progress: " + currentMapDownloadName)
                    .setSmallIcon(R.drawable.ic_stat_file_download);
        }

        progressNotificationBuilder.setContentText("Downloading: " + NumberFormatter.formatDecimal(percentageProgress) + " %");
        startForeground(PROGRESS_NOTIFICATION_ID, progressNotificationBuilder.build());
    }

    /**
     * Shows a customisable & removable notification with a default download icon. The notification
     * provides information for the download such as the Map Name & Map Size.
     * This is called when a map download is completed.
     * <br/><br/>
     *
     * <strong>NOTE: </strong> The Map Size is not the download size but what makes up the Offline Map.
     * The download size might be smaller since already download tiles are not redownloaded<br/>
     *
     *
     * @param title title to be shown on the notification
     * @param description description to be shown on the notification
     */
    private void showDownloadCompleteNotification(@NonNull String title,@NonNull String description) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(MapboxOfflineDownloaderService.this)
                .setContentTitle(title)
                .setContentText(description)
                .setSmallIcon(R.drawable.ic_stat_file_download);

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        LAST_DOWNLOAD_COMPLETE_NOTIFICATION_ID++;
        notificationManager.notify(LAST_DOWNLOAD_COMPLETE_NOTIFICATION_ID, builder.build());
    }

    /**
     * Provides periodic updates about an ongoing {@link OfflineRegion} download.
     *
     * <h3>CAUTION::</h3>
     * <strong>Should only be called to observe an ongoing download. It will otherwise resume
     * download of the {@link OfflineRegion}</strong>
     *
     * @param offlineRegion The {@link OfflineRegion} to observe
     */
    private void observeOfflineRegion(@NonNull final OfflineRegion offlineRegion) {
        //Do not remove the line below!!!
        offlineRegion.setDownloadState(OfflineRegion.STATE_ACTIVE);
        offlineRegion.setObserver(new OfflineRegion.OfflineRegionObserver() {
            @Override
            public void onStatusChanged(OfflineRegionStatus status) {
                MapboxOfflineDownloaderService.this.onStatusChanged(status, offlineRegion);
            }

            @Override
            public void onError(OfflineRegionError error) {
                MapboxOfflineDownloaderService.this.onError(error.getReason(), error.getMessage());
            }

            @Override
            public void mapboxTileCountLimitExceeded(long limit) {
                MapboxOfflineDownloaderService.this.mapboxTileCountLimitExceeded(limit);
            }
        });
    }

    /**
     * Saves a {@link MapBoxOfflineQueueTask} as {@link MapBoxOfflineQueueTask#TASK_STATUS_DONE}<br/>
     * This means the {@link OfflineRegion} can no longer be resumed if it was incomplete.<br/>
     * This also means that a {@link OfflineRegion} will still be in storage if it was not successfully deleted
     *
     * @param mapBoxOfflineQueueTask
     */
    private void persistCompletedStatus(@NonNull MapBoxOfflineQueueTask mapBoxOfflineQueueTask) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();

        mapBoxOfflineQueueTask.setTaskStatus(MapBoxOfflineQueueTask.TASK_STATUS_DONE);
        realm.commitTransaction();
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

    private boolean saveAccessToken(@NonNull String mapBoxAccessToken) {
        SharedPreferences sharedPreferences = getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString(PREFERENCE_MAPBOX_ACCESS_TOKEN, mapBoxAccessToken);
        return editor.commit();
    }

    @Override
    public void onStatusChanged(@NonNull OfflineRegionStatus status,@NonNull OfflineRegion offlineRegion) {
        double percentageDownload = (status.getRequiredResourceCount() >= 0) ? 100.0 * status.getCompletedResourceCount() / status.getRequiredResourceCount() : 0.0;
        sendBroadcast(SERVICE_ACTION_RESULT.SUCCESSFUL, currentMapDownloadName, currentServiceAction, String.valueOf(percentageDownload));

        if (status.isComplete()) {
            showDownloadCompleteNotification("Download for " + currentMapDownloadName + " Map Complete!", "Downloaded " + NumberFormatter.getFriendlyFileSize(this, status.getCompletedResourceSize()) );
            persistCompletedStatus(currentMapBoxTask);
            performNextTask();
        } else {
            showProgressNotification(currentMapDownloadName, percentageDownload);
        }
    }

    @Override
    public void onError(@NonNull String reason,@Nullable String message) {
        String finalMessage = "REASON : " + reason;
        if (message != null && !message.isEmpty()) {
            finalMessage += "\nMESSAGE: " + message;
        }
        Log.e(TAG, finalMessage);
        sendBroadcast(SERVICE_ACTION_RESULT.FAILED, currentMapDownloadName, currentServiceAction, finalMessage);

    }

    @Override
    public void mapboxTileCountLimitExceeded(long limit) {
        String finalMessage = "MapBox Tile Count limit exceeded : " + limit + "while Downloading " + currentMapDownloadName;
        Log.e(TAG, finalMessage);
        sendBroadcast(SERVICE_ACTION_RESULT.FAILED, currentMapDownloadName, currentServiceAction, finalMessage);
    }

}