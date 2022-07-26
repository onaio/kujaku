package io.ona.kujaku.tasks;

import android.os.AsyncTask;
import androidx.annotation.NonNull;

import io.ona.kujaku.callables.AsyncTaskCallable;
import io.ona.kujaku.exceptions.AsyncTaskCancelledException;
import io.ona.kujaku.listeners.OnFinishedListener;
import io.ona.kujaku.utils.LogUtil;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 03/10/2018
 */

public class GenericAsyncTask extends AsyncTask<Void, Void, Object[]> {

    private static final String TAG = GenericAsyncTask.class.getName();
    private AsyncTaskCallable toCall;
    private OnFinishedListener onFinishedListener;

    private Exception exception;

    public GenericAsyncTask(@NonNull AsyncTaskCallable toCall) {
        this.toCall = toCall;
    }

    @Override
    protected Object[] doInBackground(Void... voids) {
        try {
            return toCall.call();
        } catch (Exception e) {
            LogUtil.e(TAG, e);
            exception = e;
            this.cancel(true);

            return null;
        }
    }

    @Override
    protected void onPostExecute(Object[] objects) {
        if (onFinishedListener != null) {
            onFinishedListener.onSuccess(objects);
        }
    }

    @Override
    protected void onCancelled() {
        if (onFinishedListener != null) {
            Exception cancelException = exception == null ?
                    new AsyncTaskCancelledException() :
                    exception;

            onFinishedListener.onError(cancelException);
        }
    }

    public void setOnFinishedListener(OnFinishedListener onFinishedListener) {
        this.onFinishedListener = onFinishedListener;
    }
}
