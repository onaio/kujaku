package io.ona.kujaku.wmts.model;

import org.simpleframework.xml.ElementList;

import java.util.List;

/**
 * Describe a Wmts Contents object from the WMTS Capabilities object
 *
 * Created by Emmanuel Otin - eo@novel-t.ch 11/28/18.
 */
public class WmtsContents {

    @ElementList(inline=true, entry="Layer")
    private List<WmtsLayer> layers;

    @ElementList(inline=true, entry="TileMatrixSet")
    private List<WmtsTileMatrixSet> tileMatrixSets;

    public List<WmtsLayer> getLayers() {
        return this.layers;
    }

    public List<WmtsTileMatrixSet> geTileMatrixSets() {
        return this.tileMatrixSets;
    }

    public WmtsTileMatrixSet geTileMatrixSet(String tileMatrixSetIdentifier) {
        for (WmtsTileMatrixSet wmtsTileMatrixSet : this.tileMatrixSets) {
            if (wmtsTileMatrixSet.getIdentifier().equals(tileMatrixSetIdentifier)) {
                return wmtsTileMatrixSet;
            }
        }

        return null ;
    }


    /**
     * Return the layer corresponding to the identifier
     *
     * @param identifier
     * @return
     */
    public WmtsLayer getLayer(String identifier) {
        for (WmtsLayer layer : this.layers) {
            if (layer.getIdentifier().equals(identifier)) {
                return layer;
            }
        }

        return null ;
    }
}
