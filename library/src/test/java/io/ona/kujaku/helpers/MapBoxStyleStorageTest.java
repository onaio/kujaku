package io.ona.kujaku.helpers;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.File;

import io.ona.kujaku.BuildConfig;
import io.ona.kujaku.helpers.storage.MapBoxStyleStorage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 10/11/2017.
 */

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class MapBoxStyleStorageTest {

    @Test
    public void getStyleUrlShouldReturnAsset() {
        String assetUrl = "asset://sample_asset.json";
        MapBoxStyleStorage mapBoxStyleStorage = new MapBoxStyleStorage();
        String resultUrl = mapBoxStyleStorage.getStyleURL(assetUrl);

        assertEquals(assetUrl, resultUrl);
    }

    @Test
    public void getStyleUrlShouldReturnUrl() {
        String unsecureNetworkUrl = "http://mycompany.com/style.json";
        MapBoxStyleStorage mapBoxStyleStorage = new MapBoxStyleStorage();
        String resultUrl = mapBoxStyleStorage.getStyleURL(unsecureNetworkUrl);

        assertEquals(unsecureNetworkUrl, resultUrl);

        String secureNetworkUrl = "https://mycompany.com/style.json";
        resultUrl = mapBoxStyleStorage.getStyleURL(secureNetworkUrl);

        assertEquals(secureNetworkUrl, resultUrl);
    }

    @Test
    public void getStyleUrlShouldReturnLocalFile() {
        String localFileUrl = "file:///sdcard/SampleFolder/style.json";
        MapBoxStyleStorage mapBoxStyleStorage = new MapBoxStyleStorage();
        String resultUrl = mapBoxStyleStorage.getStyleURL(localFileUrl);

        assertEquals(localFileUrl, resultUrl);
    }

    @Test
    public void getStyleUrlShouldReturnNewLocalFile() {
        String mapboxStyle = "{ 'version': 8, 'name': 'kujaku-map', 'metadata': {}, }";
        MapBoxStyleStorage mapBoxStyleStorage = new MapBoxStyleStorage();
        String resultUrl = mapBoxStyleStorage.getStyleURL(mapboxStyle);

        File createdFile = new File(resultUrl.replace("file://", ""));

        assertTrue(resultUrl.startsWith("file://"));
        assertTrue(createdFile.exists());
    }

    @Test
    public void deleteFileShouldReturnTrue() {
        String mapboxStyle = "{ 'version': 8, 'name': 'kujaku-map', 'metadata': {}, }";
        MapBoxStyleStorage mapBoxStyleStorage = new MapBoxStyleStorage();
        String resultUrl = mapBoxStyleStorage.getStyleURL(mapboxStyle);

        File file = new File(resultUrl.replace("file://", ""));

        assertTrue(file.exists());

        boolean isDeleted = mapBoxStyleStorage.deleteFile(resultUrl.replace("file://", ""));
        assertTrue(isDeleted);
        assertFalse(file.exists());
    }

    @Test
    public void deleteFileShouldReturnFail() {
        String mapboxStyle = "{ 'version': 8, 'name': 'kujaku-map', 'metadata': {}, }";
        MapBoxStyleStorage mapBoxStyleStorage = new MapBoxStyleStorage();
        String resultUrl = mapBoxStyleStorage.getStyleURL(mapboxStyle);

        File file = new File(resultUrl.replace("file://", ""));

        assertTrue(file.exists());

        boolean isDeleted = mapBoxStyleStorage.deleteFile(resultUrl);
        assertFalse(isDeleted);
        assertTrue(file.exists());
    }
}
