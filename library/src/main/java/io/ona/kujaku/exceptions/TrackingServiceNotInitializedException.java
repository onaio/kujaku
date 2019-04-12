package io.ona.kujaku.exceptions;

/**
 * Created by Emmanuel Otin - eo@novel-t.ch 12/04/19.
 */
public class TrackingServiceNotInitializedException extends Exception {

    public TrackingServiceNotInitializedException() {
        super("The TackingService has not been initialized");
    }

    public TrackingServiceNotInitializedException(String message) {
        super(message);
    }

}
