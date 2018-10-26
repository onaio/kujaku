package io.ona.kujaku;

import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

import io.ona.kujaku.activities.MapActivity;
import io.ona.kujaku.callables.AsyncTaskCallable;
import io.ona.kujaku.domain.Point;
import io.ona.kujaku.listeners.OnFinishedListener;
import io.ona.kujaku.tasks.GenericAsyncTask;
import io.ona.kujaku.utils.Constants;

import static io.ona.kujaku.utils.IOUtil.readInputStreamAsString;

/**
 * @author Vincent Karuri
 */
public class KujakuLibrary {

    private BaseKujakuApplication hostApplication;
    private static KujakuLibrary library;
    private List<Point> mapActivityPoints;
    private static final String TAG = KujakuLibrary.class.getName();

    private KujakuLibrary() {}

    public static KujakuLibrary getInstance() {
        if (library == null) {
            library = new KujakuLibrary();
        }
        return library;
    }

    public void sendFeatureJSONToHostApp(JSONObject featureJSON) {
        getHostApplication().processFeatureJSON(featureJSON);
    }

    public void sendFeatureJSONToGeoWidget(JSONObject featureJSON) {
        // TODO: implement this
    }

    public void setHostApplication(BaseKujakuApplication hostApplication) { this.hostApplication = hostApplication; }

    public BaseKujakuApplication getHostApplication() {
        return hostApplication;
    }


    public void setMapActivityPoints(List<Point> points) {
       mapActivityPoints = points;
    }

    public List<Point> getMapActivityPoints() {
        return mapActivityPoints;
    }

    public void launchMapActivity(List<Point> points) {
        setMapActivityPoints(points);

        Intent intent = new Intent(getHostApplication(), MapActivity.class);
        createFinalStyleUsingSavedPoints(new OnFinishedListener() {
            @Override
            public void onSuccess(Object[] objects) {
                JSONObject mapboxStyleJSON = (JSONObject) objects[0];
                if (mapboxStyleJSON != null) {
                    intent.putExtra(Constants.PARCELABLE_KEY_MAPBOX_STYLES, new String[]{
                            mapboxStyleJSON.toString()
                    });
                    intent.putExtra(Constants.PARCELABLE_KEY_MAPBOX_ACCESS_TOKEN, BuildConfig.MAPBOX_SDK_ACCESS_TOKEN);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    getHostApplication().startActivity(intent);
                }
            }
            @Override
            public void onError(Exception e) {
                Log.e(TAG, Log.getStackTraceString(e));
            }
        });
    }

    private void createFinalStyleUsingSavedPoints(OnFinishedListener onFinishedListener) {
        GenericAsyncTask genericAsyncTask = new GenericAsyncTask(new AsyncTaskCallable() {
            @Override
            public Object[] call() throws Exception {
                String style = readInputStreamAsString(getHostApplication().getAssets().open("2017-nov-27-kujaku-metadata.json"));

                JSONObject mapboxStyleJSON = new JSONObject(style);
                JSONArray jsonArray = mapboxStyleJSON.getJSONArray("layers");

                jsonArray.put(new JSONObject("{\n" +
                        "            \"id\": \"new-points-layer\",\n" +
                        "            \"type\": \"symbol\",\n" +
                        "            \"source\": \"new-points-source\",\n" +
                        "            \"layout\": {\"icon-image\": \"marker-15\"},\n" +
                        "            \"paint\": {}\n" +
                        "        }"));
                return new Object[]{mapboxStyleJSON};
            }
        });
        genericAsyncTask.setOnFinishedListener(onFinishedListener);
        genericAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
}
