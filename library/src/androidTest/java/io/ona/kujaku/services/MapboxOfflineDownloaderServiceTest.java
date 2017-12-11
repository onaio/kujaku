package io.ona.kujaku.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ServiceTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.v4.content.LocalBroadcastManager;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

import java.util.UUID;
import java.util.concurrent.TimeoutException;

import io.realm.Realm;
import utils.Constants;

/**
 *
 *
 * Created by Ephraim Kigamba - ekigamba@ona.io on 05/12/2017.
 */
@RunWith(AndroidJUnit4.class)
public class MapboxOfflineDownloaderServiceTest {

    private String mapName = UUID.randomUUID().toString();

    private Context context;

    @Before
    public void setUp() {
        context = InstrumentationRegistry.getTargetContext();
        Realm.init(context);
    }

    @Rule
    public final ServiceTestRule serviceTestRule = new ServiceTestRule();

    @Test
    public void onStartCommandCalledWhenStartServiceCalled() {
        Intent serviceIntent = createMapboxOfflineDownloaderServiceIntent();
        serviceIntent = createSampleDeleteIntent(serviceIntent);

        MapboxOfflineDownloaderService mapboxOfflineDownloaderService = new MapboxOfflineDownloaderService();
        context.startService(serviceIntent);

        mapboxOfflineDownloaderService.onStartCommand(serviceIntent, 0, 0);

        assertTrue(MapboxOfflineDownloaderService.isOnStartCommandCalled());
    }

    @Test
    public void performOfflineMapTaskCalled() {
        Intent serviceIntent = createMapboxOfflineDownloaderServiceIntent();

        MapboxOfflineDownloaderService mapboxOfflineDownloaderService = new MapboxOfflineDownloaderService();
        context.startService(serviceIntent);

        assertTrue(MapboxOfflineDownloaderService.isPerformNextTaskCalled());
    }

    @Test
    public void performNextTaskCalled() {}

    @Test
    public void observeOfflineRegionCalledOnDownloadDownloadedMap() {}

    @Test
    public void persistCompletedStatusCalledOnMapDownloadCalled() {}

    @Test
    public void startServiceWithDeleteActionShouldReturnError() throws TimeoutException {
        // Create the service Intent.
        Intent serviceIntent = createMapboxOfflineDownloaderServiceIntent();

        LocalBroadcastManager.getInstance(InstrumentationRegistry.getTargetContext())
                .registerReceiver(new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {

                    }
                }, new IntentFilter(Constants.INTENT_ACTION_MAP_DOWNLOAD_SERVICE_STATUS_UPDATES));

        // Data can be passed to the service via the Intent.
        serviceIntent = createSampleDeleteIntent(serviceIntent);

        MapboxOfflineDownloaderService mapboxOfflineDownloaderService = new MapboxOfflineDownloaderService();

        context.startService(serviceIntent);
    }

    private Intent createMapboxOfflineDownloaderServiceIntent() {
        Intent serviceIntent =
                new Intent(context,
                        MapboxOfflineDownloaderService.class);

        return serviceIntent;
    }

    private Intent createSampleDeleteIntent(Intent serviceIntent) {

        // Data can be passed to the service via the Intent.
        serviceIntent.putExtra(Constants.PARCELABLE_KEY_SERVICE_ACTION, Constants.SERVICE_ACTION.DELETE_MAP);
        serviceIntent.putExtra(Constants.PARCELABLE_KEY_MAP_UNIQUE_NAME, mapName);

        return serviceIntent;
    }

}