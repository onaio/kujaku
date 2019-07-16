package io.ona.kujaku.sample;

import android.support.multidex.MultiDexApplication;

import com.crashlytics.android.Crashlytics;

import io.fabric.sdk.android.Fabric;
import io.ona.kujaku.KujakuLibrary;
import io.ona.kujaku.sample.repository.KujakuRepository;
import io.ona.kujaku.sample.repository.PointsRepository;
import timber.log.Timber;

import static io.ona.kujaku.sample.util.Constants.DATABASE_NAME;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 15/11/2017.
 */

public class MyApplication extends MultiDexApplication {

    private static final String TAG = MyApplication.class.getName();

    private KujakuRepository repository;

    private PointsRepository pointsRepository;

    private static MyApplication application;

    @Override
    public void onCreate() {
        super.onCreate();
        application = this;

        // activate Crashlytics
        Fabric.with(this, new Crashlytics());  // initialize fabric

        KujakuLibrary.setEnableMapDownloadResume(false);
        KujakuLibrary.init(this); // must initialize KujakuLibrary
        getRepository(); // initialize KujakuRepository

        Timber.plant(new Timber.DebugTree());
    }

    public static MyApplication getInstance() {
        return application;
    }

    public KujakuRepository getRepository() {
        try {
            if (repository == null) {
                repository = new KujakuRepository(getApplicationContext(), DATABASE_NAME, null, 1);
            }
        } catch (UnsatisfiedLinkError e) {
            Timber.e(e);
        }
        return repository;
    }

    public PointsRepository getPointsRepository() {
        if (pointsRepository == null) {
            pointsRepository = new PointsRepository(getRepository());
        }
        return pointsRepository;
    }
}
