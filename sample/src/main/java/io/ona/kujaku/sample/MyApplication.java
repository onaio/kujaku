package io.ona.kujaku.sample;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import io.ona.kujaku.KujakuApplication;
import io.ona.kujaku.interfaces.IKujakuApplication;
import io.ona.kujaku.sample.domain.Point;
import io.ona.kujaku.sample.repository.KujakuRepository;
import io.ona.kujaku.sample.repository.PointsRepository;
import io.ona.kujaku.views.BaseHostApplication;

import static io.ona.kujaku.utils.Constants.DATABASE_NAME;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 15/11/2017.
 */

public class MyApplication extends BaseHostApplication {


    private static final String TAG = MyApplication.class.getName();

    private MyApplication myApplication;

    private KujakuRepository repository;

    private PointsRepository pointsRepository;

    private MyApplication() { ;
        KujakuApplication.getInstance().setEnableMapDownloadResume(false);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        getRepository();
    }

    public MyApplication getInstance() {
        if (myApplication == null) {
            myApplication = new MyApplication();
            KujakuApplication.getInstance().setHostApplication(this);
        }
        return myApplication;
    }

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
    public void savePoint(JSONObject featurePoint) {
        try {
            JSONArray coordinates = featurePoint.getJSONObject("geometry").getJSONArray("coordinates");
            getPointsRepository().addOrUpdate(new Point(null, (double) coordinates.get(1), (double) coordinates.get(0)));
        } catch (Exception e) {
            Log.e(TAG, "JsonArray parse error occured");
        }
    }
}
