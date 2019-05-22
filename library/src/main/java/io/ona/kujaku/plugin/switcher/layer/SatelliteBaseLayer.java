package io.ona.kujaku.plugin.switcher.layer;

import android.support.annotation.NonNull;

import com.mapbox.mapboxsdk.style.layers.Layer;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.layers.RasterLayer;
import com.mapbox.mapboxsdk.style.sources.RasterSource;
import com.mapbox.mapboxsdk.style.sources.Source;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 2019-05-21
 */

public class SatelliteBaseLayer extends BaseLayer {

    private String satelliteLayerId = "satellite";
    private String satelliteSourceId = "mapbox://mapbox.satellite";

    private LinkedHashSet<Layer> layers = new LinkedHashSet<>();
    private List<Source> sources = new ArrayList<>();

    public SatelliteBaseLayer() {
        createLayersAndSources();
    }

    private void createLayersAndSources() {
        RasterSource rasterSource = new RasterSource(satelliteSourceId, "mapbox://mapbox.satellite", 256);

        RasterLayer rasterLayer = new RasterLayer(satelliteLayerId, satelliteSourceId);
        rasterLayer.setSourceLayer("mapbox_satellite_full");
        rasterLayer.setProperties(PropertyFactory.rasterOpacity(1f)
                , PropertyFactory.rasterSaturation(-0.55f)
                , PropertyFactory.rasterHueRotate(0f));

        layers.add(rasterLayer);
        sources.add(rasterSource);
    }

    @NonNull
    @Override
    public String getDisplayName() {
        return "Satellite";
    }

    @NonNull
    @Override
    public String[] getSourceIds() {
        return new String[] {satelliteSourceId};
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
        return "satellite-base-layer";
    }

    @NonNull
    @Override
    public String[] getLayerIds() {
        return new String[] {satelliteLayerId};
    }
}
