package io.ona.kujaku.sample.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.mapbox.mapboxsdk.geometry.LatLng;

import java.util.UUID;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import io.ona.kujaku.helpers.MapBoxWebServiceApi;
import io.ona.kujaku.sample.BuildConfig;
import io.ona.kujaku.sample.R;
import io.ona.kujaku.services.MapboxOfflineDownloaderService;
import utils.Constants;

public class MainActivity extends AppCompatActivity {

    private EditText topLeftLatEd
            , topLeftLngEd
            , bottomRightLatEd
            , bottomRightLngEd
            , mapNameEd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //callLibrary();

        bottomRightLatEd = (EditText) findViewById(R.id.edt_mainActivity_bottomRightlatitude);
        bottomRightLngEd = (EditText) findViewById(R.id.edt_mainActivity_bottomRightlongitude);
        topLeftLatEd = (EditText) findViewById(R.id.edt_mainActivity_topLeftlatitude);
        topLeftLngEd = (EditText) findViewById(R.id.edt_mainActivity_topLeftlongitude);

        mapNameEd = (EditText) findViewById(R.id.edt_mainActivity_mapName) ;

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
    }

    private void callLibrary() {
        Intent intent = new Intent(Constants.INTENT_ACTION_SHOW_MAP);
        intent.putExtra(Constants.PARCELABLE_KEY_MAPBOX_ACCESS_TOKEN, BuildConfig.MAPBOX_SDK_ACCESS_TOKEN);
        startActivity(intent);
    }

    private void downloadMap() {
        double topLeftLat = -1.29020515
                , topLeftLng = 36.78702772
                , bottomRightLat = -1.29351951
                , bottomRightLng =  36.79288566;

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

        if (isValidDouble(tllatE) && isValidDouble(tllngE) && isValidDouble(brlatE) && isValidDouble(brlngE) ) {
            topLeftLat = Double.valueOf(tllatE);
            topLeftLng = Double.valueOf(tllngE);
            bottomRightLat = Double.valueOf(brlatE);
            bottomRightLng = Double.valueOf(brlngE);

            Intent mapDownloadIntent = new Intent(this, MapboxOfflineDownloaderService.class);
            mapDownloadIntent.putExtra(Constants.PARCELABLE_KEY_MAPBOX_ACCESS_TOKEN, "pk.eyJ1Ijoib25hIiwiYSI6IlVYbkdyclkifQ.0Bz-QOOXZZK01dq4MuMImQ");
            mapDownloadIntent.putExtra(Constants.PARCELABLE_KEY_SERVICE_ACTION, Constants.SERVICE_ACTION.DOWNLOAD_MAP);
            mapDownloadIntent.putExtra(Constants.PARCELABLE_KEY_STYLE_URL, "mapbox://styles/ona/cj9jueph7034i2rphe0gp3o6m");
            mapDownloadIntent.putExtra(Constants.PARCELABLE_KEY_MAP_UNIQUE_NAME, mapName);//"Hp Invent " + UUID.randomUUID().toString());
            mapDownloadIntent.putExtra(Constants.PARCELABLE_KEY_MAX_ZOOM, 20.0);
            mapDownloadIntent.putExtra(Constants.PARCELABLE_KEY_MIN_ZOOM, 0.0);
            /*mapDownloadIntent.putExtra(Constants.PARCELABLE_KEY_TOP_LEFT_BOUND, new LatLng(37.7897, -119.5073));//new LatLngParcelable(-1.29020515, 36.78702772)); //new LatLngParcelable(-1.2920646, 36.7846043));
            mapDownloadIntent.putExtra(Constants.PARCELABLE_KEY_BOTTOM_RIGHT_BOUND, new LatLng(37.6744, -119.6815));//new LatLngParcelable(-1.29351951, 36.79288566));//new LatLngParcelable(-2.2920646, 38.7846043));*/
            mapDownloadIntent.putExtra(Constants.PARCELABLE_KEY_TOP_LEFT_BOUND, new LatLng(topLeftLat, topLeftLng)); //new LatLngParcelable(-1.2920646, 36.7846043));
            mapDownloadIntent.putExtra(Constants.PARCELABLE_KEY_BOTTOM_RIGHT_BOUND, new LatLng(bottomRightLat, bottomRightLng));//new LatLngParcelable(-2.2920646, 38.7846043));

            startService(mapDownloadIntent);
        } else {
            Toast.makeText(this, "Invalid Lat or Lng!", Toast.LENGTH_LONG)
                    .show();
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
                        Log.i("KUJAKU SAMPLE APP TAG", intent.getExtras().toString());
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
}
