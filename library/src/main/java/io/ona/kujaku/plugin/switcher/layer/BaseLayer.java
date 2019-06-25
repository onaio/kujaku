package io.ona.kujaku.plugin.switcher.layer;

import android.support.annotation.NonNull;

import com.mapbox.geojson.FeatureCollection;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.Layer;
import com.mapbox.mapboxsdk.style.sources.Source;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;

import io.ona.kujaku.exceptions.InvalidStyleStateException;
import io.ona.kujaku.layers.KujakuLayer;
import io.ona.kujaku.utils.Constants;
import timber.log.Timber;

import static com.mapbox.mapboxsdk.style.layers.Property.NONE;
import static com.mapbox.mapboxsdk.style.layers.Property.VISIBLE;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.visibility;

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
        if (style != null && style.isFullyLoaded()) {
            // Add the sources
            List<Source> sourceList = getSources();

            for (Source source : sourceList) {
                if (style.getSource(source.getId()) == null) {
                    addedSources.add(source.getId());
                    style.addSource(source);
                }
            }

            LinkedList<Layer> layerList = new LinkedList<>(getLayers());

            Layer backgroundLayer = style.getLayer(backgroundLayerName);
            if (backgroundLayer != null) {
                Iterator<Layer> layerIterator = layerList.descendingIterator();
                while (layerIterator.hasNext()) {
                    Layer layer = layerIterator.next();
                    if (style.getLayer(layer.getId()) == null) {
                        addedLayers.add(layer.getId());
                        style.addLayerAbove(layer, backgroundLayerName);
                    }
                }
            } else {
                int counter = 0;
                Iterator<Layer> layerIterator = layerList.descendingIterator();
                while (layerIterator.hasNext()) {
                    Layer layer = layerIterator.next();
                    if (style.getLayer(layer.getId()) == null) {
                        addedLayers.add(layer.getId());
                        style.addLayerAt(layer, counter);
                        counter++;
                    }
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
                layer.setProperties(visibility(VISIBLE));
                visible = true;
            }
        }
    }

    @Override
    public void disableLayerOnMap(@NonNull MapboxMap mapboxMap) {
        Style style = mapboxMap.getStyle();
        if (style != null && style.isFullyLoaded()) {
            for (String layerId : addedLayers) {
                Layer layer = style.getLayer(layerId);
                if (layer != null && VISIBLE.equals(layer.getVisibility().getValue())) {
                    layer.setProperties(visibility(NONE));
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

        if (style != null && style.isFullyLoaded()) {
            for (String layerId: addedLayers) {
                style.removeLayer(layerId);
            }

            for (String sourceId: addedSources) {
                style.removeSource(sourceId);
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
