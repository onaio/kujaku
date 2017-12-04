package io.ona.kujaku.adapters;

import org.json.JSONObject;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 24/11/2017.
 */

public class InfoWindowObject {

    private int position = -1;
    private JSONObject jsonObject;
    private boolean focused;
    private OnFocusChangeListener onFocusChangeListener;

    public InfoWindowObject(int pos, JSONObject jsonObject) {
        setPosition(pos);
        setJsonObject(jsonObject);
        focused = false;
    }

    public void setOnFocusChangeListener(OnFocusChangeListener onFocusChangeListener) {
        this.onFocusChangeListener = onFocusChangeListener;
    }

    public void setFocused(boolean focused) {
        this.focused = focused;
        if (this.onFocusChangeListener != null) {
            this.onFocusChangeListener.onFocusChanged(this);
        }
    }

    public boolean isFocused() {
        return focused;
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

    public static interface OnFocusChangeListener {
        void onFocusChanged(InfoWindowObject object);
    }
}
