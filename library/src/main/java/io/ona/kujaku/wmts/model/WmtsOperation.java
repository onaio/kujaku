package io.ona.kujaku.wmts.model;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;

/**
 * Describe a Wmts Operation object from the WMTS Capabilities object
 *
 * Created by Emmanuel Otin - eo@novel-t.ch 11/28/18.
 */
@Element(name="Operation")
public class WmtsOperation {

    @Attribute(name="name")
    private String name;

    public String getName() { return this.name; }
}
