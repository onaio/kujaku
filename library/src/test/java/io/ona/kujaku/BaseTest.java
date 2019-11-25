package io.ona.kujaku;

import android.location.Location;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.Point;

import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import io.ona.kujaku.utils.helpers.converters.GeoJSONFeature;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 05/11/2018
 *
 * 
 * Test classes should ideally extend this class which should suffice for a good number of cases.
 * Test methods should be named: after the method they are testing + what the test confirms SHOULD happen
 * + any conditions under which this will happen eg.
 *  - createNotificationShouldCreateValidNotificationBuilderWithTextAndChannelIdWhenGivenContent
 *  - getStyleUrlShouldReturnAsset
 *  - deleteFileShouldReturnTrue
 *
 *  Any other test naming structures are welcome. It should just make sense
 *
 */
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class,
        manifest = Config.NONE)
public abstract class BaseTest {

    protected Feature generateRandomFeatureWithProperties(GeoJSONFeature.Property... properties) {
        Feature feature  = Feature.fromGeometry(getRandomPoint());

        for(GeoJSONFeature.Property property: properties) {
            Object value = property.getValue();

            if (value instanceof String) {
                feature.addStringProperty(property.getName(), (String) value);
            } else if (value instanceof Number) {
                feature.addNumberProperty(property.getName(), (Number) value);
            } else if (value instanceof Boolean) {
                feature.addBooleanProperty(property.getName(), (Boolean) value);
            }
        }

        return feature;
    }

    protected Location generateLocation(float horizontalAccuracy, long time) {
        Location location = generateRandomLocation();
        location.setAccuracy(horizontalAccuracy);
        location.setTime(time);

        return location;
    }

    protected Location generateRandomLocation() {
        double minLat = -30d;
        double maxLat = 60d;
        double minLon = -30d;
        double maxLon = 60d;

        Location location = new Location("test_provider");
        location.setLatitude((Math.random() * (maxLat - minLat)) + minLat);
        location.setLongitude((Math.random() * (maxLon - minLon)) + minLon);

        return location;
    }

    protected Point getRandomPoint() {
        double minLat = -30d;
        double maxLat = 60d;
        double minLon = -30d;
        double maxLon = 60d;

        return Point.fromLngLat(
                (Math.random() * (maxLon - minLon)) + minLon,
                (Math.random() * (maxLat - minLat)) + minLat
        );
    }

    protected void insertValueInPrivateField(Class classWithField, Object instance, String fieldName, Object newValue) throws IllegalAccessException, NoSuchFieldException {
        Field instanceField = classWithField.getDeclaredField(fieldName);
        if (!instanceField.isAccessible()) {
            instanceField.setAccessible(true);
        }

        instanceField.set(instance, newValue);
    }

    protected void insertValueInPrivateStaticField(Class classWithField, String fieldName, Object newValue) throws NoSuchFieldException, IllegalAccessException {
        insertValueInPrivateField(classWithField, null, fieldName, newValue);
    }

    protected Object getValueInPrivateField(Class classWithField, Object instance, String fieldName) throws IllegalAccessException, NoSuchFieldException {
        Field instanceField = classWithField.getDeclaredField(fieldName);
        if (!instanceField.isAccessible()) {
            instanceField.setAccessible(true);
        }

        return instanceField.get(instance);
    }

    public static void setFinalStatic(Field field, Object newValue) throws NoSuchFieldException, IllegalAccessException {
        field.setAccessible(true);

        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

        field.set(null, newValue);
    }
}
