package io.ona.kujaku.services;

import android.os.AsyncTask;
import android.util.Log;

import java.io.InputStreamReader;

import java.net.HttpURLConnection;
import java.net.URL;

import io.ona.kujaku.callables.AsyncTaskCallable;
import io.ona.kujaku.exceptions.WmtsCapabilitiesException;
import io.ona.kujaku.listeners.OnFinishedListener;
import io.ona.kujaku.listeners.WmtsCapabilitiesListener;
import io.ona.kujaku.tasks.GenericAsyncTask;
import io.ona.kujaku.wmts.serializer.WmtsCapabilitiesSerializer;
import io.ona.kujaku.wmts.model.WmtsCapabilities;

/**
 * Service performs the retrieval of the WMTS Capabilities file describing all accessible layers
 *
 * Created by Emmanuel Otin - eo@novel-t.ch 11/28/18.
 */
public class WmtsCapabilitiesService {

    public static final String TAG = WmtsCapabilitiesService.class.getName();

    private String url;

    private WmtsCapabilities capabilities;

    private WmtsCapabilitiesListener listener;

    public WmtsCapabilitiesService(String url) {
        this.url = url;
    }

    public void setListener(WmtsCapabilitiesListener listener) {
        this.listener = listener;
    }

    public void setCapabilitiesUrl(String url) {
        this.url = url;
    }

    public void requestData() {

        GenericAsyncTask task = new GenericAsyncTask(new AsyncTaskCallable() {
            @Override
            public Object[] call() throws Exception {
                if (url == null) {
                    throw new WmtsCapabilitiesException("The Url of the WmtsCapabilitiesService is not defined");
                }

                URL  myUrl = new URL(url);
                HttpURLConnection connection =(HttpURLConnection) myUrl.openConnection();
                connection.setRequestMethod("GET");
                connection.setReadTimeout(15000);
                connection.setConnectTimeout(15000);

                //Connect to our url
                connection.connect();

                InputStreamReader streamReader = new InputStreamReader(connection.getInputStream());

                WmtsCapabilitiesSerializer serializer = new WmtsCapabilitiesSerializer();
                capabilities = serializer.read(WmtsCapabilities.class, streamReader, false);

                return new Object[]{capabilities};
            }
        });

        task.setOnFinishedListener(new OnFinishedListener() {
            @Override
            public void onSuccess(Object[] objects) {
                if (listener != null) {
                    listener.onCapabilitiesReceived(capabilities);
                }
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, Log.getStackTraceString(e));
                if (listener != null) {
                    listener.onCapabilitiesError(e);
                }
            }
        });

        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
}
