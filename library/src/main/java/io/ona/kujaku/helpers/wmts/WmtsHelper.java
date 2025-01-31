package io.ona.kujaku.helpers.wmts;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mapbox.bindgen.Value;
import com.mapbox.maps.Style;

import java.util.Arrays;
import java.util.HashMap;
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
    public static void addWmtsLayers(@Nullable Set<WmtsLayer> wmtsLayers, @NonNull Style style) {
        if (wmtsLayers != null) {
            for (WmtsLayer layer : wmtsLayers) {

                HashMap<String, Value> tilesetProperties = new HashMap<>();
                tilesetProperties.put("type", Value.valueOf("raster"));
                tilesetProperties.put("tiles", Value.valueOf(Arrays.toString(new String[]{layer.getTemplateUrl("tile")})));
                tilesetProperties.put("maxzoom", Value.valueOf(layer.getMaximumZoom()));
                tilesetProperties.put("minzoom", Value.valueOf(layer.getMinimumZoom()));
                tilesetProperties.put("tileSize", Value.valueOf(layer.getTilesSize()));

                style.addStyleSource(layer.getIdentifier(), Value.valueOf(tilesetProperties));

                // Create raster layer properties
                HashMap<String, Value> layerProperties = new HashMap<>();
                layerProperties.put("id", Value.valueOf(layer.getIdentifier()));
                layerProperties.put("type", Value.valueOf("raster"));
                layerProperties.put("source", Value.valueOf(layer.getIdentifier()));
                // Add layer
                style.addStyleLayer(Value.valueOf(layerProperties),null);
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
