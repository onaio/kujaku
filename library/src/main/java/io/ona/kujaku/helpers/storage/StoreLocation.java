package io.ona.kujaku.helpers.storage;

import io.ona.kujaku.location.KujakuLocation;

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
    private long tag;

    StoreLocation(KujakuLocation kujakuLocation) {
        this.provider = kujakuLocation.getProvider();
        this.latitude = kujakuLocation.getLatitude();
        this.longitude = kujakuLocation.getLongitude();
        this.accuracy = kujakuLocation.getAccuracy();
        this.altitude = kujakuLocation.getAltitude();
        this.time = kujakuLocation.getTime();
        this.bearing = kujakuLocation.getBearing();
        this.speed = kujakuLocation.getSpeed();
        this.tag = kujakuLocation.getTag();
    }

    /**
     * Static method to get a Location from a StoreLocation instance
     *
     * @param storeLocation
     * @return Location
     */
    protected static KujakuLocation kujakuLocationFromStoreLocation(StoreLocation storeLocation) {
        KujakuLocation kujakuLocation = new KujakuLocation(storeLocation.provider);
        kujakuLocation.setLatitude(storeLocation.latitude);
        kujakuLocation.setLongitude(storeLocation.longitude);
        kujakuLocation.setAccuracy(storeLocation.accuracy);
        kujakuLocation.setAltitude(storeLocation.altitude);
        kujakuLocation.setTime(storeLocation.time);
        kujakuLocation.setBearing(storeLocation.bearing);
        kujakuLocation.setSpeed(storeLocation.speed);
        kujakuLocation.setTag(storeLocation.tag);

        return kujakuLocation;
    }
}
