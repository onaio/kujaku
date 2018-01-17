package io.ona.kujaku.shadows.implementations;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 27/12/2017.
 */

public class RealmDbTestImplementation {

    public static LinkedHashMap<String, Object> db = new LinkedHashMap<>();

    public static boolean add(String primaryKeyValue, Object rowToStore) {
        return (db.put(primaryKeyValue, rowToStore) != null);
    }

    public static boolean remove(String primaryKeyValue) {
        return (db.remove(primaryKeyValue) != null);
    }

    public static Object get(String primaryKeyValue) {
        return db.get(primaryKeyValue);
    }

    public static void resetDb() {
        db.clear();
        db = new LinkedHashMap<>();
    }

    public static Object first() {
        Iterator<Object> iterator = db.values()
                .iterator();

        if (iterator.hasNext()) {
            return iterator.next();
        }

        return null;
    }
}
