package io.ona.kujaku.wmts.model;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;

/**
 * Describe a Wmts Style object from the WMTS Capabilities object
 *
 * Created by Emmanuel Otin - eo@novel-t.ch 11/28/18.
 */
public class WmtsStyle {

    @Attribute(name="isDefault")
    private boolean isDefault = false;

    @Element(name="Title",required=false)
    private String title;

    @Element(name="Identifier")
    private String identifier;

    public boolean isDefault() {
        return this.isDefault;
    }

    public String getTitle() {
        return this.title;
    }

    public String getIdentifier() {
        return this.identifier;
    }
}
