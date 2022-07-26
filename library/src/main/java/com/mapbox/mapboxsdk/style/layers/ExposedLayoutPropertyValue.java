package com.mapbox.mapboxsdk.style.layers;

import androidx.annotation.NonNull;

import com.mapbox.mapboxsdk.style.layers.LayoutPropertyValue;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 2019-05-20
 */

public class ExposedLayoutPropertyValue<T> extends LayoutPropertyValue<T> {

    public ExposedLayoutPropertyValue(@NonNull String name, T value) {
        super(name, value);
    }
}
