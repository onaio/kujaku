package io.ona.kujaku.services.options;

import android.app.PendingIntent;
import android.location.Criteria;
import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Abstract Options for Tracking Service.
 *
 * Created by Emmanuel Otin - eo@novel-t.ch 03/07/19.
 */
abstract public class TrackingServiceOptions implements Parcelable {
    private long minTime;

    long minDistance;
    long gpsMinDistance;
    long toleranceIntervalDistance;
    long distanceFromDeparture;
    long minAccuracy;

    TrackingServiceOptions() {
        this.minTime = 0;
    }

    /**
     * Get the min distance in meters between 2 points registered by the TrackingService
     *
     * @return
     */
    public long getMinDistance() {
        return minDistance;
    }

    /**
     * Get the tolerance in meters (more or less) to the min distance between 2 points registered by the TrackingService
     *
     * @return
     */
    public long getToleranceIntervalDistance() {
        return toleranceIntervalDistance;
    }

    /**
     * Define the maximum distance in meters from the first point recorded to raise the
     * {@link io.ona.kujaku.listeners.TrackingServiceListener#onCloseToDepartureLocation(Location)} function
     *
     * @return
     */
    public long getDistanceFromDeparture() {
        return distanceFromDeparture;
    }

    /**
     * Get the minimum accuracy in meters to record a location
     *
     * @return
     */
    public long getMinAccuracy() {
        return minAccuracy;
    }

    /**
     * Get the minimum time in seconds between getting a point from the {@link android.location.LocationManager#requestLocationUpdates(long, float, Criteria, PendingIntent)}
     *
     * @return
     */
    public long getMinTime() {
        return minTime;
    }

    /**
     * Get the minimum distance in meters between getting a point from the {@link android.location.LocationManager#requestLocationUpdates(long, float, Criteria, PendingIntent)}
     *
     * @return
     */
    public long getGpsMinDistance() {
        return gpsMinDistance;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Parcelable implementation of writeToParcel function
     *
     * @param dest
     * @param flags
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(minTime);
        dest.writeLong(minDistance);
        dest.writeLong(gpsMinDistance);
        dest.writeLong(toleranceIntervalDistance);
        dest.writeLong(distanceFromDeparture);
        dest.writeLong(minAccuracy);
    }

    /**
     * Parcelable implementation of createFromParcel function called in subclasses
     *
     * @param in
     */
    void createFromParcel(Parcel in) {
        this.minTime = in.readLong();
        this.minDistance = in.readLong();
        this.gpsMinDistance = in.readLong();
        this.toleranceIntervalDistance = in.readLong();
        this.distanceFromDeparture = in.readLong();
        this.minAccuracy = in.readLong();
    }
}
