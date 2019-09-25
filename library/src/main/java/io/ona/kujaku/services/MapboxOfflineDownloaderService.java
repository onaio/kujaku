package io.ona.kujaku.services;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.Log;

import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.net.ConnectivityReceiver;
import com.mapbox.mapboxsdk.offline.OfflineRegion;
import com.mapbox.mapboxsdk.offline.OfflineRegionError;
import com.mapbox.mapboxsdk.offline.OfflineRegionStatus;

import org.json.JSONException;

import io.ona.kujaku.BuildConfig;
import io.ona.kujaku.R;
import io.ona.kujaku.data.MapBoxDeleteTask;
import io.ona.kujaku.data.MapBoxDownloadTask;
import io.ona.kujaku.data.realm.RealmDatabase;
import io.ona.kujaku.data.realm.objects.MapBoxOfflineQueueTask;
import io.ona.kujaku.downloaders.MapBoxOfflineResourcesDownloader;
import io.ona.kujaku.listeners.OfflineRegionObserver;
import io.ona.kujaku.listeners.OfflineRegionStatusCallback;
import io.ona.kujaku.listeners.OnDownloadMapListener;
import io.ona.kujaku.listeners.OnPauseMapDownloadCallback;
import io.ona.kujaku.notifications.CriticalDownloadErrorNotification;
import io.ona.kujaku.notifications.DownloadCompleteNotification;
import io.ona.kujaku.notifications.DownloadProgressNotification;
import io.ona.kujaku.utils.Constants;
import io.ona.kujaku.utils.ObjectCoercer;
import io.ona.kujaku.utils.exceptions.MalformedDataException;
import io.ona.kujaku.utils.exceptions.OfflineMapDownloadException;

/**
 * Service performs Offline Map Download, Offline Map Deletion & Offline Map Download Resumption
 * <p>
 * You need to pass the following in the Intent Extras:
 * - {@link Constants#PARCELABLE_KEY_SERVICE_ACTION} - Required for all
 * - Optional {@link Constants#PARCELABLE_KEY_NETWORK_STATE} - Required for {@link SERVICE_ACTION#NETWORK_RESUME}
 * - Optional {@link Constants#PARCELABLE_KEY_MAP_UNIQUE_NAME} - Required for {@link SERVICE_ACTION#DELETE_MAP} & {@link SERVICE_ACTION#DOWNLOAD_MAP} & {@link SERVICE_ACTION#STOP_CURRENT_DOWNLOAD}
 * - Optional {@link Constants#PARCELABLE_KEY_MAPBOX_ACCESS_TOKEN} - Required for {@link SERVICE_ACTION#DELETE_MAP} & {@link SERVICE_ACTION#DOWNLOAD_MAP} & {@link SERVICE_ACTION#STOP_CURRENT_DOWNLOAD}
 * - Optional {@link Constants#PARCELABLE_KEY_STYLE_URL} - Required for {@link SERVICE_ACTION#DOWNLOAD_MAP}
 * - Optional {@link Constants#PARCELABLE_KEY_MAX_ZOOM} - Required for {@link SERVICE_ACTION#DOWNLOAD_MAP}
 * - Optional {@link Constants#PARCELABLE_KEY_MIN_ZOOM} - Required for {@link SERVICE_ACTION#DOWNLOAD_MAP}
 * - Optional {@link Constants#PARCELABLE_KEY_TOP_LEFT_BOUND} - Required for {@link SERVICE_ACTION#DOWNLOAD_MAP}
 * - Optional {@link Constants#PARCELABLE_KEY_TOP_RIGHT_BOUND} - Required for {@link SERVICE_ACTION#DOWNLOAD_MAP}
 * - Optional {@link Constants#PARCELABLE_KEY_BOTTOM_RIGHT_BOUND} - Required for {@link SERVICE_ACTION#DOWNLOAD_MAP}
 * - Optional {@link Constants#PARCELABLE_KEY_BOTTOM_LEFT_BOUND} - Required for {@link SERVICE_ACTION#DOWNLOAD_MAP}
 * <p>
 * - Optional {@link Constants#PARCELABLE_KEY_DELETE_TASK_TYPE} - Required for {@link SERVICE_ACTION#STOP_CURRENT_DOWNLOAD}
 * </p>
 * <p>
 * The service posts updates through a Local Broadcast with action {@link Constants#INTENT_ACTION_MAP_DOWNLOAD_SERVICE_STATUS_UPDATES}. The updates posted have:
 * <ol>
 * <li>{@code KEY_RESULT_STATUS} - {@code {@link SERVICE_ACTION_RESULT#SUCCESSFUL}} or {@code {@link SERVICE_ACTION_RESULT#FAILED}}</li>
 * <li>{@code KEY_RESULT_MESSAGE} - The simple message eg. download percentage, task failure message</li>
 * <li>{@code {@link Constants#PARCELABLE_KEY_MAP_UNIQUE_NAME}} - The map name</li>
 * <li>{@code KEY_RESULTS_PARENT_ACTION} - {@code {@link SERVICE_ACTION }} being performed on the map</li>
 * </ol>
 * </p>
 * </li>
 * </ol>
 * Any broadcast has the:
 * <ul>
 * <li>{@link MapboxOfflineDownloaderService#KEY_RESULT_STATUS} - Can be either a {@link MapboxOfflineDownloaderService.SERVICE_ACTION_RESULT#SUCCESSFUL} or {@link MapboxOfflineDownloaderService.SERVICE_ACTION_RESULT#FAILED}</li>
 * <li>{@link MapboxOfflineDownloaderService#KEY_RESULT_MESSAGE} - The error or success message</li>
 * <li>{@link MapboxOfflineDownloaderService#KEY_RESULTS_PARENT_ACTION} - Can either be {@link MapboxOfflineDownloaderService.SERVICE_ACTION#DOWNLOAD_MAP} or {@link MapboxOfflineDownloaderService.SERVICE_ACTION#DELETE_MAP}</li>
 * </p>
 * <p>
 * <p>
 *This services uses notification ids from 80 to 2080. The application should therefore use notification ids from 2081
 *
 *
 * Created by Ephraim Kigamba - ekigamba@ona.io on 13/11/2017.
 */
public class MapboxOfflineDownloaderService extends Service implements OfflineRegionObserver, OnDownloadMapListener {

    public enum SERVICE_ACTION_RESULT {
        SUCCESSFUL,
        FAILED
    }

    public enum SERVICE_ACTION {
        DOWNLOAD_MAP,
        DELETE_MAP,
        STOP_CURRENT_DOWNLOAD,
        NETWORK_RESUME
    }

    public static final String KEY_RESULT_STATUS = "RESULT STATUS";
    public static final String KEY_RESULT_MESSAGE = "RESULT MESSAGE";
    public static final String KEY_RESULTS_PARENT_ACTION = "RESULTS PARENT ACTION";
    private static final String TAG = MapboxOfflineDownloaderService.class.getSimpleName();

    public static final String MY_PREFERENCES = "KUJAKU PREFERENCES";
    public static final String PREFERENCE_MAPBOX_ACCESS_TOKEN = "MAPBOX ACCESS TOKEN";

    private String mapBoxAccessToken = "";
    private String currentMapDownloadName;
    private SERVICE_ACTION currentServiceAction;
    private MapBoxOfflineQueueTask currentMapBoxTask;
    private long currentMapDownloadId;

    private DownloadProgressNotification downloadProgressNotification;
    public static final int PROGRESS_NOTIFICATION_ID = 80;
    public static final int REQUEST_ID_STOP_MAP_DOWNLOAD = 1;

    // Should persist across service instances but within the same app session
    public static int LAST_DOWNLOAD_COMPLETE_NOTIFICATION_ID = 81;
    public static int LAST_DOWNLOAD_ERROR_NOTIFICATION_ID = 1081;

    private boolean isPerformingTask = false;

    private RealmDatabase realmDatabase;

    @RestrictTo(RestrictTo.Scope.TESTS)
    public boolean onStartCommandCalled = false;
    @RestrictTo(RestrictTo.Scope.TESTS)
    public boolean persistOfflineMapTaskCalled = false;
    @RestrictTo(RestrictTo.Scope.TESTS)
    public boolean performNextTaskCalled = false;
    @RestrictTo(RestrictTo.Scope.TESTS)
    public boolean observeOfflineRegionCalled = false;

    /* FOR THE DOWNLOAD PROGRESS UPDATE THREAD */
    private double mostRecentPercentageUpdate = 0;
    private String mostRecentMapNameUpdate = "";
    private long timeBetweenUpdates = 800;
    private boolean hasUpdateToPost = false;
    private Thread progressUpdateThread;
    private boolean shouldThreadDie = true;
    private Handler serviceHandler;

    private boolean shownForegroundNotification = false;

    private long tileDownloadLimit=6000;

    public MapboxOfflineDownloaderService() {
        super();
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        serviceHandler = new Handler(Looper.myLooper());
        realmDatabase = RealmDatabase.init(this);

        super.onStartCommand(intent, flags, startId);
        onStartCommandCalled = true;
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
     * @param intent Intent passed when the service was called {@link Context#startService(Intent)}
     * @return {@code TRUE} if the OfflineMapTask was successfully saved, {@code FALSE} if the OfflineMapTask could not be saved
     */
    protected boolean persistOfflineMapTask(@Nullable Intent intent) {
        persistOfflineMapTaskCalled = true;
        if (intent == null) {
            return false;
        }

        Bundle extras = intent.getExtras();
        if (extras != null
                && extras.containsKey(Constants.PARCELABLE_KEY_SERVICE_ACTION)) {
            final SERVICE_ACTION serviceAction = (SERVICE_ACTION) extras.get(Constants.PARCELABLE_KEY_SERVICE_ACTION);

            if (extras.containsKey(Constants.PARCELABLE_KEY_MAP_UNIQUE_NAME)
                    && extras.containsKey(Constants.PARCELABLE_KEY_MAPBOX_ACCESS_TOKEN)) {
                final String mapUniqueName = extras.getString(Constants.PARCELABLE_KEY_MAP_UNIQUE_NAME);
                mapBoxAccessToken = extras.getString(Constants.PARCELABLE_KEY_MAPBOX_ACCESS_TOKEN);

                Mapbox.getInstance(this, mapBoxAccessToken);

                MapBoxDownloadTask downloadTask = new MapBoxDownloadTask();
                downloadTask.setMapName(mapUniqueName);
                downloadTask.setMapBoxAccessToken(mapBoxAccessToken);

                if (serviceAction == SERVICE_ACTION.DOWNLOAD_MAP) {
                    if (extras.containsKey(Constants.PARCELABLE_KEY_STYLE_URL)
                            && extras.containsKey(Constants.PARCELABLE_KEY_MAX_ZOOM)
                            && extras.containsKey(Constants.PARCELABLE_KEY_MIN_ZOOM)
                            && extras.containsKey(Constants.PARCELABLE_KEY_TOP_LEFT_BOUND)
                            && extras.containsKey(Constants.PARCELABLE_KEY_TOP_RIGHT_BOUND)
                            && extras.containsKey(Constants.PARCELABLE_KEY_BOTTOM_RIGHT_BOUND)
                            && extras.containsKey(Constants.PARCELABLE_KEY_BOTTOM_LEFT_BOUND)) {

                        downloadTask.setPackageName("kl");
                        downloadTask.setMapBoxStyleUrl(extras.getString(Constants.PARCELABLE_KEY_STYLE_URL));
                        downloadTask.setMaxZoom(ObjectCoercer.coerceNumberObjectToDoublePrimitive(extras.get(Constants.PARCELABLE_KEY_MAX_ZOOM)));
                        downloadTask.setMinZoom(ObjectCoercer.coerceNumberObjectToDoublePrimitive(extras.get(Constants.PARCELABLE_KEY_MIN_ZOOM)));
                        downloadTask.setTopLeftBound((LatLng) extras.getParcelable(Constants.PARCELABLE_KEY_TOP_LEFT_BOUND));
                        downloadTask.setTopRightBound((LatLng) extras.getParcelable(Constants.PARCELABLE_KEY_TOP_RIGHT_BOUND));
                        downloadTask.setBottomRightBound((LatLng) extras.getParcelable(Constants.PARCELABLE_KEY_BOTTOM_RIGHT_BOUND));
                        downloadTask.setBottomLeftBound((LatLng) extras.getParcelable(Constants.PARCELABLE_KEY_BOTTOM_LEFT_BOUND));

                        realmDatabase.deletePendingOfflineMapDownloadsWithSimilarNames(mapUniqueName);

                        MapBoxDownloadTask.constructMapBoxOfflineQueueTask(downloadTask);
                        return true;
                    }
                } else if (serviceAction == SERVICE_ACTION.DELETE_MAP) {
                    MapBoxDeleteTask deleteTask = new MapBoxDeleteTask();
                    deleteTask.setMapBoxAccessToken(mapBoxAccessToken);
                    deleteTask.setMapName(mapUniqueName);

                    MapBoxDeleteTask.constructMapBoxOfflineQueueTask(deleteTask);

                    return true;
                } else {
                    final String taskType = extras.getString(Constants.PARCELABLE_KEY_DELETE_TASK_TYPE, "");

                    if (!TextUtils.isEmpty(taskType)) {
                        if (taskType.equals(MapBoxOfflineQueueTask.TASK_TYPE_DOWNLOAD) && mostRecentMapNameUpdate.equals(mapUniqueName)) {
                            MapBoxOfflineResourcesDownloader mapBoxOfflineResourcesDownloader = MapBoxOfflineResourcesDownloader.getInstance(this, mapBoxAccessToken);

                            // Remove the STOP DOWNLOAD ACTION from the notification so that it cannot be pressed and cause the app to crash!
                            showProgressNotification(mostRecentMapNameUpdate, mostRecentPercentageUpdate, false);

                            // Stop the download first
                            mapBoxOfflineResourcesDownloader.pauseMapDownload(mapUniqueName, new OnPauseMapDownloadCallback() {
                                @Override
                                public void onPauseSuccess() {
                                    MapBoxOfflineResourcesDownloader
                                            .getInstance(MapboxOfflineDownloaderService.this, mapBoxAccessToken)
                                            .deleteMap(mapUniqueName, new OfflineRegion.OfflineRegionDeleteCallback() {

                                                @Override
                                                public void onDelete() {
                                                    if (deleteTaskFromRealmDatabase(taskType, mapUniqueName)) {
                                                        sendBroadcast(SERVICE_ACTION_RESULT.SUCCESSFUL, mapUniqueName, SERVICE_ACTION.STOP_CURRENT_DOWNLOAD);
                                                        showDownloadCompleteNotification(String.format(getString(R.string.notification_stopped_download_title), mapUniqueName), getString(R.string.notification_stopped_download_content));
                                                    } else {
                                                        sendBroadcast(SERVICE_ACTION_RESULT.FAILED, mapUniqueName, SERVICE_ACTION.STOP_CURRENT_DOWNLOAD, getString(R.string.map_delete_task_error));
                                                    }
                                                    releaseQueueToPerformOtherJobs();

                                                    performNextTask();
                                                }

                                                @Override
                                                public void onError(String error) {
                                                    sendBroadcast(SERVICE_ACTION_RESULT.FAILED, mapUniqueName, SERVICE_ACTION.STOP_CURRENT_DOWNLOAD, error);
                                                }
                                            });
                                }

                                @Override
                                public void onPauseError(String error) {
                                    sendBroadcast(SERVICE_ACTION_RESULT.FAILED, mapUniqueName, SERVICE_ACTION.STOP_CURRENT_DOWNLOAD, String.format(getString(R.string.error_broadcast_for_download_pause), error));
                                }
                            });
                        } else {
                            if (deleteTaskFromRealmDatabase(taskType, mapUniqueName)) {
                                // Communicate if the delete task was successful
                                sendBroadcast(SERVICE_ACTION_RESULT.SUCCESSFUL, mapUniqueName, SERVICE_ACTION.STOP_CURRENT_DOWNLOAD);
                            } else {
                                // Send a broadcast that the delete task was unsuccessful because the task could not be found
                                sendBroadcast(SERVICE_ACTION_RESULT.FAILED, mapUniqueName, SERVICE_ACTION.STOP_CURRENT_DOWNLOAD, String.format(getString(R.string.error_broadcast_for_download_pause), "Map could not be found"));
                            }
                        }
                    }

                }

            }

        }

        return false;
    }

    private boolean deleteTaskFromRealmDatabase(@NonNull String taskType, @NonNull String mapUniqueName) {
        boolean isDownloadTask = taskType.equals(MapBoxOfflineQueueTask.TASK_TYPE_DOWNLOAD);
        return realmDatabase.deleteTask(mapUniqueName, isDownloadTask);
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
        performNextTaskCalled = true;

        if (isPerformingTask) {
            return;
        }

        final MapBoxOfflineQueueTask mapBoxOfflineQueueTask = realmDatabase.getNextTask();

        if (mapBoxOfflineQueueTask != null) {
            placeQueueOnHold();
            currentMapBoxTask = mapBoxOfflineQueueTask;
            if (MapBoxOfflineQueueTask.TASK_TYPE_DELETE.equals(mapBoxOfflineQueueTask.getTaskType())) {
                currentServiceAction = SERVICE_ACTION.DELETE_MAP;
            } else {
                currentServiceAction = SERVICE_ACTION.DOWNLOAD_MAP;

                if (mapBoxOfflineQueueTask.getTaskStatus() == MapBoxOfflineQueueTask.TASK_STATUS_NOT_STARTED) {
                    realmDatabase.persistDownloadStartedStatus(mapBoxOfflineQueueTask);
                }
            }

            getTaskStatus(mapBoxOfflineQueueTask, mapBoxAccessToken, new OfflineRegionStatusCallback() {
                @Override
                public void onStatus(OfflineRegionStatus status, OfflineRegion offlineRegion) {

                    if (MapBoxOfflineQueueTask.TASK_TYPE_DELETE.equals(mapBoxOfflineQueueTask.getTaskType())) {
                        currentServiceAction = SERVICE_ACTION.DELETE_MAP;
                        MapBoxOfflineResourcesDownloader.getInstance(MapboxOfflineDownloaderService.this, mapBoxAccessToken)
                                .deleteMap(currentMapDownloadName, new OfflineRegion.OfflineRegionDeleteCallback() {
                                    @Override
                                    public void onDelete() {
                                        sendBroadcast(SERVICE_ACTION_RESULT.SUCCESSFUL, currentMapDownloadName, SERVICE_ACTION.DELETE_MAP, "Map deleted successfully!");
                                        releaseQueueToPerformOtherJobs();
                                        realmDatabase.persistCompletedStatus(mapBoxOfflineQueueTask);
                                        performNextTask();
                                    }

                                    @Override
                                    public void onError(String error) {
                                        MapboxOfflineDownloaderService.this.onError(error, null);
                                        // An error means this cannot be solved even at a later time THUS persist the task as DONE
                                        releaseQueueToPerformOtherJobs();
                                        realmDatabase.persistCompletedStatus(mapBoxOfflineQueueTask);
                                        performNextTask();
                                    }
                                });
                        return;
                    }

                    if (status.getDownloadState() == OfflineRegion.STATE_ACTIVE) {
                        // TASK IS RUNNING
                        currentServiceAction = SERVICE_ACTION.DOWNLOAD_MAP;
                        currentMapBoxTask = mapBoxOfflineQueueTask;
                        currentMapDownloadId = offlineRegion.getID();
                        startDownloadProgressUpdater();
                        observeOfflineRegion(offlineRegion);
                    } else {
                        if (!status.isComplete()) {
                            // TASK IS NOT RUNNING
                            currentServiceAction = SERVICE_ACTION.DOWNLOAD_MAP;
                            currentMapBoxTask = mapBoxOfflineQueueTask;
                            currentMapDownloadId = offlineRegion.getID();
                            // Set the progress notification
                            startDownloadProgressUpdater();
                            queueDownloadProgressUpdate(currentMapDownloadName, 0.0);
                            showProgressNotification(currentMapDownloadName, 0.0);
                            MapBoxOfflineResourcesDownloader.getInstance(MapboxOfflineDownloaderService.this, mapBoxAccessToken)
                                    .withTileDownloadLimit(tileDownloadLimit)
                                    .resumeMapDownload(offlineRegion, MapboxOfflineDownloaderService.this);
                        } else {
                            // IGNORE IT AND SEND A BROADCAST HERE
                            realmDatabase.persistCompletedStatus(mapBoxOfflineQueueTask);
                            MapboxOfflineDownloaderService.this.onError(getString(R.string.error_similar_map_exists_and_downloaded), null);
                            releaseQueueToPerformOtherJobs();
                        }
                    }
                }

                @Override
                public void onError(String error) {
                    if (error.contains("Map could not be found")) {
                        if (MapBoxOfflineQueueTask.TASK_TYPE_DOWNLOAD.equals(mapBoxOfflineQueueTask.getTaskType())) {

                            try {
                                startDownloadProgressUpdater();
                                MapBoxOfflineResourcesDownloader.getInstance(MapboxOfflineDownloaderService.this, mapBoxAccessToken)
                                        .withTileDownloadLimit(tileDownloadLimit)
                                        .downloadMap(new MapBoxDownloadTask(mapBoxOfflineQueueTask.getTask()), MapboxOfflineDownloaderService.this);

                                //Set the progress notification
                                queueDownloadProgressUpdate(currentMapDownloadName, 0.0);
                                showProgressNotification(currentMapDownloadName, 0.0);
                            } catch (MalformedDataException | JSONException | OfflineMapDownloadException e) {
                                Log.e(TAG, Log.getStackTraceString(e));
                                releaseQueueToPerformOtherJobs();
                            }
                        } else {
                            // An error means this cannot be solved even at a later time THUS persist the task as DONE
                            String message;
                            String taskType = mapBoxOfflineQueueTask.getTaskType();
                            if (MapBoxOfflineQueueTask.TASK_TYPE_DOWNLOAD.equals(taskType)) {
                                message = getString(R.string.map_could_not_be_downloaded);
                            } else if (MapBoxOfflineQueueTask.TASK_TYPE_DELETE.equals(taskType)) {
                                message = getString(R.string.map_could_not_be_deleted);
                            } else  {
                                message = getString(R.string.map_download_could_not_be_stopped);
                            }

                            MapboxOfflineDownloaderService.this.onError(error, message);
                            releaseQueueToPerformOtherJobs();
                            realmDatabase.persistCompletedStatus(mapBoxOfflineQueueTask);
                            performNextTask();
                        }
                    } else {
                        MapboxOfflineDownloaderService.this.onError(error, null);
                        releaseQueueToPerformOtherJobs();
                    }
                }
            });
        } else {
            cleanupAndExit();
        }
    }

    private void cleanupAndExit() {
        stopDownloadProgressUpdater();
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(PROGRESS_NOTIFICATION_ID);
        stopSelf();
    }

    /**
     * Sends a local broadcast with the result of a service operation & mapName. To capture the
     * local broadcast messages, you need to use the {@link LocalBroadcastManager} to register a
     * {@link android.content.BroadcastReceiver} for action {@link Constants#INTENT_ACTION_MAP_DOWNLOAD_SERVICE_STATUS_UPDATES}
     * <p>
     * The broadcast has the following extras:
     * <ol>
     * <li>{@link MapboxOfflineDownloaderService#KEY_RESULT_STATUS} - Either SUCCESSFUL or FAILED. Type {@link MapboxOfflineDownloaderService.SERVICE_ACTION_RESULT}</li>
     * <li>{@link MapboxOfflineDownloaderService#KEY_RESULT_MESSAGE} - User-friendly and/or descriptive message of the result. Type {@link String}</li>
     * <li>{@link Constants#PARCELABLE_KEY_MAP_UNIQUE_NAME} - The Map's unique name. Type {@code {@link String}}</li>
     * <li>{@link MapboxOfflineDownloaderService#KEY_RESULTS_PARENT_ACTION} - The action that was performed to produce this result {@link MapboxOfflineDownloaderService.SERVICE_ACTION_RESULT}</li>
     * </ol>
     * <p>
     * <p>
     * <h3>Sample Usage</h3>
     * {@code LocalBroadcastManager.getInstance(context).registerReceiver(myBroadcastReceiver, new IntentFilter(utils.Constants.INTENT_ACTION_MAP_DOWNLOAD_SERVICE_STATUS_UPDATES)); }
     * </p>
     *
     * @param serviceActionResult {@link SERVICE_ACTION_RESULT#SUCCESSFUL} or {@link SERVICE_ACTION_RESULT#FAILED}
     * @param mapName             Unique name of the map
     * @param message             Additional message/information about the result eg. For a {@link SERVICE_ACTION_RESULT#FAILED} result
     */
    private void sendBroadcast(@NonNull SERVICE_ACTION_RESULT serviceActionResult, @NonNull String mapName, SERVICE_ACTION serviceAction, @NonNull String message) {
        Intent intent = new Intent();
        intent.setAction(Constants.INTENT_ACTION_MAP_DOWNLOAD_SERVICE_STATUS_UPDATES);
        intent.putExtra(KEY_RESULT_STATUS, serviceActionResult.name());
        intent.putExtra(KEY_RESULT_MESSAGE, message);
        intent.putExtra(Constants.PARCELABLE_KEY_MAP_UNIQUE_NAME, mapName);
        intent.putExtra(KEY_RESULTS_PARENT_ACTION, serviceAction);

        LocalBroadcastManager.getInstance(this)
                .sendBroadcast(intent);
    }

    /**
     * Sends a local broadcast with the result of a service operation & mapName. To capture the
     * local broadcast messages, you need to use the {@link LocalBroadcastManager} to register a
     * {@link android.content.BroadcastReceiver} for action {@link Constants#INTENT_ACTION_MAP_DOWNLOAD_SERVICE_STATUS_UPDATES}
     * <p>
     * The broadcast has the following extras:
     * <ol>
     * <li>{@link MapboxOfflineDownloaderService#KEY_RESULT_STATUS} - Either SUCCESSFUL or FAILED. Type {@link MapboxOfflineDownloaderService.SERVICE_ACTION_RESULT}</li>
     * <li>{@link MapboxOfflineDownloaderService#KEY_RESULT_MESSAGE} - User-friendly and/or descriptive message of the result. Type {@link String}</li>
     * <li>{@link Constants#PARCELABLE_KEY_MAP_UNIQUE_NAME} - The Map's unique name. Type {@code {@link String}}</li>
     * <li>{@link MapboxOfflineDownloaderService#KEY_RESULTS_PARENT_ACTION} - The action that was performed to produce this result {@link MapboxOfflineDownloaderService.SERVICE_ACTION_RESULT}</li>
     * </ol>
     * <p>
     * <p>
     * <h3>Sample Usage</h3>
     * {@code LocalBroadcastManager.getInstance(context).registerReceiver(myBroadcastReceiver, new IntentFilter(utils.Constants.INTENT_ACTION_MAP_DOWNLOAD_SERVICE_STATUS_UPDATES)); }
     * </p>
     *
     * @param serviceActionResult {@link SERVICE_ACTION_RESULT#SUCCESSFUL} or {@link SERVICE_ACTION_RESULT#FAILED}
     * @param mapName             Unique name of the map
     * @see #sendBroadcast(SERVICE_ACTION_RESULT, String, SERVICE_ACTION)
     */
    private void sendBroadcast(@NonNull SERVICE_ACTION_RESULT serviceActionResult, @NonNull String mapName, @NonNull SERVICE_ACTION serviceAction) {
        sendBroadcast(serviceActionResult, mapName, serviceAction, "");
    }

    /**
     * Asynchronously retrieves the referenced {@link OfflineRegion}'s {@link OfflineRegionStatus} which provides
     * information about the download progress & if currently downloading
     *
     * @param mapBoxOfflineQueueTask      the QueueTask with the {@link OfflineRegion} definition data
     * @param mapBoxAccessToken           the MapBox Access Token with which to download the map OR the map was downloaded
     * @param offlineRegionStatusCallback the callback to call once the {@link OfflineRegionStatus} is retrieved
     */
    private void getTaskStatus(@NonNull MapBoxOfflineQueueTask mapBoxOfflineQueueTask, @NonNull String mapBoxAccessToken, OfflineRegionStatusCallback offlineRegionStatusCallback) {
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
     * @param mapName            the unique map name
     * @param percentageProgress Download progress usually between 0-100%
     */
    private void showProgressNotification(@NonNull String mapName, double percentageProgress, boolean showAction) {
        if (downloadProgressNotification == null) {
            downloadProgressNotification = new DownloadProgressNotification(this);
            downloadProgressNotification.createInitialNotification(mapName, BuildConfig.MAPBOX_SDK_ACCESS_TOKEN, REQUEST_ID_STOP_MAP_DOWNLOAD, showAction);
        }

        downloadProgressNotification.updateNotification(percentageProgress, mapName, REQUEST_ID_STOP_MAP_DOWNLOAD, showAction);

        if (!shownForegroundNotification) {
            downloadProgressNotification.displayForegroundNotification(PROGRESS_NOTIFICATION_ID);
            shownForegroundNotification = true;
        } else {
            downloadProgressNotification.displayNotification(PROGRESS_NOTIFICATION_ID);
        }
    }

    private void showProgressNotification(@NonNull String mapName,  double percentageProgress) {
        showProgressNotification(mapName, percentageProgress, true);
    }

    /**
     * Shows a customisable & removable notification with a default download icon. The notification
     * provides information for the download such as the Map Name & Map Size.
     * This is called when a map download is completed.
     * <br/><br/>
     * <p>
     * <strong>NOTE: </strong> The Map Size is not the download size but what makes up the Offline Map.
     * The download size might be smaller since already download tiles are not redownloaded<br/>
     *
     * @param title       title to be shown on the notification
     * @param description description to be shown on the notification
     */
    private void showDownloadCompleteNotification(@NonNull String title, @NonNull String description) {
        LAST_DOWNLOAD_COMPLETE_NOTIFICATION_ID++;

        DownloadCompleteNotification downloadCompleteNotification = new DownloadCompleteNotification(this);
        downloadCompleteNotification.displayNotification(title, description, LAST_DOWNLOAD_COMPLETE_NOTIFICATION_ID);
    }

    /**
     * Provides periodic updates about an ongoing {@link OfflineRegion} download.
     * <p>
     * <h3>CAUTION::</h3>
     * <strong>Should only be called to observe an ongoing download. It will otherwise resume
     * download of the {@link OfflineRegion}</strong>
     *
     * @param offlineRegion The {@link OfflineRegion} to observe
     */
    private void observeOfflineRegion(@NonNull final OfflineRegion offlineRegion) {
        //Do not remove the line below!!!
        offlineRegion.setDownloadState(OfflineRegion.STATE_ACTIVE);
        observeOfflineRegionCalled = true;
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

    @Override
    public void onStatusChanged(@NonNull OfflineRegionStatus status, @NonNull OfflineRegion offlineRegion) {
        double percentageDownload = (status.getRequiredResourceCount() >= 0) ? 100.0 * status.getCompletedResourceCount() / status.getRequiredResourceCount() : 0.0;
        sendBroadcast(SERVICE_ACTION_RESULT.SUCCESSFUL, currentMapDownloadName, currentServiceAction, String.valueOf(percentageDownload));

        if (status.isComplete()) {
            stopDownloadProgressUpdater();
            showDownloadCompleteNotification(String.format(getString(R.string.notification_download_complete_title), currentMapDownloadName), String.format(getString(R.string.notification_download_complete_content), getFriendlyFileSize(status.getCompletedResourceSize())));

            realmDatabase.persistCompletedStatus(currentMapBoxTask);
            releaseQueueToPerformOtherJobs();
            performNextTask();
        } else {
            queueDownloadProgressUpdate(currentMapDownloadName, percentageDownload);
        }
    }

    @Override
    public void onError(String error) {
        Log.e(TAG, error);
        sendBroadcast(SERVICE_ACTION_RESULT.FAILED, currentMapDownloadName, currentServiceAction, error);
    }

    @Override
    public void onError(@NonNull String reason, @Nullable String message) {
        String finalMessage = String.format(getString(R.string.error_broadcast_message_format_part_reason), reason);
        if (message != null && !message.isEmpty()) {
            finalMessage += String.format(getString(R.string.error_broadcast_message_format_part_message), message);
        }
        onError(finalMessage);
    }

    @Override
    public void mapboxTileCountLimitExceeded(long limit) {
        String finalMessage = String.format(getString(R.string.error_mapbox_tile_count_limit), limit, currentMapDownloadName);
        Log.e(TAG, finalMessage);
        sendBroadcast(SERVICE_ACTION_RESULT.FAILED, currentMapDownloadName, SERVICE_ACTION.DOWNLOAD_MAP, finalMessage);

        // Show download error notification
        LAST_DOWNLOAD_ERROR_NOTIFICATION_ID++;
        CriticalDownloadErrorNotification criticalDownloadErrorNotification = new CriticalDownloadErrorNotification(this);
        criticalDownloadErrorNotification.displayNotification(String.format(getString(R.string.error_occurred_download_map), currentMapDownloadName)
                , String.format(getString(R.string.mapbox_tile_count_limit_of_exceeded), limit), LAST_DOWNLOAD_ERROR_NOTIFICATION_ID);

        // No other download can be performed and we should exit the service
        cleanupAndExit();
    }

    private String getFriendlyFileSize(long bytes) {
        return Formatter.formatFileSize(this, bytes);
    }

    private void queueDownloadProgressUpdate(@NonNull String mapNameUpdate, @NonNull double progressUpdate) {
        hasUpdateToPost = true;
        mostRecentPercentageUpdate = progressUpdate;
        mostRecentMapNameUpdate = mapNameUpdate;
        shouldThreadDie = false;
    }

    private void startDownloadProgressUpdater() {
        shouldThreadDie = false;
        if (progressUpdateThread == null) {
            progressUpdateThread = new Thread(new Runnable() {
                @Override
                public void run() {

                    while (!shouldThreadDie) {
                        try {
                            Thread.sleep(timeBetweenUpdates);

                            if (hasUpdateToPost && !shouldThreadDie) {
                                if (serviceHandler != null) {
                                    serviceHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            showProgressNotification(mostRecentMapNameUpdate, mostRecentPercentageUpdate);
                                        }
                                    });
                                }
                                hasUpdateToPost = false;
                            }

                        } catch (InterruptedException e) {
                            Log.e(TAG, Log.getStackTraceString(e));
                        }
                    }
                }
            });

            progressUpdateThread.start();
        }

        if (!progressUpdateThread.isAlive()) {
            progressUpdateThread.start();
        }
    }

    private void placeQueueOnHold() {
        isPerformingTask = true;
    }

    private void releaseQueueToPerformOtherJobs() {
        isPerformingTask = false;
    }

    private void stopDownloadProgressUpdater() {
        shouldThreadDie = true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopDownloadProgressUpdater();

        if (MapBoxOfflineResourcesDownloader.getInstance(this, BuildConfig.MAPBOX_SDK_ACCESS_TOKEN)
                .getConnectivityReceiverActivationCounter() > 0) {
            ConnectivityReceiver.instance(this)
                    .deactivate();
        }
    }
}