package io.ona.kujaku.utils.config;

import android.support.annotation.NonNull;

import com.mapbox.services.commons.utils.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import io.ona.kujaku.utils.exceptions.InvalidMapBoxStyleException;

/**
 * Created by Jason Rogena - jrogena@ona.io on 1/2/18.
 */

public class DataSourceConfig implements Config {
    public static final String KEY_NAME = "name";
    private String name;

    public DataSourceConfig() {

    }

    public DataSourceConfig(@NonNull String name) throws InvalidMapBoxStyleException {
        setName(name);
    }

    public DataSourceConfig(JSONObject jsonObject) throws JSONException, InvalidMapBoxStyleException {
        this(jsonObject.getString(KEY_NAME));
    }

    @Override
    public boolean isValid() {
        return !TextUtils.isEmpty(name);
    }

    public String getName() {
        return name;
    }

    public void setName(@NonNull String name) throws InvalidMapBoxStyleException {
        if (!TextUtils.isEmpty(name)) {
            this.name = name;
        } else {
            throw new InvalidMapBoxStyleException("Empty data-source name provided");
        }
    }

    @Override
    public JSONObject toJsonObject() throws JSONException {
        JSONObject object = new JSONObject();
        object.put(KEY_NAME, name);
        return object;
    }

    public static String[] extractDataSourceNames(@NonNull ArrayList<DataSourceConfig> dataSourceConfigs) {
        String[] names = new String[dataSourceConfigs.size()];
        for (int i = 0; i < dataSourceConfigs.size(); i++) {
            names[i] = dataSourceConfigs.get(i).getName();
        }

        return names;
    }
}
