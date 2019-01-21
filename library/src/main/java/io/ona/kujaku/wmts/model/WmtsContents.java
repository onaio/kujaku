package io.ona.kujaku.wmts.model;

import android.support.annotation.NonNull;

import org.simpleframework.xml.ElementList;

import java.util.List;

import javax.annotation.Nullable;

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

    @NonNull
    public List<WmtsLayer> getLayers() {
        return this.layers;
    }

    @NonNull
    public List<WmtsTileMatrixSet> geTileMatrixSets() {
        return this.tileMatrixSets;
    }

    @Nullable
    public WmtsTileMatrixSet geTileMatrixSet(@NonNull String tileMatrixSetIdentifier) {
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
    @Nullable
    public WmtsLayer getLayer(@NonNull String identifier) {
        for (WmtsLayer layer : this.layers) {
            if (layer.getIdentifier().equals(identifier)) {
                return layer;
            }
        }

        return null ;
    }
}
