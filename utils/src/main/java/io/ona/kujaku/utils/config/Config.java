package io.ona.kujaku.utils.config;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Jason Rogena - jrogena@ona.io on 1/2/18.
 */

public interface Config {
    boolean isValid();
    JSONObject toJsonObject() throws JSONException;
}
