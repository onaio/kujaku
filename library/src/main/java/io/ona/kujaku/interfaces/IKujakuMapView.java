package io.ona.kujaku.interfaces;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 26/09/2018
 */

import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.mapbox.geojson.FeatureCollection;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;

import org.json.JSONException;

import java.util.List;

import io.ona.kujaku.callbacks.AddPointCallback;
import io.ona.kujaku.domain.Point;

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
     *
     *  This is done both in the internal {@link List<Point>} data structure and visually on the map using location markers defined by the user
     *
     *  @param points A list of {@link Point}
     */
    void updateDroppedPoints(List<Point> points);
}
