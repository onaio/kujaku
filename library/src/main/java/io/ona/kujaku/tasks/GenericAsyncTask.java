package io.ona.kujaku.tasks;

import androidx.annotation.NonNull;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import io.ona.kujaku.callables.AsyncTaskCallable;
import io.ona.kujaku.exceptions.AsyncTaskCancelledException;
import io.ona.kujaku.listeners.OnFinishedListener;
import io.ona.kujaku.utils.LogUtil;

public class GenericAsyncTask {
    private static final String TAG = GenericAsyncTask.class.getName();
    private final AsyncTaskCallable toCall;
    private OnFinishedListener onFinishedListener;

    public GenericAsyncTask(@NonNull AsyncTaskCallable toCall) {
        this.toCall = toCall;
    }

    public void execute() {
        ExecutorService executorService = Executors.newSingleThreadExecutor();

        try {


            Future<Object[]> result = executorService.submit(toCall);

            if (onFinishedListener != null) {
                onFinishedListener.onSuccess(result.get());
            }

        } catch (Exception e) {
            LogUtil.e(TAG, e);
            cancel(e);

        } finally {

            if (executorService != null)
                executorService.shutdownNow();
        }

    }

    protected void cancel(Exception exception) {
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
