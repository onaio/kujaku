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

import io.ona.kujaku.parcelables.LatLngParcelable;
import io.ona.kujaku.sample.BuildConfig;
import io.ona.kujaku.sample.R;
import io.ona.kujaku.services.MapboxOfflineDownloaderService;
import io.ona.kujaku.utils.Constants;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //callLibrary();

        EditText lat = (EditText) findViewById(R.id.edt_mainActivity_latitude);
        EditText longitude = (EditText) findViewById(R.id.edt_mainActivity_longitude);

        Button startOfflineDownload = (Button) findViewById(R.id.btn_mainActivity_startOfflineDownload);
        startOfflineDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloadMap();
            }
        });
        registerLocalBroadcastReceiver();
    }

    private void callLibrary() {
        Intent intent = new Intent(Constants.INTENT_ACTION_SHOW_MAP);
        intent.putExtra(Constants.PARCELABLE_KEY_MAPBOX_ACCESS_TOKEN, BuildConfig.MAPBOX_SDK_ACCESS_TOKEN);
        startActivity(intent);
    }

    private void downloadMap() {
        Intent mapDownloadIntent = new Intent(this, MapboxOfflineDownloaderService.class);
        mapDownloadIntent.putExtra(Constants.PARCELABLE_KEY_MAPBOX_ACCESS_TOKEN, "pk.eyJ1Ijoib25hIiwiYSI6IlVYbkdyclkifQ.0Bz-QOOXZZK01dq4MuMImQ");
        mapDownloadIntent.putExtra(Constants.PARCELABLE_KEY_SERVICE_ACTION, Constants.SERVICE_ACTION.DOWNLOAD_MAP);
        mapDownloadIntent.putExtra(Constants.PARCELABLE_KEY_STYLE_URL, "mapbox://styles/ona/cj9jueph7034i2rphe0gp3o6m");
        mapDownloadIntent.putExtra(Constants.PARCELABLE_KEY_MAP_UNIQUE_NAME, "Hp Invent");
        mapDownloadIntent.putExtra(Constants.PARCELABLE_KEY_MAX_ZOOM, 20.0);
        mapDownloadIntent.putExtra(Constants.PARCELABLE_KEY_MIN_ZOOM, 0.0);
        mapDownloadIntent.putExtra(Constants.PARCELABLE_KEY_TOP_LEFT_BOUND, new LatLngParcelable(37.7897, -119.5073));//new LatLngParcelable(-1.29020515, 36.78702772)); //new LatLngParcelable(-1.2920646, 36.7846043));
        mapDownloadIntent.putExtra(Constants.PARCELABLE_KEY_BOTTOM_RIGHT_BOUND, new LatLngParcelable(37.6744, -119.6815));//new LatLngParcelable(-1.29351951, 36.79288566));//new LatLngParcelable(-2.2920646, 38.7846043));

        startService(mapDownloadIntent);
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
}
