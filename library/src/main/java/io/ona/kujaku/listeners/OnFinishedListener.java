package io.ona.kujaku.listeners;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 09/07/2018
 */

public interface OnFinishedListener {

    void onSuccess(Object[] objects);

    void onError(Exception e);
}
