package io.ona.kujaku.wmts.model;

import androidx.annotation.NonNull;

import org.simpleframework.xml.ElementList;

import java.util.List;
import io.ona.kujaku.wmts.model.common.ows.LanguageStringType;

/**
 * Describes a Wmts Service Identification object from the WMTS Capabilities object
 *
 * Created by Emmanuel Otin - eo@novel-t.ch 11/28/18.
 */
public class WmtsServiceIdentification {

    @ElementList(inline=true, entry="Title")
    private List<LanguageStringType> titles;

    @NonNull
    public List<LanguageStringType> getTitles() { return this.titles; }

    @NonNull
    public String getTitle(@NonNull String lang) {
        String result = "No Title found";

        for (LanguageStringType title: this.titles) {
            if (title.getLang() != null && title.getLang().equals(lang)) {
                result = title.getValue();
                break;
            }
        }

        return result;
    }
}
