package io.ona.kujaku.domain;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Objects;

/**
 * @author Vincent Karuri
 */
public class Point implements Parcelable {

    private Integer id;
    private double lat;
    private double lng;
    private Long dateUpdated;

    public Point(Integer id, double lat, double lng) {
        this.id = id;
        this.lat = lat;
        this.lng = lng;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public Long getDateUpdated() {
        return dateUpdated;
    }

    public void setDateUpdated(Long dateUpdated) {
        this.dateUpdated = dateUpdated;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (this == o) {
            return true;
        }
        Point point = (Point) o;
        return Double.compare(point.getLat(), getLat()) == 0 &&
                Double.compare(point.getLng(), getLng()) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getLat(), getLng());
    }

    // Parcelable methods
    public Point(Parcel in) {
        this.id = in.readInt();
        this.lat = in.readDouble();
        this.lng = in.readDouble();
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Point createFromParcel(Parcel in) {
            return new Point(in);
        }

        public Point[] newArray(int size) {
            return new Point[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeDouble(this.lat);
        dest.writeDouble(this.lng);
    }
}
