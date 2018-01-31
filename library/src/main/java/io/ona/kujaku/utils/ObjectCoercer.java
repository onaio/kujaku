package io.ona.kujaku.utils;

/**
 * Provides helper methods to coerce and/or convert objects to certain types. This especially applies
 * for Extras where documentation on the required type might not be available or vague.
 * It also serves where the developer decides not to follow the documentation.
 *
 * Created by Ephraim Kigamba - ekigamba@ona.io on 27/12/2017.
 */
public class ObjectCoercer {

    /**
     *
     * Coerces an {@link Integer}, {@code int}, {@link Double}, {@code double}, {@link Long},
     * {@code long}, {@code float} && {@link Float} to {@code double}
     *
     * @param numberObject
     * @return
     */
    public static double coerceNumberObjectToDoublePrimitive(Object numberObject) {
        if (numberObject == null) {
            return 0;
        }

        if (numberObject instanceof Integer) {
            Integer number = (Integer) numberObject;
            return number.doubleValue();
        } else if (numberObject instanceof Float) {
            Float number = (Float) numberObject;
            return number.doubleValue();
        } else if (numberObject instanceof Long) {
            Long number = (Long) numberObject;
            return number.doubleValue();
        } else {
            double finalValue;
            try {
                finalValue = (double) numberObject;
            } catch (ClassCastException e) {
                finalValue = 0d;
            }

            return finalValue;
        }
    }

    /**
     *
     * Coerces an {@link Integer}, {@code int}, {@link Double}, {@code double}, {@link Long},
     * {@code long}, {@code float} && {@link Float} to {@code float}
     *
     * @param numberObject
     * @return
     */
    public static float coerceNumberObjectToFloatPrimitive(Object numberObject) {
        if (numberObject == null) {
            return 0;
        }

        if (numberObject instanceof Integer) {
            Integer number = (Integer) numberObject;
            return number.floatValue();
        } else if (numberObject instanceof Double) {
            Double number = (Double) numberObject;
            return number.floatValue();
        } else if (numberObject instanceof Long) {
            Long number = (Long) numberObject;
            return number.floatValue();
        } else {
            float finalValue;
            try {
                finalValue = (float) numberObject;
            } catch (ClassCastException e) {
                finalValue = 0f;
            }

            return finalValue;
        }
    }
}
