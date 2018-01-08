package io.ona.kujaku.listeners;

import com.mapbox.mapboxsdk.offline.OfflineRegion;
import com.mapbox.mapboxsdk.offline.OfflineRegionStatus;

/**
 * Callback for conveying results of a request for incomplete {@link OfflineRegion} downloads<br/><br/>
 *
 * Created by Ephraim Kigamba - ekigamba@ona.io on 15/11/2017
 */

public interface IncompleteMapDownloadCallback {

    /**
     * Called when an incomplete {@link OfflineRegion} is found
     *
     * @param offlineRegion
     * @param offlineRegionStatus
     */
    void incompleteMap(OfflineRegion offlineRegion, OfflineRegionStatus offlineRegionStatus);

    /**
     * Called if an error occurs when:
     * <ul>
     *     <li>Retrieving the list of {@link OfflineRegion}s</li>
     *     <li>Retrieving the {@link OfflineRegionStatus} for an {@link OfflineRegion}</li>
     * </ul>
     *
     * @param errorReason
     */
    void onError(String errorReason);
}
