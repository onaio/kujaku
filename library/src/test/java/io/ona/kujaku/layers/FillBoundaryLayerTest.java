package io.ona.kujaku.layers;

import android.graphics.Color;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.mapboxsdk.style.layers.FillLayer;
import com.mapbox.mapboxsdk.style.layers.PropertyValue;
import com.mapbox.mapboxsdk.utils.ColorUtils;

import org.junit.Test;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.ReflectionHelpers;

import java.util.ArrayList;
import java.util.HashMap;

import io.ona.kujaku.test.shadows.ShadowLayer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by Emmanuel OTIN - eo@novel-t.ch on 24/06/2019
 */
public class FillBoundaryLayerTest extends BaseKujakuLayerTest {

    @Test
    public void testFillLayerProperties() throws NoSuchFieldException, IllegalAccessException {
        float textSize = 20f;
        float boundaryWidth = 6f;
        int colorInt = Color.GREEN;
        String labelProperty = "district-name";

        FeatureCollection featureCollection = FeatureCollection.fromFeatures(new ArrayList<Feature>());

        FillBoundaryLayer.Builder builder = new FillBoundaryLayer.Builder(featureCollection)
                .setLabelProperty(labelProperty)
                .setLabelTextSize(textSize)
                .setLabelColorInt(colorInt)
                .setBoundaryColor(colorInt)
                .setBoundaryWidth(boundaryWidth);
        FillBoundaryLayer fillBoundaryLayer = builder.build();

        ReflectionHelpers.callInstanceMethod(fillBoundaryLayer, "createBoundaryFillLayer"
                , ReflectionHelpers.ClassParameter.from(KujakuLayer.Builder.class, builder));

        FillLayer fillLayer = (FillLayer) getValueInPrivateField(FillBoundaryLayer.class, fillBoundaryLayer, "boundaryFillLayer");

        ShadowLayer shadowLayer = (ShadowLayer) Shadow.extract(fillLayer);
        HashMap<String, PropertyValue> propertyValues = shadowLayer.getPropertyValues();

        assertEquals(ColorUtils.colorToRgbaString(colorInt), (String) propertyValues.get("background-color").value);
    }


    @Test
    public void getLayerIdsShouldReturnStringArrayWithLayerIds() {
        float textSize = 20f;
        float boundaryWidth = 6f;
        int colorInt = Color.GREEN;
        String labelProperty = "district-name";

        FeatureCollection featureCollection = FeatureCollection.fromFeatures(new ArrayList<Feature>());

        FillBoundaryLayer fillBoundaryLayer = new FillBoundaryLayer.Builder(featureCollection)
                .setLabelProperty(labelProperty)
                .setLabelTextSize(textSize)
                .setLabelColorInt(colorInt)
                .setBoundaryColor(colorInt)
                .setBoundaryWidth(boundaryWidth)
                .build();

        String[] layerIds = fillBoundaryLayer.getLayerIds();

        assertEquals(3, layerIds.length);
        assertNotNull(layerIds[0]);
        assertNotNull(layerIds[1]);
        assertNotNull(layerIds[2]);
    }
}