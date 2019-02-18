package io.ona.kujaku.layers;

import android.graphics.Color;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.mapboxsdk.style.expressions.Expression;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.PropertyValue;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.utils.ColorUtils;

import org.junit.Test;
import org.robolectric.annotation.Config;
import org.robolectric.shadow.api.Shadow;

import java.util.ArrayList;
import java.util.HashMap;

import io.ona.kujaku.BaseTest;
import io.ona.kujaku.test.shadows.ShadowGeoJsonSource;
import io.ona.kujaku.test.shadows.ShadowLayer;
import io.ona.kujaku.test.shadows.ShadowLineLayer;
import io.ona.kujaku.test.shadows.ShadowSymbolLayer;

import static org.junit.Assert.*;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 18/02/2019
 */

@Config(shadows = {ShadowGeoJsonSource.class, ShadowLineLayer.class, ShadowLayer.class, ShadowSymbolLayer.class})
public class BoundaryLayerTest extends BaseTest {

    @Test
    public void testLineLayerProperties() throws NoSuchFieldException, IllegalAccessException {
        float textSize = 20f;
        float boundaryWidth = 6f;
        int colorInt = Color.GREEN;
        String labelProperty = "district-name";

        FeatureCollection featureCollection = FeatureCollection.fromFeatures(new ArrayList<Feature>());

        BoundaryLayer boundaryLayer = new BoundaryLayer.Builder(featureCollection)
                .setLabelProperty(labelProperty)
                .setLabelTextSize(textSize)
                .setLabelColorInt(colorInt)
                .setBoundaryColor(colorInt)
                .setBoundaryWidth(boundaryWidth)
                .build();

        LineLayer lineLayer = (LineLayer) getValueInPrivateField(BoundaryLayer.class, boundaryLayer, "boundaryLineLayer");

        ShadowLayer shadowLayer = (ShadowLayer) Shadow.extract(lineLayer);
        HashMap<String, PropertyValue> propertyValues = shadowLayer.getPropertyValues();

        assertEquals(boundaryWidth, (float) propertyValues.get("line-width").value, 0f);
        assertEquals(ColorUtils.colorToRgbaString(colorInt), (String) propertyValues.get("line-color").value);
    }

    @Test
    public void testSymbolLayerProperties() throws NoSuchFieldException, IllegalAccessException {
        float textSize = 20f;
        float boundaryWidth = 6f;
        int colorInt = Color.GREEN;
        String labelProperty = "district-name";

        FeatureCollection featureCollection = FeatureCollection.fromFeatures(new ArrayList<Feature>());

        BoundaryLayer boundaryLayer = new BoundaryLayer.Builder(featureCollection)
                .setLabelProperty(labelProperty)
                .setLabelTextSize(textSize)
                .setLabelColorInt(colorInt)
                .setBoundaryColor(colorInt)
                .setBoundaryWidth(boundaryWidth)
                .build();

        SymbolLayer symbolLayer = (SymbolLayer) getValueInPrivateField(BoundaryLayer.class, boundaryLayer, "boundaryLabelLayer");

        ShadowLayer shadowLayer = (ShadowLayer) Shadow.extract(symbolLayer);
        HashMap<String, PropertyValue> propertyValues = shadowLayer.getPropertyValues();

        assertEquals(textSize, (float) propertyValues.get("text-size").value, 0f);
        assertEquals(ColorUtils.colorToRgbaString(colorInt), (String) propertyValues.get("text-color").value);
        assertTrue(((Expression) propertyValues.get("text-field").value).toString().contains(labelProperty));
    }
}