package io.ona.kujaku.utils.wmts.model;

import org.simpleframework.xml.ElementList;

import java.util.List;

/**
 * Describe a Wmts Contents object from the WMTS Capabilities object
 *
 * Created by Emmanuel Otin - eo@novel-t.ch 11/28/18.
 */
public class WmtsContents {

    @ElementList(inline=true, entry="Layer")
    private List<WmtsLayer> layers;

    public List<WmtsLayer> getLayers() {
        return this.layers;
    }

    /**
     * Return the layer corresponding to the identifier
     *
     * @param identifier
     * @return
     */
    public WmtsLayer getLayer(String identifier) {
        for (WmtsLayer layer : this.layers) {
            if (layer.getIdentifier().equals(identifier)) {
                return layer;
            }
        }

        return null ;
    }
}
