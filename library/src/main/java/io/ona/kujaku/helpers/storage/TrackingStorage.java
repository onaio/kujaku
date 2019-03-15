package io.ona.kujaku.helpers.storage;

import android.location.Location;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Emmanuel OTIN - eo@novel-t.ch on 12/03/2019.
 *
 */
public class TrackingStorage extends BaseStorage {
    private static final String BASE_DIRECTORY = ".KujakuTracking";

    private static final String CURRENT_DIRECTORY = "Current";
    private static final String PREVIOUS_DIRECTORY = "Previous";

    private static final String FILE_NAME = "Tracks";
    private static final String FILE_EXTENSION = ".json";

    /**
     * Write serialized StoreLocation in a file
     *
     * @param location
     * @param index
     */
    public void writeLocation(Location location, int index) {
        String folderName = BASE_DIRECTORY + File.separator + CURRENT_DIRECTORY;
        String fileName = FILE_NAME + "_" + index + FILE_EXTENSION;

        if (! fileExists(folderName, fileName)) {
            createFile(folderName, fileName);
        }

        gsonWriteObject(folderName, fileName, new StoreLocation(location));
    }

    /**
     * Init TrackingService Store Location
     */
    public void initLocationStorage() {
        // If directory previous exists, delete it
        String previousFolderName = BASE_DIRECTORY + File.separator + PREVIOUS_DIRECTORY;
        if (directoryExists(previousFolderName)) {
            deleteFile(previousFolderName, true);
        }

        // Rename current directory to previous
        renameFile(BASE_DIRECTORY, CURRENT_DIRECTORY, BASE_DIRECTORY, PREVIOUS_DIRECTORY);
    }

    public List<Location> getCurrentRecordedLocations() {
        String folderName = BASE_DIRECTORY + File.separator + CURRENT_DIRECTORY ;

        List<Location> result = new ArrayList<Location>();

        if (directoryExists(folderName)) {
            File directory = new File(Environment.getExternalStorageDirectory(), folderName);
            if (directory.canRead()) {
                File[] files = directory.listFiles();
                if (files != null) {
                    for (int i = 0; i < files.length; i++) {
                        StoreLocation storeLoc = gsonReadObject(folderName, files[i].getName(), StoreLocation.class);
                        if (storeLoc != null) {
                            result.add(StoreLocation.locationFromStoreLocation(storeLoc));
                        }
                    }
                }
            } else {
                Log.d(TAG, "Cannot read folder " + directory.getAbsolutePath());
            }
        }

        return result;
    }

    @Override
    protected String getDirectory() {
        return BASE_DIRECTORY;
    }
}

class StoreLocation {

    String provider;
    double latitude;
    double longitude;

    StoreLocation(Location location) {
        this.provider = location.getProvider();
        this.latitude = location.getLatitude();
        this.longitude = location.getLongitude();

    }

    static Location locationFromStoreLocation(StoreLocation storeLocation) {
        Location location = new Location(storeLocation.provider);
        location.setLatitude(storeLocation.latitude);
        location.setLongitude(storeLocation.longitude);

        return location;
    }
}

