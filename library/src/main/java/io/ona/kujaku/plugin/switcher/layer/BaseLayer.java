package io.ona.kujaku.plugin.switcher.layer;

import static com.mapbox.maps.extension.style.layers.properties.generated.Visibility.NONE;
import static com.mapbox.maps.extension.style.layers.properties.generated.Visibility.VISIBLE;

import androidx.annotation.NonNull;

import com.mapbox.geojson.FeatureCollection;
import com.mapbox.maps.MapboxMap;
import com.mapbox.maps.Style;
import com.mapbox.maps.extension.style.layers.Layer;
import com.mapbox.maps.extension.style.sources.Source;
import com.mapbox.maps.extension.style.sources.SourceUtils;
import com.mapbox.maps.extension.style.sources.SourceUtils.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;

import io.ona.kujaku.exceptions.InvalidStyleStateException;
import io.ona.kujaku.layers.KujakuLayer;
import io.ona.kujaku.utils.Constants;
import timber.log.Timber;


/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 2019-05-16
 */

public abstract class BaseLayer extends KujakuLayer {

    private String backgroundLayerName = Constants.Style.BACKGROUND_LAYER_ID;
    private boolean visible;
    private boolean isRemoved;
    protected ArrayList<String> addedSources = new ArrayList<>();
    protected ArrayList<String> addedLayers = new ArrayList<>();

    //private String templateStyle = ""

    public void addLayer(@NonNull Layer layer, @NonNull Source source) {
        //Do nothing for now
    }

    @NonNull
    public abstract String getDisplayName();

    @NonNull
    public abstract String[] getSourceIds();

    public abstract LinkedHashSet<Layer> getLayers();

    public abstract List<Source> getSources();

    @NonNull
    public abstract String getId();

    @Override
    public void addLayerToMap(@NonNull MapboxMap mapboxMap) {
        Style style = mapboxMap.getStyle();
        if (style != null && style.isStyleLoaded()) {
            // Add the sources
            List<Source> sourceList = getSources();

            for (Source source : sourceList) {
                /* TODO Refactor this
                if (style.getStyleSource(source.getSourceId()) == null) {
                    addedSources.add(source.getSourceId());
                    SourceUtils.addSource(style, source);
                }*/
            }

            LinkedList<Layer> layerList = new LinkedList<>(getLayers());

            Layer backgroundLayer = null; /*style.getLayer(backgroundLayerName);*/
            if (backgroundLayer != null) {
                Iterator<Layer> layerIterator = layerList.descendingIterator();
                while (layerIterator.hasNext()) {
                    Layer layer = layerIterator.next();
                    /* TODO Refactor this
                    if (style.getLayer(layer.getLayerId()) == null) {
                        addedLayers.add(layer.getLayerId());
                        style.addLayerAbove(layer, backgroundLayerName);
                    }*/
                }
            } else {
                int counter = 0;
                Iterator<Layer> layerIterator = layerList.descendingIterator();
                while (layerIterator.hasNext()) {
                    Layer layer = layerIterator.next();
                  /* TODO Refactor this
                   if (style.getLayer(layer.getLayerId()) == null) {
                        addedLayers.add(layer.getLayerId());
                        style.addLayerAt(layer, counter);
                        counter++;
                    }*/
                }
            }

            visible = true;
        } else {
            Timber.e( new InvalidStyleStateException());
        }
    }

    @Override
    public void enableLayerOnMap(@NonNull MapboxMap mapboxMap) {
        for (Layer layer: getLayers()) {
            if (layer != null && NONE.equals(layer.getVisibility().getValue())) {
                /* TODO refactor this
                    layer.setProperties(layer.visibility(VISIBLE));*/
                visible = true;
            }
        }
    }

    @Override
    public void disableLayerOnMap(@NonNull MapboxMap mapboxMap) {
        Style style = mapboxMap.getStyle();
        if (style != null && style.isStyleLoaded()) {
            for (String layerId : addedLayers) {
                Layer layer = null;
                /*TODO Refactor this
                Layer layer = style.getStyleLayers().(layerId);*/
                if (layer != null && VISIBLE.equals(layer.getVisibility().getValue())) {
                    /* TODO Refactor this
                    layer.setProperties(layer.visibility(NONE));*/
                    visible = false;
                }
            }
        } else {
            Timber.e(new InvalidStyleStateException());
        }
    }

    @Override
    public boolean isVisible() {
        return visible;
    }

    @Override
    public boolean removeLayerOnMap(@NonNull MapboxMap mapboxMap) {
        setRemoved(true);

        // Remove the layers & sources
        Style style = mapboxMap.getStyle();

        if (style != null && style.isStyleLoaded()) {
            for (String layerId: addedLayers) {
                style.removeStyleLayer(layerId);
            }

            for (String sourceId: addedSources) {
                style.removeStyleSource(sourceId);
            }

            return true;
        } else {
            Timber.e("Could not remove the layers & source because the the style is null or not fully loaded");
            return false;
        }
    }

    @Override
    public boolean isRemoved() {
        return isRemoved;
    }

    @Override
    public void setRemoved(boolean isRemoved) {
        this.isRemoved = isRemoved;
    }

    @NonNull
    public String getBackgroundLayerName() {
        return backgroundLayerName;
    }

    public void setBackgroundLayerName(@NonNull String backgroundLayerName) {
        this.backgroundLayerName = backgroundLayerName;
    }

    @Override
    public void updateFeatures(@NonNull FeatureCollection featureCollection) {
        // No implementation of this on a BaseLayer
    }

    @Override
    public FeatureCollection getFeatureCollection() {
        return null;
    }
}
