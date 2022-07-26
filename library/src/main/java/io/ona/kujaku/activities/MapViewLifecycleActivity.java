package io.ona.kujaku.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import io.ona.kujaku.views.KujakuMapView;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 27/11/2018
 */

public abstract class MapViewLifecycleActivity extends AppCompatActivity {


    public abstract KujakuMapView getKujakuMapView();

    @Override
    protected void onResume() {
        super.onResume();
        if (getKujakuMapView() != null) getKujakuMapView().onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (getKujakuMapView() != null) getKujakuMapView().onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (getKujakuMapView() != null) getKujakuMapView().onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (getKujakuMapView() != null) getKujakuMapView().onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (getKujakuMapView() != null) getKujakuMapView().onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (getKujakuMapView() != null) getKujakuMapView().onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (getKujakuMapView() != null) getKujakuMapView().onLowMemory();
    }
}
