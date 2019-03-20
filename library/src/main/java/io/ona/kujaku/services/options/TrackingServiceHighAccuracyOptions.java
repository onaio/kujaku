package io.ona.kujaku.services.options;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Options for Tracking Service . High Accuracy as we are asking the maximum of available locations from the GPS : gpsMinDistance = 0
 *
 * Created by Emmanuel Otin - eo@novel-t.ch 03/07/19.
 */
public class TrackingServiceHighAccuracyOptions extends TrackingServiceOptions {

    public TrackingServiceHighAccuracyOptions() {
        super();
        this.minDistance = 5;
        this.gpsMinDistance = 0;
        this.toleranceIntervalDistance = 1;
        this.distanceFromDeparture = 10;
        this.minAccuracy = 50;
    }

    private TrackingServiceHighAccuracyOptions(Parcel in) {
        super.createFromParcel(in);
    }

    /**
     * Creator for Parcelable class
     */
    public static final Parcelable.Creator<TrackingServiceHighAccuracyOptions> CREATOR = new Parcelable.Creator<TrackingServiceHighAccuracyOptions>() {
        public TrackingServiceHighAccuracyOptions createFromParcel(Parcel in) {
            return new TrackingServiceHighAccuracyOptions(in);
        }

        public TrackingServiceHighAccuracyOptions[] newArray(int size) {
            return new TrackingServiceHighAccuracyOptions[size];
        }
    };
}
