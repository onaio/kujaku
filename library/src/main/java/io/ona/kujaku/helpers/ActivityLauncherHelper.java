package io.ona.kujaku.helpers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Parcelable;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import io.ona.kujaku.BuildConfig;
import io.ona.kujaku.activities.MapActivity;
import io.ona.kujaku.callables.AsyncTaskCallable;
import io.ona.kujaku.domain.Point;
import io.ona.kujaku.listeners.OnFinishedListener;
import io.ona.kujaku.tasks.GenericAsyncTask;
import io.ona.kujaku.utils.Constants;

import static io.ona.kujaku.utils.Constants.ENABLE_DROP_POINT_BUTTON;
import static io.ona.kujaku.utils.Constants.MAP_ACTIVITY_REQUEST_CODE;
import static io.ona.kujaku.utils.Constants.PARCELABLE_POINTS_LIST;
import static io.ona.kujaku.utils.IOUtil.readInputStreamAsString;

/**
 * @author Vincent Karuri
 */
public class ActivityLauncherHelper {

    public static final String TAG = ActivityLauncherHelper.class.getName();

    public static void launchMapActivity(Activity hostActivity, List<Point> points, boolean enableDropPoint) {
        Intent intent = new Intent(hostActivity, MapActivity.class);
        createCustomStyleLayer(hostActivity.getApplicationContext(), new OnFinishedListener() {
            @Override
            public void onSuccess(Object[] objects) {
                JSONObject mapboxStyleJSON = (JSONObject) objects[0];
                if (mapboxStyleJSON != null) {
                    intent.putExtra(Constants.PARCELABLE_KEY_MAPBOX_STYLES, new String[]{
                            mapboxStyleJSON.toString()
                    });
                    intent.putExtra(Constants.PARCELABLE_KEY_MAPBOX_ACCESS_TOKEN, BuildConfig.MAPBOX_SDK_ACCESS_TOKEN);
                    intent.putParcelableArrayListExtra(PARCELABLE_POINTS_LIST, (ArrayList<? extends Parcelable>) points);
                    intent.putExtra(ENABLE_DROP_POINT_BUTTON, enableDropPoint);

                    hostActivity.startActivityForResult(intent, MAP_ACTIVITY_REQUEST_CODE);
                }
            }
            @Override
            public void onError(Exception e) {
                Log.e(TAG, Log.getStackTraceString(e));
            }
        });
    }

    /**
     * This method creates a custom layer sourced from the file assets/2017-nov-27-kujaku-metadata.json
     *
     * @param onFinishedListener
     */
    private static void createCustomStyleLayer(Context context, OnFinishedListener onFinishedListener) {
        GenericAsyncTask genericAsyncTask = new GenericAsyncTask(new AsyncTaskCallable() {
            @Override
            public Object[] call() throws Exception {
                String style = readInputStreamAsString(context.getAssets().open("2017-nov-27-kujaku-metadata.json"));

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
