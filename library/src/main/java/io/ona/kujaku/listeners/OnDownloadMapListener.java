package io.ona.kujaku.listeners;

import com.mapbox.mapboxsdk.offline.OfflineRegionStatus;

/**
 * Used to post progress when a Map Download is requested
 *
 *
 * Created by Ephraim Kigamba - ekigamba@ona.io on 13/11/2017.
 */

public interface OnDownloadMapListener {

    public void onStatusChanged(OfflineRegionStatus offlineRegionStatus);

    public void onError(String errorReason, String errorMessage);
}
