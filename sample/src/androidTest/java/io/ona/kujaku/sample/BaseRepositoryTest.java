package io.ona.kujaku.sample;

import android.content.Context;

import androidx.test.runner.AndroidJUnit4;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;

import io.ona.kujaku.sample.repository.KujakuRepository;

/**
 * @author Vincent Karuri
 */
@RunWith(AndroidJUnit4.class)
public abstract class BaseRepositoryTest {

    protected static Context context;
    protected static KujakuRepository mainRepository;

    @BeforeClass
    public static void bootStrap() {
        context = MyApplication.getInstance().getApplicationContext();
        mainRepository = MyApplication.getInstance().getRepository();
    }
}
