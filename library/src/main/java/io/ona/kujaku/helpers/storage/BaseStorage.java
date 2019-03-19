package io.ona.kujaku.helpers.storage;

import android.os.Environment;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import io.ona.kujaku.activities.MapActivity;

/**
 * Created by Emmanuel OTIN - eo@novel-t.ch on 12/03/2019.
 *
 */
public abstract class BaseStorage {
    protected static final String TAG = BaseStorage.class.getSimpleName();

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
     * Serialize in a file an object with the com.google.gson library
     *
     * @param folderName
     * @param fileName
     * @param object
     * @return {@code TRUE} serialization worked, {@code FALSE} otherwise
     */
    public boolean gsonWriteObject(String folderName, String fileName, Object object) {
        File file = new File(Environment.getExternalStorageDirectory(), folderName + File.separator + fileName);
        Gson gson = new GsonBuilder().create();

        try {
            FileWriter fw  = new FileWriter(file);
            gson.toJson(object, fw);
            fw.close();

            return true;
        } catch (IOException ex) {
            Log.e(TAG, "An error occurs when writing object with Gson", ex);
        }

        return false;
    }

    /**
     * Deserialize from a file an object with the com.google.gson library
     *
     * @param folderName
     * @param fileName
     * @param objClass
     * @param <T>
     * @return
     */
    public <T> T gsonReadObject(String folderName, String fileName, Class<T> objClass) {
        File file = new File(Environment.getExternalStorageDirectory(), folderName + File.separator + fileName);
        Gson gson = new GsonBuilder().create();

        try {
            FileReader fr  = new FileReader(file);
            T obj = gson.fromJson(fr, objClass);
            fr.close();
            return obj;
        } catch (IOException ex) {
            Log.e(TAG, "An error occurs when reading object with Gson", ex);
        }

        return null ;
    }

    /**
     * Test if file exists
     *
     * @param folderName Folder name(s) eg. AppFiles where to create file
     * @param fileName Filename with extension
     * @return {@code TRUE} if file exists, {@code FALSE} otherwise
     */
    public boolean fileExists(String folderName, String fileName) {
        File file = new File(Environment.getExternalStorageDirectory(), folderName + File.separator + fileName);
        return file.exists();
    }

    /**
     * Test if directory exists
     *
     * @param folderName Folder name(s) eg. AppFiles where to create file
     * @return {@code TRUE} if directory exists, {@code FALSE} otherwise
     */
    public boolean directoryExists(String folderName) {
        File dir = new File(Environment.getExternalStorageDirectory(), folderName);
        return dir.exists() && dir.isDirectory();
    }

    /**
     * Create file
     *
     * @param folderName Folder name(s) eg. AppFiles where to create file
     * @param fileName Filename with extension
     * @return result of File.creation method
     */
    public boolean createFile(String folderName, String fileName) {
        File file = new File(Environment.getExternalStorageDirectory(), folderName + File.separator + fileName);

        if (!fileExists(folderName, fileName)) {
            try {
                new File(Environment.getExternalStorageDirectory(), folderName)
                        .mkdirs();
                return file.createNewFile();
            } catch (Exception ex) {
                Log.e(TAG, "The file already exists", ex);
            }
        }

        return false ;
    }


    /**
     * Rename a file from the old file name to the new filename
     *
     * @param oldFolderName
     * @param oldFileName
     * @param newFolderName
     * @param newFileName
     * @return result of File.renameTo method
     */
    public boolean renameFile(String oldFolderName, String oldFileName, String newFolderName, String newFileName) {
        File from = new File(Environment.getExternalStorageDirectory(),oldFolderName + File.separator + oldFileName);
        File to = new File(Environment.getExternalStorageDirectory(),newFolderName + File.separator + newFileName);
        return from.renameTo(to);
    }

    /**
     * Deletes a file on external/shared storage given the path or file name
     * This should be called on {@link MapActivity#onDestroy()} so as to clean up the resources created
     *
     * @param filePath       Path to the file eg. /emulated/storage/style.json, style.json
     * @param isCompletePath Flag indicating whether the Path is complete
     *                       eg. /emulated/storage/style.json is a complete path
     *                       style.json is not a complete path & will be resolved to /sdcard/{@link MapBoxStyleStorage#BASE_DIRECTORY}/styles.json
     * @param isFolder        if it is a folder
     * @return {@code TRUE} if the operation was SUCCESSFUL, {@code FALSE} if it failed
     */
    public boolean deleteFile(String filePath, boolean isCompletePath, boolean isFolder) {
        if (!isCompletePath) {
            filePath = Environment.getExternalStorageDirectory() + File.separator + getDirectory() + File.separator + filePath;
        }

        if (!isFolder) {
            return deleteFile(filePath);
        } else {
            return deleteFolder(filePath);
        }

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
     * Deletes a folder given the complete path
     *
     * @param folderPath Path to the file eg. /emulated/storage/folder
     * @return {@code TRUE} if the operation was SUCCESSFUL, {@code FALSE} if it failed
     */
    public boolean deleteFolder(String folderPath) {
        File folder = new File(folderPath);

        if (!folder.isDirectory()) {
            return false;
        }

        // delete all files in there
        File[] files = folder.listFiles();
        for (File file: files) {
            file.delete() ;
        }

        return folder.delete();
    }

    /**
     * Reads the contents of a file, returns them as a string
     *
     * @param folders        The directory hierarchy for the file
     * @param filename       The name of the file to read
     * @param isPathComplete
     * @return NULL if unable to read the file or a String containing the contents of the file
     */
    public String readFile(String folders, String filename, boolean isPathComplete) {
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

    /**
     * Reads the contents of a file, returns them as a string
     *
     * @param folders        The directory hierarchy for the file
     * @param filename       The name of the file to read
     * @return NULL if unable to read the file or a String containing the contents of the file
     */
    public String readFile(String folders, String filename) {
        return readFile(folders, filename, false);
    }

    /**
     * Return the directory name that needs to be defined in children classes
     *
     * @return
     */
    protected abstract String getDirectory();
}
