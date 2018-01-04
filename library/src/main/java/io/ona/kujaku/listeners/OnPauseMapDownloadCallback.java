package io.ona.kujaku.listeners;

/**
 * Used to post an update when a request to pause a Map Download is made
 *
 * Created by Ephraim Kigamba - ekigamba@ona.io on 13/11/2017.
 */

public interface OnPauseMapDownloadCallback {

    /**
     * Called when a request to pause an Offline Region's download is successful
     */
    void onPauseSuccess();

    /**
     * Called when a request to pause an Offline Region's download fails
     *
     * @param error
     */
    void onPauseError(String error);

    String
        MAP_COULD_NOT_BE_FOUND = "Map could not be found",
        MAP_WAS_NOT_DOWNLOADING = "Map was not downloading",
        MAP_DOWNLOAD_COMPLETE = "Map download complete",
        CONTEXT_PASSED_IS_NULL = "Context passed is null";

}
