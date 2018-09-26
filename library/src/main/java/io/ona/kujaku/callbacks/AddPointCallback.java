package io.ona.kujaku.callbacks;

import org.json.JSONObject;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 26/09/2018
 */

public interface AddPointCallback {

    void onPointAdd(JSONObject jsonObject);

    void onCancel();
}
