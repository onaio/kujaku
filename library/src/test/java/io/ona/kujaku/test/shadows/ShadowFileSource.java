package io.ona.kujaku.test.shadows;

import android.content.Context;
import com.mapbox.mapboxsdk.storage.FileSource;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 28/12/2017.
 */

@Implements(FileSource.class)
public class ShadowFileSource {

    @Implementation
    public static synchronized FileSource getInstance(Context context) {
        return null;
    }
}
