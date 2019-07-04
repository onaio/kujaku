package io.ona.kujaku.manager.options;

import io.ona.kujaku.manager.KujakuCircleOptions;

public abstract class SplittingManagerOptions {

    protected String circleColor ;
    protected String lineColor ;
    protected Float circleRadius ;

    protected String kujakuLineLayerColor ;
    protected String kujakuLineLayerColorSelected ;
    protected String kujakuFillLayerColor ;
    protected String kujakuFillLayerColorSelected ;

    public String getCircleColor() {
        return circleColor;
    }

    public String getLineColor() {
        return lineColor;
    }

    public Float getCircleRadius() {
        return circleRadius;
    }

    public String getKujakuFillLayerColor() {
        return kujakuFillLayerColor;
    }

    public String getKujakuLineLayerColor() {
        return kujakuLineLayerColor;
    }

    public String getKujakuFillLayerColorSelected() {
        return kujakuFillLayerColorSelected;
    }

    public String getKujakuLineLayerColorSelected() {
        return kujakuLineLayerColorSelected;
    }

    public KujakuCircleOptions getKujakuCircleOptions() {
        return new KujakuCircleOptions()
                .withCircleRadius(circleRadius)
                .withCircleColor(circleColor)
                .withDraggable(true);
    }
}
