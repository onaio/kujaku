package io.ona.kujaku.mbtiles;

import android.content.Context;
import android.graphics.Color;
import android.os.Environment;
import androidx.annotation.NonNull;
import android.text.TextUtils;
import android.util.Pair;

import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.FillLayer;
import com.mapbox.mapboxsdk.style.layers.Layer;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.RasterLayer;
import com.mapbox.mapboxsdk.style.sources.RasterSource;
import com.mapbox.mapboxsdk.style.sources.Source;
import com.mapbox.mapboxsdk.style.sources.TileSet;
import com.mapbox.mapboxsdk.style.sources.VectorSource;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.ona.kujaku.plugin.switcher.BaseLayerSwitcherPlugin;
import io.ona.kujaku.plugin.switcher.layer.MBTilesLayer;
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

    public static final String MB_TILES_EXTENSION = ".mbtiles";

    public static final String MB_TILES_DIRECTORY = "/mbtiles";

    protected TileHttpServer tileServer;

    private File mbtilesDir = new File(Environment.getExternalStorageDirectory().getPath() + MB_TILES_DIRECTORY);

    private void init(List<File> offlineFiles) {
        if (offlineFiles == null || offlineFiles.isEmpty()) {
            return;
        } else if (tileServer == null || !tileServer.isStarted()) {
            initializeMbTilesServer();
        }
    }

    public void initializeMbTileslayers(@NonNull Style style, List<File> offlineFiles) {
        init(offlineFiles);
        for (File file : offlineFiles) {
            String name = file.getName();
            if (name.endsWith(MB_TILES_EXTENSION)) {
                String id = name.substring(0, name.length() - MB_TILES_EXTENSION.length());
                addMbtiles(style, id, file);
            }
        }
    }

    public Pair<Set<Source>, Set<Layer>> initializeMbTileslayers(File offlineFile) {
        init(Collections.singletonList(offlineFile));
        Set<Source> sources = new HashSet<>();
        Set<Layer> layers = new HashSet<>();
        String name = offlineFile.getName();
        if (name.endsWith(MB_TILES_EXTENSION)) {
            String id = name.substring(0, name.length() - MB_TILES_EXTENSION.length());
            Pair<Source, List<Layer>> sourceAndLayers = addMbtiles(id, offlineFile);
            if (sourceAndLayers != null) {
                sources.add(sourceAndLayers.first);
                layers.addAll(sourceAndLayers.second);
            }
            return new Pair<>(sources, layers);
        }
        return null;
    }

    public void setMBTileLayers(Context context, BaseLayerSwitcherPlugin baseLayerSwitcherPlugin) {
        if (mbtilesDir.exists() && mbtilesDir.exists() && mbtilesDir.listFiles() != null) {
            for (File mbTile : mbtilesDir.listFiles()) {
                MBTilesLayer mbTilesLayer = new MBTilesLayer(context, mbTile, this);
                if (!TextUtils.isEmpty(mbTilesLayer.getDisplayName())) {
                    baseLayerSwitcherPlugin.addBaseLayer(mbTilesLayer, false);
                }
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
        Pair<Source, List<Layer>> sourceAndLayers = addMbtiles(id, file);
        if (sourceAndLayers != null) {
            style.addSource(sourceAndLayers.first);
            for (Layer layer : sourceAndLayers.second)
                style.addLayer(layer);
        }
    }

    private Pair<Source, List<Layer>> addMbtiles(String id, File file) {
        MbtilesFile mbtiles;
        List<Layer> mapLayers = new ArrayList<>();
        Source source = null;
        try {
            mbtiles = new MbtilesFile(file);
        } catch (MbtilesFile.UnsupportedFormatException e) {
            Timber.w(e, "The mbtiles format is not known ");
            return null;
        }

        TileSet tileSet = createTileSet(mbtiles, tileServer.getUrlTemplate(id));
        tileServer.addSource(id, mbtiles);

        if (mbtiles.getType() == MbtilesFile.Type.VECTOR) {
            source = new VectorSource(id, tileSet);
            List<MbtilesFile.VectorLayer> layers = mbtiles.getVectorLayers();
            for (MbtilesFile.VectorLayer layer : layers) {
                // Pick a colour that's a function of the filename and layer name.
                int hue = (((id + "." + layer.name).hashCode()) & 0x7fffffff) % 360;
                mapLayers.add(new FillLayer(id + "/" + layer.name + ".fill", id).withProperties(
                        fillColor(Color.HSVToColor(new float[]{hue, 0.3f, 1})),
                        fillOpacity(0.1f)
                ).withSourceLayer(layer.name));
                mapLayers.add(new LineLayer(id + "/" + layer.name + ".line", id).withProperties(
                        lineColor(Color.HSVToColor(new float[]{hue, 0.7f, 1})),
                        lineWidth(1f),
                        lineOpacity(0.7f)
                ).withSourceLayer(layer.name));
            }
        }
        if (mbtiles.getType() == MbtilesFile.Type.RASTER) {
            source = new RasterSource(id, tileSet);
            mapLayers.add(new RasterLayer(id + ".raster", id).withProperties(
                    rasterOpacity(0.5f)
            ));
        }
        Timber.i("Added %s as a %s layer at /%s", file, mbtiles.getType(), id);
        return new Pair<>(source, mapLayers);
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
