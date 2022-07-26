package io.ona.kujaku.sample.activities;

import android.os.Bundle;
import androidx.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;

import org.json.JSONObject;

import io.ona.kujaku.sample.R;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 06/11/2018
 */

public class CustomMarkerLowLevelAddPoint extends LowLevelManualAddPointMapView {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Button button = findViewById(R.id.btn_lowLevelManualAddPointMapView_doneBtn);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (kujakuMapView.isCanAddPoint()) {
                    JSONObject featurePoint = kujakuMapView.dropPoint(R.drawable.ic_subway);
                    if (featurePoint != null) {
                        Log.e("FEATURE POINT", featurePoint.toString());
                    }
                }
            }
        });

        kujakuMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull MapboxMap mapboxMap) {
                mapboxMap.setStyle(Style.MAPBOX_STREETS);
            }
        });
    }

    @Override
    protected int getContentView() {
        return R.layout.activity_low_level_manual_add_point_map_view;
    }

    @Override
    protected int getSelectedNavigationItem() {
        return R.id.nav_low_level_add_point_custom_marker;
    }
}
