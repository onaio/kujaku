package io.ona.kujaku.helpers.wmts;

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
    public static void selectWmtsStyle (WmtsLayer layer, String styleIdentifier) throws WmtsCapabilitiesException {
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
    public static void selectWmtsTileMatrix (WmtsLayer layer, String tileMatrixSetLinkIdentifier) throws WmtsCapabilitiesException {
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
    public static void setZooms(WmtsLayer layer, WmtsCapabilities capabilities){
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
    public static void setTilesSize(WmtsLayer layer, WmtsCapabilities capabilities) {
        String tileMatrixSetIdentifier = layer.getSelectedTileMatrixLinkIdentifier();
        int tileSize = capabilities.getTilesSize(tileMatrixSetIdentifier);
        layer.setTilesSize(tileSize);
    }
}
