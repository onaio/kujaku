package io.ona.kujaku.sample.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.mapbox.mapboxsdk.Mapbox;

import org.json.JSONObject;

import io.ona.kujaku.callbacks.AddPointCallback;
import io.ona.kujaku.sample.BuildConfig;
import io.ona.kujaku.sample.R;
import io.ona.kujaku.views.KujakuMapView;

public class CardActivity extends BaseNavigationDrawerActivity {

    private static final String TAG = LowLevelLocationAddPointMapView.class.getName();
    private KujakuMapView kujakuMapView;

    private boolean isCardVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, BuildConfig.MAPBOX_SDK_ACCESS_TOKEN);

        kujakuMapView = findViewById(R.id.card_activity_map_view);

        kujakuMapView.showCurrentLocationBtn(true);

        kujakuMapView.enableAddPoint(true);

        ImageButton addPointBtn = kujakuMapView.findViewById(R.id.imgBtn_mapview_locationAdditionBtn);
        kujakuMapView.setViewVisibility(addPointBtn, true);
        addPointBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                kujakuMapView.dropPoint();
            }
        });

        Button testCardView = findViewById(R.id.btn_test_card_view_display);
        testCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findViewById(R.id.feature_info_card_view).setVisibility(isCardVisible ?  View.GONE : View.VISIBLE);
                isCardVisible = !isCardVisible;
            }
        });
    }

    @Override
    protected int getContentView() {
        return R.layout.activity_card;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (kujakuMapView != null) kujakuMapView.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (kujakuMapView != null) kujakuMapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (kujakuMapView != null) kujakuMapView.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (kujakuMapView != null) kujakuMapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (kujakuMapView != null) kujakuMapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (kujakuMapView != null) kujakuMapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (kujakuMapView != null) kujakuMapView.onLowMemory();
    }

    @Override
    protected int getSelectedNavigationItem() {
        return R.id.nav_card_activity;
    }
}