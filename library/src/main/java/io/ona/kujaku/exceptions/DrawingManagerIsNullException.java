package io.ona.kujaku.exceptions;

/**
 * Created by Emmanuel Otin - eo@novel-t.ch 24/06/19.
 */
public class DrawingManagerIsNullException extends RuntimeException {

    public DrawingManagerIsNullException() {
        super("The DrawingManager is null");
    }

    public DrawingManagerIsNullException(String message) {
        super(message);
    }
}
