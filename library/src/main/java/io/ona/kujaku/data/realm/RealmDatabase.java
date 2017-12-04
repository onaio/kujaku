package io.ona.kujaku.data.realm;

import android.content.Context;
import android.support.annotation.NonNull;

import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by Jason Rogena - jrogena@ona.io on 11/20/17.
 */

public class RealmDatabase {
    private static final long VERSION = 1l;
    private static final String NAME = "kujaku.realm";
    private static RealmDatabase realmDatabase;
    private final Context context;

    public static RealmDatabase init(@NonNull Context context) {
        if (realmDatabase == null) {
            realmDatabase = new RealmDatabase(context);
        }

        return realmDatabase;
    }

    private RealmDatabase(Context context) {
        this.context = context;
        Realm.init(context.getApplicationContext());
        RealmConfiguration configuration = new RealmConfiguration.Builder()
                .name(NAME)
                .schemaVersion(VERSION)
                .build();
        Realm.setDefaultConfiguration(configuration);
    }
}
