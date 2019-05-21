package io.ona.kujaku.exceptions;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 2019-05-20
 */

public class InvalidStyleStateException extends Exception {

    public InvalidStyleStateException() {
        super("Operation failed: Style is not in a valid state");
    }
}
