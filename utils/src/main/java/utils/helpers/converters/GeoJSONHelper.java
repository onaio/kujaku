package utils.helpers.converters;

import com.cocoahero.android.geojson.Feature;
import com.cocoahero.android.geojson.FeatureCollection;
import com.cocoahero.android.geojson.GeoJSON;
import com.cocoahero.android.geojson.MultiPoint;
import com.cocoahero.android.geojson.Point;
import com.cocoahero.android.geojson.Position;
import com.mapbox.mapboxsdk.geometry.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * GeoJSON factory that converts {@link GeoJSONFeature} into a MapBox's GeoJSON data-source or a
 * GeoJSON feature-collection.
 *
 * See:
 *  https://www.mapbox.com/mapbox-gl-js/style-spec/#sources-geojson
 *  https://geojson.org/geojson-spec.html
 *
 * Created by Ephraim Kigamba - ekigamba@ona.io on 06/11/2017.
 */

public class GeoJSONHelper {
    private static final String TAG = GeoJSONHelper.class.getSimpleName();
    public static final String MAPBOX_GEOJSON_DATASOURCE_TYPE = "geojson";
    public static final JSONObject DEFAULT_FEATURE_COLLECTION;
    static {
        DEFAULT_FEATURE_COLLECTION = new JSONObject();
        try {
            DEFAULT_FEATURE_COLLECTION.put("type", GeoJSON.TYPE_FEATURE_COLLECTION);
            DEFAULT_FEATURE_COLLECTION.put("features", new JSONArray());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
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
            }
        }
    }

    public GeoJSONHelper(FeatureCollection featureCollection) {
        this.featureCollection = featureCollection;
    }

    private JSONObject getFeatureCollectionJson() throws JSONException {
        if (featureCollection != null) {
            return featureCollection.toJSON();
        }

        return DEFAULT_FEATURE_COLLECTION;
    }

    /**
     * Returns a string representation of the feature collection
     *
     * @return  String representation of the feature collection or an empty feature collection if
     *          {@link GeoJSONHelper#featureCollection} is {@code NULL}
     * @throws JSONException If unable to create the feature collection JSONObject
     */
    public String getJsonFeatureCollection() throws JSONException {
        return getFeatureCollectionJson().toString();
    }

    /**
     * Returns the feature collection wrapped in a MapBox's GeoJSON data source object.
     *
     * @return String representation of the feature collection wrapped in MapBox's GeoJSON data
     *         source or an empty string if {@link GeoJSONHelper#featureCollection} is {@code null}
     */
    public String getGeoJsonData() throws JSONException {
        JSONObject geoJson = new JSONObject();
        geoJson.put("type", MAPBOX_GEOJSON_DATASOURCE_TYPE);
        geoJson.put("data", getFeatureCollectionJson());

        return geoJson.toString();
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
