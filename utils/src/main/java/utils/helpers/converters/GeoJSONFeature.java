package utils.helpers.converters;

import com.mapbox.mapboxsdk.geometry.LatLng;

import java.util.ArrayList;
import java.util.List;

/**
 * This is an ambiguous GeoJSON feature. Depending on the feature points
 *
 * Created by Ephraim Kigamba - ekigamba@ona.io on 06/11/2017.
 */
public class GeoJSONFeature {

    private String id;
    private List<LatLng> featurePoints = new ArrayList<>();
    private List<Property> featureProperties = new ArrayList<>();
    public static enum Type {
        POINT,
        MULTI_POINT,
        LINE_STRING,
        MULTI_LINE_STRING,
        POLYGON,
        MULTI_POLYGON,
        GEOMETRY_COLLECTION
    }
    private Type featureType = Type.POINT;

    public GeoJSONFeature() {}

    public GeoJSONFeature(List<LatLng> featurePoints, List<Property> featureProperties) {
        this.featurePoints = featurePoints;
        this.featureProperties = featureProperties;

        if (featurePoints != null && featurePoints.size() > 1) {
            featureType = Type.MULTI_POINT;
        }
    }

    public GeoJSONFeature(List<LatLng> featurePoints) {
        this(featurePoints, new ArrayList<Property>());
    }

    public GeoJSONFeature addPoint(LatLng latLng) {
        if (featurePoints == null) {
            featurePoints = new ArrayList<>();
        }

        if (featurePoints.size() > 0) {
            featureType = Type.MULTI_POINT;
        }

        featurePoints.add(latLng);
        return this;
    }

    public GeoJSONFeature addProperty(String name, Object value) {
        if (featureProperties == null) {
            featureProperties = new ArrayList<>();
        }
        featureProperties.add(new Property(name, value));
        return this;
    }

    public List<LatLng> getFeaturePoints() {
        return featurePoints;
    }

    public List<Property> getFeatureProperties() {
        return featureProperties;
    }

    public Type getFeatureType() {
        return featureType;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean hasId() {
        return (id != null && !id.isEmpty());
    }

    static class Property {
        private String name;
        private Object value;

        public Property(String name, Object value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Object getValue() {
            return value;
        }

        public void setValue(Object value) {
            this.value = value;
        }
    }
}
