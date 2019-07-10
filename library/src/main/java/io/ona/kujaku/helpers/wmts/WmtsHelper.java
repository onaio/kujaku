package io.ona.kujaku.helpers.wmts;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.RasterLayer;
import com.mapbox.mapboxsdk.style.sources.RasterSource;
import com.mapbox.mapboxsdk.style.sources.TileSet;

import java.util.Set;

import io.ona.kujaku.exceptions.WmtsCapabilitiesException;
import io.ona.kujaku.wmts.model.WmtsCapabilities;
import io.ona.kujaku.wmts.model.WmtsLayer;

/**
 * Isolate specific Wmts functions
 *
 *  Created by Emmanuel Otin - eo@novel-t.ch on 20/06/2019
 */
public class WmtsHelper {

    /**
     * Verify if Style exists for the Layer
     *
     * @param layer
     * @param styleIdentifier
     * @throws WmtsCapabilitiesException
     */
    private static void selectWmtsStyle (@NonNull WmtsLayer layer, @Nullable String styleIdentifier) throws WmtsCapabilitiesException {
        if (styleIdentifier != null && !styleIdentifier.isEmpty()) {
            // Check if style is known
            if (layer.getStyle(styleIdentifier) == null) {
                throw new WmtsCapabilitiesException(String.format("Style with identifier %1$s is not available for Layer %2$s", styleIdentifier, layer.getIdentifier()));
            } else {
                layer.setSelectedStyleIdentifier(styleIdentifier);
            }
        }
    }

    /**
     * Verify if TileMatrixSetlink exists exists for the Layer
     *
     * @param layer
     * @param tileMatrixSetLinkIdentifier
     * @throws WmtsCapabilitiesException
     */
    private static void selectWmtsTileMatrix (@NonNull WmtsLayer layer, @Nullable String tileMatrixSetLinkIdentifier) throws WmtsCapabilitiesException {
        if (tileMatrixSetLinkIdentifier != null && !tileMatrixSetLinkIdentifier.isEmpty()) {
            // Check if style is known
            if (layer.getTileMatrixSetLink(tileMatrixSetLinkIdentifier) == null) {
                throw new WmtsCapabilitiesException(String.format("tileMatrixSetLink with identifier %1$s is not available for Layer %2$s", tileMatrixSetLinkIdentifier, layer.getIdentifier()));
            } else {
                layer.setSelectedTileMatrixLinkIdentifier(tileMatrixSetLinkIdentifier);
            }
        }
    }

    /**
     * Set the Maximum and Minimum Zoom for this layer
     *
     * @param layer
     * @param capabilities
     */
    private static void setZooms(@NonNull WmtsLayer layer, @NonNull  WmtsCapabilities capabilities){
        String tileMatrixSetIdentifier = layer.getSelectedTileMatrixLinkIdentifier();

        int maxZoom = capabilities.getMaximumTileMatrixZoom(tileMatrixSetIdentifier);
        int minZoom = capabilities.getMinimumTileMatrixZoom(tileMatrixSetIdentifier);

        layer.setMaximumZoom(maxZoom);
        layer.setMinimumZoom(minZoom);
    }

    /**
     * Set the tiles Size for this layer
     *
     * @param layer
     * @param capabilities
     */
    private static void setTilesSize(@NonNull WmtsLayer layer, @NonNull WmtsCapabilities capabilities) {
        String tileMatrixSetIdentifier = layer.getSelectedTileMatrixLinkIdentifier();
        int tileSize = capabilities.getTilesSize(tileMatrixSetIdentifier);
        layer.setTilesSize(tileSize);
    }

    /**
     * Add all Wmts Layers in wmtsLayers on the map
     */
    public static void addWmtsLayers(@Nullable Set<WmtsLayer> wmtsLayers,@NonNull Style style) {
        // Add WmtsLayers
        if (wmtsLayers != null) {
            for (WmtsLayer layer : wmtsLayers) {
                if (style.getSource(layer.getIdentifier()) == null) {

                    TileSet tileSet = new TileSet("tileset", layer.getTemplateUrl("tile"));
                    tileSet.setMaxZoom(layer.getMaximumZoom());
                    tileSet.setMinZoom(layer.getMinimumZoom());

                    RasterSource webMapSource = new RasterSource(
                            layer.getIdentifier(),
                            tileSet, layer.getTilesSize());
                    style.addSource(webMapSource);

                    RasterLayer webMapLayer = new RasterLayer(layer.getIdentifier(), layer.getIdentifier());
                    style.addLayer(webMapLayer);
                }
            }
        }
    }

    /**
     * Identify and return layer with specific style & specific tileMatrixSet to the wmtsLayer list
     *
     * @param capabilities
     * @param layerIdentifier
     * @param styleIdentifier
     * @param tileMatrixSetLinkIdentifier
     */
    public static WmtsLayer identifyLayer(@Nullable WmtsCapabilities capabilities, @Nullable String layerIdentifier
            , @Nullable String styleIdentifier, @Nullable String tileMatrixSetLinkIdentifier) throws WmtsCapabilitiesException {
        WmtsLayer layerIdentified;

        if (capabilities == null) {
            throw new WmtsCapabilitiesException ("capabilities object is null or empty");
        }

        if (layerIdentifier == null || layerIdentifier.isEmpty()) { // Take first layer accessible
            if (capabilities.getLayers() == null || capabilities.getLayers().size() == 0) {
                // No layer available
                throw new WmtsCapabilitiesException("No layer available in the capacities object");
            } else {
                layerIdentified = capabilities.getLayers().get(0);
            }
        } else {
            // Get the identified layer
            layerIdentified = capabilities.getLayer(layerIdentifier);
        }

        if (layerIdentified == null) {
            throw new WmtsCapabilitiesException(String.format("Layer with identifier %1$s is unknown", layerIdentifier));
        }

        WmtsHelper.selectWmtsStyle(layerIdentified, styleIdentifier);
        WmtsHelper.selectWmtsTileMatrix(layerIdentified, tileMatrixSetLinkIdentifier);
        WmtsHelper.setZooms(layerIdentified, capabilities);
        WmtsHelper.setTilesSize(layerIdentified, capabilities);

        return layerIdentified;
    }
}
