package io.ona.kujaku;

import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

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
