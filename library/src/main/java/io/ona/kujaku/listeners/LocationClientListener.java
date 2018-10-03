package io.ona.kujaku.listeners;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 03/10/2018
 */

public interface LocationClientListener {

    void onClientStart();

    void onClientStartFailure();

    void onClientStop();
}
