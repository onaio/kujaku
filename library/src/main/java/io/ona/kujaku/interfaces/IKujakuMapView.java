package io.ona.kujaku.interfaces;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 26/09/2018
 */

import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.mapbox.geojson.FeatureCollection;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.style.expressions.Expression;

import org.json.JSONException;

import java.util.List;

import io.ona.kujaku.callbacks.AddPointCallback;
import io.ona.kujaku.listeners.BoundsChangeListener;
import io.ona.kujaku.domain.Point;
import io.ona.kujaku.listeners.OnFeatureClickListener;

public interface IKujakuMapView extends IKujakuMapViewLowLevel {

    /**
     * Enables adding a point on the map. You can either use GPS by passing a {@code true} on @param useGPS
     * which disables the scrolling and animates the location updates on the map while showing the location accuracy
     * until the user selects a certain position.
     *
     * @param useGPS
     * @param addPointCallback returns the location chosen by user from GPS or calls {@link AddPointCallback#onCancel()}
     *                         if the user cancels the operation
     */
    void addPoint(boolean useGPS, @NonNull AddPointCallback addPointCallback);


    /**
     * Enables adding a point on the map. You can either use GPS by passing a {@code true} on @param useGPS
     * which disables the scrolling and animates the location updates on the map while showing the location accuracy
     * until the user selects a certain position.
     *
     * @param useGPS
     * @param addPointCallback returns the location chosen by user from GPS or calls {@link AddPointCallback#onCancel()}
     *                         if the user cancels the operation
     * @param markerOptions Specifies the marker properties
     */
    void addPoint(boolean useGPS, @NonNull AddPointCallback addPointCallback, @Nullable MarkerOptions markerOptions);


    /**
     * Enables adding a point on the map. You can either use GPS by passing a {@code true} on @param useGPS
     * which disables the scrolling and animates the location updates on the map while showing the location accuracy
     * until the user selects a certain position.
     *
     * @param useGPS
     * @param addPointCallback returns the location chosen by user from GPS or calls {@link AddPointCallback#onCancel()}
     *                         if the user cancels the operation
     * @param markerResourceId This is the resource id of the expected marker icon to used.
     *                         This resource will be used at 100% opacity
     */
    void addPoint(boolean useGPS, @NonNull AddPointCallback addPointCallback, @DrawableRes int markerResourceId);


    void showCurrentLocationBtn(boolean isVisible);


    /**
     * Enables/disables location on the map to show the user location on the map without the user
     * intervention. If the MY LOCATION BUTTON is visible, it will turn blue as long as this mode is on.
     * This can be turned off by the user if s/he touches the map to scroll it to a specific location.
     *
     * @param focusOnMyLocation
     */
    void focusOnUserLocation(boolean focusOnMyLocation);


    /**
     *  Add new {@link com.mapbox.geojson.Feature Feature} points to the {@link io.ona.kujaku.views.KujakuMapView map}
     *
     *  A {@link com.mapbox.geojson.Feature feature} will be uniquely identified by its id field and a {@link com.mapbox.geojson.Feature Feature} with
     *  an already existing feature id will be ignored and not added to the {@link io.ona.kujaku.views.KujakuMapView map}
     *
     *  @param featureCollection A {@link FeatureCollection FeatureCollection} of {@link com.mapbox.geojson.Feature Feature} points to be added to the map
     *
     *  @throws JSONException
     */
    void addFeaturePoints(FeatureCollection featureCollection) throws JSONException;


    /**
     *  Update the properties of {@link com.mapbox.geojson.Feature Feature} points that are already on the {@link io.ona.kujaku.views.KujakuMapView map}
     *
     *  If the {@link com.mapbox.geojson.Feature Feature} point does not already exist, it is added to the map by calling the {@link #addFeaturePoints(FeatureCollection) addFeaturePoints}
     *  function and passing the new {@link FeatureCollection FeatureCollection}
     *
     *  @param featureCollection A {@link FeatureCollection FeatureCollection} of {@link com.mapbox.geojson.Feature Feature} points whose properties will be updated on the map
     *
     *   @throws JSONException
     */
    void updateFeaturePointProperties(FeatureCollection featureCollection) throws JSONException;


    /**
     *  Update the list of points displayed in KujakuMapView
     * Enables the app to get notified when the bounding box of the map changes if a user performs a pinch
     * or scroll movement. The listener registers the movement once it reaches the end so as no to have
     * a huge performance hit in cases where this is used to update the map with features. In case you
     * want to have more control of when to receive such updates, use
     * {@link com.mapbox.mapboxsdk.maps.MapboxMap#addOnMoveListener(com.mapbox.mapboxsdk.maps.MapboxMap.OnMoveListener)} and
     * consume {@link com.mapbox.mapboxsdk.maps.MapboxMap.OnMoveListener#onMove(com.mapbox.android.gestures.MoveGestureDetector)}
     * <p>
     * There is an initial call to {@code boundsChangeListener} so that the host application can know
     * the current bounding box
     *
     * @param boundsChangeListener
     */
    void setBoundsChangeListener(@Nullable BoundsChangeListener boundsChangeListener);


    /**
     * This function updates the list of points displayed in KujakuMapView
     *
     *  This is done both in the internal {@link List<Point>} data structure and visually on the map using location markers defined by the user
     *
     *  @param points A list of {@link Point}
     */
    void updateDroppedPoints(List<Point> points);

    /**
     * Sets an {@link OnFeatureClickListener} which will be fired when a feature on the map in either of the {@code layerIds}
     * is touched/clicked
     *
     * @param onFeatureClickListener
     * @param layerIds
     */
    void setOnFeatureClickListener(@NonNull OnFeatureClickListener onFeatureClickListener, @Nullable String... layerIds);

    /**
     * Sets an {@link OnFeatureClickListener} which will be fired when a feature on the map in either of the {@code layerIds}
     * is touched/clicked and/or fulfilling the filter defined in {@code filter}
     *
     * @param onFeatureClickListener
     * @param expressionFilter
     * @param layerIds
     */
    void setOnFeatureClickListener(@NonNull OnFeatureClickListener onFeatureClickListener, @Nullable Expression expressionFilter, @Nullable String... layerIds);

    /**
     * Checks if the map warms GPS(this just means the location service that is going to be used).
     * Warming the GPS in this case means that it starts the location services as soon as you open
     * the map so that getting your location is instant
     *
     * @return
     */
    boolean isWarmGps();

    /**
     * Enables or disables the starting of location services as soon as the view is created.
     * Warming the GPS in this case means that it starts the location services as soon as you open
     * the map so that getting your location is instant
     *
     * @param warmGps
     */
    void setWarmGps(boolean warmGps);
}
