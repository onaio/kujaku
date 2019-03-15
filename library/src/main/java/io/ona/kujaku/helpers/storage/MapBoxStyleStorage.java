package io.ona.kujaku.helpers.storage;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.io.File;
import java.util.UUID;

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

public class MapBoxStyleStorage extends BaseStorage {
    public static final String BASE_DIRECTORY = ".KujakuStyles";

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
                || stylePathOrJSON.startsWith("https:")
                || stylePathOrJSON.matches(Constants.MAP_BOX_URL_FORMAT)) {
            return stylePathOrJSON;
        }

        String fileName = "";
        boolean fileCreated = false;

        while (!fileCreated) {
            fileName = UUID.randomUUID().toString() + ".json";
            fileName = writeToFile(BASE_DIRECTORY, fileName, stylePathOrJSON);
            fileCreated = !TextUtils.isEmpty(fileName);
        }

        return "file://" + fileName;
    }

    /**
     * Caches Mapbox styles in Folder: {@value BASE_DIRECTORY} as user/stylename eg. mapbox://style/edwin/9iowdcjsd
     * will be saved in <strong>{@value BASE_DIRECTORY}/edwin/9iowdcjsd</strong>
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

            String fileAbsolutePath = writeToFile(BASE_DIRECTORY + File.separator + folder, filename, mapBoxStyleJSON);
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

    @Override
    protected String getDirectory() {
        return BASE_DIRECTORY;
    }
}
