package io.ona.kujaku.test.shadows;

import android.support.annotation.NonNull;

import com.mapbox.mapboxsdk.style.layers.Layer;
import com.mapbox.mapboxsdk.style.layers.PropertyValue;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

import java.util.HashMap;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 11/02/2019
 */

@Implements(Layer.class)
public class ShadowLayer {

    protected String shadowLayerId;

    private HashMap<String, PropertyValue> propertyValues = new HashMap<>();

    @Implementation
    public void setProperties(@NonNull PropertyValue<?>... properties) {
        for (PropertyValue propertyValue: properties) {
            propertyValues.put(propertyValue.name, propertyValue);
        }
    }

    public HashMap<String, PropertyValue> getPropertyValues() {
        return propertyValues;
    }


    @Implementation
    @NonNull
    public String getId() {
        return shadowLayerId;
    }

}
