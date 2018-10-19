package io.ona.kujaku;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;

import com.mapbox.mapboxsdk.geometry.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import io.ona.kujaku.activities.MapActivity;
import io.ona.kujaku.domain.Point;
import io.ona.kujaku.utils.Constants;
import io.ona.kujaku.utils.helpers.converters.GeoJSONFeature;
import io.ona.kujaku.utils.helpers.converters.GeoJSONHelper;

/**
 * @author Vincent Karuri
 */
public class KujakuLibrary {

    private BaseKujakuApplication hostApplication;
    private static KujakuLibrary library;
    private List<Point> mapActivityPoints;

    private KujakuLibrary() {}

    public static KujakuLibrary getInstance() {
        if (library == null) {
            library = new KujakuLibrary();
        }
        return library;
    }

    public void sendFeatureJSONToHostApp(JSONObject featureJSON) {
        getHostApplication().processFeatureJSON(featureJSON);
    }

    public void sendFeatureJSONToGeoWidget(JSONObject featureJSON) {
        // TODO: implement this
    }

    public void setHostApplication(BaseKujakuApplication hostApplication) { this.hostApplication = hostApplication; }

    public BaseKujakuApplication getHostApplication() {
        return hostApplication;
    }


    public void setMapActivityPoints(List<Point> points) {
       mapActivityPoints = points;
    }

    public List<Point> getMapActivityPoints() {
        return mapActivityPoints;
    }

    public void lauchMapActivity(List<Point> points) {
        setMapActivityPoints(points);
        Intent intent = new Intent(getHostApplication(), MapActivity.class);
        intent.putExtra(Constants.PARCELABLE_KEY_MAPBOX_ACCESS_TOKEN, BuildConfig.MAPBOX_SDK_ACCESS_TOKEN);
        getHostApplication().startActivity(intent);
    }
}
