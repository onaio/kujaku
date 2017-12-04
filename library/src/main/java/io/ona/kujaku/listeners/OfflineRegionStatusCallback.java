package io.ona.kujaku.listeners;

/**
 * Callback used to provide an Offline Region's Status
 *
 * Created by Ephraim Kigamba - ekigamba@ona.io on 21/11/2017.
 */

import com.mapbox.mapboxsdk.offline.OfflineRegion;
import com.mapbox.mapboxsdk.offline.OfflineRegionStatus;

/**
 * This callback receives an asynchronous response containing the OfflineRegionStatus
 * of the offline region, or a {@link String} error message otherwise.
 */
public interface OfflineRegionStatusCallback {
    /**
     * Receives the status
     *
     * @param status the offline region status
     * @param offlineRegion the {@link OfflineRegion} queried
     */
    void onStatus(OfflineRegionStatus status, OfflineRegion offlineRegion);

    /**
     * Receives the error message
     *
     * @param error the error message
     */
    void onError(String error);
}
