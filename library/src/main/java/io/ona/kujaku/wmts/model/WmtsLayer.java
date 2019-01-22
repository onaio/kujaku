package io.ona.kujaku.wmts.model;

import android.support.annotation.NonNull;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;

import java.util.List;

import javax.annotation.Nullable;

import io.ona.kujaku.wmts.model.common.ows.LanguageStringType;

/**
 * Describes a Wmts Layer object from the WMTS Capabilities object
 *
 * Created by Emmanuel Otin - eo@novel-t.ch 11/28/18.
 */
public class WmtsLayer {

    @ElementList(inline=true, entry="Title")
    private List<LanguageStringType> titles;

    @Element(name="Identifier")
    private String identifier;

    @ElementList(inline=true, entry="Style")
    private List<WmtsStyle> styles;

    @ElementList(inline=true, entry="TileMatrixSetLink")
    private List<WmtsTileMatrixSetLink> tileMatrixSetLinks;

    @ElementList(inline=true, entry="ResourceURL")
    private List<WmtsResourceUrl> resourceURLs;

    private String selectedStyleIdentifier;

    private String selectedTileMatrixLinkIdentifier;

    private int maximumZoom;

    private int minimumZoom;
  
    private int tilesSize;

    @NonNull
    public List<LanguageStringType> getTitles() {
        return this.titles;
    }

    @NonNull
    public String getIdentifier() {
        return this.identifier;
    }

    public void setSelectedStyleIdentifier(@NonNull String selectedStyleIdentifier) {
        this.selectedStyleIdentifier = selectedStyleIdentifier;
    }

    public void setSelectedTileMatrixLinkIdentifier(@NonNull String selectedTileMatrixLinkIdentifier) {
        this.selectedTileMatrixLinkIdentifier = selectedTileMatrixLinkIdentifier;
    }

    @Nullable
    public WmtsStyle getStyle(@NonNull String styleIdentifier) {
        for (WmtsStyle style : this.styles) {
            if (style.getIdentifier().equals(styleIdentifier)) {
                return style;
            }
        }

        return null ;
    }

    @Nullable
    public WmtsTileMatrixSetLink getTileMatrixSetLink(@NonNull String tileMatrixLinkIdentifier) {
        for (WmtsTileMatrixSetLink tileMatrixSetLink : this.tileMatrixSetLinks) {
            if (tileMatrixSetLink.getTileMatrixSet().equals(tileMatrixLinkIdentifier)) {
                return tileMatrixSetLink;
            }
        }

        return null ;
    }

    @Nullable
    public String getTemplateUrl(@NonNull String resourceType) {
        WmtsResourceUrl url = this.getResourceUrl(resourceType);

        if (url == null) {
            return null;
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

        return url.getTemplate(this.selectedStyleIdentifier, this.getSelectedTileMatrixLinkIdentifier());
    }

    @NonNull
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

    public int getTilesSize() {
        return this.tilesSize;
    }

    public void setTilesSize(int tilesSize) {
        this.tilesSize = tilesSize;
    }

    @Nullable
    private WmtsStyle getDefaultStyle() {
        for (WmtsStyle style : this.styles) {
            if (style.isDefault()) {
                return style;
            }
        }

        return null;
    }

    @Nullable
    private WmtsResourceUrl getResourceUrl(@NonNull String resourceType) {
        for (WmtsResourceUrl url: this.resourceURLs) {
            if (url.getResourceType().equals(resourceType) ){
                return url;
            }
        }

        return null;
    }
}
