package io.ona.kujaku.test.shadows;

import android.content.Context;

import org.mockito.Mockito;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

import io.ona.kujaku.data.realm.RealmDatabase;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 19/01/2018.
 */
@Implements(RealmDatabase.class)
public class ShadowRealmDatabase {

    @Implementation
    public static RealmDatabase init(Context context) {
        RealmDatabase realmDatabase = Mockito.mock(RealmDatabase.class);

        return realmDatabase;
    }

    @Implementation
    public boolean deletePendingOfflineMapDownloadsWithSimilarNames(String mapName) {
        // Do nothing
        return true;
    }
}
