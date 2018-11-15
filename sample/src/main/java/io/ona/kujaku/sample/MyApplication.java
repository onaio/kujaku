package io.ona.kujaku.sample;

import android.app.Application;

import com.crashlytics.android.Crashlytics;

import io.fabric.sdk.android.Fabric;
import io.ona.kujaku.KujakuApplication;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 15/11/2017.
 */

public class MyApplication extends KujakuApplication {

    public MyApplication() {
        super(false);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // initialize fabric
        Fabric.with(this, new Crashlytics());
    }
}
