package io.ona.kujaku.comparators;

import android.support.annotation.NonNull;

import com.mapbox.geojson.Feature;

import org.threeten.bp.LocalDateTime;
import org.threeten.bp.format.DateTimeFormatter;

import java.util.Comparator;

import io.ona.kujaku.layers.ArrowLineLayer;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 11/02/2019
 */

public class ArrowLineSortConfigComparator implements Comparator<Feature> {

    private ArrowLineLayer.SortConfig sortConfig;

    public ArrowLineSortConfigComparator(@NonNull ArrowLineLayer.SortConfig sortConfig) {
        this.sortConfig = sortConfig;
    }

    @Override
    public int compare(Feature feature1, Feature feature2) {
        String sortProperty = sortConfig.getSortProperty();
        int compareResult = 0;

        if (sortConfig.getPropertyType() == ArrowLineLayer.SortConfig.PropertyType.NUMBER) {
            Number number1 = feature1.getNumberProperty(sortProperty);
            Number number2 = feature2.getNumberProperty(sortProperty);

            compareResult = Double.compare(number1.doubleValue(), number2.doubleValue());
        } else if (sortConfig.getPropertyType() == ArrowLineLayer.SortConfig.PropertyType.STRING) {
            String value1 = feature1.getStringProperty(sortProperty);
            String value2 = feature2.getStringProperty(sortProperty);

            compareResult = value1.compareTo(value2);
        } else if (sortConfig.getPropertyType() == ArrowLineLayer.SortConfig.PropertyType.DATE_TIME) {
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(sortConfig.getDateTimeFormat());

            LocalDateTime localDateTime1 = LocalDateTime.parse(feature1.getStringProperty(sortProperty), dateTimeFormatter);
            LocalDateTime localDateTime2 = LocalDateTime.parse(feature2.getStringProperty(sortProperty), dateTimeFormatter);

            compareResult = localDateTime1.compareTo(localDateTime2);
        }

        if (sortConfig.getSortOrder() == ArrowLineLayer.SortConfig.SortOrder.DESC) {
            compareResult *= -1;
        }

        return compareResult;
    }
}
