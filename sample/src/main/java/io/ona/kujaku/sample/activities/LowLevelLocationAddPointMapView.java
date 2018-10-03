package io.ona.kujaku.sample.activities;

import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.geometry.LatLng;

import org.json.JSONObject;

import io.ona.kujaku.listeners.OnLocationChanged;
import io.ona.kujaku.sample.BuildConfig;
import io.ona.kujaku.sample.R;
import io.ona.kujaku.views.KujakuMapView;

public class LowLevelLocationAddPointMapView extends AppCompatActivity {

    private KujakuMapView kujakuMapView;
    private boolean canAddPoint = true;
    private Location location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_low_level_location_add_point_map_view);

        Mapbox.getInstance(this, BuildConfig.MAPBOX_SDK_ACCESS_TOKEN);

        kujakuMapView = findViewById(R.id.kmv_lowLevelLocationAddPointMapView_mapView);

        Button doneBtn = findViewById(R.id.btn_lowLevelLocationAddPointMapView_doneBtn);
        Button goToMyLocationBtn = findViewById(R.id.btn_lowLevelLocationAddPointMapView_myLocationBtn);

        doneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (kujakuMapView.isCanAddPoint() && location != null) {
                    JSONObject featurePoint = kujakuMapView.dropPoint(new LatLng(location.getLatitude(), location.getLongitude()));
                    if (featurePoint != null) {
                        Log.e("FEATURE POINT", featurePoint.toString());
                    }
                    location = null;
                }
            }
        });

        goToMyLocationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                kujakuMapView.enableAddPoint(true, new OnLocationChanged() {
                    @Override
                    public void onLocationChanged(Location location) {
                        LowLevelLocationAddPointMapView.this.location = location;
                    }
                });
            }
        });
    }
}
