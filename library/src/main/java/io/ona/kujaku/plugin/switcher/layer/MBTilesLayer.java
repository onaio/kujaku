package io.ona.kujaku.plugin.switcher.layer;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Pair;

import com.mapbox.mapboxsdk.style.layers.Layer;
import com.mapbox.mapboxsdk.style.sources.Source;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import io.ona.kujaku.mbtiles.MBTilesHelper;

import static io.ona.kujaku.mbtiles.MBTilesHelper.MB_TILES_EXTENSION;

/**
 * Created by samuelgithengi on 9/30/19.
 */
public class MBTilesLayer extends BaseLayer {

    private Context context;

    private MBTilesHelper mbTilesHelper;

    private LinkedHashSet<Layer> layers = new LinkedHashSet<>();
    private List<Source> sources = new ArrayList<>();
    private String[] sourceIds = new String[]{};
    private String[] layerIds = new String[]{};

    private String name;

    public MBTilesLayer(Context context, File offlineFile, MBTilesHelper mbTilesHelper) {
        this.context = context;
        this.mbTilesHelper = mbTilesHelper;
        createLayersAndSources(offlineFile);
    }


    private void createLayersAndSources(File offlineFile) {
        Pair<Set<Source>, Set<Layer>> sourcesAndLayers = mbTilesHelper.initializeMbTileslayers(offlineFile);
        if (sourcesAndLayers != null) {
            name = offlineFile.getName().substring(0, offlineFile.getName().length() - MB_TILES_EXTENSION.length());
            sources = new ArrayList<>(sourcesAndLayers.first);
            layers = new LinkedHashSet<>(sourcesAndLayers.second);
            List<String> sourceIdList = new ArrayList<>();
            for (Source source : sources) {
                sourceIdList.add(source.getId());
            }
            this.sourceIds = sourceIdList.toArray(this.sourceIds);

            List<String> layerIdList = new ArrayList<>();
            for (Layer layer : layers) {
                layerIdList.add(layer.getId());
            }
            this.layerIds = layerIdList.toArray(this.layerIds);
        }

    }

    @NonNull
    @Override
    public String getDisplayName() {
        return  name;
    }

    @NonNull
    @Override
    public String[] getSourceIds() {
        return sourceIds;
    }

    @Override
    public LinkedHashSet<Layer> getLayers() {
        return layers;
    }

    @Override
    public List<Source> getSources() {
        return sources;
    }

    @NonNull
    @Override
    public String getId() {
        return "mbtiles-" + name;
    }

    @NonNull
    @Override
    public String[] getLayerIds() {
        return layerIds;
    }
}
