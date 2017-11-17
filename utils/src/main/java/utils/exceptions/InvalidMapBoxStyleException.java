package utils.exceptions;

/**
 * Thrown when a MapBox style object is invalid or has missing required data.
 *
 * Created by Jason Rogena - jrogena@ona.io on 11/7/17.
 */

public class InvalidMapBoxStyleException extends Exception {
    public InvalidMapBoxStyleException(String message) {
        super(message);
    }
}
