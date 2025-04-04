package io.ona.kujaku.listeners;


import com.mapbox.geojson.Point;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 06/11/2018
 */

public interface BoundsChangeListener {

    void onBoundsChanged(Point topLeft, Point topRight, Point bottomRight, Point bottomLeft);
}
