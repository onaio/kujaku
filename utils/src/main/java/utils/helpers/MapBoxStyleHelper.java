package utils.helpers;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.services.commons.utils.MapboxUtils;
import com.mapbox.services.commons.utils.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import utils.exceptions.InvalidMapBoxStyleException;

/**
 * Helps manipulate the contents of a MapBox Style object.
 * <p>
 * See:
 * https://www.mapbox.com/mapbox-gl-js/style-spec/
 * <p>
 * Created by Jason Rogena - jrogena@ona.io on 11/7/17.
 * Created by Ephraim Kigamba - ekigamba@ona.io on 11/7/17.
 */

public class MapBoxStyleHelper {
    public static final String KEY_KUJAKU = "kujaku";
    private final JSONObject styleObject;
    private final KujakuConfig kujakuConfig;

    public MapBoxStyleHelper(JSONObject styleObject) throws JSONException {
        this.styleObject = styleObject;
        if (this.styleObject.has("metadata")
                && this.styleObject.getJSONObject("metadata").has(KEY_KUJAKU)) {
            this.kujakuConfig = new KujakuConfig(this.styleObject.getJSONObject("metadata").getJSONObject(KEY_KUJAKU));
        }
        else {
            this.kujakuConfig = new KujakuConfig();
        }
    }

    public JSONObject build() throws InvalidMapBoxStyleException, JSONException {
        if (kujakuConfig.isValid()) {
            if (!styleObject.has("metadata")) {
                styleObject.put("metadata", new JSONObject());
            }
            styleObject.getJSONObject("metadata").put(KEY_KUJAKU, kujakuConfig.toJSONObject());
        } else {
            throw new InvalidMapBoxStyleException("The Kujaku configuraiton in the MapBox style is incomplete");
        }

        return styleObject;
    }

    public boolean insertGeoJsonDataSource(@NonNull String sourceName, JSONObject geoJson, @NonNull String layerId)
            throws JSONException, InvalidMapBoxStyleException {
        if (styleObject != null && !TextUtils.isEmpty(sourceName)) {
            JSONObject styleSources = new JSONObject();
            if (styleObject.has("sources")) {
                styleSources = styleObject.getJSONObject("sources");
            }
            styleSources.put(sourceName, geoJson);
            styleObject.put("sources", styleSources);

            return linkDataSourceToLayer(sourceName, layerId, null);
        }

        return false;
    }

    public KujakuConfig getKujakuConfig() {
        return kujakuConfig;
    }

    /**
     * Links MapBox data-source to a pre-existing MapBox layer
     *
     * @param sourceName  Name of the MapBox datasource
     * @param layerId     Id of the MapBox layer
     * @param sourceLayer Layer on the vector tile source to use
     * @return {@code TRUE} if able to associate the data-source with the layer
     * @throws JSONException               If unable to parse the style object
     * @throws InvalidMapBoxStyleException If the style object is missing required components
     */
    public boolean linkDataSourceToLayer(@NonNull String sourceName, @NonNull String layerId, @Nullable String sourceLayer)
            throws JSONException, InvalidMapBoxStyleException {
        if (!TextUtils.isEmpty(sourceName) && !TextUtils.isEmpty(layerId) && styleObject != null) {
            if (styleObject.has("layers")) {
                JSONArray layers = styleObject.getJSONArray("layers");
                for (int i = 0; i < layers.length(); i++) {
                    JSONObject currentLayer = layers.getJSONObject(i);
                    if (currentLayer.has("id") && !currentLayer.getString("id").isEmpty()) {
                        if (layerId.equals(currentLayer.getString("id"))) {
                            currentLayer.put("source", sourceName);
                            if (TextUtils.isEmpty(sourceLayer) && currentLayer.has("source-layer")) {
                                currentLayer.remove("source-layer");
                            } else if (!TextUtils.isEmpty(sourceLayer)) {
                                currentLayer.put("source-layer", sourceLayer);
                            }

                            return true;
                        }
                    } else {
                        throw new InvalidMapBoxStyleException("Layer with no Id");
                    }
                }
            } else {
                throw new InvalidMapBoxStyleException("Provided style does not have layers");
            }
        }

        return false;
    }

    /**
     * Makes layers invisible by changing the visibility property of the layer. This is useful
     * where layers in a style have dummy data which is not going to be replaced
     *
     * @param layerIds String array of <a href="https://www.mapbox.com/mapbox-gl-js/style-spec/#layer-id">layerIds</a>
     * @return
     * @throws JSONException
     * @throws InvalidMapBoxStyleException If the style object is missing required components
     */
    public boolean disableLayers(@Nullable String[] layerIds)
            throws JSONException, InvalidMapBoxStyleException {
        if (layerIds == null || layerIds.length < 1 || styleObject == null) {
            return false;
        }

        ArrayList<String> layerIdsList = new ArrayList<>(Arrays.asList(layerIds));

        if (styleObject.has("layers")) {
            JSONArray layers = styleObject.getJSONArray("layers");
            for (int i = 0; i < layers.length(); i++) {
                JSONObject currentLayer = layers.getJSONObject(i);
                if (currentLayer.has("id") && !currentLayer.getString("id").isEmpty()) {
                    for (Iterator<String> layerIdIterator = layerIdsList.iterator(); layerIdIterator.hasNext(); ) {
                        String layerId = layerIdIterator.next();
                        if (layerId.equals(currentLayer.getString("id"))) {
                            JSONObject layout = new JSONObject();
                            layout.put("visibility", "none");
                            currentLayer.put("layout", layout);

                            layerIdIterator.remove();
                            if (layerIdsList.isEmpty()) {
                                return true;
                            }
                        }
                    }
                } else {
                    throw new InvalidMapBoxStyleException("Layer with no Id");
                }
            }
        }

        return true;
    }

    public static class KujakuConfig {
        public static final String KEY_INFO_WINDOW = "info_window";
        public static final String KEY_SORT_FIELDS = "sort_fields";
        public static final String KEY_DATA_SOURCE_NAMES = "data_source_names";
        private final JSONArray sortFields;
        private final JSONArray dataSourceNames;
        private final InfoWindowConfig infoWindowConfig;

        public KujakuConfig() {
            sortFields = new JSONArray();
            dataSourceNames = new JSONArray();
            infoWindowConfig = new InfoWindowConfig();
        }

        public KujakuConfig(JSONObject config) throws JSONException {
            if (config.has(KEY_INFO_WINDOW)) {
                infoWindowConfig = new InfoWindowConfig(config.getJSONObject(KEY_INFO_WINDOW));
            } else {
                infoWindowConfig = new InfoWindowConfig();
            }
            if (config.has(KEY_SORT_FIELDS)) {
                sortFields = config.getJSONArray(KEY_SORT_FIELDS);
            } else {
                sortFields = new JSONArray();
            }
            if (config.has(KEY_DATA_SOURCE_NAMES)) {
                dataSourceNames = config.getJSONArray(KEY_DATA_SOURCE_NAMES);
            } else {
                dataSourceNames = new JSONArray();
            }
        }

        /**
         * Add a GeoJson property in the GeoJson data sources that will be used to sort the data
         *
         * @param type      The data type to be used when sorting. Can be 'date', 'string', or 'int'
         * @param dataField The id of the GeoJson property
         * @throws JSONException
         * @throws InvalidMapBoxStyleException
         */
        public void addSortField(@NonNull String type, @NonNull String dataField)
                throws JSONException, InvalidMapBoxStyleException {
            JSONObject sortField = new JSONObject();
            if (!TextUtils.isEmpty(type) && !TextUtils.isEmpty(dataField)) {
                sortField.put("type", type);
                sortField.put("data_field", dataField);
            } else {
                throw new InvalidMapBoxStyleException("The provided sort field has an empty property");
            }
            sortFields.put(sortField);
        }

        /**
         * Adds the provided MapBox Style data-source to the list of data-sources to be considered
         * when rendering data.
         *
         * @param dataSourceName The name (JSON key) of the MapBox Style data-source
         * @throws InvalidMapBoxStyleException
         */
        public void addDataSourceName(@NonNull String dataSourceName) throws InvalidMapBoxStyleException {
            if (!TextUtils.isEmpty(dataSourceName)) {
                dataSourceNames.put(dataSourceName);
            } else {
                throw new InvalidMapBoxStyleException("The provided data source name is empty");
            }
        }

        /**
         * Checks whether all required configurations have been set
         *
         * @return @code{TRUE} if all required configurations have been set
         */
        public boolean isValid() {
            if (sortFields != null && sortFields.length() > 0
                    && dataSourceNames != null && dataSourceNames.length() > 0
                    && infoWindowConfig != null && infoWindowConfig.isValid()) {
                return true;
            }

            return false;
        }

        protected JSONObject toJSONObject() throws JSONException {
            JSONObject config = new JSONObject();
            JSONObject infoWindow = infoWindowConfig.toJSONObject();
            config.put(KEY_INFO_WINDOW, infoWindow);
            config.put(KEY_DATA_SOURCE_NAMES, dataSourceNames);
            config.put(KEY_SORT_FIELDS, sortFields);

            return config;
        }

        public JSONArray getSortFields() {
            return sortFields;
        }

        public JSONArray getDataSourceNames() {
            return dataSourceNames;
        }

        public InfoWindowConfig getInfoWindowConfig() {
            return infoWindowConfig;
        }

        public static class InfoWindowConfig {
            public static final String KEY_VISIBLE_PROPERTIES = "visible_properties";
            public static final String KEY_VP_ID = "id";
            public static final String KEY_VP_LABEL = "label";
            private final JSONArray visibleProperties;

            public InfoWindowConfig() {
                visibleProperties = new JSONArray();
            }

            public InfoWindowConfig(JSONObject config) throws JSONException {
                if (config.has(KEY_VISIBLE_PROPERTIES)) {
                    visibleProperties = config.getJSONArray(KEY_VISIBLE_PROPERTIES);
                } else {
                    visibleProperties = new JSONArray();
                }
            }

            public JSONArray getVisibleProperties() {
                return visibleProperties;
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
            public boolean isValid() {
                if (visibleProperties != null && visibleProperties.length() > 0) {
                    return true;
                }

                return false;
            }

            /**
             * Returns a JSONObject representation of the configuration
             *
             * @return A {@link JSONObject} representing the configuration
             * @throws JSONException
             */
            protected JSONObject toJSONObject() throws JSONException {
                JSONObject config = new JSONObject();
                config.put(KEY_VISIBLE_PROPERTIES, visibleProperties);
                return config;
            }
        }
    }
}
