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

        if (location != null) {
            gsonWriteObject(folderName, fileName, new StoreLocation(location));
        }
    }

    /**
     * Init TrackingService Store Location
     */
    public void initLocationStorage() {
        // If directory previous exists, delete it
        String previousFolderName = BASE_DIRECTORY + File.separator + PREVIOUS_DIRECTORY;
        if (directoryExists(previousFolderName)) {
            deleteFile(PREVIOUS_DIRECTORY, false, true);
        }

        // Rename current directory to previous
        renameFile(BASE_DIRECTORY, CURRENT_DIRECTORY, BASE_DIRECTORY, PREVIOUS_DIRECTORY);
    }

    /**
     * Get Current list of Locations
     *
     * @return
     */
    public List<Location> getCurrentRecordedLocations() {
        String folderName = BASE_DIRECTORY + File.separator + CURRENT_DIRECTORY ;
        return getRecordedLocations(folderName);
    }

    /**
     * Get Preivous list of Locations
     *
     * @return
     */
    public List<Location> getPreviousRecordedLocations() {
        String folderName = BASE_DIRECTORY + File.separator + PREVIOUS_DIRECTORY ;
        return getRecordedLocations(folderName);
    }

    /**
     * Get List of Locations in a specific folder
     *
     * @param folderName
     * @return
     */
    private List<Location> getRecordedLocations(String folderName) {
        List<Location> result = new ArrayList<>();

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
