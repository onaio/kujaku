package io.ona.kujaku.sample.activities;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.mapbox.mapboxsdk.geometry.LatLng;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;
import io.ona.kujaku.KujakuLibrary;
import io.ona.kujaku.callables.AsyncTaskCallable;
import io.ona.kujaku.domain.Point;
import io.ona.kujaku.helpers.storage.MapBoxStyleStorage;
import io.ona.kujaku.helpers.MapBoxWebServiceApi;
import io.ona.kujaku.helpers.OfflineServiceHelper;
import io.ona.kujaku.listeners.OnFinishedListener;
import io.ona.kujaku.sample.BuildConfig;
import io.ona.kujaku.sample.MyApplication;
import io.ona.kujaku.sample.R;
import io.ona.kujaku.services.MapboxOfflineDownloaderService;
import io.ona.kujaku.tasks.GenericAsyncTask;
import io.ona.kujaku.utils.Constants;
import io.ona.kujaku.utils.Permissions;

import static io.ona.kujaku.utils.Constants.MAP_ACTIVITY_REQUEST_CODE;
import static io.ona.kujaku.utils.Constants.NEW_FEATURE_POINTS_JSON;

public class MainActivity extends BaseNavigationDrawerActivity {

    private EditText topLeftLatEd;
    private EditText topLeftLngEd;
    private EditText bottomRightLatEd;
    private EditText bottomRightLngEd;
    private EditText mapNameEd;
    private EditText topRightLatEd;
    private EditText topRightLngEd;
    private EditText bottomLeftLatEd;
    private EditText bottomLeftLngEd;
    private EditText mapNameToDeleteEd;

    private Button stopMapDownloadBtn;

    private static final String SAMPLE_JSON_FILE_NAME = "2017-nov-27-kujaku-metadata.json";
    private static final int PERMISSIONS_REQUEST_CODE = 9823;
    private String[] basicPermissions = new String[]{
            //Manifest.permission.WRITE_EXTERNAL_STORAGE,
            //Manifest.permission.READ_EXTERNAL_STORAGE
    };

    // Kujaku library uses notification ids 80 to 2080
    private int lastNotificationId = 2081;
    private final static String TAG = MainActivity.class.getSimpleName();

    private List<Point> points;

    private Activity mainActivity = this;

    private String currentMapDownload;
    private boolean canStopMapDownload = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestBasicPermissions();

        bottomRightLatEd = findViewById(R.id.edt_mainActivity_bottomRightLatitude);
        bottomRightLngEd = findViewById(R.id.edt_mainActivity_bottomRightLongitude);
        topLeftLatEd = findViewById(R.id.edt_mainActivity_topLeftLatitude);
        topLeftLngEd = findViewById(R.id.edt_mainActivity_topLeftLongitude);
        bottomLeftLatEd = findViewById(R.id.edt_mainActivity_bottomLeftLatitude);
        bottomLeftLngEd = findViewById(R.id.edt_mainActivity_bottomLeftLongitude);
        topRightLatEd = findViewById(R.id.edt_mainActivity_topRightLatitude);
        topRightLngEd = findViewById(R.id.edt_mainActivity_topRightLongitude);
        mapNameToDeleteEd = findViewById(R.id.edt_mainActivity_mapNameToDelete);

        stopMapDownloadBtn = findViewById(R.id.btn_mainActivity_stopOfflineDownloadUsingHelper);
        Button deleteMapDownloadBtn = findViewById(R.id.btn_mainActivity_deleteOfflineDownloadUsingHelper);

        mapNameEd = findViewById(R.id.edt_mainActivity_mapName);

        Button startOfflineDownload = findViewById(R.id.btn_mainActivity_startOfflineDownload);
        startOfflineDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloadMap(false);
            }
        });

        Button startOfflineDownloadUsingHelper = findViewById(R.id.btn_mainActivity_startOfflineDownloadUsingHelper);
        startOfflineDownloadUsingHelper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloadMap(true);
            }
        });

        stopMapDownloadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(currentMapDownload)) {
                    stopMapDownload(currentMapDownload);
                }
            }
        });

        deleteMapDownloadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mapNameToDelete = mapNameToDeleteEd.getText().toString();

                if (!TextUtils.isEmpty(mapNameToDelete)) {
                    OfflineServiceHelper.deleteOfflineMap(MainActivity.this, mapNameToDelete, BuildConfig.MAPBOX_SDK_ACCESS_TOKEN);
                }
            }
        });

        final EditText mapBoxStyleUrl = (EditText) findViewById(R.id.edt_mainActivity_mapboxStyleURL);
        mapBoxStyleUrl.setText("mapbox://styles/ona/cj9jueph7034i2rphe0gp3o6m");
        Button btnDownloadMapBoxStyle = (Button) findViewById(R.id.btn_mainActivity_downloadMapboxStyle);
        btnDownloadMapBoxStyle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloadMapBoxStyle(mapBoxStyleUrl.getText().toString());
            }
        });

        setTitle(R.string.main_activity_title);

        // Fetch previously dropped points
        final OnFinishedListener onPointsFetchFinishedListener = new OnFinishedListener() {
            @Override
            public void onSuccess(Object[] objects) {
                points = (List<Point>) objects[0];
                KujakuLibrary.getInstance().launchMapActivity(mainActivity, BuildConfig.MAPBOX_SDK_ACCESS_TOKEN, points, true);
            }
            @Override
            public void onError(Exception e) {
                Log.e(TAG, Log.getStackTraceString(e));
            }
        };

        Button btnLaunchKujakuMap = findViewById(R.id.btn_mainActivity_launchKujakuMap);
        btnLaunchKujakuMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: will need to figure out how to get new points added after initial MainActivity instantiation
                if (points == null || points.size() == 0) {
                    fetchDroppedPoints(onPointsFetchFinishedListener);
                } else {
                    KujakuLibrary.getInstance().launchMapActivity(mainActivity, BuildConfig.MAPBOX_SDK_ACCESS_TOKEN, points, true);
                }
            }
        });
        registerLocalBroadcastReceiver();

        Button btnOpenMapActivity = findViewById(R.id.btn_mainActivity_openMapActivity);
        btnOpenMapActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: will need to figure out how to get new points added after initial MainActivity instantiation
                if (points == null || points.size() == 0) {
                    fetchDroppedPoints(onPointsFetchFinishedListener);
                } else {
                    KujakuLibrary.getInstance().launchMapActivity(mainActivity, BuildConfig.MAPBOX_SDK_ACCESS_TOKEN, points, true);
                }
            }
        });
    }


    private void fetchDroppedPoints(OnFinishedListener onFinishedListener) {
        GenericAsyncTask genericAsyncTask = new GenericAsyncTask(new AsyncTaskCallable() {
            @Override
            public Object[] call() throws Exception {
                List<Point> droppedPoints = MyApplication.getInstance().getPointsRepository().getAllPoints();
                return new Object[]{droppedPoints};
            }
        });
        genericAsyncTask.setOnFinishedListener(onFinishedListener);
        genericAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void setCanStopMapDownload(boolean enabled) {
        /*
         * Purpose of this:
         * 1. Is not have to call stopMapDownloadBtn.setVisibility(enabled ? View.VISIBLE : View.GONE);
         * repetitively if the desired state is current.
         * 2. Also, views can tend to be unreactive to events if you update them too frequently.
         * Map progress updates are expected to be frequent
         */
        if (canStopMapDownload != enabled) {
            stopMapDownloadBtn.setVisibility(enabled ? View.VISIBLE : View.GONE);
        }

        canStopMapDownload = enabled;
    }

    @Override
    protected int getContentView() {
        return R.layout.activity_main;
    }

    @Override
    protected int getSelectedNavigationItem() {
        return R.id.nav_main_activity;
    }

    private void downloadMap(boolean useDownloadHelper) {
        double topLeftLat = 37.7897;
        double topLeftLng = -119.5073;
        double bottomRightLat = 37.6744;
        double bottomRightLng = -119.6815;
        double topRightLat = 37.7897;
        double topRightLng = -119.6815;
        double bottomLeftLat = 37.6744;
        double bottomLeftLng = -119.5073;

        String tllatE = topLeftLatEd.getText().toString();
        String tllngE = topLeftLngEd.getText().toString();
        String brlatE = bottomRightLatEd.getText().toString();
        String brlngE = bottomRightLngEd.getText().toString();
        String trLatE = topRightLatEd.getText().toString();
        String trLngE = topRightLngEd.getText().toString();
        String blLatE = bottomLeftLatEd.getText().toString();
        String blLngE = bottomLeftLngEd.getText().toString();

        String mapName = mapNameEd.getText().toString();
        if (mapName.isEmpty()) {
            Toast.makeText(this, "Please enter a Map Name!", Toast.LENGTH_LONG)
                    .show();
            return;
        }

        if (isValidDouble(tllatE) && isValidDouble(tllngE) && isValidDouble(brlatE) && isValidDouble(brlngE)
                && isValidDouble(trLatE) && isValidDouble(trLngE) && isValidDouble(blLatE) && isValidDouble(blLngE)) {
            topLeftLat = Double.valueOf(tllatE);
            topLeftLng = Double.valueOf(tllngE);
            bottomRightLat = Double.valueOf(brlatE);
            bottomRightLng = Double.valueOf(brlngE);
            topRightLat = Double.valueOf(trLatE);
            topRightLng = Double.valueOf(trLngE);
            bottomLeftLat = Double.valueOf(blLatE);
            bottomLeftLng = Double.valueOf(blLngE);

            setCanStopMapDownload(true);
        } else {
            Toast.makeText(this, "Invalid Lat or Lng! Reverting to default values", Toast.LENGTH_LONG)
                    .show();
        }

        currentMapDownload = mapName;

        String mapboxStyle = "mapbox://styles/ona/cj9jueph7034i2rphe0gp3o6m";
        LatLng topLeftBound = new LatLng(topLeftLat, topLeftLng);
        LatLng topRightBound = new LatLng(topRightLat, topRightLng);
        LatLng bottomRightBound = new LatLng(bottomRightLat, bottomRightLng);
        LatLng bottomLeftBound = new LatLng(bottomLeftLat, bottomLeftLng);

        double maxZoom = 20.0;
        double minZoom = 0.0;

        OfflineServiceHelper.ZoomRange zoomRange = new OfflineServiceHelper.ZoomRange(minZoom, maxZoom);

        if (useDownloadHelper) {
            OfflineServiceHelper.requestOfflineMapDownload(this
                    , mapName
                    , mapboxStyle
                    , BuildConfig.MAPBOX_SDK_ACCESS_TOKEN
                    , topLeftBound
                    , topRightBound
                    , bottomRightBound
                    , bottomLeftBound
                    , zoomRange
            );
        } else {
            Intent mapDownloadIntent = new Intent(this, MapboxOfflineDownloaderService.class);
            mapDownloadIntent.putExtra(Constants.PARCELABLE_KEY_MAPBOX_ACCESS_TOKEN, BuildConfig.MAPBOX_SDK_ACCESS_TOKEN);
            mapDownloadIntent.putExtra(Constants.PARCELABLE_KEY_SERVICE_ACTION, MapboxOfflineDownloaderService.SERVICE_ACTION.DOWNLOAD_MAP);
            mapDownloadIntent.putExtra(Constants.PARCELABLE_KEY_STYLE_URL, mapboxStyle);
            mapDownloadIntent.putExtra(Constants.PARCELABLE_KEY_MAP_UNIQUE_NAME, mapName);
            mapDownloadIntent.putExtra(Constants.PARCELABLE_KEY_MAX_ZOOM, maxZoom);
            mapDownloadIntent.putExtra(Constants.PARCELABLE_KEY_MIN_ZOOM, minZoom);
            mapDownloadIntent.putExtra(Constants.PARCELABLE_KEY_TOP_LEFT_BOUND, topLeftBound);
            mapDownloadIntent.putExtra(Constants.PARCELABLE_KEY_TOP_RIGHT_BOUND, topRightBound);
            mapDownloadIntent.putExtra(Constants.PARCELABLE_KEY_BOTTOM_RIGHT_BOUND, bottomRightBound);
            mapDownloadIntent.putExtra(Constants.PARCELABLE_KEY_BOTTOM_LEFT_BOUND, bottomLeftBound);

            startService(mapDownloadIntent);
        }
    }

    private void stopMapDownload(@NonNull String mapName) {
        OfflineServiceHelper.stopMapDownload(this, mapName, BuildConfig.MAPBOX_SDK_ACCESS_TOKEN);
    }

    private boolean isValidDouble(String doubleString) {
        String doubleRegex = "[+-]{0,1}[0-9]*.{0,1}[0-9]*";
        return (!doubleString.isEmpty() && doubleString.matches(doubleRegex));
    }

    private void registerLocalBroadcastReceiver() {
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        Bundle bundle = intent.getExtras();
                        if (bundle != null) {
                            Log.i("KUJAKU SAMPLE APP TAG", intent.getExtras().toString());
                            if (bundle.containsKey(MapboxOfflineDownloaderService.KEY_RESULT_STATUS)
                                    && bundle.containsKey(MapboxOfflineDownloaderService.KEY_RESULT_MESSAGE)
                                    && bundle.containsKey(MapboxOfflineDownloaderService.KEY_RESULTS_PARENT_ACTION)
                                    && bundle.containsKey(Constants.PARCELABLE_KEY_MAP_UNIQUE_NAME)) {

                                String mapUniqueName = bundle.getString(Constants.PARCELABLE_KEY_MAP_UNIQUE_NAME);
                                String resultStatus = bundle.getString(MapboxOfflineDownloaderService.KEY_RESULT_STATUS);
                                MapboxOfflineDownloaderService.SERVICE_ACTION serviceAction = (MapboxOfflineDownloaderService.SERVICE_ACTION) bundle.get(MapboxOfflineDownloaderService.KEY_RESULTS_PARENT_ACTION);

                                String message = bundle.getString(MapboxOfflineDownloaderService.KEY_RESULT_MESSAGE);

                                if (MapboxOfflineDownloaderService.SERVICE_ACTION_RESULT.FAILED.name().equals(resultStatus)) {
                                    if (!TextUtils.isEmpty(message)) {
                                        if (!message.contains("MapBox Tile Count limit exceeded")) {
                                            showInfoNotification("Error occurred " + mapUniqueName + ":" + serviceAction.name(), message);
                                        }
                                    }

                                    if (serviceAction == MapboxOfflineDownloaderService.SERVICE_ACTION.DELETE_MAP && !TextUtils.isEmpty(message)) {
                                        Toasty.error(MainActivity.this, message)
                                                .show();
                                    }
                                    /*
                                    (FACT) This is an error update from the service. If this is not
                                    a DELETE_MAP action and the update is about the map that we expect
                                    to be currently downloading, held by currentMapDownload variable, then we
                                    need to disable the STOP MAP DOWNLOAD since the download has already been
                                    stopped after the error. If we left this as true, then we would be misleading
                                    the user that they can stop a non-existent download.
                                     */
                                    else if (!TextUtils.isEmpty(mapUniqueName) && mapUniqueName.equals(currentMapDownload)) {
                                        setCanStopMapDownload(false);
                                    }
                                } else {
                                    // We should disable the stop offline download button if it was stopped successfully
                                    if (serviceAction == MapboxOfflineDownloaderService.SERVICE_ACTION.STOP_CURRENT_DOWNLOAD) {
                                        currentMapDownload = null;
                                        setCanStopMapDownload(false);
                                    } else {
                                        if (!TextUtils.isEmpty(message)) {
                                            // This is a download progress message
                                            if (isValidDouble(message)) {
                                                if (Double.valueOf(message) == 100d) {
                                                    currentMapDownload = null;
                                                    setCanStopMapDownload(false);
                                                } else {
                                                    setCanStopMapDownload(true);
                                                }
                                            } else {
                                                Toasty.info(MainActivity.this, message)
                                                        .show();
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            Log.i("KUJAKU SAMPLE APP TAG", "Broadcast message has null Extras");
                        }
                    }
                }, new IntentFilter(Constants.INTENT_ACTION_MAP_DOWNLOAD_SERVICE_STATUS_UPDATES));
    }

    private void downloadMapBoxStyle(String mapboxStyleUrl) {
        MapBoxWebServiceApi mapBoxWebServiceApi = new MapBoxWebServiceApi(this, BuildConfig.MAPBOX_SDK_ACCESS_TOKEN);
        mapBoxWebServiceApi.retrieveStyleJSON(mapboxStyleUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Toast.makeText(MainActivity.this, response, Toast.LENGTH_SHORT)
                        .show();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this, "Error downloading MapBox Style JSON : " + error.getMessage(), Toast.LENGTH_SHORT)
                        .show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case MAP_ACTIVITY_REQUEST_CODE:
                if (resultCode == Activity.RESULT_OK) {
                    // data from a dropped feature point
                    if (data.hasExtra(NEW_FEATURE_POINTS_JSON)) {
                        saveDroppedPoints(data);
                    }
                    // data from a clicked feature point
                    String geoJSONFeature = getString(R.string.error_msg_could_not_retrieve_chosen_feature);
                    if (data.hasExtra(Constants.PARCELABLE_KEY_GEOJSON_FEATURE)) {
                        geoJSONFeature = data.getStringExtra(Constants.PARCELABLE_KEY_GEOJSON_FEATURE);
                    }
                    Toast.makeText(this, geoJSONFeature, Toast.LENGTH_LONG)
                            .show();
                }
                break;
            default:
                break;
        }
    }

    private void saveDroppedPoints(Intent data) {
        GenericAsyncTask genericAsyncTask = new GenericAsyncTask(new AsyncTaskCallable() {
            @Override
            public Object[] call() throws Exception {
                List<String> geoJSONFeatures = data.getStringArrayListExtra(NEW_FEATURE_POINTS_JSON);
                for (String geoJSONFeature : geoJSONFeatures) {
                    try {
                        JSONObject featurePoint = new JSONObject(geoJSONFeature);
                        JSONArray coordinates = featurePoint.getJSONObject("geometry").getJSONArray("coordinates");
                        Point newPoint = new Point(null, (double) coordinates.get(1), (double) coordinates.get(0));
                        MyApplication.getInstance().getPointsRepository().addOrUpdate(newPoint);
                        newPoint.setId((int) (Math.random() * Integer.MAX_VALUE));
                        points.add(newPoint);
                    } catch (Exception e) {
                        Log.e(TAG, "JsonArray parse error occured");
                    }
                }
                return null;
            }
        });
        genericAsyncTask.setOnFinishedListener(null);
        genericAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
  
    private void confirmSampleStyleAvailable() {
        MapBoxStyleStorage mapBoxStyleStorage = new MapBoxStyleStorage();
        String style = mapBoxStyleStorage.readStyle("file:///sdcard/Dukto/2017-nov-27-kujaku-metadata.json");
        if (TextUtils.isEmpty(style)) {
            //Write the file to storage
            String sampleStyleString = readAssetFile(SAMPLE_JSON_FILE_NAME);
            mapBoxStyleStorage.writeToFile("Dukto", SAMPLE_JSON_FILE_NAME, sampleStyleString);
        }
    }

    public String readAssetFile(String inFile) {
        String fileStringContents = "";

        try {
            InputStream stream = getAssets().open(inFile);

            int size = stream.available();
            byte[] buffer = new byte[size];
            stream.read(buffer);
            stream.close();
            fileStringContents = new String(buffer);
        } catch (IOException e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }

        return fileStringContents;
    }

    private void requestBasicPermissions() {
        ArrayList<String> notGivenPermissions = new ArrayList<>();

        for (String permission : basicPermissions) {
            if (!Permissions.check(this, permission)) {
                notGivenPermissions.add(permission);
            }
        }

        if (notGivenPermissions.size() > 0) {
            Permissions.request(this, notGivenPermissions.toArray(new String[notGivenPermissions.size()]), PERMISSIONS_REQUEST_CODE);
        } else {
            confirmSampleStyleAvailable();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            requestBasicPermissions();
        }
    }

    private void showInfoNotification(String title, String content) {
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setContentTitle(title)
                .setContentText(content)
                .setSmallIcon(android.R.drawable.ic_dialog_info);

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(lastNotificationId, notificationBuilder.build());
    }
}
