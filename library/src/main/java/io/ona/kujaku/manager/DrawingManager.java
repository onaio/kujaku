package io.ona.kujaku.manager;

import android.graphics.Color;
import android.graphics.PointF;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Geometry;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.annotation.Circle;
import com.mapbox.mapboxsdk.plugins.annotation.CircleManager;
import com.mapbox.mapboxsdk.plugins.annotation.FillManager;
import com.mapbox.mapboxsdk.plugins.annotation.FillOptions;
import com.mapbox.mapboxsdk.plugins.annotation.LineManager;
import com.mapbox.mapboxsdk.plugins.annotation.LineOptions;
import com.mapbox.mapboxsdk.plugins.annotation.OnCircleClickListener;
import com.mapbox.mapboxsdk.plugins.annotation.OnCircleDragListener;
import com.mapbox.mapboxsdk.plugins.annotation.OnCircleLongClickListener;
import com.mapbox.mapboxsdk.style.expressions.Expression;

import java.util.ArrayList;
import java.util.List;

import io.ona.kujaku.layers.FillBoundaryLayer;
import io.ona.kujaku.layers.KujakuLayer;
import io.ona.kujaku.listeners.OnDrawingCircleClickListener;
import io.ona.kujaku.listeners.OnDrawingCircleLongClickListener;
import io.ona.kujaku.views.KujakuMapView;

/**
 * Manager use to draw polygons on Map.
 * Encapsulate a FillManager, LineManager and CircleManager
 *
 * Created by Emmanuel Otin - eo@novel-t.ch on 19/06/2019
 */
public class DrawingManager {

    private KujakuMapView kujakuMapView;
    private MapboxMap mapboxMap;
    private List<KujakuCircle> circles;
    private KujakuCircle currentKujakuCircle;

    private FillManager fillManager;
    private LineManager lineManager;
    private CircleManager circleManager;

    private OnDrawingCircleClickListener onDrawingCircleClickListener;
    private OnDrawingCircleLongClickListener onDrawingCircleLongClickListener;

    private boolean drawingEnabled;

    private KujakuLayer currentLayer;

    /**
     * Constructor
     *
     * @param mapView
     * @param mapboxMap
     * @param style
     */
    public DrawingManager(@NonNull KujakuMapView mapView, @NonNull MapboxMap mapboxMap, @NonNull Style style) {
        this.kujakuMapView = mapView;
        this.mapboxMap = mapboxMap;

        this.circles = new ArrayList<>();
        this.setCurrentKujakuCircle(null);

        fillManager = new FillManager(mapView, mapboxMap, style);
        lineManager = new LineManager(mapView, mapboxMap, style);
        circleManager = new CircleManager(mapView, mapboxMap, style);

        circleManager.addClickListener(new OnCircleClickListener() {
            @Override
            public void onAnnotationClick(Circle circle) {
                if (drawingEnabled && onDrawingCircleClickListener != null) {
                    onDrawingCircleClickListener.onCircleClick(circle);
                }
            }
        });

        circleManager.addLongClickListener(new OnCircleLongClickListener() {
                @Override
                public void onAnnotationLongClick(Circle circle) {
                   if (drawingEnabled && onDrawingCircleLongClickListener != null) {
                       onDrawingCircleLongClickListener.onCircleLongClick(circle);
                   }
                }
            });

        circleManager.addDragListener(new OnCircleDragListener() {
            @Override
            public void onAnnotationDragStarted(Circle circle) {
                // Left empty on purpose
            }

            @Override
            public void onAnnotationDrag(Circle circle) {
                refreshPolygon();
            }

            @Override
            public void onAnnotationDragFinished(Circle circle) {
                // Left empty on purpose
            }
        });

        mapboxMap.addOnMapClickListener(new MapboxMap.OnMapClickListener() {
            @Override
            public boolean onMapClick(@NonNull LatLng point) {
                final PointF pixel = mapboxMap.getProjection().toScreenLocation(point);
                List<Feature> features = mapboxMap.queryRenderedFeatures(pixel, (Expression) null, CircleManager.ID_GEOJSON_LAYER);

                if (features.size() == 0 && drawingEnabled && onDrawingCircleClickListener != null) {
                    onDrawingCircleClickListener.onCircleNotClick(point);
                }

                return false;
            }
        });

        mapboxMap.addOnMapLongClickListener(new MapboxMap.OnMapLongClickListener() {
            @Override
            public boolean onMapLongClick(@NonNull LatLng point) {
                final PointF pixel = mapboxMap.getProjection().toScreenLocation(point);
                List<Feature> features = mapboxMap.queryRenderedFeatures(pixel, (Expression) null, CircleManager.ID_GEOJSON_LAYER);

                // Get the first feature within the list if one exist
                if (features.size() == 0 && drawingEnabled && onDrawingCircleLongClickListener != null) {
                    onDrawingCircleLongClickListener.onCircleNotLongClick(point);
                }

                return false;
            }
        });
    }

    public static KujakuCircleOptions getKujakuCircleOptions(){
        return new KujakuCircleOptions()
                .withCircleRadius(15.0f)
                .withCircleColor("black")
                .withMiddleCircle(false)
                .withDraggable(false);
    }

    public static KujakuCircleOptions getKujakuCircleMiddleOptions() {
        return new KujakuCircleOptions()
                .withCircleRadius(10.0f)
                .withCircleColor("black")
                .withMiddleCircle(true)
                .withDraggable(false);
    }

    public static KujakuCircleOptions getKujakuCircleDraggableOptions() {
        return new KujakuCircleOptions()
                .withCircleRadius(20.0f)
                .withCircleColor("red")
                .withMiddleCircle(false)
                .withDraggable(true);
    }

    /**
     * Drawing enabled/disabled
     *
     * @param drawing
     */
    private void setDrawing(boolean drawing) {
        this.drawingEnabled = drawing;
    }

    /**
     * Set currentCircle when circle is clicked or longClicked
     *
     * @param circle
     */
    private void setCurrentCircle(@Nullable Circle circle) {
        this.currentKujakuCircle = getKujakuCircle(circle);
    }

    /**
     * Set currentCircle when circle is clicked or longClicked
     *
     * @param circle
     */
    private void setCurrentKujakuCircle(@Nullable KujakuCircle circle) {
        this.currentKujakuCircle = circle;
    }

    /**
     * Start Drawing. A KujakuLayer can be passed to init the drawing.
     * @param kujakuLayer
     * @return
     */
    public boolean startDrawingKujakuLayer(@Nullable KujakuLayer kujakuLayer) {
        this.currentLayer = kujakuLayer;

        if (kujakuLayer == null) {
            this.startDrawingPoints(new ArrayList<>());
            return true;
        }

        if (kujakuLayer.getFeatureCollection().features() != null && kujakuLayer.getFeatureCollection().features().size() >= 1) {
            Geometry geometry = kujakuLayer.getFeatureCollection().features().get(0).geometry();
            if (geometry instanceof Polygon) {
                // hide layer
                kujakuLayer.disableLayerOnMap(mapboxMap);

                Polygon polygon = (Polygon) geometry;
                List<Point> points = polygon.coordinates().get(0);
                this.startDrawingPoints(points);
                return true;
            }
        }

        return false;
    }

    /**
     * Start Drawing. A list of point can be passed to init the drawing.
     * Start drawing on the map / or editing an already existing kujakuLayer
     *
     * @param points
     */
    public void startDrawingPoints(@Nullable List<Point> points) {
        setDrawing(true);
        setCurrentCircle(null);

        if (points != null && points.size() > 0) {
            for (Point p : points) {
                this.create(getKujakuCircleOptions().withLatLng(new LatLng(p.latitude(), p.longitude())), false);
            }

            this.refresh(true);
        }
    }

    /**
     * Stop drawing and create a new Kujaku layer
     *
     * @return
     */
    public boolean stopDrawingAndDisplayLayer() {
        Polygon polygon = this.stopDrawing();

        Feature feature = Feature.fromGeometry(polygon);
        FeatureCollection collection = FeatureCollection.fromFeature(feature);

        if (this.currentLayer != null) { // Update layer
            this.currentLayer.updateFeatures(collection);
            this.currentLayer.enableLayerOnMap(mapboxMap);

        } else {                        // Create layer
            FillBoundaryLayer layer = new FillBoundaryLayer.Builder(collection)
                    .setBoundaryColor(Color.BLACK)
                    .setBoundaryWidth(3f)
                    .build();

            kujakuMapView.addLayer(layer);
        }
        this.currentLayer = null;

        return true;
    }

    /**
     * Stop drawing and return the drawn polygon
     *
     * @return
     */
    public Polygon stopDrawing() {
        setDrawing(false);
        setCurrentCircle(null);

        // convert into polygon
        List<Point> points = new ArrayList<>();
        List<List<Point>> lists = new ArrayList<>();

        // Return only real points, not middles
        for (KujakuCircle c: this.circles) {
            if (! c.isMiddleCircle()) {
                points.add(c.getCircle().getGeometry());
            }
        }

        lists.add(points);
        // Delete annotations
        this.deleteAll();
        // Refresh
        this.refresh(false);

        return Polygon.fromLngLats(lists);
    }

    /**
     * Is Drawing enabled ?
     * @return
     */
    public boolean isDrawingEnabled() {
        return this.drawingEnabled;
    }


    /**
     * Retrieve the KujakuCircle corresponding to the Circle
     *
     * @param circle
     * @return
     */
    private KujakuCircle getKujakuCircle(@Nullable Circle circle) {
        if (circle == null) {
            return null;
        }

        for (KujakuCircle c : circles) {
            if (c.getCircle().getId() == circle.getId()) {
                return c;
            }
        }

        return null;
    }

    /**
     * Creation of middle points between 2 real points
     */
    private void createMiddlePoints() {
        if (this.getKujakuCircles().size() > 1) {
            List<KujakuCircleOptions> newCirclesOptions = new ArrayList<>();

            List<KujakuCircle> circles = this.getKujakuCircles();
            Circle circle1 = null;
            Circle circle2 = null;

            for(int i = 0 ; i < circles.size() ; i ++) {
                KujakuCircle circle = circles.get(i);

                if (!circle.isMiddleCircle()) {
                    if (circle1 == null) {
                        circle1 = circle.getCircle();
                        newCirclesOptions.add(getKujakuCircleOptions().withLatLng(circle1.getLatLng()));
                    } else {
                        circle2 = circle.getCircle();
                    }
                }

                if (circle1 != null && circle2 != null) {
                    KujakuCircleOptions newKujakuCircleOptions = this.createMiddleKujakuCircleOptions(circle1, circle2, getKujakuCircleMiddleOptions()) ;
                    newCirclesOptions.add(newKujakuCircleOptions);
                    newCirclesOptions.add(getKujakuCircleOptions().withLatLng(circle2.getLatLng()));

                    circle1 = circle2;
                    circle2 = null;
                }
            }

            if (circle1 != null) {
                circle2 = this.getKujakuCircles().get(0).getCircle();

                KujakuCircleOptions newKujakuCircleOptions = this.createMiddleKujakuCircleOptions(circle1, circle2, getKujakuCircleMiddleOptions()) ;
                newCirclesOptions.add(newKujakuCircleOptions);
            }

            this.deleteAll();
            this.createFromList(newCirclesOptions);
        }
    }

    /**
     * Create new KujakuCircleOptions between circle1 and circle2 lat long. Copy options from parameter options
     *
     * @param circle1
     * @param circle2
     * @param options
     * @return
     */
    private KujakuCircleOptions createMiddleKujakuCircleOptions(@NonNull Circle circle1, @NonNull Circle circle2, @NonNull KujakuCircleOptions options) {
        double lonEast = Math.max(circle1.getLatLng().getLongitude(), circle2.getLatLng().getLongitude());
        double lonWest = Math.min(circle1.getLatLng().getLongitude(), circle2.getLatLng().getLongitude());
        double latNorth = Math.max(circle1.getLatLng().getLatitude(), circle2.getLatLng().getLatitude());
        double latSouth = Math.min(circle1.getLatLng().getLatitude(), circle2.getLatLng().getLatitude());

        LatLng latLng =
                LatLngBounds.from(latNorth, lonEast, latSouth, lonWest).getCenter();

        return options
                .withLatLng(latLng);
    }

    /**
     * Refresh the entire Polygon appearance
     *
     */
    private void refreshPolygon() {
        fillManager.deleteAll();
        lineManager.deleteAll();

        if (this.getKujakuCircles().size() > 1) {
            List<LatLng> list = new ArrayList<>();
            for (int i = 0 ; i < this.getKujakuCircles().size() ; i++) {
                if (! this.getKujakuCircles().get(i).isMiddleCircle()) {
                    list.add(this.getKujakuCircles().get(i).getCircle().getLatLng());
                }
            }

            List<List<LatLng>> lists =  new ArrayList<>();
            lists.add(list);

            fillManager.create(new FillOptions()
                    .withLatLngs(lists)
                    .withFillOpacity(Float.valueOf("0.5")));

            // We add the first point to the end of the list too
            if (this.getKujakuCircles().size() > 2) {
                list.add(this.getKujakuCircles().get(0).getCircle().getLatLng());
            }

            lineManager.create(new LineOptions()
                    .withLatLngs(list)
                    .withLineOpacity(Float.valueOf("0.5")));
        }
    }

    /**
     * Draw circle with kujakuCircleOptions
     *
     * @param latLng
     * @return
     */
    public Circle drawCircle(@NonNull LatLng latLng) {
        return this.create(DrawingManager.getKujakuCircleOptions().withLatLng(latLng));
    }

    /***
     * Create a new Circle, add it to the circle list and refresh the polygon
     *
     * @param options
     * @return
     */
    public Circle create(@NonNull KujakuCircleOptions options) {
       return this.create(options, true);
    }

    /**
     * Create a new Circle and add it to the circle list
     * Refresh the polygon depends of the refresh variable
     *
     * @param options
     * @param refresh
     * @return
     */
    private Circle create(@NonNull KujakuCircleOptions options, boolean refresh) {
        Circle circle = circleManager.create(options);
        KujakuCircle previousCircle = null;

        if (circles.size() > 0) {
            previousCircle = circles.get(circles.size() -1);
        }

        KujakuCircle kujakuCircle = new KujakuCircle(circle, previousCircle, options.getMiddleCircle());
        circles.add(kujakuCircle);

        if (refresh) {
            this.refresh(true);
        }

        return circle;
    }

    /**
     * Create circles from list of KujakuCircleOptions
     *
     * @param options
     */
    private void createFromList(@NonNull List<KujakuCircleOptions> options) {
        for (KujakuCircleOptions option: options) {
            this.create(option, false);
        }
        if (this.circles.size() >= 2) {
            this.getKujakuCircles().get(0).setPreviousKujakuCircle(this.getKujakuCircles().get(this.getKujakuCircles().size() - 1));
        }

        this.refresh(false);
    }

    /**
     * Refresh the Polygon
     */
    private void refresh(boolean createMiddlePoints) {
        if (createMiddlePoints) {
            this.createMiddlePoints();
        }
        this.refreshPolygon();
    }

    /**
     * Delete all
     */
    private void deleteAll() {
        this.circles.clear();
        this.circleManager.deleteAll();
        this.fillManager.deleteAll();
        this.lineManager.deleteAll();
    }

    /**
     * Delete a Circle and the middles circles before and after
     *
     * @param kujakuCircle
     */
    public void delete(@Nullable KujakuCircle kujakuCircle) {
        // Previous and next circle to delete if AreMiddle
        if (kujakuCircle == null) {
            return ;
        }

        KujakuCircle previousCircle = kujakuCircle.getPreviousKujakuCircle();
        KujakuCircle nextCircle = kujakuCircle.getNextKujakuCircle();

        circleManager.delete(kujakuCircle.getCircle());
        this.circles.remove(kujakuCircle);

        if (previousCircle != null && previousCircle.isMiddleCircle()) {
            circleManager.delete(previousCircle.getCircle());
            this.circles.remove(previousCircle);
        }

        if (nextCircle != null && nextCircle.isMiddleCircle()) {
            circleManager.delete(nextCircle.getCircle());
            this.circles.remove(nextCircle);
        }

        this.setCurrentCircle(null);
        this.refresh(true);
    }

    /**
     * Delete the current selected circle
     */
    public void deleteDrawingCurrentCircle() {
        this.delete(this.getCurrentKujakuCircle());
    }
    /**
     * Set Circle draggable
     * Remove middle circles after & before when a circle is draggable
     *
     * @param draggable
     * @param circle
     */
    public void setDraggable(boolean draggable, @NonNull Circle circle) {
        circle.setDraggable(draggable);
        KujakuCircleOptions options;

        if (draggable) {
            options = getKujakuCircleDraggableOptions();
            this.setCurrentCircle(circle);
        } else {
            options = getKujakuCircleOptions();
            this.setCurrentCircle(null);
        }

        circle.setCircleColor(options.getCircleColor());
        circle.setCircleRadius(options.getCircleRadius());

        if (draggable) {
            KujakuCircle previousCircle = null;
            KujakuCircle nextCircle = null;

            for (KujakuCircle c : circles) {
                if (c.getCircle().getId() == circle.getId()) {
                    this.setCurrentKujakuCircle(c);
                    c.setMiddleCircle(options.getMiddleCircle());
                    previousCircle = c.getPreviousKujakuCircle();
                    nextCircle = c.getNextKujakuCircle();
                    break;
                }
            }

            if (previousCircle != null && previousCircle.isMiddleCircle()) {
                circleManager.delete(previousCircle.getCircle());
                if (previousCircle.getPreviousKujakuCircle() != null) {
                    previousCircle.getPreviousKujakuCircle().setNextKujakuCircle(getCurrentKujakuCircle());
                }
                this.circles.remove(previousCircle);
            }

            if (nextCircle != null && nextCircle.isMiddleCircle()) {
                circleManager.delete(nextCircle.getCircle());
                if (nextCircle.getNextKujakuCircle() != null) {
                    nextCircle.getNextKujakuCircle().setPreviousKujakuCircle(getCurrentKujakuCircle());
                }
                this.circles.remove(nextCircle);
            }
        }

        circleManager.update(circle);

        if (!draggable) {
            this.refresh(true);
        }
    }

    /**
     * Unset the draggable property to the current selected circle
     */
    public void unsetCurrentCircleDraggable() {
        if(this.getCurrentKujakuCircle() != null) {
            this.setDraggable(false, this.getCurrentKujakuCircle().getCircle());
        }
    }

    /**
     * Return true if the circle is a middle one
     *
     * @param circle
     * @return
     */
    public boolean isMiddleCircle(@NonNull Circle circle) {
        for (KujakuCircle c : circles) {
            if (c.getCircle().getId() == circle.getId()) {
                return c.isMiddleCircle();
            }
        }

        return false;
    }

    /**
     * Return a list of All KujakuCircles
     *
     * @return
     */
    public List<KujakuCircle> getKujakuCircles() {
        return this.circles;
    }

    /**
     * Return the current selected Kujaku Circle
     *
     * @return
     */
    public KujakuCircle getCurrentKujakuCircle() {
        return this.currentKujakuCircle;
    }

    /**
     * Set a listener for OnDrawingCircleClickListener
     *
     * @param listener
     */
    public void addOnDrawingCircleClickListener(OnDrawingCircleClickListener listener) {
        this.onDrawingCircleClickListener = listener;
    }

    /**
     * Set a listener for the OnDrawingCircleLongClickListener
     *
     * @param listener
     */
    public void addOnDrawingCircleLongClickListener(OnDrawingCircleLongClickListener listener) {
        this.onDrawingCircleLongClickListener = listener;
    }
}
