package io.ona.kujaku.sample.repository;

import android.util.Log;

import net.sqlcipher.Cursor;
import net.sqlcipher.database.SQLiteDatabase;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import io.ona.kujaku.domain.Point;

import static io.ona.kujaku.utils.Constants.INSERT_OR_REPLACE;

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

    public void addOrUpdate(Point point) {

        if (point == null) {
            return;
        }

        if (point.getDateUpdated() == null) {
            point.setDateUpdated(Calendar.getInstance().getTimeInMillis());
        }

        try {
            SQLiteDatabase database = getWritableDatabase();

            String query = String.format(INSERT_OR_REPLACE, POINTS_TABLE);
            query += "(" + StringUtils.repeat("?", ",", POINTS_TABLE_COLUMNS.length) + ")";
            database.execSQL(query, createQueryValues(point));
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
    }

    public List<Point> getAllPoints() {

        List<Point> points = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = getReadableDatabase().rawQuery("SELECT * " + " FROM " +  POINTS_TABLE, null);
            points = readPoints(cursor);
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return points;
    }

    public Point getPoint(String id) {
        Point point = null;
        Cursor cursor = null;
        try {
            cursor = getReadableDatabase().rawQuery("SELECT * " + " FROM " + POINTS_TABLE + " WHERE " + ID + "=?", new String[]{id});
            List<Point> points = readPoints(cursor);
            point = points.size() > 0 ? points.get(0) : null;
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return point;
    }

    private List<Point> readPoints(Cursor cursor) {

        List<Point> points = new ArrayList<>();
        try {
            if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
                while (!cursor.isAfterLast()) {
                    points.add(createPoint(cursor));
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
        return points;
    }

    private Point createPoint(Cursor cursor) {
        return new Point(
                cursor.getInt(cursor.getColumnIndex(ID)),
                cursor.getDouble(cursor.getColumnIndex(LAT)),
                cursor.getDouble(cursor.getColumnIndex(LNG))
        );
    }

    private Object[] createQueryValues(Point Point) {
        Object[] values = new Object[]{
                Point.getId(),
                Point.getLat(),
                Point.getLng(),
                Point.getDateUpdated()
        };
        return values;
    }
}
