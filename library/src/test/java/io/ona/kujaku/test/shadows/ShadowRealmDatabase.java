package io.ona.kujaku.test.shadows;

import android.content.Context;
import android.support.annotation.NonNull;

import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

import io.ona.kujaku.data.realm.RealmDatabase;
import io.realm.Realm;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 19/01/2018.
 */
@Implements(RealmDatabase.class)
public class ShadowRealmDatabase {

    @Implementation
    public static RealmDatabase init(Context context) {
        PowerMockito.mockStatic(RealmDatabase.class);

        RealmDatabase realmDatabase = Mockito.mock(RealmDatabase.class);

        return realmDatabase;
    }

    @Implementation
    public boolean deletePendingOfflineMapDownloadsWithSimilarNames(String mapName) {
        // Do nothing
        return true;
    }
}
