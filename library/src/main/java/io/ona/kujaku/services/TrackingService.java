package io.ona.kujaku.services;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;

import android.content.ServiceConnection;
import android.graphics.Color;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import io.ona.kujaku.R;
import io.ona.kujaku.helpers.storage.TrackingStorage;
import io.ona.kujaku.listeners.TrackingServiceListener;
import io.ona.kujaku.location.KujakuLocation;
import io.ona.kujaku.services.options.TrackingServiceOptions;
import io.ona.kujaku.services.options.TrackingServiceSaveBatteryOptions;


/**
 * Tracking Service used in Foreground to avoid any memory cleaning from Android
 * /!\ The application need to be set on Mode "No Battery optimization" in any case
 * <p>
 * Created by Emmanuel Otin - eo@novel-t.ch 03/07/19.
 */
public class TrackingService extends Service {
    private final static String TAG = TrackingService.class.getSimpleName();
    private final static String PARTIAL_WAKE_LOCK_TAG = "TrackingService:PartialWakeLock";

    private final static String ACTIVITY_EXTRA_NAME = "launch_activity_class";
    private final static String OPTIONS_EXTRA_NAME = "tracking_service_options";

    // Wait time in milli sec to wait for the service thread to exit
    private final static long WAIT_TIME_SERVICE_THREAD = 400;

    private static volatile CountDownLatch serviceThreadRunningLatch;

    // Service status
    private static volatile int serviceStatus = TrackingServiceStatus.STOPPED;

    // Location Manager
    private volatile LocationManager locationManager;

    private volatile Handler gpsHandler;
    private Handler uiHandler;

    private volatile KujakuLocation lastRecordedKujakuLocation;
    private volatile KujakuLocation pendingRecordingKujakuLocation;
    private volatile KujakuLocation lastBestKujakuLocation;

    // Store the recorded locations
    private List<KujakuLocation> recordedKujakuLocations;

    private volatile KujakuLocation firstKujakuLocationReceived = null;

    // Tracks Options parameters
    private TrackingServiceOptions trackingServiceOptions;

    // Use for notification
    private PendingIntent notificationPendingIntent;

    // Binder given to clients
    private final IBinder binder = new LocalBinder();

    // Listener to register to some Tracking functions
    private TrackingServiceListener trackingServiceListener = null;

    // To prevent device from sleeping
    private PowerManager.WakeLock wakeLock;
    private PowerManager powerManager;

    //Physical Storage
    private TrackingStorage storage;

    public static class TrackingServiceStatus {
        // To record the service status
        final public static int STOPPED = 0;
        final public static int STOPPED_GPS = 1;
        final public static int WAITING_FIRST_FIX = 2;
        final public static int WAITING_FIRST_RECORD = 3;
        final public static int RUNNING = 4;
    }


    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(TAG, "Initializing tracking service.");

        powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        storage = new TrackingStorage();

        recordedKujakuLocations = new ArrayList<>();
    }

    /**
     * Initialize service
     */
    private void initialize() {
        // Variables
        lastRecordedKujakuLocation = null;
        lastBestKujakuLocation = null;
        pendingRecordingKujakuLocation = null;
        firstKujakuLocationReceived = null;

        // Storage
        storage.initKujakuLocationStorage();
    }

    /**
     * Get Tracking Service Options from the intent
     *
     * @param intent
     */
    private void getTrackingServiceOptions(Intent intent) {
        // Get parameters for Parcelable TrackingServiceOptions
        TrackingServiceOptions options = intent.getParcelableExtra(OPTIONS_EXTRA_NAME);
        if (options == null) {
            trackingServiceOptions = new TrackingServiceSaveBatteryOptions();
        } else {
            trackingServiceOptions = options;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.d(TAG, "Main ThreadID: " + android.os.Process.myTid());

        createNotificationPendingIntent(intent);

        getTrackingServiceOptions(intent);

        // Make sure we start clean. The service instance still exists after
        // stopping and so the variable are not re-initialized.
        initialize();

        // Start the service in foreground to avoid as much as
        // possible that the service is killed by OS.
        startServiceForeground();

        Log.d(TAG, "Min distance gps setting: " + trackingServiceOptions.getMinDistance());
        Log.d(TAG, "Tolerance interval distance setting: " + trackingServiceOptions.getToleranceIntervalDistance());
        Log.d(TAG, "Tag for location: " + trackingServiceOptions.getTag());

        switch (TrackingService.serviceStatus) {
            case TrackingServiceStatus.RUNNING:
            case TrackingServiceStatus.WAITING_FIRST_FIX:
            case TrackingServiceStatus.WAITING_FIRST_RECORD:
                Log.w(TAG, "Service thread is already running.");
                return Service.START_STICKY;

            default:
                Log.d(TAG, "Service starting.");

                // Prevent the device from sleeping
                if (!this.getWakeLock().isHeld()) {
                    this.getWakeLock().acquire();
                }

                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    Log.i(TAG, "Start tracking service thread.");

                    try {
                        // Set the latch that will be unset when the service thread exits
                        serviceThreadRunningLatch = new CountDownLatch(1);
                        // Start the thread processing notifications
                        serviceThread.start();

                    } catch (IllegalThreadStateException e) {

                        Log.e(TAG, "Failed to start service thread.", e);
                        setServiceStatus(TrackingServiceStatus.STOPPED);

                        // Stop the service as there is something really
                        // wrong
                        stopSelf();

                        return Service.START_NOT_STICKY;
                    }

                    setServiceStatus(TrackingServiceStatus.WAITING_FIRST_FIX);

                    Log.i(TAG, "Tracking service running.");

                    return Service.START_STICKY;

                } else {

                    setServiceStatus(TrackingServiceStatus.STOPPED_GPS);

                    // Creation not successful because either GPS is not enabled or
                    Log.w(TAG,
                            "Abort service when starting because GPS not enabled.");

                    // Stop the service
                    stopSelf();

                    return Service.START_NOT_STICKY;
                }
        }
    }

    @Override
    public void onDestroy() {

        Log.d(TAG, "Tracking service stopping.");

        try {
            // Remove listeners
            if (locationManager != null && locationListener != null) {
                Log.d(TAG, "Remove location manager updates.");
                locationManager.removeUpdates(locationListener);
            }

            // Stop the service thread by posting a runnable in the loop.
            if (gpsHandler != null) {
                Log.d(TAG, "Quitting looper");
                gpsHandler.post(stopServiceThread);
            }

            if (wakeLock != null && wakeLock.isHeld()) {
                Log.d(TAG, "Release wake lock.");
                wakeLock.release();
            }

        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Failed to stop service properly.", e);
        }

        Log.d(TAG, "Wait for the threads to exit.");

        // Wait for the threads to die. This is required to implement an async stop. See Utils.
        try {
            if (serviceThreadRunningLatch != null) {
                if (!serviceThreadRunningLatch.await(WAIT_TIME_SERVICE_THREAD, TimeUnit.MILLISECONDS)) {
                    Log.w(TAG, "Time out waiting for service thread to exit.");
                }
                Log.d(TAG, "Service thread has stopped.");
            }

        } catch (InterruptedException ie) {
            Log.e(TAG, "Main application thread was interrupted.", ie);
        }

        setServiceStatus(TrackingServiceStatus.STOPPED);

        super.onDestroy();

        Log.i(TAG, "Tracking service stopped.");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    /**
     * @param intent
     */
    private void createNotificationPendingIntent(Intent intent) {
        Class<?> cls = getActivityClassFromCanonicalName(intent);

        // The stack builder object will contain an artificial back stack for the started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        // ALL THIS is required to start the service in the foreground!
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);

        // Adds the back stack for the Intent (but not the Intent itself)
        if (cls != null) {
            // Creates an explicit intent for an Activity
            Intent startActivityIntent = new Intent(this, cls);

            stackBuilder.addParentStack(cls);
            // Adds the Intent that starts the Activity to the top of the stack
            stackBuilder.addNextIntent(startActivityIntent);
        }

        notificationPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /**
     * Get the launching Activity class name from the intent
     *
     * @param intent
     * @return
     */
    private Class<?> getActivityClassFromCanonicalName(Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras == null) {
            return null;
        }

        String classname = extras.getString(ACTIVITY_EXTRA_NAME);
        Class<?> cls = null;
        try {
            cls = Class.forName(classname);
        } catch (ClassNotFoundException ex) {
            Log.e(TAG, "Launch activity class not found", ex);
        }

        return cls;
    }

    /***
     * Register LocationManager request locations updates
     */
    @SuppressWarnings({"MissingPermission"})
    private void registerLocationListener() {
        Log.d(TAG, "Register location update listener.");
        // https://stackoverflow.com/questions/33022662/android-locationmanager-vs-google-play-services
        // FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                trackingServiceOptions.getMinTime(),
                trackingServiceOptions.getGpsMinDistance(),
                locationListener, Looper.myLooper());
    }

    /**
     * Set TrackingService Status
     *
     * @param status
     */
    private void setServiceStatus(int status) {
        serviceStatus = status;
    }

    /**
     * Process a new kujakuLocation
     *
     * @param kujakuLocation
     */
    private synchronized void processLocation(KujakuLocation kujakuLocation) {
        double distanceBetweenLocations;

        if (lastRecordedKujakuLocation == null) {
            Log.d(TAG, "First kujakuLocation since service started or GPS was lost");

            // Create pending
            overwritePendingLocation(kujakuLocation);

            // Create a fake last recorded kujakuLocation to have a fix
            // reference point to compare new tracks to.
            // We cannot compare new tracks always to the pending
            // one using toleranceInterval as the
            // pending kujakuLocation can be updated for ever theoretically
            // if the accuracy keeps being better
            lastRecordedKujakuLocation = kujakuLocation;

            return;
        }

        // lastRecordedKujakuLocation is not null
        distanceBetweenLocations = kujakuLocation.distanceTo(lastRecordedKujakuLocation);

        Log.d(TAG, "Distance to last recorded kujakuLocation (m) = " + distanceBetweenLocations);

        if ((distanceBetweenLocations < (trackingServiceOptions.getMinDistance() - trackingServiceOptions.getToleranceIntervalDistance()))) {
            Log.d(TAG, "New kujakuLocation too close from last recorded kujakuLocation.");
            return;
        }

        if (distanceBetweenLocations < (trackingServiceOptions.getMinDistance() + trackingServiceOptions.getToleranceIntervalDistance())) {
            Log.d(TAG, "New kujakuLocation within distance tolerance from last recorded kujakuLocation.");

            // Check if there is a pending kujakuLocation
            if (pendingRecordingKujakuLocation == null) {
                Log.d(TAG, "No pending kujakuLocation.");
                overwritePendingLocation(kujakuLocation);
                return;
            } else {
                if (selectLocation(kujakuLocation, pendingRecordingKujakuLocation)) {
                    overwritePendingLocation(kujakuLocation);
                    Log.d(TAG, "New kujakuLocation is better than pending kujakuLocation.");

                    return;

                } else {
                    Log.d(TAG,
                            "New kujakuLocation has worse accuracy than pending one.");
                    return;

                } // end test on better kujakuLocation

            } // end test if pending

        } else {
            Log.d(TAG, "New kujakuLocation out of distance tolerance.");
            if (pendingRecordingKujakuLocation == null) {
                // As this kujakuLocation is out of tolerance, the next one will also be.
                // So we record it now. We cannot wait for better accuracy.
                overwritePendingLocation(kujakuLocation);
                recordPendingLocation();
                return;

            } else {
                // Record pending which becomes the last kujakuLocation
                recordPendingLocation();
                // Recursive call as we have a new lastRecordedKujakuLocation
                processLocation(kujakuLocation);
                return;
            }

        } // End test new kujakuLocation within time tolerance
    }

    /**
     * Compare two track accuracy. Return true if new better than old
     *
     * @param newKujakuLocation
     * @param oldKujakuLocation
     * @return
     */
    private boolean selectLocation(KujakuLocation newKujakuLocation, KujakuLocation oldKujakuLocation) {
        double newDist = newKujakuLocation.distanceTo(lastRecordedKujakuLocation);
        double oldDist = oldKujakuLocation.distanceTo(lastRecordedKujakuLocation);

        // Old track real, new track real => keep best accuracy
        if (newKujakuLocation.getAccuracy() < oldKujakuLocation.getAccuracy()) {
            return true;
        } else {
            if (newKujakuLocation.getAccuracy() == oldKujakuLocation.getAccuracy()) {
                // Check if closer to targeted distance
                return newDist < oldDist;
            } else {
                return false;
            }
        }
    }

    /**
     * Overwrite pending with a new kujakuLocation
     *
     * @param kujakuLocation
     */
    private void overwritePendingLocation(KujakuLocation kujakuLocation) {
        Log.d(TAG, "Overwrite pending kujakuLocation.");

        pendingRecordingKujakuLocation = kujakuLocation;

        // Remember the last real Location. pendingRecordingKujakuLocation can be null but
        // lastBestKujakuLocation cannot be.
        // Each pending kujakuLocation will be recorded at some point and become the new
        // reference to compare to
        lastBestKujakuLocation = kujakuLocation;
    }

    /**
     * Register Location
     */
    private synchronized void recordPendingLocation() {
        if (pendingRecordingKujakuLocation != null) {
            Log.d(TAG, "Record pending location.");

            // We store the location in our list
            recordedKujakuLocations.add(pendingRecordingKujakuLocation);

            informNewTrackReceivedListener(pendingRecordingKujakuLocation);
            informCloseToDepartureLocationListener(pendingRecordingKujakuLocation);

            storage.writeLocation(pendingRecordingKujakuLocation, recordedKujakuLocations.size());
        } else {
            Log.d(TAG, "Service is not recording.");
        }

        lastRecordedKujakuLocation = pendingRecordingKujakuLocation;
        pendingRecordingKujakuLocation = null;
    }

    /**
     * Volatile because different methods are called from the main thread and serviceThread
     */
    private volatile LocationListener locationListener = new LocationListener() {
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

            switch (status) {
                case LocationProvider.AVAILABLE:
                    Log.d(TAG, "GPS available.");
                    break;

                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    Log.d(TAG, "GPS temporary unavailable.");
                    break;

                case LocationProvider.OUT_OF_SERVICE:
                    Log.d(TAG, "GPS out of service.");
                    break;

                default:
                    break;
            }
        }

        @Override
        public void onProviderEnabled(String provider) {
            // See GPS Broadcast receiver
        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.i(TAG, "GPS Provider has been disabled.");
            Log.i(TAG, "Stopping tracking service.");
            // Stop the service
            TrackingService.this.stopSelf();
        }

        @Override
        public void onLocationChanged(Location location) {
            Log.d(TAG, "GPS position received");
            Log.d(TAG, "GPS Location ThreadID: " + android.os.Process.myTid());

            // This should never happen, but just in case (we really don't
            // want the service to crash):
            if (location == null) {
                Log.d(TAG, "No location available.");
                return;
            }

            KujakuLocation kujakuLocation = new KujakuLocation(location, trackingServiceOptions.getTag());

            // First Location received
            informFirstLocationReceivedListener(kujakuLocation);

            // Ignore if the accuracy is too bad:
            if (kujakuLocation.getAccuracy() > trackingServiceOptions.getMinAccuracy()) {
                Log.d(TAG, "Track ignored because of accuracy.");
                return;
            }

            if (lastBestKujakuLocation == null) {
                lastBestKujakuLocation = kujakuLocation;
            } else {
                if (kujakuLocation.getAccuracy() <= lastBestKujakuLocation.getAccuracy()) {
                    // Remember this location is better than the previous one.
                    // lastBestKujakuLocation is rebased in overwritePendingLocation but an
                    // ignored location can have better accuracy
                    // even if not recorded

                    Log.d(TAG,
                            "New location is used as latest best accuracy location.");
                    lastBestKujakuLocation = kujakuLocation;
                }
            }

            // process location received from GPS
            processLocation(kujakuLocation);
        }
    };

    /**
     * Service thread with looper to handle GPS notification Thread to process all notifications
     */
    private volatile Thread serviceThread = new Thread("TrackingService") {
        public void run() {
            Log.d(TAG, "Tracking thread started.");
            // preparing a looper on current thread
            // the current thread is being detected implicitly
            Looper.prepare();

            Log.d(TAG, "Register GPS status listener.");

            // No need to do it in thread as the listener only logs
            // which is fast
            // locationManager.addGpsStatusListener(gpsListener);

            // now, the handler will automatically bind to the
            // Looper that is attached to the current thread
            // You don't need to specify the Looper explicitly
            gpsHandler = new Handler();

            // Ui Handler
            uiHandler = new Handler(Looper.getMainLooper());

            // Register the Location listener
            registerLocationListener();

            // After the following line the thread will start
            // running the message loop and will not normally
            // exit the loop unless a problem happens or you
            // quit() the looper (see below)
            Looper.loop();

            Log.d(TAG, "Exiting looper.");

            if (pendingRecordingKujakuLocation != null) {

                Log.d(TAG, "Record last pending location.");
                recordPendingLocation();
            }

            // Mark to notify thread is exiting
            serviceThreadRunningLatch.countDown();
        }
    };

    /**
     * Call by service Thread to stop itself
     */
    private Runnable stopServiceThread = new Runnable() {
        @Override
        public void run() {
            Looper looper = Looper.myLooper();
            if (looper != null) {
                looper.quit();
            } else {
                Log.e(TAG, "Cannot stop service thread.");
            }
        }
    };

    /**
     * Get Wake Lock
     *
     * @return
     */
    private PowerManager.WakeLock getWakeLock() {
        if (this.wakeLock == null) {
            // Create the wakeLock to prevent the device from sleeping
            this.wakeLock = this.powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                    PARTIAL_WAKE_LOCK_TAG);
        }

        return this.wakeLock;
    }

    /**
     * To ensure the service is not killed too easily
     */
    protected void startServiceForeground() {
        String channel = "";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channel = createChannel();
        }

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, channel)
                .setSmallIcon(android.R.drawable.ic_menu_mylocation)
                .setContentTitle(String.format("%1$s %2$s", getString(R.string.app_name), getString(R.string.tracking_service_name)))
                .setOngoing(true)
                .setWhen(System.currentTimeMillis());

        Notification notification = mBuilder
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(notificationPendingIntent)
                .build();

        startForeground(1, notification);
    }

    /**
     * Create new channel for the Tracking Service
     *
     * @return
     */
    @NonNull
    @TargetApi(26)
    private synchronized String createChannel() {
        String channelId = getString(R.string.tracking_service_channel_id);
        String channelName = getString(R.string.tracking_service_channel_name);

        NotificationManager mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel mChannel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH);

        mChannel.enableLights(true);
        mChannel.setLightColor(Color.BLUE);
        mChannel.enableVibration(true);
        if (mNotificationManager != null) {
            mNotificationManager.createNotificationChannel(mChannel);
        } else {
            stopSelf();
        }
        return channelId;
    }

    /** TrackingService Listener methods **/

    /**
     * Register listener
     *
     * @param listener
     */
    public void registerTrackingServiceListener(@NonNull TrackingServiceListener listener) {
        this.trackingServiceListener = listener;
    }

    /**
     * Unregister listener
     */
    public void unregisterTrackingServiceListener() {
        this.trackingServiceListener = null;
    }

    /**
     * Inform listener that first kujakuLocation is received
     *
     * @param kujakuLocation
     */
    private void informFirstLocationReceivedListener(KujakuLocation kujakuLocation) {
        if (this.trackingServiceListener != null && this.firstKujakuLocationReceived == null) {
            uiHandler.post(new Runnable() {
                @Override
                public void run() {
                    trackingServiceListener.onFirstLocationReceived(kujakuLocation);
                }
            });
            this.firstKujakuLocationReceived = kujakuLocation;
        }
        this.setServiceStatus(TrackingServiceStatus.WAITING_FIRST_RECORD);
    }

    /**
     * Inform listener that a new track is registered
     *
     * @param kujakuLocation
     */
    private void informNewTrackReceivedListener(KujakuLocation kujakuLocation) {
        if (this.trackingServiceListener != null && kujakuLocation != null) {
            uiHandler.post(new Runnable() {
                @Override
                public void run() {
                    trackingServiceListener.onNewLocationReceived(kujakuLocation);
                }
            });
        }
        this.setServiceStatus(TrackingServiceStatus.RUNNING);
    }

    /**
     * Inform listener that kujakuLocation registered is close to the departure kujakuLocation
     *
     * @param kujakuLocation
     */
    private void informCloseToDepartureLocationListener(KujakuLocation kujakuLocation) {
        if (this.trackingServiceListener != null && kujakuLocation != null) {
            if (this.getNumberOfLocationsRecorded() == 1) {
                return;
            }

            Location departure = this.getFirstLocationRecorded();

            if (departure != null && departure.distanceTo(kujakuLocation) <= trackingServiceOptions.getDistanceFromDeparture()) {
                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        trackingServiceListener.onCloseToDepartureLocation(kujakuLocation);
                    }
                });
            }
        }
    }

    /**
     * Get First Location recorded
     *
     * @return
     */
    private Location getFirstLocationRecorded() {
        if (this.recordedKujakuLocations != null && this.recordedKujakuLocations.size() > 0) {
            return this.recordedKujakuLocations.get(0);
        }

        return null;
    }

    /**
     * Get number of locations recorded
     *
     * @return
     */
    private int getNumberOfLocationsRecorded() {
        if (this.recordedKujakuLocations != null) {
            return this.recordedKujakuLocations.size();
        }

        return 0;
    }


    /**** Public methods that can be used after bind ***/

    /**
     * Return all locations recorded
     *
     * @return
     */
    public List<KujakuLocation> getRecordedKujakuLocations() {
        return this.recordedKujakuLocations;
    }

    /**
     * Set Tag for next points
     *
     * @param tag
     */
    public void setTag(long tag) {
        this.trackingServiceOptions.setTag(tag);
    }

    /**
     * Record pending Location
     *
     * @param tag
     */
    public void takeLocation(long tag) {
        if (pendingRecordingKujakuLocation != null) {
            KujakuLocation pendingLocation = new KujakuLocation(pendingRecordingKujakuLocation, pendingRecordingKujakuLocation.getTag());

            if (tag != -1) {
                pendingRecordingKujakuLocation.setTag(tag);
            }
            recordPendingLocation();
            pendingRecordingKujakuLocation = pendingLocation;
        }
    }

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        public TrackingService getService() {
            // Return this instance of LocalService so clients can call public methods
            return TrackingService.this;
        }
    }

    /*** static methods ***/

    /**
     * Start Service and bind it
     *
     * @param context
     * @param cls
     * @param connection
     * @param options
     */
    public static void startAndBindService(Context context, Class<?> cls, ServiceConnection connection, TrackingServiceOptions options) {
        Intent mIntent = TrackingService.getIntent(context, cls, options);
        TrackingService.bindService(context, mIntent, connection);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(mIntent);
        } else {
            context.startService(mIntent);
        }
    }

    /***
     * Bind service
     *
     * @param context
     * @param intent
     * @param connection
     */
    public static boolean bindService(Context context, Intent intent, ServiceConnection connection) {
        return context.bindService(intent, connection, BIND_AUTO_CREATE);
    }

    /**
     * Create TrackingService intent
     *
     * @param context
     * @param cls
     * @param options
     * @return
     */
    public static Intent getIntent(Context context, Class<?> cls, TrackingServiceOptions options) {
        Intent mIntent = new Intent(context, TrackingService.class);

        if (cls != null) {
            mIntent.putExtra(ACTIVITY_EXTRA_NAME, cls.getCanonicalName());
        }

        if (options != null) {
            mIntent.putExtra(OPTIONS_EXTRA_NAME, options);
        }

        return mIntent;
    }

    /**
     * Stop Service and unbind it
     *
     * @param context
     * @param connection
     */
    public static void stopAndUnbindService(Context context, ServiceConnection connection) {
        Intent mIntent = new Intent(context, TrackingService.class);
        context.stopService(mIntent);
        TrackingService.unBindService(context, connection);
    }

    /**
     * Unbind service from the activity
     *
     * @param context
     * @param connection
     */
    public static void unBindService(Context context, ServiceConnection connection) {
        try {
            context.unbindService(connection);
        } catch (IllegalArgumentException ex) {
            Log.e(TAG, "UnBindService failed", ex);
        }
    }

    /**
     * Return {@code TRUE} if tracking service is running {@code FALSE} otherwise
     *
     * @return
     */
    public static boolean isRunning() {
        return TrackingService.serviceStatus == TrackingService.TrackingServiceStatus.RUNNING ||
                TrackingService.serviceStatus == TrackingService.TrackingServiceStatus.WAITING_FIRST_FIX ||
                TrackingService.serviceStatus == TrackingService.TrackingServiceStatus.WAITING_FIRST_RECORD;
    }

    /**
     * Return service status
     *
     * @return
     */
    public static int getTrackingServiceStatus() {
        return TrackingService.serviceStatus;
    }

    /**
     * Return Recorded Locations
     *
     * @return
     */
    public static List<KujakuLocation> getCurrentRecordedKujakuLocations() {
        TrackingStorage storage = new TrackingStorage();
        return storage.getCurrentRecordedKujakuLocations();
    }

    /**
     * Return Previous Recorded Locations
     *
     * @return
     */
    public static List<KujakuLocation> getPreviousRecordedKujakuLocations() {
        TrackingStorage storage = new TrackingStorage();
        return storage.getPreviousRecordedKujakuLocations();
    }
}
