package io.ona.kujaku.sample.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.mapbox.mapboxsdk.Mapbox;

import org.json.JSONObject;

import io.ona.kujaku.callbacks.AddPointCallback;
import io.ona.kujaku.sample.BuildConfig;
import io.ona.kujaku.sample.R;
import io.ona.kujaku.views.KujakuMapView;

public class HighLevelMapView extends AppCompatActivity {

    private static final String TAG = HighLevelMapView.class.getName();

    private KujakuMapView kujakuMapView;
    private boolean canAddPoint = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_high_level_map_view);

        Mapbox.getInstance(this, BuildConfig.MAPBOX_SDK_ACCESS_TOKEN);

        kujakuMapView = findViewById(R.id.kmv_highLevelMapView_mapView);
        kujakuMapView.addPoint(false, new AddPointCallback() {
            @Override
            public void onPointAdd(JSONObject jsonObject) {
                Log.d(TAG, jsonObject.toString());
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "User cancelled adding points");
            }
        });

    }
}
