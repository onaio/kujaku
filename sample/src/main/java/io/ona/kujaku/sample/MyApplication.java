package io.ona.kujaku.sample;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import io.ona.kujaku.BaseKujakuApplication;
import io.ona.kujaku.domain.Point;
import io.ona.kujaku.sample.repository.KujakuRepository;
import io.ona.kujaku.sample.repository.PointsRepository;

import static io.ona.kujaku.utils.Constants.DATABASE_NAME;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 15/11/2017.
 */

public class MyApplication extends BaseKujakuApplication {

    private static final String TAG = MyApplication.class.getName();

    private KujakuRepository repository;

    private PointsRepository pointsRepository;

    private static MyApplication application;

    @Override
    public void onCreate() {
        super.onCreate();
        application = this;
        setEnableMapDownloadResume(false);
        init(this); // must initialize base application
        getRepository();
    }

    public static MyApplication getInstance() { return application; }

    public KujakuRepository getRepository() {
        try {
            if (repository == null) {
                repository = new KujakuRepository(getApplicationContext(), DATABASE_NAME, null, 1);
            }
        } catch (UnsatisfiedLinkError e) {
            Log.e(TAG, "Error on getRepository: " + e);
        }
        return repository;
    }

    public PointsRepository getPointsRepository() {
        if (pointsRepository == null) {
            pointsRepository = new PointsRepository(getRepository());
        }
        return pointsRepository;
    }

    @Override
    public void processFeatureJSON(JSONObject featurePoint) {
        try {
            JSONArray coordinates = featurePoint.getJSONObject("geometry").getJSONArray("coordinates");
            getPointsRepository().addOrUpdate(new Point(null, (double) coordinates.get(1), (double) coordinates.get(0)));
        } catch (Exception e) {
            Log.e(TAG, "JsonArray parse error occured");
        }
    }
}
