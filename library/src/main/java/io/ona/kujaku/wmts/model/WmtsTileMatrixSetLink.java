package io.ona.kujaku.wmts.model;

import androidx.annotation.NonNull;

import org.simpleframework.xml.Element;

/**
 * Describes a Wmts TileMatrixSetLink object from the WMTS Capabilities object
 *
 * Created by Emmanuel Otin - eo@novel-t.ch 11/28/18.
 */
public class WmtsTileMatrixSetLink {

    @Element(name="TileMatrixSet")
    private String tileMatrixSet;

    @NonNull
    public String getTileMatrixSet() {
        return this.tileMatrixSet;
    }
}
