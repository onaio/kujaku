package io.ona.kujaku.sample;

import android.app.Application;

import io.ona.kujaku.sample.R;

public class TestApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        setTheme(R.style.AppTheme); //or just R.style.Theme_AppCompat
    }
}