package io.ona.kujaku.wmts.model.common.ows;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Text;

/**
 * Describes a ows:LanguageStringType element
 *
 * Created by Emmanuel Otin - eo@novel-t.ch 16/01/18
 */
public class LanguageStringType {

    @Text
    private String value;

    @Attribute(name="lang", required=false)
    private String lang;

    public String getValue() {
        return this.value;
    }

    public String getLang() {
        return this.lang;
    }
}
