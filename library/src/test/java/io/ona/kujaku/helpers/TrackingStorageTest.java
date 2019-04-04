package io.ona.kujaku.helpers;

import android.location.Location;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import io.ona.kujaku.BuildConfig;
import io.ona.kujaku.helpers.storage.TrackingStorage;

import static android.location.LocationManager.GPS_PROVIDER;

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
    public void baseStorageDeleteFolderTest() {
        TrackingStorage storage = new TrackingStorage();
        Assert.assertTrue(storage.createFile(".KujakuTracking/test", "test.file"));
        Assert.assertTrue(storage.directoryExists(".KujakuTracking/test"));
        Assert.assertTrue(storage.fileExists(".KujakuTracking/test", "test.file"));
        Assert.assertTrue(storage.deleteFile("test", false, true));
        Assert.assertFalse(storage.fileExists(".KujakuTracking/test", "test.file"));
        Assert.assertFalse(storage.directoryExists(".KujakuTracking/test"));
    }

    @Test
    public void baseStorageReadFileTest() {
        TrackingStorage storage = new TrackingStorage();
        String content = "This is a writing test";

        storage.writeToFile(".KujakuTracking/test", "test.file", content);
        String getContent = storage.readFile(".KujakuTracking/test", "test.file");

        Assert.assertEquals(content + '\n', getContent);
        Assert.assertTrue(storage.deleteFile("test", false, true));
    }

    @Test
    public void initLocationStorageTest() {
        TrackingStorage storage = new TrackingStorage();
        storage.initLocationStorage();

        Location location = new Location(GPS_PROVIDER);
        location.setLatitude(1.1);
        location.setLongitude(1.2);
        storage.writeLocation(location, 1);

        Assert.assertEquals(storage.getCurrentRecordedLocations().size(),1);
        storage.initLocationStorage();
        Assert.assertEquals(storage.getPreviousRecordedLocations().size(),1);
        Assert.assertEquals(storage.getCurrentRecordedLocations().size(),0);
    }
}
