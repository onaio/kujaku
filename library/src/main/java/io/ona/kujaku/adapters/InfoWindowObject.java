package io.ona.kujaku.adapters;

import org.json.JSONObject;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 24/11/2017.
 */

public class InfoWindowObject {

    private int position = -1;
    private JSONObject jsonObject;

    public InfoWindowObject(int pos, JSONObject jsonObject) {
        setPosition(pos);
        setJsonObject(jsonObject);
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public JSONObject getJsonObject() {
        return jsonObject;
    }

    public void setJsonObject(JSONObject jsonObject) {
        this.jsonObject = jsonObject;
    }
}
