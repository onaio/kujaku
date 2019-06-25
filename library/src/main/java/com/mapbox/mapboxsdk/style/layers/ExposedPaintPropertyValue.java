package com.mapbox.mapboxsdk.style.layers;

import android.support.annotation.NonNull;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 2019-05-20
 */

public class ExposedPaintPropertyValue<T> extends PaintPropertyValue<T> {

    public ExposedPaintPropertyValue(@NonNull String name, T value) {
        super(name, value);
    }
}
