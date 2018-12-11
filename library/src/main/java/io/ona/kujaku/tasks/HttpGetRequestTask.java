package io.ona.kujaku.tasks;

import android.support.annotation.NonNull;

import io.ona.kujaku.callables.AsyncTaskCallable;
import io.ona.kujaku.listeners.OnFinishedListener;

public class HttpGetRequestTask extends GenericAsyncTask {
    public HttpGetRequestTask(@NonNull AsyncTaskCallable toCall) {
        super(toCall);
    }

    @Override
    protected Object[] doInBackground(Void... voids) {
        return super.doInBackground(voids);
    }

    @Override
    protected void onPostExecute(Object[] objects) {
        super.onPostExecute(objects);
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
    }

    @Override
    public void setOnFinishedListener(OnFinishedListener onFinishedListener) {
        super.setOnFinishedListener(onFinishedListener);
    }
}
