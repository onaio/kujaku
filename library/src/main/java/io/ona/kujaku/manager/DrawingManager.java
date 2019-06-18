package io.ona.kujaku.manager;

import android.graphics.PointF;
import android.support.annotation.NonNull;

import com.mapbox.geojson.Feature;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapView;
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

import io.ona.kujaku.listeners.OnDrawingCircleClickListener;
import io.ona.kujaku.listeners.OnDrawingCircleLongClickListener;

public class DrawingManager {

    private List<KujakuCircle> circles;
    private KujakuCircle currentCircle;

    private FillManager fillManager;
    private LineManager lineManager;
    private CircleManager circleManager;

    private OnDrawingCircleClickListener onDrawingCircleClickListener;
    private OnDrawingCircleLongClickListener onDrawingCircleLongClickListener;

    public static KujakuCircleOptions circleOptions = new KujakuCircleOptions()
            .withCircleRadius(10.0f)
            .withCircleColor("black")
            .withMiddleCircle(false)
            .withDraggable(false);

    public static KujakuCircleOptions circleMiddleOptions = new KujakuCircleOptions()
            .withCircleRadius(5.0f)
            .withCircleColor("black")
            .withMiddleCircle(true)
            .withDraggable(false);

    public static KujakuCircleOptions circleDraggableOptions = new KujakuCircleOptions()
            .withCircleRadius(20.0f)
            .withCircleColor("red")
            .withMiddleCircle(false)
            .withDraggable(true);


    public DrawingManager(@NonNull MapView mapView, @NonNull MapboxMap mapboxMap, @NonNull Style style) {
        this.circles = new ArrayList<>();
        currentCircle = null;
        //firstCircle = null;

        fillManager = new FillManager(mapView, mapboxMap, style);
        lineManager = new LineManager(mapView, mapboxMap, style);
        circleManager = new CircleManager(mapView, mapboxMap, style);

        circleManager.addClickListener(new OnCircleClickListener() {
            @Override
            public void onAnnotationClick(Circle circle) {
                //setCurrentCircle(circle);

                if (onDrawingCircleClickListener != null) {
                    onDrawingCircleClickListener.onCircleClick(circle);
                }
            }
        });

        circleManager.addLongClickListener(new OnCircleLongClickListener() {
                @Override
                public void onAnnotationLongClick(Circle circle) {
                    // setCurrentCircle(circle);

                   if (onDrawingCircleLongClickListener != null) {
                       onDrawingCircleLongClickListener.onCircleLongClick(circle);
                   }
                }
            });

        circleManager.addDragListener(new OnCircleDragListener() {
            @Override
            // Left empty on purpose
            public void onAnnotationDragStarted(Circle circle) {
            }

            @Override
            // Left empty on purpose
            public void onAnnotationDrag(Circle circle) {
                refreshPolygon();
            }

            @Override
            // Left empty on purpose
            public void onAnnotationDragFinished(Circle circle) {
            }
        });

        mapboxMap.addOnMapClickListener(new MapboxMap.OnMapClickListener() {
            @Override
            public boolean onMapClick(@NonNull LatLng point) {
                final PointF pixel = mapboxMap.getProjection().toScreenLocation(point);
                List<Feature> features = mapboxMap.queryRenderedFeatures(pixel, (Expression) null, CircleManager.ID_GEOJSON_LAYER);

                if (features.size() == 0) {
                    if (onDrawingCircleClickListener != null && getCurrentKujakuCircle() == null) {
                        onDrawingCircleClickListener.onCircleNotClick(point);
                    }
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
                if (features.size() == 0) {
                    if (onDrawingCircleLongClickListener != null) {
                        onDrawingCircleLongClickListener.onCircleNotLongClick(point);
                    }
                }

                return false;
            }
        });
    }

    /**
     * Set currentCircle when circle is clicked or longClicked
     *
     * @param circle
     */
    private void setCurrentCircle(Circle circle) {
        currentCircle = getKujakuCircle(circle);
    }

    /**
     * Retrieve the KujakuCircle corresponding to the Circle
     *
     * @param circle
     * @return
     */
    private KujakuCircle getKujakuCircle(Circle circle) {
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
                        newCirclesOptions.add(this.getKujakuCircles().get(i).getCircleOptions());
                    } else {
                        circle2 = circle.getCircle();
                    }
                }

                if (circle1 != null && circle2 != null) {
                    KujakuCircleOptions newKujakuCircleOptions = this.createMiddleKujakuCircleOptions(circle1, circle2, circleMiddleOptions) ;
                    newCirclesOptions.add(newKujakuCircleOptions);
                    newCirclesOptions.add(this.getKujakuCircles().get(i).getCircleOptions());

                    circle1 = circle2;
                    circle2 = null;
                }
            }

            if (circle1 != null) {
                circle2 = this.getKujakuCircles().get(0).getCircle();

                KujakuCircleOptions newKujakuCircleOptions = this.createMiddleKujakuCircleOptions(circle1, circle2, circleMiddleOptions) ;
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
    private KujakuCircleOptions createMiddleKujakuCircleOptions(Circle circle1, Circle circle2, KujakuCircleOptions options) {
        double lonEast = circle1.getLatLng().getLongitude() > circle2.getLatLng().getLongitude() ? circle1.getLatLng().getLongitude() : circle2.getLatLng().getLongitude();
        double lonWest = circle1.getLatLng().getLongitude() > circle2.getLatLng().getLongitude() ? circle2.getLatLng().getLongitude() : circle1.getLatLng().getLongitude();
        double latNorth = circle1.getLatLng().getLatitude() > circle2.getLatLng().getLatitude() ? circle1.getLatLng().getLatitude() : circle2.getLatLng().getLatitude() ;
        double latSouth = circle1.getLatLng().getLatitude() > circle2.getLatLng().getLatitude() ? circle2.getLatLng().getLatitude() : circle1.getLatLng().getLatitude() ;

        LatLng latLng =
                LatLngBounds.from(latNorth, lonEast, latSouth, lonWest).getCenter();

        return new KujakuCircleOptions()
                .withMiddleCircle(options.getMiddleCircle())
                .withDraggable(options.getDraggable())
                .withCircleRadius(options.getCircleRadius())
                .withCircleColor(options.getCircleColor())
                .withLatLng(latLng);
    }

    /**
     * Refresh the entire Polygon appearance
     *
     */
    private void refreshPolygon() {
        fillManager.deleteAll();
        fillManager.updateSource();
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

            if (this.getKujakuCircles().size() > 2) { // on ajoute le premier point en fin de liste pour tracer la ligne
                list.add(this.getKujakuCircles().get(0).getCircle().getLatLng());
            }

            lineManager.create(new LineOptions()
                    .withLatLngs(list)
                    .withLineOpacity(Float.valueOf("0.5")));
        }
    }

    /***
     * Create a new Circle and add it to the circle list
     *
     * @param options
     * @return
     */
    public Circle create(@NonNull KujakuCircleOptions options) {
        Circle circle = circleManager.create(options);
        KujakuCircle previousCircle = null;
        if (circles.size() > 0) {
            previousCircle = circles.get(circles.size() -1);
        }
        KujakuCircle kujakuCircle = new KujakuCircle(circle, previousCircle, options.getMiddleCircle());
        circles.add(kujakuCircle);

        return circle;
    }

    /**
     * Create circles from list of KujakuCircleOptions
     *
     * @param options
     */
    private void createFromList(@NonNull List<KujakuCircleOptions> options) {
        for (KujakuCircleOptions option: options) {
            this.create(option);
        }
        if (this.circles.size() >= 2) {
            this.getKujakuCircles().get(0).setPreviousCircle(this.getKujakuCircles().get(this.getKujakuCircles().size() - 1));
        }
    }

    /**
     * Refresh the Polygon
     */
    public void refresh() {
        this.createMiddlePoints();
        this.refreshPolygon();
    }

    /**
     * Delete all circles
     */
    private void deleteAll() {
        this.circles.clear();
        this.circleManager.deleteAll();
    }

    /**
     * Delete a Circle and the middles circles before and after
     *
     * @param kujakuCircle
     */
    public void delete(KujakuCircle kujakuCircle) {
        // Previous and next circle to delete id AreMiddle
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
        this.refresh();
    }

    public void setDraggable(boolean draggable, Circle circle) {
        circle.setDraggable(draggable);
        KujakuCircleOptions options;

        if (draggable) {
            options = circleDraggableOptions;
            this.setCurrentCircle(circle);
        } else {
            options = circleOptions;
            this.setCurrentCircle(null);
        }

        circle.setCircleColor(options.getCircleColor());
        circle.setCircleRadius(options.getCircleRadius());

        if (draggable) {
            KujakuCircle previousCircle = null;
            KujakuCircle nextCircle = null;

            for (KujakuCircle c : circles) {
                if (c.getCircle().getId() == circle.getId()) {
                    this.currentCircle = c;
                    c.setMiddleCircle(options.getMiddleCircle());
                    previousCircle = c.getPreviousKujakuCircle();
                    nextCircle = c.getNextKujakuCircle();
                    break;
                }
            }

            if (previousCircle != null && previousCircle.isMiddleCircle()) {
                circleManager.delete(previousCircle.getCircle());
                if (previousCircle.getPreviousKujakuCircle() != null) {
                    previousCircle.getPreviousKujakuCircle().setNextCircle(this.currentCircle);
                }
                this.circles.remove(previousCircle);
            }

            if (nextCircle != null && nextCircle.isMiddleCircle()) {
                circleManager.delete(nextCircle.getCircle());
                if (nextCircle.getNextKujakuCircle() != null) {
                    nextCircle.getNextKujakuCircle().setPreviousCircle(this.currentCircle);
                }
                this.circles.remove(nextCircle);
            }
        }

        circleManager.update(circle);
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
    private List<KujakuCircle> getKujakuCircles() {
        return circles;
    }

    /**
     * Return the current selected Kujaku Circle
     *
     * @return
     */
    public KujakuCircle getCurrentKujakuCircle() {
        return this.currentCircle;
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
