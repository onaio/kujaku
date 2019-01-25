package io.ona.kujaku.views;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 25/01/2019
 */
import android.content.Context;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.MapboxMapOptions;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

import java.util.Locale;

import io.ona.kujaku.R;

/**
 * Created by clyde on 2/10/2016.
 * This class extends the Mapbox Mapview to add a scale at the bottom
 */

public class ScaledMapView extends KujakuMapView implements MapboxMap.OnCameraMoveListener, OnMapReadyCallback {
    private TextView scaleText;
    private TextView zoomLevel;
    private MapboxMap mapboxMap;
    private OnMapReadyCallback callback;
    private ScaleUnit scaleUnit = ScaleUnit.KM;
    private float labelWidth = 0.33f;

    public float getLabelWidth() {
        return labelWidth;
    }

    public void setLabelWidth(float labelWidth) {
        if(labelWidth > 1f)
            labelWidth = 1f;
        else if(labelWidth < 0.1f)
            labelWidth = 0.1f;
        this.labelWidth = labelWidth;
    }

    public ScaleUnit getScaleUnit() {
        return scaleUnit;
    }

    public void setScaleUnit(ScaleUnit scaleUnit) {
        this.scaleUnit = scaleUnit;
    }


    /**
     * To ensure this is called when the camera changes, either this ScaledMapView must be passed
     * to mapboxMap.addOnCameraMoveListener() or whatever is listening must also call this method
     * when it is called.
     *
     */
    @Override
    public void onCameraMove() {
        if (scaleText == null) {
            /*ViewParent v = getParent();
            if (v instanceof FrameLayout) {
                scaleText = (TextView)inflate(getContext(), R.layout.mapscale, null);
                ((FrameLayout)v).addView(scaleText);
            }*/
            scaleText = ((View) getParent()).findViewById(R.id.scale_text);
        }

        if (zoomLevel == null) {
            zoomLevel = ((View) getParent()).findViewById(getResources().getIdentifier("zoom_level", "id", "io.ona.kujaku.sample"));
        }

        if (zoomLevel != null) {
            zoomLevel.setText("Zoom: " + mapboxMap.getCameraPosition().zoom);
        }

        if (scaleText != null) {
            // compute the horizontal span in metres of the bottom of the map
            LatLngBounds latLngBounds = mapboxMap.getProjection().getVisibleRegion().latLngBounds;
            float span[] = new float[1];
            Location.distanceBetween(latLngBounds.getLatSouth(), latLngBounds.getLonEast(),
                    latLngBounds.getLatSouth(), latLngBounds.getLonWest(), span);

            float totalWidth = span[0] / scaleUnit.ratio;
            // calculate an initial guess at step size
            float tempStep = totalWidth * labelWidth;

            // get the magnitude of the step size
            float mag = (float)Math.floor(Math.log10(tempStep));
            float magPow = (float)Math.pow(10, mag);

            // calculate most significant digit of the new step size
            float magMsd = (int)(tempStep / magPow + 0.5);

            // promote the MSD to either 1, 2, or 5
            if (magMsd > 5.0f)
                magMsd = 10.0f;
            else if (magMsd > 2.0f)
                magMsd = 5.0f;
            else if (magMsd > 1.0f)
                magMsd = 2.0f;
            float length = magMsd * magPow;

            length *= 1000;
            if (length >= 1f)
                scaleText.setText(String.format(Locale.US, "%.0f metres", length));
            else
                scaleText.setText(String.format(Locale.US, "%.2f metres", length));
            // set the total width to the appropriate fraction of the display
            length /= 1000;
            int width = Math.round(getWidth() * length / totalWidth);
            ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) scaleText.getLayoutParams();
            layoutParams.width = width;
            //new ConstraintLayout.LayoutParams(width, ViewGroup.LayoutParams.WRAP_CONTENT);
            //layoutParams.bottomMargin = 4;
            scaleText.setLayoutParams(layoutParams);
        }
    }

    enum ScaleUnit {
        MILE("mile", 1609.344f),
        NM("nm", 1852.0f),
        KM("km", 1000.0f);

        ScaleUnit(String unit, float ratio) {
            this.unit = unit;
            this.ratio = ratio;
        }

        String unit;
        float ratio;
    }

    public ScaledMapView(@NonNull Context context) {
        super(context);
    }

    public ScaledMapView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ScaledMapView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ScaledMapView(@NonNull Context context, @Nullable MapboxMapOptions options) {
        super(context, options);
    }

    @Override
    public void getMapAsync(OnMapReadyCallback callback) {
        this.callback = callback;
        super.getMapAsync(this);
    }

    @Override
    public void onMapReady(MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;
        // if the owner of this view is listening for the map, pass it through. If not, we must
        // listen for camera events ourselves.

        mapboxMap.getUiSettings().setRotateGesturesEnabled(false);
        mapboxMap.getUiSettings().setTiltGesturesEnabled(false);

        callback.onMapReady(mapboxMap);
        mapboxMap.addOnCameraMoveListener(this);
        onCameraMove();
    }
}
