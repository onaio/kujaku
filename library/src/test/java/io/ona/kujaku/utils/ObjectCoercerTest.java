package io.ona.kujaku.utils;

import org.json.JSONObject;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 27/12/2017.
 */
public class ObjectCoercerTest {

    @Test
    public void coerceNumberObjectToDoublePrimitiveWhenGivenDoubleObjectShouldReturnDoublePrimitive() {
        double expectedDouble = 8.394;
        Double doubleObject = new Double(expectedDouble);

        assertEquals(expectedDouble, ObjectCoercer.coerceNumberObjectToDoublePrimitive(doubleObject), 0);
    }

    @Test
    public void coerceNumberObjectToDoublePrimitiveWhenGivenFloatObjectShouldReturnDoublePrimitive() {
        double expectedDouble = 8.394F;
        Float floatObject = new Float(expectedDouble);

        assertEquals(expectedDouble, ObjectCoercer.coerceNumberObjectToDoublePrimitive(floatObject), 0);
    }

    @Test
    public void coerceNumberObjectToDoublePrimitiveWhenGivenIntegerObjectShouldReturnDoublePrimitive() {
        double expectedDouble = 8;
        Integer integerObject = new Integer(8);

        assertEquals(expectedDouble, ObjectCoercer.coerceNumberObjectToDoublePrimitive(integerObject), 0);
    }

    @Test
    public void coerceNumberObjectToDoublePrimitiveWhenGivenLongObjectShouldReturnDoublePrimitive() {
        double expectedDouble = 9882398;
        Long longObject = new Long(9882398);

        assertEquals(expectedDouble, ObjectCoercer.coerceNumberObjectToDoublePrimitive(longObject), 0);
    }

    @Test
    public void coerceNumberObjectToDoublePrimitiveWhenGivenDoublePrimitiveShouldReturnDoublePrimitive() {
        double expectedDouble = 8923.23099;

        assertEquals(expectedDouble, ObjectCoercer.coerceNumberObjectToDoublePrimitive(expectedDouble), 0);
    }

    @Test
    public void coerceNumberObjectToDoublePrimitiveWhenGivenIntegerPrimitiveShouldReturnDoublePrimitive() {
        double expectedDouble = 898232;
        int intPrimtive = 898232;

        assertEquals(expectedDouble, ObjectCoercer.coerceNumberObjectToDoublePrimitive(intPrimtive), 0);
    }

    @Test
    public void coerceNumberObjectToDoublePrimitiveWhenGivenFloatPrimitiveShouldReturnDoublePrimitive() {
        double expectedDouble = 898.234f;
        float floatPrimitive = 898.234f;

        assertEquals(expectedDouble, ObjectCoercer.coerceNumberObjectToDoublePrimitive(floatPrimitive), 0);
    }

    @Test
    public void coerceNumberObjectToDoublePrimitiveWhenGivenLongPrimitiveShouldReturnDoublePrimitive() {
        double expectedDouble = 898090L;
        long longPrimitive = 898090L;

        assertEquals(expectedDouble, ObjectCoercer.coerceNumberObjectToDoublePrimitive(longPrimitive), 0);
    }

    @Test
    public void coerceNumberObjectToFloatPrimitiveWhenGivenDoubleObjectShouldReturnDoublePrimitive() {
        float expectedFloat = 8.394F;
        Double doubleObject = new Double(expectedFloat);

        assertEquals(expectedFloat, ObjectCoercer.coerceNumberObjectToFloatPrimitive(doubleObject), 0);
    }

    @Test
    public void coerceNumberObjectToFloatPrimitiveWhenGivenFloatObjectShouldReturnDoublePrimitive() {
        float expectedFloat = 8.394F;
        Float floatObject = new Float(expectedFloat);

        assertEquals(expectedFloat, ObjectCoercer.coerceNumberObjectToFloatPrimitive(floatObject), 0);
    }

    @Test
    public void coerceNumberObjectToFloatPrimitiveWhenGivenIntegerObjectShouldReturnDoublePrimitive() {
        float expectedFloat = 8;
        Integer integerObject = new Integer(8);

        assertEquals(expectedFloat, ObjectCoercer.coerceNumberObjectToFloatPrimitive(integerObject), 0);
    }

    @Test
    public void coerceNumberObjectToFloatPrimitiveWhenGivenLongObjectShouldReturnDoublePrimitive() {
        float expectedFloat = 9882398;
        Long longObject = new Long(9882398);

        assertEquals(expectedFloat, ObjectCoercer.coerceNumberObjectToFloatPrimitive(longObject), 0);
    }

    @Test
    public void coerceNumberObjectToFloatPrimitiveWhenGivenDoublePrimitiveShouldReturnDoublePrimitive() {
        float expectedFloat = 8923.23099F;
        double doublePrimitive = new Double(8923.23099F);

        assertEquals(expectedFloat, ObjectCoercer.coerceNumberObjectToFloatPrimitive(expectedFloat), 0);
    }

    @Test
    public void coerceNumberObjectToFloatPrimitiveWhenGivenIntegerPrimitiveShouldReturnDoublePrimitive() {
        float expectedFloat = 898232;
        int intPrimtive = 898232;

        assertEquals(expectedFloat, ObjectCoercer.coerceNumberObjectToFloatPrimitive(intPrimtive), 0);
    }

    @Test
    public void coerceNumberObjectToFloatPrimitiveWhenGivenFloatPrimitiveShouldReturnDoublePrimitive() {
        float expectedFloat = 898.234f;

        assertEquals(expectedFloat, ObjectCoercer.coerceNumberObjectToDoublePrimitive(expectedFloat), 0);
    }


    @Test
    public void coerceNumberObjectToFloatPrimitiveWhenGivenLongPrimitiveShouldReturnFloatPrimitive() {
        float expectedFloat = 898090L;
        long longPrimitive = 898090L;

        assertEquals(expectedFloat, ObjectCoercer.coerceNumberObjectToFloatPrimitive(longPrimitive), 0);
    }

    @Test
    public void coerceNumberObjectToFloatPrimitiveWhenGivenNonNumberObjectShouldReturnZero() {
        JSONObject jsonObject = new JSONObject();
        float expectedFloat = new Float(0);

        assertEquals(expectedFloat, ObjectCoercer.coerceNumberObjectToFloatPrimitive(jsonObject), 0);
    }

    @Test
    public void coerceNumberObjectToDoublePrimitiveWhenGivenNonNumberObjectShouldReturnZero() {
        JSONObject jsonObject = new JSONObject();
        double expectedDouble = new Double(0);

        assertEquals(expectedDouble, ObjectCoercer.coerceNumberObjectToDoublePrimitive(jsonObject), 0);
    }

}