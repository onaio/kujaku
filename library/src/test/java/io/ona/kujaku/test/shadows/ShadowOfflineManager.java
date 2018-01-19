package io.ona.kujaku.test.shadows;

import android.content.Context;

import com.mapbox.mapboxsdk.offline.OfflineManager;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 27/12/2017.
 */

@Implements(OfflineManager.class)
public class ShadowOfflineManager {

    @Implementation
    public static synchronized OfflineManager getInstance(Context context) {
        Constructor<OfflineManager> constructor = null;
        try {
            constructor = OfflineManager.class.getDeclaredConstructor(new Class[0]);
            constructor.setAccessible(true);
            OfflineManager offlineManager = constructor.newInstance();

            return offlineManager;
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        return null;
    }

}
