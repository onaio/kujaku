package io.ona.kujaku.manager;

import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.plugins.annotation.CircleOptions;

/**
 * KujakuCircleOptions extends CircleOptions
 * Add isMiddle Point functionality (Point between to real point)
 *
 * Created by Emmanuel Otin - eo@novel-t.ch on 19/06/2019
 */
public class KujakuCircleOptions extends CircleOptions {

    private boolean isMiddleCircle = false;

    /**
     * Set is middle to initialise the circle with.
     * <p>
     * Circle isMiddle.
     * </p>
     * @param middle
     * @return this
     */
    public KujakuCircleOptions withMiddleCircle(boolean middle) {
        this.isMiddleCircle =  middle;
        return this;
    }

    /**
     * Get the middle value
     * <p>
     * Circle isMiddle.
     * </p>
     * @return isMiddle value
     */
    public boolean getMiddleCircle() {
        return isMiddleCircle;
    }

    public KujakuCircleOptions withCircleRadius(Float circleRadius) {
        super.withCircleRadius(circleRadius);
        return this;
    }

    public KujakuCircleOptions withCircleColor(String circleColor) {
        super.withCircleColor(circleColor);
        return this;
    }

    public KujakuCircleOptions withCircleBlur(Float circleBlur) {
        super.withCircleBlur(circleBlur);
        return this;
    }

    public KujakuCircleOptions withCircleOpacity(Float circleOpacity) {
        super.withCircleOpacity(circleOpacity);
        return this;
    }

    public KujakuCircleOptions withCircleStrokeWidth(Float circleStrokeWidth) {
        super.withCircleStrokeWidth(circleStrokeWidth);
        return this;
    }

    public KujakuCircleOptions withCircleStrokeColor(String circleStrokeColor) {
        super.withCircleStrokeColor(circleStrokeColor);
        return this;
    }

    public KujakuCircleOptions withCircleStrokeOpacity(Float circleStrokeOpacity) {
        super.withCircleStrokeOpacity(circleStrokeOpacity);
        return this;
    }

    public KujakuCircleOptions withLatLng(LatLng latLng) {
        super.withLatLng(latLng);
        return this;
    }

    public KujakuCircleOptions withGeometry(Point geometry) {
        super.withGeometry(geometry);
        return this;
    }

    public KujakuCircleOptions withDraggable(boolean draggable) {
        super.withDraggable(draggable);
        return this;
    }
}
