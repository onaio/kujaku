package io.ona.kujaku.sorting.objects;

import android.support.annotation.NonNull;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 24/11/2017.
 */

public class SortField {

    public enum FieldType {
        NUMBER,
        DATE,
        STRING
    }
    private FieldType type;
    private String dataField;
    private static final String TAG = SortField.class.getSimpleName();

    public SortField(@NonNull String type,@NonNull String dataField) {
        setType(type);
        setDataField(dataField);
    }

    public SortField() {}

    public FieldType getType() {
        return type;
    }

    public void setType(@NonNull FieldType type) {
        this.type = type;
    }

    public void setType(@NonNull String type) {
        if (FieldType.NUMBER.toString().equalsIgnoreCase(type)) {
            setType(FieldType.NUMBER);
        } else if (FieldType.DATE.toString().equalsIgnoreCase(type)) {
            setType(FieldType.DATE);
        } else  {
            setType(FieldType.STRING);
        }
    }

    public String getDataField() {
        return dataField;
    }

    public void setDataField(@NonNull String dataField) {
        this.dataField = dataField;
    }

    public static SortField extract(@NonNull JSONObject jsonObject) {
        SortField sortField = null;

        try {
            sortField = new SortField();
            sortField.setDataField(jsonObject.getString("data_field"));
            sortField.setType(jsonObject.getString("type"));
        } catch (JSONException e) {
            Log.e(TAG, Log.getStackTraceString(e));
            sortField = null;
        }

        return sortField;
    }
}
