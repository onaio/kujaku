package io.ona.kujaku.helpers;

import android.os.Environment;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.snatik.storage.Storage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.UUID;

import io.ona.kujaku.activities.MapActivity;

import io.ona.kujaku.utils.Constants;

/**
 * Helps add the MapBox Style to the MapBox MapView when provided as string since the MapBox API only allows
 * urls i.e. assets, network & storage. Mapbox style should follow the <a href="">Mapbox Style Spec</a><br/><br/>
 * <p>
 * Basically stores the style on shared External Storage<br/><br/>
 * <p>
 * HOW TO USE<br/>
 * ----------<br/>
 * <p>
 * {@code MapBoxStyleStorage mapboxStyleStorage = new MapBoxStyleStorage()}
 * {@code mapboxStyleStorage.getStyleURL("file:///storage/Downloads/style.json")}
 * {@code mapboxStyleStorage.getStyleURL("asset://town_style.json")}
 * {@code mapboxStyleStorage.getStyleURL("https://companysite.com/style/style.json")}
 * {@code mapboxStyleStorage.getStyleURL("{ 'version': 8, 'name': 'kujaku-map', 'metadata': {}, }")}<br/><br/>
 * {@code mapboxStyleStorage.deleteFile("json.style", false)}
 * {@code mapboxStyleStorage.deleteFile("/sdcard/Downloads/json.style", true)}
 * {@code mapboxStyleStorage.deleteFile("/sdcard/Downloads/json.style")}
 * <p>
 * Created by Ephraim Kigamba - ekigamba@ona.io on 09/11/2017.
 */

public class MapBoxStyleStorage {
    public static final String DIRECTORY = ".KujakuStyles";
    private static final String TAG = MapBoxStyleStorage.class.getSimpleName();

    /**
     * Converts the Mapbox style supplied to a usable resource by the Mapbox API by ensuring that the
     * Mapbox style passed is either a file, network url or android asset file. It then provides an
     * appropriate resource link where one is not provided
     *
     * @param stylePathOrJSON Mapbox Style Path or Mapbox Style JSON String
     * @return Path to the MapBox Style in the format {@code file://[Path_to_file] }
     */
    public String getStyleURL(@NonNull String stylePathOrJSON) {
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
            fileCreated = !TextUtils.isEmpty(fileName);
        }

        return "file://" + fileName;
    }

    /**
     * Writes content to a file on local storage
     *
     * @param folderName Folder name(s) eg. AppFiles where to create file
     * @param fileName   Filename with extension
     * @param content    String content to be written to the file
     * @return Absolute Path to the Stored file if SUCCESSFUL eg /emulated/storage/... or NULL is the operation FAILS
     * This operation fails if the file exists, permissions denied, invalid
     */
    public String writeToFile(String folderName, String fileName, String content) {
        File file = new File(Environment.getExternalStorageDirectory(), folderName + File.separator + fileName);

        new File(Environment.getExternalStorageDirectory(), folderName)
                .mkdirs();

        if (!file.exists()) {
            try {
                FileWriter fileWriter = new FileWriter(file.getAbsoluteFile());
                fileWriter.write(content);
                fileWriter.flush();
                fileWriter.close();

                return file.getAbsolutePath();
            } catch (IOException e) {
                Log.e(TAG, Log.getStackTraceString(e));
            }
        }

        return null;
    }

    /**
     * Deletes a file on external/shared storage given the path or file name
     * This should be called on {@link MapActivity#onDestroy()} so as to clean up the resources created
     *
     * @param filePath       Path to the file eg. /emulated/storage/style.json, style.json
     * @param isCompletePath Flag indicating whether the Path is complete
     *                       eg. /emulated/storage/style.json is a complete path
     *                       style.json is not a complete path & will be resolved to /sdcard/{@link MapBoxStyleStorage#DIRECTORY}/styles.json
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
     * This should be called on {@link MapActivity#onDestroy()} so as to clean up the resources created
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

    /**
     * Caches Mapbox styles in Folder: {@value DIRECTORY} as user/stylename eg. mapbox://style/edwin/9iowdcjsd
     * will be saved in <strong>{@value DIRECTORY}/edwin/9iowdcjsd</strong>
     *
     * @param mapBoxUrl
     * @param mapBoxStyleJSON
     * @return <p>- {@code TRUE} if the style is cached successfully<br/>
     * - {@code FALSE} if the style is not cached successfully</p>
     */
    public boolean cacheStyle(@NonNull String mapBoxUrl, @NonNull String mapBoxStyleJSON) {
        if (mapBoxUrl.matches(Constants.MAP_BOX_URL_FORMAT)) {
            String[] mapBoxPaths = mapBoxUrl.replace("mapbox://styles/", "").split("/");
            String folder = mapBoxPaths[0];
            String filename = mapBoxPaths[1];

            String fileAbsolutePath = writeToFile(DIRECTORY + File.separator + folder, filename, mapBoxStyleJSON);
            return !TextUtils.isEmpty(fileAbsolutePath);
        }

        return false;
    }

    /**
     * Retrieves the cached style if one exists
     *
     * @param mapBoxUrl
     * @return <p>- The cached style JSON String, if it exists on local storage<br>
     * - Empty String("") if the style is not cached</p>
     */
    public String getCachedStyle(String mapBoxUrl) {
        if (mapBoxUrl.matches(Constants.MAP_BOX_URL_FORMAT)) {
            String[] mapBoxPaths = mapBoxUrl.replace("mapbox://styles/", "").split("/");
            String folder = mapBoxPaths[0];
            String filename = mapBoxPaths[1];

            return readFile(folder, filename);
        }
        return null;
    }

    /**
     * Reads a style on local storage given the path using the format {@literal file://{file_path}}
     *
     * @param protocolledFilePath
     * @return <p>- NULL if the file does not exist<br/>
     * - The style's JSON String if the file exists</p>
     */
    public String readStyle(@NonNull String protocolledFilePath) {
        String fileProtocolOrSth = "file://";
        if (protocolledFilePath.isEmpty() || !protocolledFilePath.startsWith(fileProtocolOrSth)) {
            return null;
        }
        String folders = "";

        if (protocolledFilePath.lastIndexOf(File.separator) > 6) {
            folders = protocolledFilePath.substring(
                    fileProtocolOrSth.length(),
                    protocolledFilePath.lastIndexOf(File.separator));
        }

        String fileName = protocolledFilePath.substring(
                protocolledFilePath.lastIndexOf(File.separator) + 1
        );

        return readFile(folders, fileName, true);
    }

    private String readFile(String folders, String filename) {
        return readFile(folders, filename, false);
    }

    /**
     * Reads the contents of a file, returns them as a string
     *
     * @param folders        The directory hierarchy for the file
     * @param filename       The name of the file to read
     * @param isPathComplete
     * @return NULL if unable to read the file or a String containing the contents of the file
     */
    private String readFile(String folders, String filename, boolean isPathComplete) {
        File fileFolders;

        if (isPathComplete) {
            fileFolders = new File(folders);
        } else {
            fileFolders = new File(Environment.getExternalStorageDirectory() + folders);
        }

        if (!fileFolders.exists()) {
            fileFolders.mkdirs();
        }

        File finalFile;
        if (isPathComplete) {
            finalFile = new File(folders + File.separator + filename);
        } else {
            finalFile = new File(Environment.getExternalStorageDirectory(), folders + File.separator + filename);
        }
        StringBuilder text = new StringBuilder();

        try {
            BufferedReader br = new BufferedReader(new FileReader(finalFile));
            String line;

            while ((line = br.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
            br.close();
        } catch (IOException e) {
            Log.e(TAG, Log.getStackTraceString(e));
            return null;
        }

        return text.toString();

    }
}
