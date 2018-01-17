package io.ona.kujaku.shadows;

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

    @Implementation
    public static Realm getDefaultInstance() {
        PowerMockito.mockStatic(Realm.class);

        Realm mockRealm = Mockito.mock(Realm.class);

        return mockRealm;
    }
}
