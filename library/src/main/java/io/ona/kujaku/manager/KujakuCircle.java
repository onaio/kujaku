package io.ona.kujaku.manager;

import com.mapbox.mapboxsdk.plugins.annotation.Circle;

/**
 * KujakuCircle class embeds a Circle instance
 * and define the previous Circle and the Next one to navigate between circles
 *
 * Created by Emmanuel Otin - eo@novel-t.ch on 19/06/2019
 */
public class KujakuCircle {

    private boolean isMiddleCircle;
    private Circle circle;

    private KujakuCircle previousKujakuCircle;
    private KujakuCircle nextKujakuCircle;

    KujakuCircle(Circle circle, KujakuCircle previousKujakuCircle, boolean isMiddleCircle) {
       this.circle = circle;
       this.previousKujakuCircle = previousKujakuCircle;
       this.isMiddleCircle = isMiddleCircle;

       if (this.previousKujakuCircle != null) {
           this.previousKujakuCircle.setNextKujakuCircle(this);
       }
    }

    /**
     * Return the Circle instance
     *
     * @return
     */
    public Circle getCircle() {
        return this.circle;
    }

    void setMiddleCircle(boolean middleCircle) {
        this.isMiddleCircle = middleCircle ;
    }

    boolean isMiddleCircle() {
        return this.isMiddleCircle;
    }

    void setNextKujakuCircle(KujakuCircle circle) {
        this.nextKujakuCircle = circle;
        circle.previousKujakuCircle = this;
    }

    void setPreviousKujakuCircle(KujakuCircle circle) {
        this.previousKujakuCircle = circle;
        circle.nextKujakuCircle = this;
    }

    KujakuCircle getPreviousKujakuCircle() {
        return this.previousKujakuCircle;
    }

    KujakuCircle getNextKujakuCircle() {
        return this.nextKujakuCircle;
    }

    KujakuCircleOptions getCircleOptions() {
        Float blur = null;
        String color = null;
        Float opacity = null;
        Float radius = null;

        try {
            blur = this.getCircle().getCircleBlur();
        } catch (Exception ex) {
        }

        try {
            color = this.getCircle().getCircleColor();
        } catch (Exception ex) {
        }

        try {
            opacity = this.getCircle().getCircleOpacity();
        } catch (Exception ex) {
        }

        try {
            radius = this.getCircle().getCircleRadius();
        } catch (Exception ex) {
        }

        return new KujakuCircleOptions()
                .withMiddleCircle(this.isMiddleCircle)
                .withCircleBlur(blur)
                .withCircleColor(color)
                .withCircleOpacity(opacity)
                .withCircleRadius(radius)
               /* .withCircleStrokeColor(this.getCircle().getCircleStrokeColor())
                .withCircleStrokeOpacity(this.getCircle().getCircleStrokeOpacity())
                .withCircleStrokeWidth(this.getCircle().getCircleStrokeWidth())*/
                .withDraggable(this.getCircle().isDraggable())
                .withGeometry(this.getCircle().getGeometry())
                .withLatLng(this.getCircle().getLatLng());
    }
}
