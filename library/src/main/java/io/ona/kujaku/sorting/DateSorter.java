package io.ona.kujaku.sorting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import io.ona.kujaku.adapters.InfoWindowObject;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 24/11/2017.
 */

public class DateSorter {

    ArrayList<InfoWindowObject> infoWindowObjects = new ArrayList<>();


    //Todo: Change this sorting algorithms to merge-sort
    /*public LinkedHashMap<String, InfoWindowObject> sortAscending(HashMap<String, InfoWindowObject> items) {

    }

    public LinkedHashMap<String, InfoWindowObject> sortDescending(HashMap<String, InfoWindowObject> items) {}

    private void quicksort(int low, int high) {
        int i = low, j = high;
        // Get the pivot element from the middle of the list
        int pivot = numbers[low + (high-low)/2];

        // Divide into two lists
        while (i <= j) {
            // If the current value from the left list is smaller than the pivot
            // element then get the next element from the left list
            while (numbers[i] < pivot) {
                i++;
            }
            // If the current value from the right list is larger than the pivot
            // element then get the next element from the right list
            while (numbers[j] > pivot) {
                j--;
            }

            // If we have found a value in the left list which is larger than
            // the pivot element and if we have found a value in the right list
            // which is smaller than the pivot element then we exchange the
            // values.
            // As we are done we can increase i and j
            if (i <= j) {
                exchange(i, j);
                i++;
                j--;
            }
        }
        // Recursion
        if (low < j)
            quicksort(low, j);
        if (i < high)
            quicksort(i, high);
    }

    private void exchange(int i, int j) {
        int temp = numbers[i];
        numbers[i] = numbers[j];
        numbers[j] = temp;
    }*/
}
