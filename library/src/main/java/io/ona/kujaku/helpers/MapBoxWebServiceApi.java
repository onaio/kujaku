package io.ona.kujaku.helpers;

import android.support.annotation.NonNull;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 16/11/2017.
 */

public class MapBoxWebServiceApi {

    private String mapboxApiUrl = "https://api.mapbox.com"
            , stylesPath = "/styles";

    public void retrieveStyleJSON(@NonNull String username,@NonNull String styleId, Response.Listener<String> responseListener, Response.ErrorListener errorListener) {
        if (username.isEmpty() || styleId.isEmpty()) {
            errorListener.onErrorResponse(new VolleyError("Invalid username OR styleId"));
            return;
        }
        String url = mapboxApiUrl + stylesPath + "/" + username + "/" + styleId;

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, responseListener, errorListener);
    }

    public void retrieveStyleJSON(@NonNull String mapBoxStyleURL, Response.Listener<String> responseListener, Response.ErrorListener errorListener) {
        if (!mapBoxStyleURL.matches("mapbox://[A-Za-z0-9]+/[A-Za-z0-9]+")) {
            errorListener.onErrorResponse(new VolleyError("Invalid MapBox Style URL "));
            return;
        }

        String[] styleParts = mapBoxStyleURL.split("/");
        String username = styleParts[styleParts.length - 2];
        String styleId = styleParts[styleParts.length - 1];
        retrieveStyleJSON(username, styleId, responseListener, errorListener);
    }
}
