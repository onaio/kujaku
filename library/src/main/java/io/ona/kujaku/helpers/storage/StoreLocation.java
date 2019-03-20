package io.ona.kujaku.helpers.storage;

import android.location.Location;

/**
 * Class StoreLocation used to store location with Gson
 */
class StoreLocation {

    private String provider;
    private double latitude;
    private double longitude;
    private float accuracy;
    private double altitude;
    private long time;
    private float bearing;
    private float speed;

    StoreLocation(Location location) {
        this.provider = location.getProvider();
        this.latitude = location.getLatitude();
        this.longitude = location.getLongitude();
        this.accuracy = location.getAccuracy();
        this.altitude = location.getAltitude();
        this.time = location.getTime();
        this.bearing = location.getBearing();
        this.speed = location.getSpeed();
    }

    /**
     * Static method to get a Location from a StoreLocation instance
     *
     * @param storeLocation
     * @return Location
     */
    static Location locationFromStoreLocation(StoreLocation storeLocation) {
        Location location = new Location(storeLocation.provider);
        location.setLatitude(storeLocation.latitude);
        location.setLongitude(storeLocation.longitude);
        location.setAccuracy(storeLocation.accuracy);
        location.setAltitude(storeLocation.altitude);
        location.setTime(storeLocation.time);
        location.setBearing(storeLocation.bearing);
        location.setSpeed(storeLocation.speed);

        return location;
    }
}
