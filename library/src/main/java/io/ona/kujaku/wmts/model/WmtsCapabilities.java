package io.ona.kujaku.wmts.model;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

/**
 * Describe a Wmts Capabilities object
 *
 * Created by Emmanuel Otin - eo@novel-t.ch 11/28/18.Created by Emmanuel Otin - eo@novel-t.ch 11/28/18.
 */
@Root(name="Capabilities")
public class WmtsCapabilities {

    @Element(name="ServiceIdentification")
    private WmtsServiceIdentification serviceIdentification;

    @ElementList(name="OperationsMetadata")
    private List<WmtsOperation> operations;

    @Element(name="Contents")
    private WmtsContents contents;

    @Attribute(name="version")
    private String version;

    public List<WmtsOperation> getOperations() { return this.operations; }

    public WmtsServiceIdentification getServiceIdentification() { return this.serviceIdentification; }

    public String getVersion() {
        return this.version;
    }

    public WmtsLayer getLayer(String identifier) {
        if (this.contents == null) {
            return null;
        }

        return this.contents.getLayer(identifier);
    }

    public List<WmtsLayer> getLayers() {
        if (this.contents == null) {
            return null;
        }

        return this.contents.getLayers();
    }

    public int getMaximumTileMatrixZoom(String tileMatrixIdentifier) {
        WmtsTileMatrixSet tileMatrixSet = this.getTileMatrixSet(tileMatrixIdentifier);

        if (tileMatrixSet == null) {
            return 0;
        }

        return tileMatrixSet.getMaximumZoom();
    }

    public int getMinimumTileMatrixZoom(String tileMatrixIdentifier) {
        WmtsTileMatrixSet tileMatrixSet = this.getTileMatrixSet(tileMatrixIdentifier);

        if (tileMatrixSet == null) {
            return 0;
        }

        return tileMatrixSet.getMinimumZoom();
    }

    private WmtsTileMatrixSet getTileMatrixSet(String tileMatrixIdentifier) {
        if (this.contents == null) {
            return null;
        }

        return this.contents.geTileMatrixSet(tileMatrixIdentifier);
    }
}
