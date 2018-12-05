package io.ona.kujaku.exceptions;

/**
 * Created by Emmanuel Otin - eo@novel-t.ch 04/12/18.
 */
public class WmtsCapabilitiesException extends Exception {

    public WmtsCapabilitiesException() {
        super("Wmts Capabilities error");
    }

    public WmtsCapabilitiesException(String message) {
        super(message);
    }

}
