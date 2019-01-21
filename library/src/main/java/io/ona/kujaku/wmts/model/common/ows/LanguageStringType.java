package io.ona.kujaku.wmts.model.common.ows;

import android.support.annotation.NonNull;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Text;

import javax.annotation.Nullable;

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

    @NonNull
    public String getValue() {
        return this.value;
    }

    @Nullable
    public String getLang() {
        return this.lang;
    }
}
