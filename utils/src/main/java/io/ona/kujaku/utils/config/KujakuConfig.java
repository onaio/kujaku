package io.ona.kujaku.utils.config;

import android.support.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import io.ona.kujaku.utils.exceptions.InvalidMapBoxStyleException;

/**
 * Created by Jason Rogena - jrogena@ona.io on 1/2/18.
 */
public class KujakuConfig implements Config {
    public static final String KEY_INFO_WINDOW = "info_window";
    public static final String KEY_SORT_FIELDS = "sort_fields";
    public static final String KEY_DATA_SOURCES = "data_sources";
    private final ArrayList<SortFieldConfig> sortFieldConfigs;
    private final ArrayList<DataSourceConfig> dataSourceConfigs;
    private final InfoWindowConfig infoWindowConfig;

    public KujakuConfig() {
        sortFieldConfigs = new ArrayList<>();
        dataSourceConfigs = new ArrayList<>();
        infoWindowConfig = new InfoWindowConfig();
    }

    public KujakuConfig(JSONObject config) throws JSONException, InvalidMapBoxStyleException {
        if (config.has(KEY_INFO_WINDOW)) {
            infoWindowConfig = new InfoWindowConfig(config.getJSONObject(KEY_INFO_WINDOW));
        } else {
            infoWindowConfig = new InfoWindowConfig();
        }
        
        sortFieldConfigs = new ArrayList<>();
        if (config.has(KEY_SORT_FIELDS)) {
            JSONArray array = config.getJSONArray(KEY_SORT_FIELDS);
            for (int i = 0; i < array.length(); i++) {
                sortFieldConfigs.add(new SortFieldConfig(array.getJSONObject(i)));
            }
        }

        dataSourceConfigs = new ArrayList<>();
        if (config.has(KEY_DATA_SOURCES)) {
            JSONArray array = config.getJSONArray(KEY_DATA_SOURCES);
            for (int i = 0; i < array.length(); i++) {
                dataSourceConfigs.add(new DataSourceConfig(array.getJSONObject(i)));
            }
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
    public void addSortFieldConfig(@NonNull String type, @NonNull String dataField)
            throws JSONException, InvalidMapBoxStyleException {
        SortFieldConfig sortField = new SortFieldConfig();
        sortField.setType(type);
        sortField.setDataField(dataField);

        sortFieldConfigs.add(sortField);
    }

    public void addSortFieldConfig(@NonNull SortFieldConfig sortFieldConfig) {
        sortFieldConfigs.add(sortFieldConfig);
    }

    /**
     * Adds the provided MapBox Style data-source to the list of data-sources to be considered
     * when rendering data.
     *
     * @param dataSourceName The name (JSON key) of the MapBox Style data-source
     */
    public void addDataSourceConfig(@NonNull String dataSourceName) throws InvalidMapBoxStyleException {
        dataSourceConfigs.add(new DataSourceConfig(dataSourceName));
    }

    /**
     * Checks whether all required configurations have been set
     *
     * @return @code{TRUE} if all required configurations have been set
     */
    public boolean isValid() {
        if (sortFieldConfigs == null || sortFieldConfigs.size() == 0) {
            return false;
        }
        for (SortFieldConfig curConfig : sortFieldConfigs) {
            if (!curConfig.isValid()) return false;
        }

        if (dataSourceConfigs == null || dataSourceConfigs.size() == 0) {
            return false;
        }
        for (DataSourceConfig curConfig : dataSourceConfigs) {
            if (!curConfig.isValid()) return false;
        }

        if (infoWindowConfig != null) {
            return  infoWindowConfig.isValid();
        }

        return false;
    }

    @Override
    public JSONObject toJsonObject() throws JSONException {
        JSONObject config = new JSONObject();
        config.put(KEY_INFO_WINDOW, infoWindowConfig.toJsonObject());
        config.put(KEY_DATA_SOURCES, getDataSourceConfigJsonArray());
        config.put(KEY_SORT_FIELDS, getSortFieldsConfigJsonArray());

        return config;
    }

    private JSONArray getSortFieldsConfigJsonArray() throws JSONException {
        JSONArray array = new JSONArray();
        for (SortFieldConfig curConfig : sortFieldConfigs) {
            array.put(curConfig.toJsonObject());
        }

        return array;
    }

    private JSONArray getDataSourceConfigJsonArray() throws JSONException {
        JSONArray array = new JSONArray();
        for (DataSourceConfig curConfig : dataSourceConfigs) {
            array.put(curConfig.toJsonObject());
        }

        return array;
    }

    public ArrayList<SortFieldConfig> getSortFieldConfigs() {
        return sortFieldConfigs;
    }

    public ArrayList<DataSourceConfig> getDataSourceConfigs() {
        return dataSourceConfigs;
    }

    public InfoWindowConfig getInfoWindowConfig() {
        return infoWindowConfig;
    }

}
