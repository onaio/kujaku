package io.ona.kujaku.services.options;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Options for Tracking Service. Save Battery as we are asking locations updates from the GPS every 5 meters : gpsMinDistance = 5
 *
 * Created by Emmanuel Otin - eo@novel-t.ch 03/07/19.
 */
public class TrackingServiceSaveBatteryOptions extends TrackingServiceOptions {

    public TrackingServiceSaveBatteryOptions() {
        super();
        this.minDistance = 5;
        this.gpsMinDistance = 5;
        this.toleranceIntervalDistance = 1;
        this.distanceFromDeparture = 10;
        this.minAccuracy = 50;
    }

    private TrackingServiceSaveBatteryOptions(Parcel in) {
        super.createFromParcel(in);
    }

    /**
     * Creator for Parcelable class
     */
    public static final Parcelable.Creator<TrackingServiceSaveBatteryOptions> CREATOR = new Parcelable.Creator<TrackingServiceSaveBatteryOptions>() {
        public TrackingServiceSaveBatteryOptions createFromParcel(Parcel in) {
            return new TrackingServiceSaveBatteryOptions(in);
        }

        public TrackingServiceSaveBatteryOptions[] newArray(int size) {
            return new TrackingServiceSaveBatteryOptions[size];
        }
    };
}
