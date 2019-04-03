package io.ona.kujaku.helpers;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import io.ona.kujaku.BuildConfig;
import io.ona.kujaku.helpers.storage.TrackingStorage;

/**
 * Created by Emmanuel Otin - eo@novel-t.ch on 03/04/2019.
 */

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, manifest = Config.NONE)
public class TrackingStorageTest {

    @Test
    public void baseStorageDeleteFileTest() {
        TrackingStorage storage = new TrackingStorage();
        Assert.assertTrue(storage.createFile(".KujakuTracking/test", "test.file"));
        Assert.assertTrue(storage.fileExists(".KujakuTracking/test", "test.file"));
        Assert.assertTrue(storage.deleteFile("test/test.file", false, false));
        Assert.assertFalse(storage.fileExists(".KujakuTracking/test", "test.file"));
    }


    @Test
    public void baseStorageDeleteFolder() {
        TrackingStorage storage = new TrackingStorage();
        Assert.assertTrue(storage.createFile(".KujakuTracking/test", "test.file"));
        Assert.assertTrue(storage.directoryExists(".KujakuTracking/test"));
        Assert.assertTrue(storage.fileExists(".KujakuTracking/test", "test.file"));
        Assert.assertTrue(storage.deleteFile("test", false, true));
        Assert.assertFalse(storage.fileExists(".KujakuTracking/test", "test.file"));
        Assert.assertFalse(storage.directoryExists(".KujakuTracking/test"));
    }

    @Test
    public void baseStorageReadFile() {
        TrackingStorage storage = new TrackingStorage();
        String content = "This is a writing test";

        storage.writeToFile(".KujakuTracking/test", "test.file", content);
        String getContent = storage.readFile(".KujakuTracking/test", "test.file");

        Assert.assertEquals(content + '\n', getContent);
        Assert.assertTrue(storage.deleteFile("test", false, true));
    }
}
