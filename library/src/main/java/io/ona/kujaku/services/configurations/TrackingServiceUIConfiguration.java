package io.ona.kujaku.services.configurations;

/**
 * Abstract UI Configuration for Tracking Service Icon.
 *
 * Created by Emmanuel Otin - eo@novel-t.ch 11/04/19.
 */
abstract public class TrackingServiceUIConfiguration {
    // Drawable resource
    protected int recordingDrawable;
    // Drawable resource
    protected int stoppedDrawable;
    // Drawable resource
    protected int backgroundDrawable;

    // Dimension resource
    protected int layoutWidth;
    // Dimension resource
    protected int layoutHeight;

    // Dimension resource
    protected int layoutMarginTop;
    // Dimension resource
    protected int layoutMarginLeft;
    // Dimension resource
    protected int layoutMarginRight;
    // Dimension resource
    protected int layoutMarginBottom;

    // Dimension resource
    protected int padding;

    // Layout Gravity
    protected int layoutGravity;

    protected boolean displayTrackingServiceIcons;

    public boolean displayIcons() {
        return this.displayTrackingServiceIcons;
    }

    public int getRecordingDrawable() {
        return recordingDrawable;
    }

    public int getStoppedDrawable() {
        return stoppedDrawable;
    }

    public int getBackgroundDrawable() {
        return backgroundDrawable;
    }

    public int getLayoutWidth() {
        return layoutWidth;
    }

    public int getLayoutHeight() {
        return layoutHeight;
    }

    public int getLayoutMarginTop() {
        return layoutMarginTop;
    }

    public int getLayoutMarginLeft() {
        return layoutMarginLeft;
    }

    public int getLayoutMarginRight() {
        return layoutMarginRight;
    }

    public int getLayoutMarginBottom() {
        return layoutMarginBottom;
    }

    public int getLayoutGravity() {
        return layoutGravity;
    }

    public int getPadding() {
        return this.padding;
    }
}
