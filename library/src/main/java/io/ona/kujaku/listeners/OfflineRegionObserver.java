package io.ona.kujaku.listeners;

import com.mapbox.mapboxsdk.offline.OfflineRegionError;
import com.mapbox.mapboxsdk.offline.OfflineRegionStatus;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 13/11/2017.
 */

public interface OfflineRegionObserver {
    /**
     * Implement this method to be notified of a change in the status of an
     * offline region. Status changes include any change in state of the members
     * of OfflineRegionStatus.
     * <p>
     * This method will be executed on the main thread.
     * </p>
     *
     * @param status the changed status
     */
    void onStatusChanged(OfflineRegionStatus status);

    /**
     * Implement this method to be notified of errors encountered while downloading
     * regional resources. Such errors may be recoverable; for example the implementation
     * will attempt to re-request failed resources based on an exponential backoff
     * algorithm, or when it detects that network access has been restored.
     * <p>
     * This method will be executed on the main thread.
     * </p>
     *
     * @param reason the offline region error message
     * @param message the offline region error reason
     */
    void onError(String reason, String message);

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
