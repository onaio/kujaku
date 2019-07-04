package io.ona.kujaku.manager;

import android.graphics.Color;
import android.support.annotation.NonNull;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Geometry;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.annotation.Circle;
import com.mapbox.mapboxsdk.plugins.annotation.CircleManager;
import com.mapbox.mapboxsdk.plugins.annotation.Line;
import com.mapbox.mapboxsdk.plugins.annotation.LineManager;
import com.mapbox.mapboxsdk.plugins.annotation.LineOptions;
import com.mapbox.mapboxsdk.plugins.annotation.OnCircleDragListener;
import com.mapbox.turf.models.LineIntersectsResult;

import java.util.ArrayList;
import java.util.List;

import io.ona.kujaku.layers.FillBoundaryLayer;
import io.ona.kujaku.layers.KujakuLayer;
import io.ona.kujaku.listeners.OnSplittingClickListener;
import io.ona.kujaku.views.KujakuMapView;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineColor;

/**
 * Manager use to split a polygon
 * Encapsulate a LineManager and CircleManager
 *
 * Created by Emmanuel Otin - eo@novel-t.ch on 02/07/2019
 */
public class SplittingManager {

    private KujakuMapView kujakuMapView;
    private MapboxMap mapboxMap;
    private KujakuLayer kujakuLayer;
    private Circle circleStart;
    private Circle circleEnd;
    private Line splittingLine;

    private List<Point> polygonToSplit;

    private LineManager lineManager;
    private CircleManager circleManager;

    private OnSplittingClickListener onSplittingClickListener;
    //private OnDrawingCircleLongClickListener onDrawingCircleLongClickListener;

    private boolean splittingEnabled;

    /**
     * Constructor
     *
     * @param mapView
     * @param mapboxMap
     * @param style
     */
    public SplittingManager(@NonNull KujakuMapView mapView, @NonNull MapboxMap mapboxMap, @NonNull Style style) {
        this.kujakuMapView = mapView;
        this.mapboxMap = mapboxMap;

        lineManager = AnnotationRepositoryManager.getLineManagerInstance(mapView, mapboxMap, style);
        circleManager = AnnotationRepositoryManager.getCircleManagerInstance(mapView, mapboxMap, style);

        circleManager.addDragListener(new OnCircleDragListener() {
            @Override
            public void onAnnotationDragStarted(Circle circle) {
                // Left empty on purpose
            }

            @Override
            public void onAnnotationDrag(Circle circle) {
                refreshSplitLine();
            }

            @Override
            public void onAnnotationDragFinished(Circle circle) {
                // Left empty on purpose
            }
        });
        mapboxMap.addOnMapClickListener(new MapboxMap.OnMapClickListener() {
            @Override
            public boolean onMapClick(@NonNull LatLng point) {
                if (splittingEnabled && onSplittingClickListener != null) {
                    onSplittingClickListener.onSplittingClick(point);
                }

                return false;
            }
        });
    }

    public static KujakuCircleOptions getKujakuCircleOptions() {
        return new KujakuCircleOptions()
                .withCircleRadius(10.0f)
                .withCircleColor("red")
                .withDraggable(true);
    }

    /**
     * Start Splitting. A KujakuLayer has to be passed to init the drawing.
     * @param kujakuLayer
     * @return
     */
    public boolean startSplittingKujakuLayer(@NonNull KujakuLayer kujakuLayer) {
        this.stopSplitting();
        this.kujakuLayer = kujakuLayer;
        this.startSplitting();

        return this.splittingEnabled;
    }

    private void startSplitting() {
        if (this.kujakuLayer != null) {
            Geometry geometry = this.kujakuLayer.getFeatureCollection().features().get(0).geometry();

            if (geometry instanceof Polygon) {
                Polygon polygon = (Polygon) geometry;
                this.polygonToSplit = polygon.coordinates().get(0);
                this.polygonToSplit.add(this.polygonToSplit.get(0));
                this.splittingEnabled = true;

                kujakuLayer.updateLineLayerProperties(lineColor("red"));
            }
        }
    }

    public void stopSplitting() {
        if (this.kujakuLayer != null) {
            this.kujakuLayer.updateLineLayerProperties(lineColor("black"));
            this.kujakuLayer = null;
        }
        this.deleteAll();
    }

    /**
     * Draw circle with kujakuCircleOptions
     *
     * @param latLng
     * @return
     */
    public Circle drawCircle(LatLng latLng) {
        return this.create(SplittingManager.getKujakuCircleOptions().withLatLng(latLng));
    }


    /**
     * Create new Circle1 or Circle 2 drawing the splitting line
     * Refresh the splitting line
     *
     * @param options
     * @return
     */
    private Circle create(@NonNull KujakuCircleOptions options) {
        if (this.checkSplitPoints()) {
            return null;
        }

        Circle circle = circleManager.create(options);

        if (this.circleStart == null) {
            this.circleStart = circle;
        } else if (this.circleEnd == null) {
            this.circleEnd = circle;
        }

        this.refreshSplitLine();

        return circle;
    }

    private boolean checkSplitPoints() {
        return this.circleStart != null && this.circleEnd != null ;
    }

    /**
     * Refresh the split line when start or end points are dragging
     */
    private void refreshSplitLine() {
        if (this.checkSplitPoints()) {
            this.deleteLine();

            List<LatLng> list = new ArrayList<>();
            list.add(this.circleStart.getLatLng());
            list.add(this.circleEnd.getLatLng());

            this.splittingLine = lineManager.create(new LineOptions()
                    .withLatLngs(list)
                    .withLineColor("red")
                    .withLineOpacity(Float.valueOf("0.5")));
        }
    }

    /**
     * Function splitting the polygon into list of Polygons
     */
    public void split() {
        if (!checkSplitPoints()) {
            return ;
        }

        Point pointA = null;
        Point pointB = null;
        Point splitA = Point.fromLngLat(this.circleStart.getLatLng().getLongitude(), this.circleStart.getLatLng().getLatitude());
        Point splitB = Point.fromLngLat(this.circleEnd.getLatLng().getLongitude(), this.circleEnd.getLatLng().getLatitude());

        List<Point> initialPolygon = new ArrayList<>();
        List<Point> newPolygon = new ArrayList<>();
        boolean crossed = false ;

        List<List<Point>> polygons = new ArrayList<>();

        for (Point p : polygonToSplit) {
            if (pointA == null) {
                pointA = p;
                continue;
            }

            pointB = p;

            // Check if the split line cross this line
            LineIntersectsResult result = lineIntersects(pointA, pointB, splitA, splitB);

            if (result != null && result.onLine1() && result.onLine2()) {
                crossed = !crossed ;
                // Cross the line
                Point cross = Point.fromLngLat(result.horizontalIntersection(), result.verticalIntersection());

                if (crossed) {
                    this.addPointsToPolygons(pointA, pointB, cross, initialPolygon, newPolygon);
                } else {
                    this.addPointsToPolygons(pointA, pointB, cross, newPolygon, initialPolygon);

                    polygons.add(newPolygon);
                    newPolygon = new ArrayList<>();
                }
            } else {
                if (!crossed) {
                    this.addPointsToPolygons(pointA, pointB, null, initialPolygon, initialPolygon);
                } else {
                    this.addPointsToPolygons(pointA, pointB, null, newPolygon, newPolygon);
                }
            }
            pointA = pointB;
        }

        polygons.add(initialPolygon);

        display(polygons);
        this.kujakuLayer.removeLayerOnMap(mapboxMap);
        this.deleteAll();
    }

    /**
     * Add points A, B and cross to Lists of points A and B.
     *
     * @param pointA
     * @param pointB
     * @param crossPoint
     * @param polygonA
     * @param polygonB
     */
    private void addPointsToPolygons(@NonNull Point pointA, @NonNull Point pointB, Point crossPoint,  @NonNull List<Point> polygonA,  @NonNull List<Point> polygonB) {
        if (!polygonA.contains(pointA)) {
            polygonA.add(pointA);
        }
        if (crossPoint != null) {
            polygonA.add(crossPoint);
            polygonB.add(crossPoint);
        }
        if (!polygonB.contains(pointB)) {
            polygonB.add(pointB);
        }
    }

    private void display(List<List<Point>> polygons) {
        for (List<Point> list : polygons) {
            List<List<Point>> lists = new ArrayList<>();
            lists.add(list);
            Polygon polygon =  Polygon.fromLngLats(lists);

            Feature feature = Feature.fromGeometry(polygon);
            FeatureCollection collection = FeatureCollection.fromFeature(feature);
            FillBoundaryLayer layer = new FillBoundaryLayer.Builder(collection)
                    .setBoundaryColor(Color.BLACK)
                    .setBoundaryWidth(3f)
                    .build();

            kujakuMapView.addLayer(layer);
        }
    }

    /**
     * Delete circles and line
     */
    private void deleteAll() {
        if (this.circleStart != null) {
            this.circleManager.delete(this.circleStart);
            this.circleStart = null;
        }

        if (this.circleEnd != null) {
            this.circleManager.delete(this.circleEnd);
            this.circleEnd = null;
        }

        this.splittingEnabled = false;

        this.deleteLine();
    }

    /**
     * Delete the line
     */
    private void deleteLine() {
        if (this.splittingLine != null) {
            lineManager.delete(this.splittingLine);
        }
    }


    /**
     * Return true is splitting is enabled
     *
     * @return value of splittingEnabled
     */
    public boolean isSplittingEnabled() {
        return this.splittingEnabled;
    }

    /**
     * Return true if the 2 points have been drawn
     *
     * @return if split() function is ready
     */
    public boolean isSplittingReady() {
        return this.checkSplitPoints();
    }

    /**
     * Set a listener for OnDrawingCircleClickListener
     *
     * @param listener
     */
    public void addOnSplittingClickListener(OnSplittingClickListener listener) {
        this.onSplittingClickListener = listener;
    }

//    /**
//     * Set a listener for the OnDrawingCircleLongClickListener
//     *
//     * @param listener
//     */
//    public void addOnDrawingCircleLongClickListener(OnDrawingCircleLongClickListener listener) {
//        this.onDrawingCircleLongClickListener = listener;
//    }

    private static LineIntersectsResult lineIntersects(Point start, Point end, Point splitStart, Point splitEnd) {
        return lineIntersects(start.longitude(),
                start.latitude(),
                end.longitude(),
                end.latitude(),
                splitStart.longitude(),
                splitStart.latitude(),
                splitEnd.longitude(),
                splitEnd.latitude());
    }

    /**
     * Calculate the intersection point between 2 lines
     *
     * @param line1StartX
     * @param line1StartY
     * @param line1EndX
     * @param line1EndY
     * @param line2StartX
     * @param line2StartY
     * @param line2EndX
     * @param line2EndY
     * @return
     */
    private static LineIntersectsResult lineIntersects(double line1StartX, double line1StartY,
                                                                      double line1EndX, double line1EndY,
                                                                      double line2StartX, double line2StartY,
                                                                      double line2EndX, double line2EndY) {
        // If the lines intersect, the result contains the x and y of the intersection
        // (treating the lines as infinite) and booleans for whether line segment 1 or line
        // segment 2 contain the point
        LineIntersectsResult result = LineIntersectsResult.builder()
                .onLine1(false)
                .onLine2(false)
                .build();

        double denominator = ((line2EndY - line2StartY) * (line1EndX - line1StartX))
                - ((line2EndX - line2StartX) * (line1EndY - line1StartY));
        if (denominator == 0) {
            if (result.horizontalIntersection() != null && result.verticalIntersection() != null) {
                return result;
            } else {
                return null;
            }
        }
        double varA = line1StartY - line2StartY;
        double varB = line1StartX - line2StartX;
        double numerator1 = ((line2EndX - line2StartX) * varA) - ((line2EndY - line2StartY) * varB);
        double numerator2 = ((line1EndX - line1StartX) * varA) - ((line1EndY - line1StartY) * varB);
        varA = numerator1 / denominator;
        varB = numerator2 / denominator;

        // if we cast these lines infinitely in both directions, they intersect here:
        result = result.toBuilder().horizontalIntersection(line1StartX
                + (varA * (line1EndX - line1StartX))).build();
        result = result.toBuilder().verticalIntersection(line1StartY
                + (varA * (line1EndY - line1StartY))).build();

        // if line1 is a segment and line2 is infinite, they intersect if:
        if (varA > 0 && varA < 1) {
            result = result.toBuilder().onLine1(true).build();
        }
        // if line2 is a segment and line1 is infinite, they intersect if:
        if (varB > 0 && varB < 1) {
            result = result.toBuilder().onLine2(true).build();
        }
        // if line1 and line2 are segments, they intersect if both of the above are true
        if (result.onLine1() && result.onLine2()) {
            return result;
        } else {
            return null;
        }
    }
}
