package io.ona.kujaku.listeners;

import com.mapbox.mapboxsdk.offline.OfflineRegion;
import com.mapbox.mapboxsdk.offline.OfflineRegionStatus;

/**
 * Callback used to post progress when an incomplete MapBox Map Download is resumed
 *
 * Created by Ephraim Kigamba - ekigamba@ona.io on 15/11/2017.
 */

public interface IncompleteMapDownloadCallback {

    void incompleteMap(OfflineRegion offlineRegion, OfflineRegionStatus offlineRegionStatus);

    void onError(String errorReason, String errorMessage);
}
