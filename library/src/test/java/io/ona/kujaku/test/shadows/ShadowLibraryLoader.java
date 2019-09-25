package io.ona.kujaku.test.shadows;

import com.mapbox.mapboxsdk.LibraryLoader;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/**
 * Created by samuelgithengi on 9/25/19.
 */
@Implements(LibraryLoader.class)
public class ShadowLibraryLoader {

    @Implementation
    public static void load(){

    }
}
