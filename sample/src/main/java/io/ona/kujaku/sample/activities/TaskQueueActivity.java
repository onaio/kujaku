package io.ona.kujaku.sample.activities;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

import es.dmoral.toasty.Toasty;
import io.ona.kujaku.data.realm.RealmDatabase;
import io.ona.kujaku.data.realm.objects.MapBoxOfflineQueueTask;
import io.ona.kujaku.sample.R;
import io.realm.RealmResults;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 05/02/2018.
 */

public class TaskQueueActivity extends BaseNavigationDrawerActivity {

    private static final String TAG = TaskQueueActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.task_queue_activity_title);

        ListView listView = (ListView) findViewById(R.id.lv_taskQueueActivity_taskList);

        RealmDatabase realmDatabase = RealmDatabase.init(this);

        RealmResults<MapBoxOfflineQueueTask> realmResults = realmDatabase.getTasks();
        displayQueueTasks(listView, realmResults);
    }

    private void displayQueueTasks(@NonNull ListView listView, @NonNull final RealmResults<MapBoxOfflineQueueTask> realmResults) {
        if (realmResults.size() == 0) {
            Toasty.info(this, getString(R.string.no_offline_download_tasks), Toast.LENGTH_LONG)
                    .show();
        }

        listView.setAdapter(new ArrayAdapter(TaskQueueActivity.this, android.R.layout.simple_list_item_1, new String[realmResults.size()]) {
            @NonNull
            @Override
            public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                if (convertView == null) {
                    convertView = getLayoutInflater().inflate(android.R.layout.simple_list_item_1, parent, false);
                }

                MapBoxOfflineQueueTask mapBoxOfflineQueueTask = realmResults.get(position);

                String toDisplay = getString(R.string.error_msg_json_exception);
                try {
                    toDisplay = String.format(getString(R.string.task_item_display_template),
                            mapBoxOfflineQueueTask.getTaskType(),
                            getTaskDetails(mapBoxOfflineQueueTask),
                            (mapBoxOfflineQueueTask.getTaskStatus() == MapBoxOfflineQueueTask.TASK_STATUS_DONE) ?
                                    "DONE" :
                                    (mapBoxOfflineQueueTask.getTaskStatus() == MapBoxOfflineQueueTask.TASK_STATUS_STARTED) ?
                                            "STARTED" : "NOT STARTED",
                            new Gson().toJson(mapBoxOfflineQueueTask.getDateCreated())
                    );
                } catch (JSONException jsonException) {
                    Log.e(TAG, Log.getStackTraceString(jsonException));
                }

                final TextView textView = (TextView) convertView;
                textView.setText(toDisplay);

                return textView;
            }
        });
    }

    private String getTaskDetails(MapBoxOfflineQueueTask mapBoxOfflineQueueTask) throws JSONException {
        JSONObject downloadTask = mapBoxOfflineQueueTask.getTask();

        Iterator<String> taskKeys =  downloadTask.keys();

        StringBuilder stringBuilder = new StringBuilder("\n\n");
        while (taskKeys.hasNext()) {
            String key = taskKeys.next();
            stringBuilder.append("\t");
            stringBuilder.append(key);

            Object value = downloadTask.get(key);
            stringBuilder.append(": ");

            if (value instanceof String || value instanceof Integer || value instanceof Double
                    || value instanceof Float || value instanceof Long) {
                stringBuilder.append(value);
            } else {
                Gson gson = new Gson();
                stringBuilder.append(gson.toJson(value));
            }

            stringBuilder.append("\n");
        }
        stringBuilder.append("\n");

        return stringBuilder.toString();
    }

    @Override
    protected int getContentView() {
        return R.layout.task_queue_activity;
    }

    @Override
    protected int getSelectedNavigationItem() {
        return R.id.nav_task_queue;
    }
}
