package io.ona.kujaku.helpers;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.UUID;

/**
 * Helps add the MapBox Style to the MapBox MapView when provided as string since the MapBox API only allows
 * urls i.e. assets, network & storage. Mapbox style should follow the <a href="">Mapbox Style Spec</a><br/><br/>
 *
 * Basically stores the style on shared External Storage<br/><br/>
 *
 * HOW TO USE<br/>
 * ----------<br/>
 *
 * {@code MapBoxStyleStorage mapboxStyleStorage = new MapBoxStyleStorage()}
 * {@code mapboxStyleStorage.getFilePath("file:///storage/Downloads/style.json")}
 * {@code mapboxStyleStorage.getFilePath("asset://town_style.json")}
 * {@code mapboxStyleStorage.getFilePath("https://companysite.com/style/style.json")}
 * {@code mapboxStyleStorage.getFilePath("{ 'version': 8, 'name': 'kujaku-map', 'metadata': {}, }")}<br/><br/>
 * {@code mapboxStyleStorage.deleteFile("json.style", false)}
 * {@code mapboxStyleStorage.deleteFile("/sdcard/Downloads/json.style", true)}
 * {@code mapboxStyleStorage.deleteFile("/sdcard/Downloads/json.style")}
 *
 * Created by Ephraim Kigamba - ekigamba@ona.io on 09/11/2017.
 */

public class MapBoxStyleStorage {
    private static final String DIRECTORY = ".KujakuStyles";
    private static final String TAG = MapBoxStyleStorage.class.getSimpleName();

    /**
     * Converts the Mapbox style supplied to a usable resource by the Mapbox API by ensuring that the
     * Mapbox style passed is either a file, network url or android asset file. It then provides an
     * appropriate resource link where one is not provided
     *
     * @param stylePathOrJSON Mapbox Style Path or Mapbox Style JSON String
     * @return Path to the MapBox Style in the format {@code file://[Path_to_file] }
     */
    public String getFilePath(String stylePathOrJSON) {
        if (stylePathOrJSON.startsWith("file:")
                || stylePathOrJSON.startsWith("asset:")
                || stylePathOrJSON.startsWith("http:")
                || stylePathOrJSON.startsWith("https:")) {
            return stylePathOrJSON;
        }

        String fileName = "";
        boolean fileCreated = false;

        while (!fileCreated) {
            fileName = UUID.randomUUID().toString() + ".json";
            fileName = writeToFile(DIRECTORY, fileName, stylePathOrJSON);
            fileCreated = !fileName.isEmpty();
        }

        return "file://" + fileName;
    }

    /**
     *
     * @param folderName Folder name(s) eg. AppFiles where to create file
     * @param fileName Filename with extension
     * @param content String content to be written to the file
     * @return Absolute Path to the Stored file if SUCCESSFUL eg /emulated/storage/... OR "" is the operation FAILS
     *          This operation fails if the file exists, permissions denied, invalid
     */
    private String writeToFile(String folderName, String fileName, String content) {
        File file = new File(Environment.getExternalStorageDirectory(), folderName + File.separator + fileName);

        new File(Environment.getExternalStorageDirectory(), folderName)
                .mkdir();

        if (!file.exists()) {
            try {
                FileWriter fileWriter = new FileWriter(file.getAbsoluteFile());
                fileWriter.write(content);
                fileWriter.flush();
                fileWriter.close(); file.getAbsolutePath();

                return file.getAbsolutePath();
            } catch (IOException e) {
                Log.e(TAG, Log.getStackTraceString(e));
            }
        }

        return "";
    }

    /**
     * Deletes a file on external/shared storage given the path or file name
     *
     * @param filePath Path to the file eg. /emulated/storage/style.json, style.json
     * @param isCompletePath Flag indicating whether the Path is complete
     *                       eg. /emulated/storage/style.json is a complete path
     *                              style.json is not a complete path & will be resolved to /sdcard/{@link MapBoxStyleStorage#DIRECTORY}/styles.json
     * @return {@code TRUE} if the operation was SUCCESSFUL, {@code FALSE} if it failed
     */
    public boolean deleteFile(String filePath, boolean isCompletePath) {
        if (!isCompletePath) {
            filePath = Environment.getExternalStorageDirectory() + DIRECTORY + File.separator + filePath;
        }
        return deleteFile(filePath);
    }

    /**
     * Deletes a file given the complete path
     *
     * @param filePath Path to the file eg. /emulated/storage/style.json
     * @return {@code TRUE} if the operation was SUCCESSFUL, {@code FALSE} if it failed
     */
    public boolean deleteFile(String filePath) {
        File file = new File(filePath);

        if (!file.exists()) {
            return false;
        }
        return file.delete();
    }
}
