package io.ona.kujaku.helpers.converters;

import android.util.Log;

import com.cocoahero.android.geojson.Feature;
import com.cocoahero.android.geojson.FeatureCollection;
import com.cocoahero.android.geojson.GeoJSON;
import com.cocoahero.android.geojson.MultiPoint;
import com.cocoahero.android.geojson.Point;
import com.cocoahero.android.geojson.Position;
import com.mapbox.mapboxsdk.geometry.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Ephraim Kigamba on 06/11/2017.
 */

public class GeoJSONHelper {

    private FeatureCollection featureCollection;

    /**
     * Currently only supports point point && multipoint features
     *
     * @param geoJSONFeatures
     */
    public GeoJSONHelper(GeoJSONFeature... geoJSONFeatures) {
        //Create a new feature collection
        featureCollection = new FeatureCollection();

        if (geoJSONFeatures == null) {
            return;
        }

        for(GeoJSONFeature geoJSONFeature : geoJSONFeatures) {

            try {
                featureCollection.addFeature(getFeature(geoJSONFeature));
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e(GeoJSONHelper.class.getName(), Log.getStackTraceString(e));
            }
        }
    }

    public GeoJSONHelper(FeatureCollection featureCollection) {
        this.featureCollection = featureCollection;
    }

    public String getJsonFeatureCollection() {
        if (featureCollection == null) {
            return "";
        }

        try {
            return featureCollection.toJSON().toString();
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(GeoJSONHelper.class.getName(), Log.getStackTraceString(e));
            return "";
        }
    }

    public String getGeoJsonData() {
        if (featureCollection == null) {
            return "";
        }

        JSONObject geoJson = new JSONObject();

        try {
            geoJson.put("type", "geojson");
            geoJson.put("data", featureCollection.toJSON());
            return geoJson.toString();
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(GeoJSONHelper.class.getName(), Log.getStackTraceString(e));
            return "";
        }
    }

    private Feature getFeature(GeoJSONFeature geoJSONFeature) throws JSONException {
        if (geoJSONFeature == null) {
            return null;
        }

        Feature finalFeature;
        GeoJSONFeature.Type featureType = geoJSONFeature.getFeatureType();

        if (featureType == GeoJSONFeature.Type.POINT) {
            LatLng latLng = geoJSONFeature.getFeaturePoints().get(0);
            Point point = new Point(latLng.getLatitude(), latLng.getLongitude(), latLng.getAltitude());
            finalFeature = new Feature(point);

        } else if (featureType == GeoJSONFeature.Type.MULTI_POINT) {
            MultiPoint multiPoint = new MultiPoint();

            for (LatLng latLng: geoJSONFeature.getFeaturePoints()) {
                multiPoint.addPosition(new Position(latLng.getLatitude(), latLng.getLongitude(), latLng.getAltitude()));
            }
            finalFeature = new Feature(multiPoint);
        } else {
            finalFeature = new Feature();
        }

        // Add the id
        if (geoJSONFeature.hasId()) {
            finalFeature.setIdentifier(geoJSONFeature.getId());
        }

        //Add properties
        JSONObject jsonProperties = new JSONObject();

        for (GeoJSONFeature.Property property: geoJSONFeature.getFeatureProperties()) {
            jsonProperties.put(property.getName(), property.getValue());
        }

        finalFeature.setProperties(jsonProperties);

        return finalFeature;
    }
}
