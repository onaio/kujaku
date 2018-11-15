package io.ona.kujaku.sample.repository;

import android.content.Context;
import android.util.Log;

import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteOpenHelper;

/**
 * @author Vincent Karuri
 */
public class KujakuRepository extends SQLiteOpenHelper {

    private static final String TAG = PointsRepository.class.getName();
    protected SQLiteDatabase readableDatabase;
    protected SQLiteDatabase writableDatabase;
    private String password = "";


    public KujakuRepository(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, null, version);
        SQLiteDatabase.loadLibs(context); // this must be added
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        createTables(database);
    }

    private void createTables(SQLiteDatabase database) {
        // instantiate all tables here
        PointsRepository.createTable(database);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {

    }

    public SQLiteDatabase getReadableDatabase() {
        return getReadableDatabase(password);
    }

    public SQLiteDatabase getWritableDatabase() {
        return getWritableDatabase(password); // could add password field if you wanted
    }

    @Override
    public synchronized SQLiteDatabase getReadableDatabase(String password) {
        try {
            if (readableDatabase == null || !readableDatabase.isOpen()) {
                readableDatabase = super.getReadableDatabase(password);
            }
            return readableDatabase;
        } catch (Exception e) {
            Log.e(TAG, "Database Error. " + e.getMessage());
        }
        return readableDatabase;
    }

    @Override
    public synchronized SQLiteDatabase getWritableDatabase(String password) {
        try {
            if (writableDatabase == null || !writableDatabase.isOpen()) {
                writableDatabase = super.getWritableDatabase(password);
            }
        } catch (Exception e) {
            Log.e(TAG, "Database Error. " + e.getMessage());
        }
        return writableDatabase;
    }

    @Override
    public synchronized void close() {
        if (readableDatabase != null) {
            readableDatabase.close();
        }

        if (writableDatabase != null) {
            writableDatabase.close();
        }
        super.close();
    }
}
