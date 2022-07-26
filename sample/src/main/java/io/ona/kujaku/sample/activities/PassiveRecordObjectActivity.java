package io.ona.kujaku.sample.activities;

import android.os.Bundle;
import androidx.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;

import java.util.ArrayList;
import java.util.List;

import io.ona.kujaku.callbacks.OnLocationServicesEnabledCallBack;
import io.ona.kujaku.domain.Point;
import io.ona.kujaku.exceptions.TrackingServiceNotInitializedException;
import io.ona.kujaku.listeners.TrackingServiceListener;
import io.ona.kujaku.location.KujakuLocation;
import io.ona.kujaku.sample.R;
import io.ona.kujaku.services.TrackingService;
import io.ona.kujaku.services.configurations.TrackingServiceDefaultUIConfiguration;
import io.ona.kujaku.services.options.TrackingServiceHighAccuracyOptions;
import io.ona.kujaku.views.KujakuMapView;

public class PassiveRecordObjectActivity extends BaseNavigationDrawerActivity implements TrackingServiceListener {

    private static final String TAG = PassiveRecordObjectActivity.class.getName();

    private KujakuMapView kujakuMapView;

    private Button startStopBtn;
    private Button forceLocationBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        kujakuMapView = findViewById(R.id.kmv_passiveRecordObject_mapView);
        kujakuMapView.onCreate(savedInstanceState);
        kujakuMapView.showCurrentLocationBtn(true);

        this.startStopBtn = findViewById(R.id.btn_passiveRecordObject_StartStopRecording);
        this.forceLocationBtn = findViewById(R.id.btn_passiveRecordObject_ForcePoint);

        kujakuMapView.initTrackingService(PassiveRecordObjectActivity.this,
                new TrackingServiceDefaultUIConfiguration(),
                new TrackingServiceHighAccuracyOptions());

        this.startStopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (((Button)v).getText().equals(getString(R.string.start_recording))) {

                    // Start Service
                    try {
                        kujakuMapView.startTrackingService(getApplicationContext(),
                                PassiveRecordObjectActivity.class);
                    } catch (TrackingServiceNotInitializedException ex) {
                        Log.e(TAG, "Error while starting the Tracking Service", ex);
                    }

                    ((Button)v).setText(getString(R.string.stop_recording));

                } else {

                    // Get the Tracks recorded
                    List<KujakuLocation> tracks = kujakuMapView.stopTrackingService(getApplicationContext());

                    //List<Location> othersTracks = new TrackingStorage().getCurrentRecordedKujakuLocations();
                    //displayTracksRecorded(othersTracks);

                    ((Button)v).setText(getString(R.string.start_recording));
                    forceLocationBtn.setEnabled(false);
                }
            }
        });

        this.forceLocationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                kujakuMapView.trackingServiceTakeLocation(45000);
            }
        });

        kujakuMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull MapboxMap mapboxMap) {
                mapboxMap.setStyle(Style.MAPBOX_STREETS);
                kujakuMapView.focusOnUserLocation(true);
            }
        });

        kujakuMapView.setWarmGps(true, null, null, new OnLocationServicesEnabledCallBack() {
            @Override
            public void onSuccess() {
                startStopBtn.setEnabled(true);
            }
        });
    }

    private void InitRecordingButton() {
        if (TrackingService.isRunning()) {
            startStopBtn.setText("Stop Recording");
            forceLocationBtn.setEnabled(true);
        } else {
            startStopBtn.setText("Start Recording");
            forceLocationBtn.setEnabled(true);
        }
    }

    private void displayTracksRecorded(List<KujakuLocation> locations) {
        List<Point> points = new ArrayList<>();
        for (KujakuLocation location: locations) {
            points.add(new Point(location.hashCode(), location.getLatitude(), location.getLongitude()));
        }
        kujakuMapView.updateDroppedPoints(points);
    }

    /**** TrackingServiceListener ****/
    @Override
    public void onServiceDisconnected() {
        Toast.makeText(getApplicationContext(), "Service disconnected", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onServiceConnected(TrackingService service) {
        Toast.makeText(getApplicationContext(), "Service connected", Toast.LENGTH_SHORT).show();

        displayTracksRecorded(kujakuMapView.getTrackingServiceRecordedKujakuLocations());
        InitRecordingButton();
    }

    @Override
    public void onFirstLocationReceived(KujakuLocation location) {
        forceLocationBtn.setEnabled(true);
        Toast.makeText(getApplicationContext(), "First Location received", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onNewLocationReceived(KujakuLocation location) {
        Toast.makeText(getApplicationContext(), "New Location received", Toast.LENGTH_SHORT).show();
        List<Point> points = new ArrayList<>();
        points.add(new Point(location.hashCode(), location.getLatitude(), location.getLongitude()));
        kujakuMapView.updateDroppedPoints(points);
    }

    @Override
    public void onCloseToDepartureLocation(KujakuLocation location) {
        Toast.makeText(getApplicationContext(), "Location recorded is closed to the departure location", Toast.LENGTH_LONG).show();
    }

    /**** TrackingServiceListener END ****/

    @Override
    protected int getContentView() {
        return R.layout.activity_passive_record_object_map_view;
    }

    @Override
    protected int getSelectedNavigationItem() {
        return R.id.nav_passive_record_object;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (kujakuMapView != null) kujakuMapView.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (kujakuMapView != null) kujakuMapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (kujakuMapView != null) kujakuMapView.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (kujakuMapView != null) kujakuMapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (kujakuMapView != null) kujakuMapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (kujakuMapView != null) kujakuMapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (kujakuMapView != null) kujakuMapView.onLowMemory();
    }
}
