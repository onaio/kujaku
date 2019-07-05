package io.ona.kujaku.layers;

import android.support.annotation.NonNull;

import com.mapbox.geojson.FeatureCollection;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.FillLayer;
import com.mapbox.mapboxsdk.style.layers.Layer;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Extends BoundaryLayer by adding a new Fill Layer
 *
 * Created by Emmanuel Otin - eo@novel-t.ch on 20/06/2019
 */
public class FillBoundaryLayer extends BoundaryLayer {

    private String BOUNDARY_FILL_LAYER_ID = UUID.randomUUID().toString();

    private FillLayer boundaryFillLayer;

    private FillBoundaryLayer(@NonNull KujakuLayer.Builder builder) {
       super(builder);
    }

    private void createBoundaryFillLayer(@NonNull KujakuLayer.Builder builder) {
        boundaryFillLayer = new FillLayer(BOUNDARY_FILL_LAYER_ID, BOUNDARY_FEATURE_SOURCE_ID)
                .withProperties(
                        PropertyFactory.backgroundColor(builder.boundaryColor),
                        PropertyFactory.fillOpacity(0.5f)
                );
    }

    @Override @NonNull
    public String[] getLayerIds() {
        List<String> both = new ArrayList<>();

        Collections.addAll(both, super.getLayerIds());
        Collections.addAll(both, BOUNDARY_FILL_LAYER_ID) ;

        return both.toArray(new String[both.size()]);
    }

    @Override
    protected void createLayers(@NonNull MapboxMap mapboxMap) {
        // Create Parent layers
        super.createLayers(mapboxMap);

        if (mapboxMap.getStyle().getLayer(BOUNDARY_FILL_LAYER_ID) != null) {
            BOUNDARY_FILL_LAYER_ID = UUID.randomUUID().toString();
        }

        createBoundaryFillLayer(builder);
    }

    @Override
    protected ArrayList<Layer> getLayers(@NonNull MapboxMap mapboxMap) {
        ArrayList<Layer> layers = super.getLayers(mapboxMap);
        layers.add(mapboxMap.getStyle().getLayerAs(BOUNDARY_FILL_LAYER_ID));

        return layers;
    }

    @Override
    protected void addLayersBelow(@NonNull MapboxMap mapboxMap) {
        super.addLayersBelow(mapboxMap);
        mapboxMap.getStyle().addLayerBelow(boundaryFillLayer, builder.belowLayerId);
    }

    @Override
    protected void addLayers(@NonNull MapboxMap mapboxMap) {
        super.addLayers(mapboxMap);
        mapboxMap.getStyle().addLayer(boundaryFillLayer);
    }

    @Override
    protected void removeLayers(@NonNull Style style) {
        super.removeLayers(style);
        style.removeLayer(boundaryFillLayer);
    }

    public static class Builder extends KujakuLayer.Builder<FillBoundaryLayer, Builder> {

        public Builder(@NonNull FeatureCollection featureCollection) {
            super(featureCollection);
        }

        /** The solution for the unchecked cast warning. */
        public Builder getThis() {
            return this;
        }

        public FillBoundaryLayer build() {
            return new FillBoundaryLayer(this);
        }
    }
}
