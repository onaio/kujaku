package io.ona.kujaku.data.realm;

import android.content.Context;
import android.support.test.InstrumentationRegistry;

import org.junit.After;
import org.junit.Before;

import java.lang.reflect.Field;
import java.util.ArrayList;

import io.ona.kujaku.data.realm.objects.MapBoxOfflineQueueTask;
import io.realm.Realm;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 15/01/2018.
 */

public class RealmRelatedInstrumentedTest {

    protected ArrayList<MapBoxOfflineQueueTask> addedRecords = new ArrayList<>();
    protected Context context;

    @Before
    public void setUp() throws Exception {
        context = InstrumentationRegistry.getTargetContext();
    }

    @After
    public void tearDown() throws Exception {
        //Delete the added Realm records here and/or restore the previous records
        Realm.init(context);
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        for (MapBoxOfflineQueueTask mapBoxOfflineQueueTask : addedRecords) {
            if (mapBoxOfflineQueueTask.isValid()) {
                mapBoxOfflineQueueTask.deleteFromRealm();
            }
        }
        realm.commitTransaction();
    }

    protected void insertValueInPrivateField(Class classWithField, Object instance, String fieldName, Object newValue) throws IllegalAccessException, NoSuchFieldException {
        Field instanceField = classWithField.getDeclaredField(fieldName);
        if (!instanceField.isAccessible()) {
            instanceField.setAccessible(true);
        }

        instanceField.set(instance, newValue);
    }

    protected void insertValueInPrivateStaticField(Class classWithField, String fieldName, Object newValue) throws NoSuchFieldException, IllegalAccessException {
        insertValueInPrivateField(classWithField, null, fieldName, newValue);
    }

    protected Object getValueInPrivateField(Class classWithField, Object instance, String fieldName) throws IllegalAccessException, NoSuchFieldException {
        Field instanceField = classWithField.getDeclaredField(fieldName);
        if (!instanceField.isAccessible()) {
            instanceField.setAccessible(true);
        }

        return instanceField.get(instance);
    }


}
