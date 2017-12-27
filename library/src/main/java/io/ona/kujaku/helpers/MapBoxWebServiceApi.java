package io.ona.kujaku.helpers;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.android.volley.NetworkError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.File;

import utils.Constants;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 16/11/2017.
 */

public class MapBoxWebServiceApi {

    private String mapboxApiUrl = "https://api.mapbox.com"
            , stylesPath = "/styles/v1";
    private RequestQueue requestQueue;
    private String mapboxAccessToken;

    public MapBoxWebServiceApi(Context context, @NonNull String mapboxAccessToken) {
        requestQueue = Volley.newRequestQueue(context);
        this.mapboxAccessToken = mapboxAccessToken;
    }

    public void retrieveStyleJSON(@NonNull final String username, @NonNull final String styleId, @NonNull final Response.Listener<String> responseListener, @NonNull final Response.ErrorListener errorListener) {
        if (username.isEmpty() || styleId.isEmpty()) {
            errorListener.onErrorResponse(new VolleyError("Invalid username OR styleId"));
            return;
        }
        String url = mapboxApiUrl + stylesPath + "/" + username + "/" + styleId + "?access_token=" + mapboxAccessToken;

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (response != null && !response.isEmpty()) {
                    MapBoxStyleStorage mapBoxStyleStorage = new MapBoxStyleStorage();
                    mapBoxStyleStorage.cacheStyle("mapbox://styles/" + username + File.separator + styleId, response);
                }

                responseListener.onResponse(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error instanceof NetworkError) {
                    String cachedStyle = (new MapBoxStyleStorage())
                            .getCachedStyle("mapbox://styles/" + username + File.separator + styleId);
                    if (!TextUtils.isEmpty(cachedStyle)) {
                        responseListener.onResponse(cachedStyle);
                        return;
                    }
                }
                errorListener.onErrorResponse(error);

            }
        });
        requestQueue.add(stringRequest);
    }

    public void retrieveStyleJSON(@NonNull String mapBoxStyleURL,@NonNull Response.Listener<String> responseListener,@NonNull Response.ErrorListener errorListener) {
        if (!mapBoxStyleURL.matches(Constants.MAP_BOX_URL_FORMAT)) {
            errorListener.onErrorResponse(new VolleyError("Invalid MapBox Style URL "));
            return;
        }

        String[] styleParts = mapBoxStyleURL.split("/");
        String username = styleParts[styleParts.length - 2];
        String styleId = styleParts[styleParts.length - 1];
        retrieveStyleJSON(username, styleId, responseListener, errorListener);
    }
}
