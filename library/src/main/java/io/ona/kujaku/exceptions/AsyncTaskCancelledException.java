package io.ona.kujaku.exceptions;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 09/07/2018
 */

public class AsyncTaskCancelledException extends Exception {

    public AsyncTaskCancelledException() {
        super("AsyncTask was cancelled");
    }

    public AsyncTaskCancelledException(Class asyncTaskClass) {
        super("AsyncTask was cancedlled : " + asyncTaskClass.getName());
    }

    public AsyncTaskCancelledException(String message) {
        super(message);
    }
}
