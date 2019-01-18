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
    void onStatusChanged(OfflineRegionStatus offlineRegionStatus, OfflineRegion offlineRegion);

    /**
     * Called when an error occurs during download or when an Offline download of a Region is requested
     *
     * @param error
     */
    void onError(String error);

    /*
     * Implement this method to be notified when the limit on the number of Mapbox
     * tiles stored for offline regions has been reached.
     *
     * Once the limit has been reached, the SDK will not download further offline
     * tiles from Mapbox APIs until existing tiles have been removed. Contact your
     * Mapbox sales representative to raise the limit.
     *
     * This limit does not apply to non-Mapbox tile sources.
     *
     * This method will be executed on the main thread.
     */
    void mapboxTileCountLimitExceeded(long limit);
}
