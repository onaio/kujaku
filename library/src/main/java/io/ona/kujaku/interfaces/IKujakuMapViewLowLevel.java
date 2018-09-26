package io.ona.kujaku.interfaces;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONObject;

import io.ona.kujaku.listeners.OnLocationChanged;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 26/09/2018
 */

public interface IKujakuMapViewLowLevel {

    void enableAddPoint(boolean canAddPoint);

    void enableAddPoint(boolean canAddPoint, OnLocationChanged onLocationChanged);

    JSONObject dropPoint(LatLng latLng);
}
