package io.ona.kujaku.helpers;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.UUID;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 09/11/2017.
 */

public class MapBoxStyleStorage {
    private static final String DIRECTORY = ".KujakuStyles";
    private static final String TAG = MapBoxStyleStorage.class.getSimpleName();

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

    public boolean deleteFile(String filePath, boolean isCompletePath) {
        if (!isCompletePath) {
            filePath = Environment.getExternalStorageDirectory() + DIRECTORY + File.separator + filePath;
        }
        return deleteFile(filePath);
    }

    public boolean deleteFile(String filePath) {
        File file = new File(filePath);

        if (!file.exists()) {
            return false;
        }
        return file.delete();
    }
}
