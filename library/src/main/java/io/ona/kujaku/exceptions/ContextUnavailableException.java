package io.ona.kujaku.exceptions;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 03/10/2018
 */

public class ContextUnavailableException extends Exception {

    public ContextUnavailableException() {
        super("Context is null!");
    }

    public ContextUnavailableException(String message) {
        super(message);
    }
}
