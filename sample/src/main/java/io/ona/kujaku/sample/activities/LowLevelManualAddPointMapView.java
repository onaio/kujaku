package io.ona.kujaku.sample.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.mapbox.mapboxsdk.Mapbox;

import org.json.JSONObject;

import io.ona.kujaku.sample.BuildConfig;
import io.ona.kujaku.sample.R;
import io.ona.kujaku.views.KujakuMapView;

public class LowLevelManualAddPointMapView extends AppCompatActivity {

    private KujakuMapView kujakuMapView;
    private boolean canAddPoint = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_low_level_manual_add_point_map_view);

        Mapbox.getInstance(this, BuildConfig.MAPBOX_SDK_ACCESS_TOKEN);

        kujakuMapView = findViewById(R.id.kmv_lowLevelManualAddPointMapView_mapView);
        kujakuMapView.enableAddPoint(true);

        Button button = findViewById(R.id.btn_lowLevelManualAddPointMapView_doneBtn);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (kujakuMapView.isCanAddPoint()) {
                    JSONObject featurePoint = kujakuMapView.dropPoint();
                    Log.e("FEATURE POINT", featurePoint.toString());
                }
            }
        });

        button.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                kujakuMapView.enableAddPoint(!kujakuMapView.isCanAddPoint());

                return true;
            }
        });

    }
}
