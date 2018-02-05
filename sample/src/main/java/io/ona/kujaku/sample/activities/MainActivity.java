package io.ona.kujaku.sample.activities;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import io.ona.kujaku.activities.MapActivity;
import io.ona.kujaku.helpers.MapBoxStyleStorage;
import io.ona.kujaku.helpers.MapBoxWebServiceApi;
import io.ona.kujaku.sample.BuildConfig;
import io.ona.kujaku.sample.R;
import io.ona.kujaku.services.MapboxOfflineDownloaderService;
import io.ona.kujaku.utils.Constants;
import io.ona.kujaku.utils.Permissions;

public class MainActivity extends BaseNavigationDrawerActivity {

    private EditText topLeftLatEd, topLeftLngEd, bottomRightLatEd, bottomRightLngEd, mapNameEd;

    protected static final int MAP_ACTIVITY_REQUEST_CODE = 43;
    private static final String SAMPLE_JSON_FILE_NAME = "2017-nov-27-kujaku-metadata.json";
    private static final int PERMISSIONS_REQUEST_CODE = 9823;
    private String[] basicPermissions = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    private int lastNotificationId = 200;
    private final static String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        requestBasicPermissions();

        bottomRightLatEd = (EditText) findViewById(R.id.edt_mainActivity_bottomRightlatitude);
        bottomRightLngEd = (EditText) findViewById(R.id.edt_mainActivity_bottomRightlongitude);
        topLeftLatEd = (EditText) findViewById(R.id.edt_mainActivity_topLeftlatitude);
        topLeftLngEd = (EditText) findViewById(R.id.edt_mainActivity_topLeftlongitude);

        mapNameEd = (EditText) findViewById(R.id.edt_mainActivity_mapName);

        Button startOfflineDownload = (Button) findViewById(R.id.btn_mainActivity_startOfflineDownload);
        Button openMapActivity = (Button) findViewById(R.id.btn_mainActivity_openMapActivity);

        startOfflineDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloadMap();
            }
        });
        openMapActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callLibrary();
            }
        });
        registerLocalBroadcastReceiver();

        Button launchKujakuMap = (Button) findViewById(R.id.btn_mainActivity_launchKujakuMap);
        launchKujakuMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callLibrary();
            }
        });

        final EditText mapBoxStyleUrl = (EditText) findViewById(R.id.edt_mainActivity_mapboxStyleURL);
        mapBoxStyleUrl.setText("mapbox://styles/ona/cj9jueph7034i2rphe0gp3o6m");
        Button downloadMapBoxStyle = (Button) findViewById(R.id.btn_mainActivity_downloadMapboxStyle);
        downloadMapBoxStyle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloadMapBoxStyle(mapBoxStyleUrl.getText().toString());
            }
        });

        setTitle(R.string.main_activity_title);
    }

    @Override
    protected int getContentView() {
        return R.layout.activity_main;
    }

    @Override
    protected int getSelectedNavigationItem() {
        return R.id.nav_main_activity;
    }

    private void callLibrary() {
        Intent intent = new Intent(this, MapActivity.class);
        intent.putExtra(Constants.PARCELABLE_KEY_MAPBOX_STYLES, new String[]{
                "file:///sdcard/Dukto/2017-nov-27-kujaku-metadata.json"
        });
        intent.putExtra(Constants.PARCELABLE_KEY_MAPBOX_ACCESS_TOKEN, BuildConfig.MAPBOX_SDK_ACCESS_TOKEN);

        startActivityForResult(intent, MAP_ACTIVITY_REQUEST_CODE);
    }

    private void downloadMap() {
        double topLeftLat = 37.7897, topLeftLng = -119.5073, bottomRightLat = 37.6744, bottomRightLng = -119.6815;

        String tllatE = topLeftLatEd.getText().toString();
        String tllngE = topLeftLngEd.getText().toString();
        String brlatE = bottomRightLatEd.getText().toString();
        String brlngE = bottomRightLngEd.getText().toString();

        String mapName = mapNameEd.getText().toString();
        if (mapName.isEmpty()) {
            Toast.makeText(this, "Please enter a Map Name!", Toast.LENGTH_LONG)
                    .show();
            return;
        }

        if (isValidDouble(tllatE) && isValidDouble(tllngE) && isValidDouble(brlatE) && isValidDouble(brlngE)) {
            topLeftLat = Double.valueOf(tllatE);
            topLeftLng = Double.valueOf(tllngE);
            bottomRightLat = Double.valueOf(brlatE);
            bottomRightLng = Double.valueOf(brlngE);

            Intent mapDownloadIntent = new Intent(this, MapboxOfflineDownloaderService.class);
            mapDownloadIntent.putExtra(Constants.PARCELABLE_KEY_MAPBOX_ACCESS_TOKEN, BuildConfig.MAPBOX_SDK_ACCESS_TOKEN);
            mapDownloadIntent.putExtra(Constants.PARCELABLE_KEY_SERVICE_ACTION, MapboxOfflineDownloaderService.SERVICE_ACTION.DOWNLOAD_MAP);
            mapDownloadIntent.putExtra(Constants.PARCELABLE_KEY_STYLE_URL, "mapbox://styles/ona/cj9jueph7034i2rphe0gp3o6m");
            mapDownloadIntent.putExtra(Constants.PARCELABLE_KEY_MAP_UNIQUE_NAME, mapName);
            mapDownloadIntent.putExtra(Constants.PARCELABLE_KEY_MAX_ZOOM, 20.0);
            mapDownloadIntent.putExtra(Constants.PARCELABLE_KEY_MIN_ZOOM, 0.0);
            mapDownloadIntent.putExtra(Constants.PARCELABLE_KEY_TOP_LEFT_BOUND, new LatLng(topLeftLat, topLeftLng));
            mapDownloadIntent.putExtra(Constants.PARCELABLE_KEY_BOTTOM_RIGHT_BOUND, new LatLng(bottomRightLat, bottomRightLng));

            startService(mapDownloadIntent);
        } else {
            Toast.makeText(this, "Invalid Lat or Lng! Reverting to default values", Toast.LENGTH_LONG)
                    .show();

            Intent mapDownloadIntent = new Intent(this, MapboxOfflineDownloaderService.class);
            mapDownloadIntent.putExtra(Constants.PARCELABLE_KEY_MAPBOX_ACCESS_TOKEN, BuildConfig.MAPBOX_SDK_ACCESS_TOKEN);
            mapDownloadIntent.putExtra(Constants.PARCELABLE_KEY_SERVICE_ACTION, MapboxOfflineDownloaderService.SERVICE_ACTION.DOWNLOAD_MAP);
            mapDownloadIntent.putExtra(Constants.PARCELABLE_KEY_STYLE_URL, "mapbox://styles/ona/cj9jueph7034i2rphe0gp3o6m");
            mapDownloadIntent.putExtra(Constants.PARCELABLE_KEY_MAP_UNIQUE_NAME, mapName);
            mapDownloadIntent.putExtra(Constants.PARCELABLE_KEY_MAX_ZOOM, 20.0);
            mapDownloadIntent.putExtra(Constants.PARCELABLE_KEY_MIN_ZOOM, 0.0);
            mapDownloadIntent.putExtra(Constants.PARCELABLE_KEY_TOP_LEFT_BOUND, new LatLng(topLeftLat, topLeftLng));
            mapDownloadIntent.putExtra(Constants.PARCELABLE_KEY_BOTTOM_RIGHT_BOUND, new LatLng(bottomRightLat, bottomRightLng));

            startService(mapDownloadIntent);
        }
    }

    private boolean isValidDouble(String doubleString) {
        String doubleRegex = "[+-]{0,1}[0-9]*.{0,1}[0-9]*";
        if (!doubleString.isEmpty() && doubleString.matches(doubleRegex)) {
            return true;
        }

        return false;
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

                                if (resultStatus.equals(MapboxOfflineDownloaderService.SERVICE_ACTION_RESULT.FAILED.name())) {
                                    String message = bundle.getString(MapboxOfflineDownloaderService.KEY_RESULT_MESSAGE);
                                    showInfoNotification("Error occurred " + mapUniqueName + ":" + serviceAction.name(), message);
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
        lastNotificationId++;
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setContentTitle(title)
                .setContentText(content)
                .setSmallIcon(android.R.drawable.ic_dialog_info);

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(lastNotificationId, notificationBuilder.build());
    }
}
