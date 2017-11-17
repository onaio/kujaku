package utils.exceptions;

/**
 * Thrown when an error occurs trying to download a MapBox Offline Map
 *
 *
 * Created by Ephraim Kigamba - ekigamba@ona.io on 13/11/2017.
 */

public class OfflineMapDownloadException extends Exception {

    public OfflineMapDownloadException(String message) {
        super(message);
    }
}
