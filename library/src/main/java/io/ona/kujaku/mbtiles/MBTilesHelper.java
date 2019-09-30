package io.ona.kujaku.mbtiles;

import android.graphics.Color;
import android.support.annotation.NonNull;

import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.FillLayer;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.RasterLayer;
import com.mapbox.mapboxsdk.style.sources.RasterSource;
import com.mapbox.mapboxsdk.style.sources.TileSet;
import com.mapbox.mapboxsdk.style.sources.VectorSource;

import java.io.File;
import java.io.IOException;
import java.util.List;

import timber.log.Timber;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillOpacity;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineOpacity;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineWidth;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.rasterOpacity;

/**
 * Created by samuelgithengi on 9/29/19.
 */
public class MBTilesHelper {

    protected TileHttpServer tileServer;

    public void initializeMbTileslayers(@NonNull Style style, List<String> offlineFiles) {
        if (offlineFiles == null || offlineFiles.isEmpty()) {
            return;
        } else if (tileServer == null || !tileServer.isStarted()) {
            initializeMbTilesServer();
        }

        for (String fileName : offlineFiles) {
            File file = new File(fileName);
            if (fileName.endsWith(".mbtiles")) {
                String name = file.getName();
                String id = name.substring(0, name.length() - ".mbtiles".length());
                addMbtiles(style, id, file);
            }
        }
    }

    private void initializeMbTilesServer() {
        // Mapbox SDK only knows how to fetch tiles via HTTP.  If we want it to
        // display tiles from a local file, we have to serve them locally over HTTP.
        try {
            tileServer = new TileHttpServer();
            tileServer.start();
        } catch (IOException e) {
            Timber.e(e, "Could not start the TileHttpServer");
        }

    }

    public void onDestroy() {
        if (tileServer != null) {
            tileServer.destroy();
        }
    }


    private void addMbtiles(Style style, String id, File file) {
        MbtilesFile mbtiles;
        try {
            mbtiles = new MbtilesFile(file);
        } catch (MbtilesFile.UnsupportedFormatException e) {
            Timber.w(e, "The mbtiles format is not known ");
            return;
        }

        TileSet tileSet = createTileSet(mbtiles, tileServer.getUrlTemplate(id));
        tileServer.addSource(id, mbtiles);

        if (mbtiles.getType() == MbtilesFile.Type.VECTOR) {
            style.addSource(new VectorSource(id, tileSet));
            List<MbtilesFile.VectorLayer> layers = mbtiles.getVectorLayers();
            for (MbtilesFile.VectorLayer layer : layers) {
                // Pick a colour that's a function of the filename and layer name.
                int hue = (((id + "." + layer.name).hashCode()) & 0x7fffffff) % 360;
                style.addLayer(new FillLayer(id + "/" + layer.name + ".fill", id).withProperties(
                        fillColor(Color.HSVToColor(new float[]{hue, 0.3f, 1})),
                        fillOpacity(0.1f)
                ).withSourceLayer(layer.name));
                style.addLayer(new LineLayer(id + "/" + layer.name + ".line", id).withProperties(
                        lineColor(Color.HSVToColor(new float[]{hue, 0.7f, 1})),
                        lineWidth(1f),
                        lineOpacity(0.7f)
                ).withSourceLayer(layer.name));
            }
        }
        if (mbtiles.getType() == MbtilesFile.Type.RASTER) {
            style.addSource(new RasterSource(id, tileSet));
            style.addLayer(new RasterLayer(id + ".raster", id).withProperties(
                    rasterOpacity(0.5f)
            ));
        }
        Timber.i("Added %s as a %s layer at /%s", file, mbtiles.getType(), id);
    }

    private TileSet createTileSet(MbtilesFile mbtiles, String urlTemplate) {
        TileSet tileSet = new TileSet("2.2.0", urlTemplate);

        // Configure the TileSet using the metadata in the .mbtiles file.
        tileSet.setName(mbtiles.getMetadata("name"));
        try {
            tileSet.setMinZoom(Integer.parseInt(mbtiles.getMetadata("minzoom")));
            tileSet.setMaxZoom(Integer.parseInt(mbtiles.getMetadata("maxzoom")));
        } catch (NumberFormatException e) { /* ignore */ }

        String[] parts = mbtiles.getMetadata("center").split(",");
        if (parts.length == 3) {  // latitude, longitude, zoom
            try {
                tileSet.setCenter(
                        Float.parseFloat(parts[0]), Float.parseFloat(parts[1]),
                        (float) Integer.parseInt(parts[2])
                );
            } catch (NumberFormatException e) { /* ignore */ }
        }

        parts = mbtiles.getMetadata("bounds").split(",");
        if (parts.length == 4) {  // left, bottom, right, top
            try {
                tileSet.setBounds(
                        Float.parseFloat(parts[0]), Float.parseFloat(parts[1]),
                        Float.parseFloat(parts[2]), Float.parseFloat(parts[3])
                );
            } catch (NumberFormatException e) { /* ignore */ }
        }

        return tileSet;
    }
}
