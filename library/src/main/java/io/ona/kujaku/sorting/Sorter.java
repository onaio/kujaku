package io.ona.kujaku.sorting;

import android.support.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;
import org.threeten.bp.DateTimeUtils;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.Date;

import io.ona.kujaku.adapters.InfoWindowObject;
import io.ona.kujaku.utils.config.SortFieldConfig;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 29/11/2017.
 */

public class Sorter {

    private static final String TAG = Sorter.class.getSimpleName();
    private ArrayList<InfoWindowObject> infoWindowObjects = new ArrayList<>();
    private InfoWindowObject[] infoWindowObjectsHelper;

    public Sorter(@NonNull ArrayList<InfoWindowObject> infoWindowObjects) {
        this.infoWindowObjects = infoWindowObjects;
        infoWindowObjectsHelper = new InfoWindowObject[infoWindowObjects.size()];
    }


    public ArrayList<InfoWindowObject> mergeSort(int low, int high, String fieldName, SortFieldConfig.FieldType fieldType) throws JSONException {
        // check if low is smaller than high, if not then the array is sorted
        if (low < high) {
            // Get the index of the element which is in the middle
            int middle = low + (high - low) / 2;
            // Sort the left side of the array
            mergeSort(low, middle, fieldName, fieldType);
            // Sort the right side of the array
            mergeSort(middle + 1, high, fieldName, fieldType);
            // Combine them both
            merge(low, middle, high, fieldName, fieldType);
        }

        return  infoWindowObjects;
    }

    private void merge(int low, int middle, int high, String fieldName, SortFieldConfig.FieldType fieldType) throws JSONException {

        // Copy both parts into the helper array
        for (int i = low; i <= high; i++) {
            infoWindowObjectsHelper[i] = infoWindowObjects.get(i);
        }

        int i = low;
        int j = middle + 1;
        int k = low;
        // Copy the smallest values from either the left or the right side back
        // to the original array
        while (i <= middle && j <= high) {
            if (compare(infoWindowObjectsHelper[i], infoWindowObjectsHelper[j], fieldName, fieldType) < 1) {
                infoWindowObjects.set(k, infoWindowObjectsHelper[i]);
                i++;
            } else {
                infoWindowObjects.set(k, infoWindowObjectsHelper[j]);
                j++;
            }

            k++;
        }
        // Copy the rest of the left side of the array into the target array
        while (i <= middle) {
            infoWindowObjects.set(k, infoWindowObjectsHelper[i]);
            k++;
            i++;
        }

        // Since we are sorting in-place any leftover elements from the right side
        // are already at the right position.

    }

    private int compare(@NonNull InfoWindowObject object1, @NonNull InfoWindowObject object2, @NonNull String fieldName, @NonNull SortFieldConfig.FieldType fieldType) throws JSONException {
        JSONObject jsonObject1 = object1.getJsonObject();
        JSONObject jsonObject2 = object2.getJsonObject();

        if (jsonObject1.has("properties") && jsonObject2.has("properties")) {

            JSONObject object1Properties = jsonObject1.getJSONObject("properties");
            JSONObject object2Properties = jsonObject2.getJSONObject("properties");

            if (object1Properties.has(fieldName) && object2Properties.has(fieldName)) {
                if (fieldType == SortFieldConfig.FieldType.DATE) {
                    Date date1 = getDateFromISO8601(object1Properties.getString(fieldName));
                    Date date2 = getDateFromISO8601(object2Properties.getString(fieldName));

                    return compare(date1, date2);
                } else if (fieldType == SortFieldConfig.FieldType.NUMBER) {
                    double number1 = object1Properties.getDouble(fieldName);
                    double number2 = object2Properties.getDouble(fieldName);

                    return compare(number1, number2);
                } else if (fieldType == SortFieldConfig.FieldType.STRING) {
                    String s1 = object1Properties.getString(fieldName);
                    String s2 = object2Properties.getString(fieldName);

                    return compare(s1, s2);
                }
            }
        }

        return 0;
    }

    private int compare(@NonNull Object object1, @NonNull Object object2) {
        if (object1 instanceof Date) {
            Date date1 = (Date) object1;
            Date date2 = (Date) object2;

            return date1.compareTo(date2);
        }

        // Test this
        if (object1 instanceof Double) {
            Double d1 = new Double((double) object1);
            Double d2 = new Double((double) object2);
            return d1.compareTo(d2);
        }

        // Also test this
        if (object1 instanceof  String) {
            String s1 = (String) object1;
            String s2 = (String) object2;

            return s1.compareTo(s2);
        }

        return 0;
    }

    private Date getDateFromISO8601(@NonNull String dateString) {
        LocalDateTime localDateTime = LocalDateTime.parse(dateString, DateTimeFormatter.ISO_DATE_TIME);
        return DateTimeUtils.toDate(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }
}
