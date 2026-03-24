package io.ona.kujaku.sample.repository;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.util.Log;


import net.zetetic.database.sqlcipher.SQLiteDatabase;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import io.ona.kujaku.domain.PointModel;

import static io.ona.kujaku.sample.util.Constants.INSERT_OR_REPLACE;

/**
 * @author Vincent Karuri
 */
public class PointsRepository extends BaseRepository {

    public static final String TAG = PointsRepository.class.getName();
    public static final String POINTS_TABLE = "points";
    public static final String ID = "id";
    public static final String LAT = "lat";
    public static final String LNG =  "lng";
    public static final String DATE_UPDATED = "date_updated";

    public static final String[] POINTS_TABLE_COLUMNS = {ID, LAT, LNG, DATE_UPDATED};

    public static final String CREATE_POINTS_TABLE =

            "CREATE TABLE " + POINTS_TABLE
            + "("
                    + ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + LAT + " REAL NOT NULL,"
                    + LNG + " REAL NOT NULL,"
                    + DATE_UPDATED + " INTEGER"
            + ")";

    public PointsRepository(KujakuRepository repository) { super(repository); }

    public static void createTable(SQLiteDatabase database) {
        database.execSQL(CREATE_POINTS_TABLE);
    }

    public void addOrUpdate(PointModel pointModel) {

        if (pointModel == null) {
            return;
        }

        if (pointModel.getDateUpdated() == null) {
            pointModel.setDateUpdated(Calendar.getInstance().getTimeInMillis());
        }

        try {
            SQLiteDatabase database = getWritableDatabase();

            String query = String.format(INSERT_OR_REPLACE, POINTS_TABLE);
            query += "(" + StringUtils.repeat("?", ",", POINTS_TABLE_COLUMNS.length) + ")";
            database.execSQL(query, createQueryValues(pointModel));
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
    }

    public List<PointModel> getAllPoints() {

        List<PointModel> pointModels = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = getReadableDatabase().rawQuery("SELECT * " + " FROM " +  POINTS_TABLE, null);
            pointModels = readPoints(cursor);
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return pointModels;
    }

    public PointModel getPoint(String id) {
        PointModel pointModel = null;
        Cursor cursor = null;
        try {
            cursor = getReadableDatabase().rawQuery("SELECT * " + " FROM " + POINTS_TABLE + " WHERE " + ID + "=?", new String[]{id});
            List<PointModel> pointModels = readPoints(cursor);
            pointModel = pointModels.size() > 0 ? pointModels.get(0) : null;
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return pointModel;
    }

    private List<PointModel> readPoints(Cursor cursor) {

        List<PointModel> pointModels = new ArrayList<>();
        try {
            if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
                while (!cursor.isAfterLast()) {
                    pointModels.add(createPoint(cursor));
                    cursor.moveToNext();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return pointModels;
    }

    @SuppressLint("Range")
    private PointModel createPoint(Cursor cursor) {
        return new PointModel(
                cursor.getInt(cursor.getColumnIndex(ID)),
                cursor.getDouble(cursor.getColumnIndex(LAT)),
                cursor.getDouble(cursor.getColumnIndex(LNG))
        );
    }

    private Object[] createQueryValues(PointModel pointModel) {
        Object[] values = new Object[]{
                pointModel.getId(),
                pointModel.getLat(),
                pointModel.getLng(),
                pointModel.getDateUpdated()
        };
        return values;
    }
}
