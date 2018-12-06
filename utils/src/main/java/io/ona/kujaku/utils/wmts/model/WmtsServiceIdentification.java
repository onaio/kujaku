package io.ona.kujaku.utils.wmts.model;

import org.simpleframework.xml.Element;

/**
 * Describe a Wmts Service Identification object from the WMTS Capabilities object
 *
 * Created by Emmanuel Otin - eo@novel-t.ch 11/28/18.
 */
public class WmtsServiceIdentification {

    @Element(name="Title")
    private String title;

    @Element(name="ServiceType")
    private String serviceType;

    @Element(name="ServiceTypeVersion")
    private String serviceTypeVersion;

    public String getTitle() { return this.title; }
    public String getServiceType() { return this.serviceType; }
    public String getServiceTypeVersion() { return this.serviceTypeVersion; }
}
