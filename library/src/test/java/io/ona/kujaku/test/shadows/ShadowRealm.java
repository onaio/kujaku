package io.ona.kujaku.test.shadows;

import org.mockito.Mockito;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

import io.realm.Realm;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 28/12/2017.
 */

@Implements(Realm.class)
public class ShadowRealm {

    @Implementation
    public static Realm getDefaultInstance() {
        Realm mockRealm = Mockito.mock(Realm.class);

        return mockRealm;
    }
}
