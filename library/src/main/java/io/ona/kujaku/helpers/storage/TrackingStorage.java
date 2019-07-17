package io.ona.kujaku.helpers.storage;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.ona.kujaku.location.KujakuLocation;

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
     * @param kujakuLocation
     * @param index
     */
    public void writeLocation(KujakuLocation kujakuLocation, int index) {
        String folderName = BASE_DIRECTORY + File.separator + CURRENT_DIRECTORY;
        String fileName = FILE_NAME + "_" + index + FILE_EXTENSION;

        if (! fileExists(folderName, fileName)) {
            createFile(folderName, fileName);
        }

        if (kujakuLocation != null) {
            gsonWriteObject(folderName, fileName, new StoreLocation(kujakuLocation));
        }
    }

    /**
     * Init TrackingService Store KujakuLocation
     */
    public void initKujakuLocationStorage() {
        // If directory previous exists, delete it
        String previousFolderName = BASE_DIRECTORY + File.separator + PREVIOUS_DIRECTORY;
        if (directoryExists(previousFolderName)) {
            deleteFile(PREVIOUS_DIRECTORY, false, true);
        }

        // Rename current directory to previous
        renameFile(BASE_DIRECTORY, CURRENT_DIRECTORY, BASE_DIRECTORY, PREVIOUS_DIRECTORY);
    }

    /**
     * Get Current list of KujakuLocations
     *
     * @return
     */
    public List<KujakuLocation> getCurrentRecordedKujakuLocations() {
        String folderName = BASE_DIRECTORY + File.separator + CURRENT_DIRECTORY ;
        return getRecordedKujakuLocations(folderName);
    }

    /**
     * Get Previous list of KujakuLocations
     *
     * @return
     */
    public List<KujakuLocation> getPreviousRecordedKujakuLocations() {
        String folderName = BASE_DIRECTORY + File.separator + PREVIOUS_DIRECTORY ;
        return getRecordedKujakuLocations(folderName);
    }

    /**
     * Get List of KujakuLocations in a specific folder
     *
     * @param folderName
     * @return
     */
    private List<KujakuLocation> getRecordedKujakuLocations(String folderName) {
        List<KujakuLocation> result = new ArrayList<>();

        if (directoryExists(folderName)) {
            File directory = new File(Environment.getExternalStorageDirectory(), folderName);
            if (directory.canRead()) {
                File[] files = directory.listFiles();
                if (files != null) {
                    for (int i = 0; i < files.length; i++) {
                        StoreLocation storeLoc = gsonReadObject(folderName, files[i].getName(), StoreLocation.class);
                        if (storeLoc != null) {
                            result.add(StoreLocation.kujakuLocationFromStoreLocation(storeLoc));
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
