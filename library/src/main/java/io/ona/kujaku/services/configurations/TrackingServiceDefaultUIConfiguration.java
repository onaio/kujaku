package io.ona.kujaku.services.configurations;

import android.view.Gravity;
import io.ona.kujaku.R;

/**
 * Default UI Configuration for Tracking Service Icon.
 *
 * Created by Emmanuel Otin - eo@novel-t.ch 11/04/19.
 */
public class TrackingServiceDefaultUIConfiguration extends TrackingServiceUIConfiguration{

    public TrackingServiceDefaultUIConfiguration() {
        super();

        this.displayTrackingServiceIcons = true;

        this.recordingDrawable = R.drawable.ic_recording_red;
        this.stoppedDrawable = R.drawable.ic_recording_gray;
        this.backgroundDrawable = R.drawable.circle_button_black_border;

        this.layoutWidth = R.dimen.tracking_service_location_dimen;
        this.layoutHeight = R.dimen.tracking_service_location_dimen;

        this.layoutMarginTop = R.dimen.tracking_service_location_margin;
        this.layoutMarginLeft = R.dimen.tracking_service_location_margin;
        this.layoutMarginRight = R.dimen.tracking_service_location_margin;
        this.layoutMarginBottom = R.dimen.tracking_service_location_margin;

        this.padding = R.dimen.tracking_service_location_padding;

        this.layoutGravity = Gravity.TOP | Gravity.LEFT;
    }
}
