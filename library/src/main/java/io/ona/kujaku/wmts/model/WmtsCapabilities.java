package io.ona.kujaku.wmts.model;

import androidx.annotation.NonNull;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

import java.util.List;

import javax.annotation.Nullable;

/**
 * Describes a Wmts Capabilities object
 *
 * Created by Emmanuel Otin - eo@novel-t.ch 11/28/18
 */
@Root(name="Capabilities")
public class WmtsCapabilities {

    @Element(name="ServiceIdentification", required=false)
    private WmtsServiceIdentification serviceIdentification;

    @Element(name="Contents")
    private WmtsContents contents;

    @Attribute(name="version", required=false)
    private String version;

    @Nullable
    public WmtsServiceIdentification getServiceIdentification() {
        return this.serviceIdentification;
    }

    @Nullable
    public String getVersion() {
        return this.version;
    }

    @Nullable
    public WmtsLayer getLayer(@NonNull String identifier) {
        if (this.contents == null) {
            return null;
        }

        return this.contents.getLayer(identifier);
    }

    @Nullable
    public List<WmtsLayer> getLayers() {
        if (this.contents == null) {
            return null;
        }

        return this.contents.getLayers();
    }

    public int getMaximumTileMatrixZoom(@NonNull String tileMatrixIdentifier) {
        WmtsTileMatrixSet tileMatrixSet = this.getTileMatrixSet(tileMatrixIdentifier);

        if (tileMatrixSet == null) {
            return 0;
        }

        return tileMatrixSet.getMaximumZoom();
    }

    public int getMinimumTileMatrixZoom(@NonNull String tileMatrixIdentifier) {
        WmtsTileMatrixSet tileMatrixSet = this.getTileMatrixSet(tileMatrixIdentifier);

        if (tileMatrixSet == null) {
            return 0;
        }

        return tileMatrixSet.getMinimumZoom();
    }

    public int getTilesSize(@NonNull String tileMatrixIdentifier) {
        WmtsTileMatrixSet tileMatrixSet = this.getTileMatrixSet(tileMatrixIdentifier);

        if (tileMatrixSet == null) {
            return 0;
        }

        return tileMatrixSet.getTilesSize();
    }

    @Nullable
    private WmtsTileMatrixSet getTileMatrixSet(@NonNull String tileMatrixIdentifier) {
        if (this.contents == null) {
            return null;
        }

        return this.contents.geTileMatrixSet(tileMatrixIdentifier);
    }
}
