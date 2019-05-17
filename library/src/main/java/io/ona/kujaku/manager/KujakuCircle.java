package io.ona.kujaku.manager;

import com.mapbox.mapboxsdk.plugins.annotation.Circle;

public class KujakuCircle {

    private boolean isMiddleCircle;
    private Circle circle;

    private KujakuCircle previousCircle;
    private KujakuCircle nextCircle;

    KujakuCircle(Circle circle, KujakuCircle previousCircle, boolean isMiddleCircle) {
       this.circle = circle;
       this.previousCircle = previousCircle;
       this.isMiddleCircle = isMiddleCircle;

       if (this.previousCircle != null) {
           this.previousCircle.setNextCircle(this);
       }
    }

    public Circle getCircle() {
        return this.circle;
    }

    void setMiddleCircle(boolean middleCircle) {
        this.isMiddleCircle = middleCircle ;
    }

    public boolean isMiddleCircle() {
        return this.isMiddleCircle;
    }

    void setNextCircle(KujakuCircle circle) {
        this.nextCircle = circle;
        circle.previousCircle = this;
    }

    void setPreviousCircle(KujakuCircle circle) {
        this.previousCircle = circle;
        circle.nextCircle = this;
    }

    KujakuCircle getPreviousKujakuCircle() {
        return this.previousCircle;
    }

    KujakuCircle getNextKujakuCircle() {
        return this.nextCircle;
    }

    public KujakuCircleOptions getCircleOptions() {
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
