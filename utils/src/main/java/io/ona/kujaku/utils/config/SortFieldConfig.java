package io.ona.kujaku.utils.config;

import android.support.annotation.NonNull;

import com.mapbox.services.commons.utils.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import io.ona.kujaku.utils.exceptions.InvalidMapBoxStyleException;
import io.ona.kujaku.utils.helpers.MapBoxStyleHelper;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 24/11/2017.
 */

public class SortFieldConfig implements Config {
    private static final String TAG = SortFieldConfig.class.getSimpleName();
    public static final String KEY_DATA_FIELD = "data_field";
    public static final String KEY_TYPE = "type";

    public enum FieldType {
        NUMBER,
        DATE,
        STRING
    }

    private FieldType type;
    private String dataField;

    public SortFieldConfig(@NonNull FieldType fieldType, @NonNull String dataField) {
        setType(fieldType);
        setDataField(dataField);
    }

    public SortFieldConfig(@NonNull String type, @NonNull String dataField)
            throws InvalidMapBoxStyleException {
        setType(type);
        setDataField(dataField);
    }

    public SortFieldConfig(@NonNull JSONObject jsonObject)
            throws JSONException, InvalidMapBoxStyleException {
        this(jsonObject.getString(KEY_TYPE), jsonObject.getString(KEY_DATA_FIELD));
    }

    public SortFieldConfig() {
    }

    public FieldType getType() {
        return type;
    }

    public void setType(@NonNull FieldType type) {
        this.type = type;
    }

    public void setType(@NonNull String type) throws InvalidMapBoxStyleException {
        if (isValidType(type)) {
            if (FieldType.NUMBER.toString().equalsIgnoreCase(type)) {
                setType(FieldType.NUMBER);
            } else if (FieldType.DATE.toString().equalsIgnoreCase(type)) {
                setType(FieldType.DATE);
            } else {
                setType(FieldType.STRING);
            }
        } else {
            throw new InvalidMapBoxStyleException("Unknown SortField type '" + type + "'");
        }
    }

    public static boolean isValidType(String type) {
        if (FieldType.NUMBER.toString().equalsIgnoreCase(type)
                || FieldType.DATE.toString().equalsIgnoreCase(type)
                || FieldType.STRING.toString().equalsIgnoreCase(type)) {
            return true;
        }

        return false;
    }

    public String getDataField() {
        return dataField;
    }

    public void setDataField(@NonNull String dataField) {
        this.dataField = dataField;
    }

    public static SortFieldConfig[] extractSortFieldConfigs(@NonNull MapBoxStyleHelper styleHelper)
            throws JSONException, InvalidMapBoxStyleException {
        ArrayList<SortFieldConfig> sortFieldConfigs = styleHelper.getKujakuConfig()
                .getSortFieldConfigs();
        SortFieldConfig[] sortFields = sortFieldConfigs.toArray(new SortFieldConfig[]{});

        return sortFields;
    }

    @Override
    public boolean isValid() {
        return type != null && !TextUtils.isEmpty(dataField);
    }

    @Override
    public JSONObject toJsonObject() throws JSONException {
        JSONObject object = new JSONObject();
        object.put(KEY_TYPE, type.toString().toLowerCase());
        object.put(KEY_DATA_FIELD, dataField);
        return object;
    }
}
