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

    /**
     * set this circle a middle one if middleCircle == true
     *
     * @param middleCircle
     */
    void setMiddleCircle(boolean middleCircle) {
        this.isMiddleCircle = middleCircle ;
    }

    /**
     * Is this circle a middle one between 2 reals
     *
     * @return
     */
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
}
