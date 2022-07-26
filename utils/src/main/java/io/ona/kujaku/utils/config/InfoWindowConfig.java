package io.ona.kujaku.utils.config;

import androidx.annotation.NonNull;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import io.ona.kujaku.utils.exceptions.InvalidMapBoxStyleException;

/**
 * Holds configurations for the info-window UI element.
 * <p>
 * Created by Jason Rogena - jrogena@ona.io on 1/2/18.
 */
public class InfoWindowConfig implements Config {
    public static final String KEY_VISIBLE_PROPERTIES = "visible_properties";
    public static final String KEY_VP_ID = "id";
    public static final String KEY_VP_LABEL = "label";
    private JSONArray visibleProperties;

    public InfoWindowConfig() {
        visibleProperties = new JSONArray();
    }

    public InfoWindowConfig(JSONObject config) throws JSONException, InvalidMapBoxStyleException {
        visibleProperties = new JSONArray();
        if (config.has(KEY_VISIBLE_PROPERTIES)) {
            setVisibleProperties(config.getJSONArray(KEY_VISIBLE_PROPERTIES));
        }
    }

    public JSONArray getVisibleProperties() {
        return visibleProperties;
    }

    public void setVisibleProperties(@NonNull JSONArray visibleProperties) throws JSONException, InvalidMapBoxStyleException {
        for (int i = 0; i < visibleProperties.length(); i++) {
            JSONObject curVisibleProperty = visibleProperties.getJSONObject(i);
            addVisibleProperty(curVisibleProperty.getString(KEY_VP_ID),
                    curVisibleProperty.getString(KEY_VP_LABEL));
        }
    }

    /**
     * Adds to the list of GeoJson properties defined in the data source layers that will be
     * visible in an info window when a feature is clicked on the map
     *
     * @param id    The id of the GeoJson property
     * @param label The label the property should be given on the info window
     * @throws JSONException               If there's a problem inserting either the id or label to the configuration JSON Object
     * @throws InvalidMapBoxStyleException If either the id or label for the property is empty
     */
    public void addVisibleProperty(@NonNull String id, @NonNull String label)
            throws JSONException, InvalidMapBoxStyleException {
        if (!TextUtils.isEmpty(id) && !TextUtils.isEmpty(label)) {
            JSONObject property = new JSONObject();
            property.put(KEY_VP_ID, id);
            property.put(KEY_VP_LABEL, label);
            visibleProperties.put(property);
        } else {
            throw new InvalidMapBoxStyleException("The provided property has an empty key or label");
        }
    }

    /**
     * Checks whether all required configurations have been set
     *
     * @return {@code TRUE} if all required configurations have been set
     */
    @Override
    public boolean isValid() {
        return (visibleProperties != null && visibleProperties.length() > 0);
    }

    /**
     * Returns a JSONObject representation of the configuration
     *
     * @return A {@link JSONObject} representing the configuration
     * @throws JSONException
     */
    @Override
    public JSONObject toJsonObject() throws JSONException {
        JSONObject config = new JSONObject();
        config.put(KEY_VISIBLE_PROPERTIES, visibleProperties);
        return config;
    }
}
