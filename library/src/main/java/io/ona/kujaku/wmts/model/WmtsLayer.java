package io.ona.kujaku.wmts.model;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;

import java.util.List;

/**
 * Describe a Wmts Layer object from the WMTS Capabilities object
 *
 * Created by Emmanuel Otin - eo@novel-t.ch 11/28/18.
 */
public class WmtsLayer {

    @Element(name="Title")
    private String title;

    @Element(name="Identifier")
    private String identifier;

    @ElementList(inline=true, entry="Style")
    private List<WmtsStyle> styles;

    @Element(name="Format")
    private String format;

    @ElementList(inline=true, entry="TileMatrixSetLink")
    private List<WmtsTileMatrixSetLink> tileMatrixSetLinks;

    @Element(name="ResourceURL")
    private WmtsResourceUrl resourceURL;

    private String selectedStyleIdentifier;

    private String selectedTileMatrixLinkIdentifier;

    private int maximumZoom;

    private int minimumZoom;

    public String getFormat() {
        return this.format;
    }

    public String getTitle() {
        return this.title;
    }

    public String getIdentifier() {
        return this.identifier;
    }

    public void setSelectedStyleIdentifier(String selectedStyleIdentifier) {
        this.selectedStyleIdentifier = selectedStyleIdentifier;
    }

    public void setSelectedTileMatrixLinkIdentifier(String selectedTileMatrixLinkIdentifier) {
        this.selectedTileMatrixLinkIdentifier = selectedTileMatrixLinkIdentifier;
    }

    public WmtsStyle getStyle(String styleIdentifier) {
        for (WmtsStyle style : this.styles) {
            if (style.getIdentifier().equals(styleIdentifier)) {
                return style;
            }
        }

        return null ;
    }

    public WmtsTileMatrixSetLink getTileMatrixSetLink(String tileMatrixLinkIdentifier) {
        for (WmtsTileMatrixSetLink tileMatrixSetLink : this.tileMatrixSetLinks) {
            if (tileMatrixSetLink.getTileMatrixSet().equals(tileMatrixLinkIdentifier)) {
                return tileMatrixSetLink;
            }
        }

        return null ;
    }

    public String getTemplateUrl() {
        if (this.resourceURL == null) {
            return null ;
        }

        if (this.selectedStyleIdentifier == null || this.selectedStyleIdentifier.isEmpty()) {
            // Search for default style
            WmtsStyle style = this.getDefaultStyle();
            if (style == null) {
               // search for the first available style
                this.selectedStyleIdentifier = this.styles.get(0).getIdentifier();
            } else {
                this.selectedStyleIdentifier = style.getIdentifier();
            }
        }

        return this.resourceURL.getTemplate(this.selectedStyleIdentifier, this.getSelectedTileMatrixLinkIdentifier());
    }

    public String getSelectedTileMatrixLinkIdentifier() {
        if (this.selectedTileMatrixLinkIdentifier == null || this.selectedTileMatrixLinkIdentifier.isEmpty()) {
            // Get First one
            this.selectedTileMatrixLinkIdentifier = this.tileMatrixSetLinks.get(0).getTileMatrixSet();
        }

        return this.selectedTileMatrixLinkIdentifier;
    }

    public int getMaximumZoom() {
        return this.maximumZoom;
    }

    public void setMaximumZoom(int maxZoom) {
        this.maximumZoom = maxZoom;
    }

    public int getMinimumZoom() {
        return this.minimumZoom;
    }

    public void setMinimumZoom(int minZoom) {
        this.minimumZoom = minZoom;
    }

    private WmtsStyle getDefaultStyle() {
        if (this.styles == null || this.styles.isEmpty()) {
            return null;
        }

        for (WmtsStyle style : this.styles) {
            if (style.isDefault()) {
                return style;
            }
        }

        return null;
    }

}
