package io.ona.kujaku.interfaces;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 26/09/2018
 */

import android.support.annotation.NonNull;

import io.ona.kujaku.callbacks.AddPointCallback;

public interface IKujakuMapView extends IKujakuMapViewLowLevel {

    void addPoint(boolean useGPS, @NonNull AddPointCallback addPointCallback);

}
