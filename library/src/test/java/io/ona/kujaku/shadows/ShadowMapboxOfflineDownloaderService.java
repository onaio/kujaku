package io.ona.kujaku.shadows;

import android.support.annotation.NonNull;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.shadows.ShadowService;

import io.ona.kujaku.data.realm.objects.MapBoxOfflineQueueTask;
import io.ona.kujaku.services.MapboxOfflineDownloaderService;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 28/12/2017.
 */

@Implements(MapboxOfflineDownloaderService.class)
public class ShadowMapboxOfflineDownloaderService extends ShadowService {

    @Implementation
    private void persistCompletedStatus(MapBoxOfflineQueueTask mapBoxOfflineQueueTask) {
        mapBoxOfflineQueueTask.setTaskStatus(MapBoxOfflineQueueTask.TASK_STATUS_DONE);
    }
}
