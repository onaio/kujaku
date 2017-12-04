package utils.exceptions;

/**
 * Thrown when data eg. {@link org.json.JSONObject} is malformed/invalid/insufficient
 *
 * Created by Ephraim Kigamba - ekigamba@ona.io on 20/11/2017.
 * Created by Jason Rogena - jrogena@ona.io on 20/11/2017.
 */

public class MalformedDataException extends Exception {

    public MalformedDataException(String message) {
        super(message);
    }

    public MalformedDataException(String message, Exception exception) {
        super(message, exception);
    }
}
