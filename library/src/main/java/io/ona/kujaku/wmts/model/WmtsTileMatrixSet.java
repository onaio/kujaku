package io.ona.kujaku.wmts.model;

import android.support.annotation.NonNull;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Describes a Wmts TileMatrixSet object from the WMTS Capabilities object
 *
 * Created by Emmanuel Otin - eo@novel-t.ch 14/01/19.
 */
public class WmtsTileMatrixSet {

    @Element(name="Identifier")
    private String identifier;

    @ElementList(inline=true, entry="TileMatrix")
    private List<WmtsTileMatrix> tileMatrixs;

    private int maximumZoom = -1;
    private int minimumZoom = -1;
    private int tilesSize = -1;

    @NonNull
    public String getIdentifier() {
        return this.identifier;
    }

    @NonNull
    public List<WmtsTileMatrix> getTileMatrixs() {
        return this.tileMatrixs;
    }

    public int getTilesSize() {
        if (this.tilesSize == -1) {
            this.initData();
        }

        return this.tilesSize;
    }

    /**
     * Get the maximum Zoom authorized
     * @return int
     */
    public int getMaximumZoom() {
        if (this.maximumZoom == -1) {
            this.initData();
        }

        return this.maximumZoom;
    }

    /**
     * Get the minimum Zoom authorized
     * @return int
     */
    public int getMinimumZoom() {
        if (this.minimumZoom == -1) {
            this.initData();

        }

        return this.minimumZoom;
    }

    /**
     * Init maximum Zoom and minimum Zoom
     */
    private void initData() {
        if (this.tileMatrixs == null || this.tileMatrixs.size() == 0) {
            this.maximumZoom = this.minimumZoom = this.tilesSize = 0;
        } else {
            this.sortTileMatrixsByIdentifier();
            this.maximumZoom = this.tileMatrixs.get(tileMatrixs.size() - 1).getIdentifier();
            this.minimumZoom = this.tileMatrixs.get(0).getIdentifier();
            this.tilesSize = this.tileMatrixs.get(0).getTileWidth(); // We get the tile width from the first tileMatrix
        }
    }

    /**
     * Sort this.tileMatrixs by identifier field ASC
     */
    private void sortTileMatrixsByIdentifier() {
        Collections.sort(this.tileMatrixs, new Comparator<WmtsTileMatrix>() {
            @Override
            public int compare(WmtsTileMatrix item1, WmtsTileMatrix item2) {
                return item1.getIdentifier() - item2.getIdentifier();
            }
        });
    }
}
