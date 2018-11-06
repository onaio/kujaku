package io.ona.kujaku.listeners;

import com.mapbox.mapboxsdk.geometry.LatLng;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 06/11/2018
 */

public interface BoundsChangeListener {

    void onBoundsChanged(LatLng topLeft, LatLng topRight, LatLng bottomRight, LatLng bottomLeft);
}
