package io.ona.kujaku.parcelables;


import android.os.Parcel;
import android.os.Parcelable;

import com.mapbox.mapboxsdk.geometry.LatLng;

/**
 * This is a parcelable {@link LatLng}
 *
 * Created by Ephraim Kigamba - ekigamba@ona.io on 13/11/2017.
 */

public class LatLngParcelable extends LatLng implements Parcelable {

    public static final Parcelable.Creator CREATOR = new Creator() {
        @Override
        public Object createFromParcel(Parcel source) {
            return new LatLngParcelable(source);
        }

        @Override
        public Object[] newArray(int size) {
            return new LatLngParcelable[size];
        }
    };

    public LatLngParcelable(Parcel source) {
        setLatitude(source.readDouble());
        setLongitude(source.readDouble());
        setAltitude(source.readDouble());
    }

    public LatLngParcelable(double latitude, double longitude) {
        setLatitude(latitude);
        setLongitude(longitude);
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeDouble(getLatitude());
        out.writeDouble(getLongitude());
        out.writeDouble(getAltitude());
    }
}
