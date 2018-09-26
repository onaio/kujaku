package io.ona.kujaku.callbacks;

import org.json.JSONObject;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 26/09/2018
 */

public interface AddPointCallback {

    /**
     * Called when the user selects a specific point on the UI.
     *
     * @param jsonObject
     */
    void onPointAdd(JSONObject jsonObject);

    /**
     * This method is called if & when the user cancels the <strong>Add Point</strong> operation.
     *
     */
    void onCancel();
}
