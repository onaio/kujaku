package io.ona.kujaku.listeners;

import com.mapbox.mapboxsdk.offline.OfflineRegion;
import com.mapbox.mapboxsdk.offline.OfflineRegionStatus;

/**
 * Used to post progress when a Map Download is requested
 *
 *
 * Created by Ephraim Kigamba - ekigamba@ona.io on 13/11/2017.
 */

public interface OnDownloadMapListener {

    /**
     * Called to provide periodic downloading progress updates or DOWNLOAD COMPLETE message
     *
     * @param offlineRegionStatus The Offline Region's status
     * @param offlineRegion The Offline Region whose status was queried
     */
    public void onStatusChanged(OfflineRegionStatus offlineRegionStatus, OfflineRegion offlineRegion);

    /**
     * Called when an error occurs during download or when an Offline download of a Region is requested
     *
     * @param errorReason
     */
    public void onError(String errorReason, String errorMessage);
}
