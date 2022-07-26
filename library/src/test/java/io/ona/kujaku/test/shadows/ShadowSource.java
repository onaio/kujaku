package io.ona.kujaku.test.shadows;

import androidx.annotation.NonNull;

import com.mapbox.mapboxsdk.style.sources.Source;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

import java.util.UUID;


/**
 * Created by samuelgithengi on 10/7/19.
 */
@Implements(Source.class)
public class ShadowSource {

    @Implementation
    @NonNull
    public String getId() {
        return UUID.randomUUID().toString();
    }
}
