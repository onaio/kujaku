package io.ona.kujaku.listeners;

import android.support.annotation.NonNull;
import io.ona.kujaku.interfaces.ILocationClient;

/**
 * Called when the {@link ILocationClient} is available and started in the {@link io.ona.kujaku.interfaces.IKujakuMapView}
 *
 * Created by Ephraim Kigamba - ekigamba@ona.io on 25/02/2019
 */
public interface LocationClientStartedCallback {

    void onStarted(@NonNull ILocationClient iLocationClient);
}
