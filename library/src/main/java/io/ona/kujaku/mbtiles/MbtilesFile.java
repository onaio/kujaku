package io.ona.kujaku.mbtiles;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import timber.log.Timber;

/**
 * This class provides access to the metadata and tiles in a .mbtiles file.
 * An .mbtiles file is a SQLite database file containing specific tables and
 * columns, including tiles that may contain raster images or vector geometry.
 * See https://github.com/mapbox/mbtiles-spec for the detailed specification.
 */
public class MbtilesFile implements Closeable, TileHttpServer.TileSource {
    public enum Type { RASTER, VECTOR }

    protected File file;
    protected SQLiteDatabase db;
    protected String format;
    protected Type type;
    protected String contentType = "application/octet-stream";
    protected String contentEncoding = "identity";

    public MbtilesFile(File file) throws SQLiteException, UnsupportedFormatException {
        this.file = file;
        db = SQLiteDatabase.openOrCreateDatabase(file, null);

        // The "format" code indicates whether the binary tiles are raster image
        // files (JPEG, PNG) or protobuf-encoded vector geometry (PBF, MVT).
        format = getMetadata("format").toLowerCase(Locale.US);

        //Added this as a default some mbtiles lacks format in the metadata
        if(format.isEmpty())
            format="png";

        if (format.equals("pbf") || format.equals("mvt")) {
            contentType = "application/protobuf";
            contentEncoding = "gzip";
            type = Type.VECTOR;
        } else if (format.equals("jpg") || format.equals("jpeg")) {
            contentType = "image/jpeg";
            type = Type.RASTER;
        } else if (format.equals("png")) {
            contentType = "image/png";
            type = Type.RASTER;
        } else {
            db.close();
            throw new UnsupportedFormatException(file, format);
        }
    }

    public Type getType() {
        return type;
    }

    public void close() {
        db.close();
    }

    /** Queries the "metadata" table, which has just "name" and "value" columns. */
    public @NonNull String getMetadata(String key) {
        try (Cursor results = db.query("metadata", new String[] {"value"},
                "name = ?", new String[] {key}, null, null, null, null)) {
            return results.moveToFirst() ? results.getString(0) : "";
        }
    }

    /** Puts together the HTTP response for a given tile. */
    public TileHttpServer.Response getTile(int zoom, int x, int y) {
        // TMS coordinates are used in .mbtiles files, so Y needs to be flipped.
        byte[] data = getTileBlob(zoom, x, (1 << zoom) - 1 - y);
        return data == null ? null :
                new TileHttpServer.Response(data, contentType, contentEncoding);
    }

    /** Fetches a tile out of the .mbtiles SQLite database. */
    // PMD complains about returning null for an array return type, but we
    // really do want to return null when there is no tile available.
    @SuppressWarnings("PMD.ReturnEmptyArrayRatherThanNull")
    public byte[] getTileBlob(int zoom, int column, int row) {
        // We have to use String.format because the templating mechanism in
        // SQLiteDatabase.query is written for a strange alternate universe
        // in which numbers don't exist -- it only supports strings!
        String selection = String.format(
                Locale.US,
                "zoom_level = %d and tile_column = %d and tile_row = %d",
                zoom, column, row
        );

        try (Cursor results = db.query("tiles", new String[] {"tile_data"},
                selection, null, null, null, null)) {
            if (results.moveToFirst()) {
                try {
                    return results.getBlob(0);
                } catch (IllegalStateException e) {
                    Timber.w(e, "Could not select tile data at zoom %d, column %d, row %d", zoom, column, row);
                    // In Android, the SQLite cursor can handle at most 2 MB in one row;
                    // exceeding 2 MB in an .mbtiles file is rare, but it can happen.
                    // When an attempt to fetch a large row fails, the database ends up
                    // in an unusable state, so we need to close it and reopen it.
                    // See https://stackoverflow.com/questions/20094421/cursor-window-window-is-full
                    db.close();
                    db = SQLiteDatabase.openOrCreateDatabase(file, null);
                }
            }
        }
        return null;
    }

    /** Returns information about the vector layers available in the tiles. */
    public List<VectorLayer> getVectorLayers() {
        List<VectorLayer> layers = new ArrayList<>();
        JSONArray jsonLayers;
        try {
            JSONObject json = new JSONObject(getMetadata("json"));
            jsonLayers = json.getJSONArray("vector_layers");
            for (int i = 0; i < jsonLayers.length(); i++) {
                layers.add(new VectorLayer(jsonLayers.getJSONObject(i)));
            }
        } catch (JSONException e) {
            Timber.e(e);
        }
        return layers;
    }

    /** Vector layer metadata.  See https://github.com/mapbox/mbtiles-spec for details. */
    public static class VectorLayer {
        public final String name;

        public VectorLayer(JSONObject json) {
            name = json.optString("id", "");
        }
    }

    public class UnsupportedFormatException extends IOException {
        public UnsupportedFormatException(File file, String format) {
            super(String.format("Unrecognized .mbtiles format \"%s\" in %s", format, file));
        }
    }
}