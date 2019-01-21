package io.ona.kujaku.wmts.model;

import org.simpleframework.xml.Attribute;

import java.util.Locale;

/**
 * Describes a Wmts Resource Url object from the WMTS Capabilities object
 *
 * Created by Emmanuel Otin - eo@novel-t.ch 11/28/18.
 */
public class WmtsResourceUrl {

    public static final String STYLE = "{Style}";

    public static final String TILE_MATRIX_SET = "{TileMatrixSet}";

    /**
     * To be replaced with {z}
     */
    public static final String TILE_MATRIX = "{TileMatrix}";

    /**
     * To be replaced with {x}
     */
    public static final String TILE_COL = "{TileCol}";

    /**
     * To be replaced with {y}
     */
    public static final String TILE_ROW = "{TileRow}";

    public static final String TILE_COL_X = "{x}";
    public static final String TILE_ROW_Y = "{y}";
    public static final String TILE_MATRIX_Z = "{z}";

    @Attribute(name="format")
    private String format;

    @Attribute(name="resourceType")
    private String resourceType;

    @Attribute(name="template")
    private String template;

    public String getFormat() { return this.format; }

    public String getResourceType() { return this.resourceType; }

    /**
     * Return template Url
     * @return string
     */
    public String getTemplate(String styleIdentifier, String tileMatrixSetLinkIdentifier) {
        String _template = this.template.replace(STYLE, styleIdentifier);
        _template = _template.replace(STYLE.toLowerCase(Locale.ROOT), styleIdentifier); // We accept the style variable "{style}"
        _template = _template.replace(TILE_MATRIX_SET, tileMatrixSetLinkIdentifier);
        _template = _template.replace(TILE_MATRIX, TILE_MATRIX_Z);
        _template = _template.replace(TILE_COL, TILE_COL_X);
        _template = _template.replace(TILE_ROW, TILE_ROW_Y);
        return _template;
    }
}
