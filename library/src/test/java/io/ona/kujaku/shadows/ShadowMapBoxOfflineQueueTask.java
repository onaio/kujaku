package io.ona.kujaku.shadows;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

import io.ona.kujaku.data.realm.objects.MapBoxOfflineQueueTask;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 22/12/2017.
 */

@Implements(MapBoxOfflineQueueTask.class)
public class ShadowMapBoxOfflineQueueTask {

    public ShadowMapBoxOfflineQueueTask() {

    }
}
