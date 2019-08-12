package io.ona.kujaku.manager.options;

/**
 * Default Splitting Manager Options class
 *
 * Created by Emmanuel Otin - eo@novel-t.ch on 05/07/2019
 */
public class SplittingManagerDefaultOptions extends SplittingManagerOptions {

    public SplittingManagerDefaultOptions() {
        super();

        this.circleColor = "red";
        this.circleRadius = 10.0f;
        this.lineColor = "red";

        this.kujakuFillLayerColorSelected = "red";
        this.kujakuFillLayerColor = "black";
    }
}
