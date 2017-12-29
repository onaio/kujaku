package io.ona.kujaku.shadows;

import android.content.Context;

import com.mapbox.mapboxsdk.offline.OfflineManager;

import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import io.realm.Realm;
import io.realm.internal.SharedRealm;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 28/12/2017.
 */

@Implements(Realm.class)
public class ShadowRealm {

    private void __constructor__() {
    }



    private void __constructor__(SharedRealm sharedRealm) {
        // Do nothing
    }

    @Implementation
    public static Realm getDefaultInstance() {
        /*Constructor<Realm> constructor = null;
        try {
            constructor = Realm.class.getDeclaredConstructor(SharedRealm.class);
            constructor.setAccessible(true);
            Realm realm = constructor.newInstance((SharedRealm) null);

            return realm;
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        return null;*/

        //PowerMockito.mockStatic(Realm.class);

        Realm mockRealm = Mockito.mock(Realm.class);

        Mockito.doNothing()
                .when(mockRealm)
                .beginTransaction();

        Mockito.doNothing()
                .when(mockRealm)
                .commitTransaction();

        return mockRealm;
    }
/*
    @Implementation
    public void beginTransaction() {
    }

    @Implementation
    public void commitTransaction() {
    }

    @Implementation
    public static synchronized void init(Context context) {
        // Do nothing
    }*/
}
