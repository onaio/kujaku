package io.ona.kujaku.sample.repository;

import android.content.Context;

import net.zetetic.database.sqlcipher.SQLiteDatabase;
import net.zetetic.database.sqlcipher.SQLiteOpenHelper;

public class KujakuRepository extends SQLiteOpenHelper {

    private static final String TAG = PointsRepository.class.getName();
    protected SQLiteDatabase readableDatabase;
    protected SQLiteDatabase writableDatabase;
    private String password = "";


    public KujakuRepository(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, null, version);
        System.loadLibrary("sqlcipher");
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
