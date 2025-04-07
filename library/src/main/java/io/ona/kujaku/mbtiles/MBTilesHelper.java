package io.ona.kujaku.mbtiles;

import android.content.Context;
import android.graphics.Color;
import android.os.Environment;
import androidx.annotation.NonNull;
import android.text.TextUtils;
import android.util.Pair;


import com.mapbox.maps.Style;
import com.mapbox.maps.extension.style.layers.Layer;
import com.mapbox.maps.extension.style.layers.generated.FillLayer;
import com.mapbox.maps.extension.style.layers.generated.LineLayer;
import com.mapbox.maps.extension.style.layers.generated.RasterLayer;
import com.mapbox.maps.extension.style.sources.Source;
import com.mapbox.maps.extension.style.sources.TileSet;
import com.mapbox.maps.extension.style.sources.generated.RasterSource;
import com.mapbox.maps.extension.style.sources.generated.VectorSource;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.ona.kujaku.plugin.switcher.BaseLayerSwitcherPlugin;
import io.ona.kujaku.plugin.switcher.layer.MBTilesLayer;
import timber.log.Timber;


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
            /* TODO Refactor this
            style.addSource(sourceAndLayers.first);
            for (Layer layer : sourceAndLayers.second)
                style.addLayer(layer);*/
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
           // source = new VectorSource(id, tileSet);
            source = new VectorSource(new VectorSource.Builder(id).tileSet(tileSet));
            List<MbtilesFile.VectorLayer> layers = mbtiles.getVectorLayers();
            for (MbtilesFile.VectorLayer layer : layers) {
                // Pick a colour that's a function of the filename and layer name.
                int hue = (((id + "." + layer.name).hashCode()) & 0x7fffffff) % 360;
                mapLayers.add(new FillLayer(id + "/" + layer.name + ".fill", id)
                        .fillColor(Color.HSVToColor(new float[]{hue, 0.3f, 1}))
                        .fillOpacity(0.1f)
                        .sourceLayer(layer.name));

                mapLayers.add(new LineLayer(id + "/" + layer.name + ".line", id).lineColor(Color.HSVToColor(new float[]{hue, 0.7f, 1}))
                        .lineWidth(1f)
                        .lineOpacity(0.7f)
                        .sourceLayer(layer.name));
            }
        }
        if (mbtiles.getType() == MbtilesFile.Type.RASTER) {
            source = new RasterSource(new RasterSource.Builder(id).tileSet(tileSet));
            mapLayers.add(new RasterLayer(id + ".raster", id)
                    .rasterOpacity(0.5f)
            );
        }
        Timber.i("Added %s as a %s layer at /%s", file, mbtiles.getType(), id);
        return new Pair<>(source, mapLayers);
    }

    private TileSet createTileSet(MbtilesFile mbtiles, String urlTemplate) {
        //TileSet tileSet = new TileSet("2.2.0", urlTemplate);
        int minZoom = 0;
        int maxZoom = 30;
        try {
            minZoom = Integer.parseInt(mbtiles.getMetadata("minzoom"));
            maxZoom = Integer.parseInt(mbtiles.getMetadata("maxzoom"));
        } catch (NumberFormatException e) { /* ignore */ }

        String[] centerParts = mbtiles.getMetadata("center").split(",");
        Double latitude = null;
        Double longitude = null;
        Double zoom = null;
        if (centerParts.length == 3) {  // latitude, longitude, zoom
            try {
                latitude = Double.parseDouble(centerParts[0]);
                longitude = Double.parseDouble(centerParts[1]);
                zoom = (double) Integer.parseInt(centerParts[2]);

            } catch (NumberFormatException e) { /* ignore */ }
        }

        String[] boundspartS = mbtiles.getMetadata("bounds").split(",");
        Double left = -180.0;
        Double bottom = -90.0;
        Double right = 180.0;
        Double top = 90.0;
        if (boundspartS.length == 4) {  // left, bottom, right, top
            try {
                left = Double.parseDouble(boundspartS[0]);
                bottom = Double.parseDouble(boundspartS[1]);
                right =Double.parseDouble(boundspartS[2]);
                top = Double.parseDouble(boundspartS[3]);

            } catch (NumberFormatException e) { /* ignore */ }
        }

        TileSet tileSet = new TileSet.Builder("2.2.0", Collections.singletonList(urlTemplate))
                // Configure the TileSet using the metadata in the .mbtiles file.
                .name(mbtiles.getMetadata("name"))
                .minZoom(minZoom)
                .maxZoom(maxZoom)
                .center(Arrays.asList(latitude,longitude,zoom))
                .bounds(Arrays.asList(left, bottom, right, top))
                .build();


        return tileSet;
    }
}
