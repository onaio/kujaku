package io.ona.kujaku.plugin.switcher;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.mapbox.mapboxsdk.maps.Style;

import java.util.ArrayList;

import io.ona.kujaku.R;
import io.ona.kujaku.plugin.switcher.layer.BaseLayer;
import io.ona.kujaku.views.KujakuMapView;
import timber.log.Timber;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 2019-05-16
 */

public class BaseLayerSwitcherPlugin implements AdapterView.OnItemClickListener {

    private KujakuMapView kujakuMapView;
    private Style style;
    private FloatingActionButton layerSwitcherFab;

    @Nullable
    private BaseLayer currentBaseLayer;

    private ArrayList<BaseLayer> baseLayers = new ArrayList<>();
    private PopupWindow listPopupWindow;
    private ArrayAdapter<String> popupWindowAdapter;

    public BaseLayerSwitcherPlugin(@NonNull KujakuMapView kujakuMapView, @NonNull Style style) {
        this.kujakuMapView = kujakuMapView;
        this.style = style;
    }

    public boolean addBaseLayer(@NonNull BaseLayer baseLayer, boolean isDefault) {
        if (baseLayers.contains(baseLayer)) {
            // Todo: switch the currentBaseLayer to this one here
            if (isDefault)
            return false;
        }

        for (BaseLayer addedBaseLayer: baseLayers) {
            if (addedBaseLayer.getId().equals(baseLayer.getId())) {
                // Todo: switch the currentBaseLayer to this one here
                return false;
            }
        }

        this.baseLayers.add(baseLayer);
        if (listPopupWindow != null) {
            if (popupWindowAdapter != null) {
                popupWindowAdapter.add(baseLayer.getDisplayName());
            } else {
                Timber.e("Cannot update popupWindowAdapter because it is null");
            }

        }
        return true;
    }

    public boolean removeBaseLayerFromMap(@NonNull BaseLayer baseLayer) {
        kujakuMapView.removeLayer(baseLayer);
        return true;
    }

    public boolean removeBaseLayer(@NonNull BaseLayer baseLayer) {
        if (baseLayers.remove(baseLayer)) {
            if (listPopupWindow != null) {
                if (popupWindowAdapter != null) {
                    popupWindowAdapter.remove(baseLayer.getDisplayName());
                } else {
                    Timber.e("Cannot update popupWindowAdapter because it is null");
                }

            }
            return true;
        } else {
            for (BaseLayer addedBaseLayer : baseLayers) {
                if (addedBaseLayer.getId().equals(baseLayer.getId())) {
                    baseLayers.remove(addedBaseLayer);

                    if (listPopupWindow != null) {
                        if (popupWindowAdapter != null) {
                            popupWindowAdapter.remove(baseLayer.getDisplayName());
                        } else {
                            Timber.e("Cannot update popupWindowAdapter because it is null");
                        }

                    }
                    return true;
                }
            }

            return false;
        }
    }

    @NonNull
    public ArrayList<BaseLayer> getBaseLayers() {
        return baseLayers;
    }

    public void show() {
        showPluginSwitcherView();
    }

    public void showPluginSwitcherView() {
        if (kujakuMapView != null) {
            if (layerSwitcherFab == null) {
                layerSwitcherFab = kujakuMapView.findViewById(R.id.fab_mapview_layerSwitcher);
            }

            layerSwitcherFab.setVisibility(View.VISIBLE);
            layerSwitcherFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listPopupWindow == null) {

                        if (baseLayers.size() > 0) {
                            showPopup(layerSwitcherFab);
                        } else {
                            Toast.makeText(kujakuMapView.getContext(), R.string.no_base_layers_availabe, Toast.LENGTH_LONG)
                                    .show();
                        }
                    } else {
                        if (listPopupWindow.isShowing()) {
                            listPopupWindow.dismiss();
                        } else {
                            showPopup(layerSwitcherFab);
                        }
                    }
                }
            });
        }
    }

    public void showPopup(@NonNull View anchorView) {
        if (listPopupWindow == null) {
            Context context = kujakuMapView.getContext();
            listPopupWindow = new PopupWindow(context, null, 0, R.style.Widget_AppCompat_Light_ListPopupWindow);
            listPopupWindow.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
            listPopupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);

            View view = LayoutInflater.from(context).inflate(R.layout.popupwindow_layer_switcher_selector, null);
            listPopupWindow.setContentView(view);

            String[] layerNames = new String[getBaseLayers().size()];

            int counter = 0;
            int selectedPosition = -1;
            for (BaseLayer baseLayer : getBaseLayers()) {
                layerNames[counter] = baseLayer.getDisplayName();

                if (currentBaseLayer != null && (baseLayer == currentBaseLayer || baseLayer.getId().equals(currentBaseLayer.getId()))) {
                    selectedPosition = counter;
                }

                counter++;
            }

            popupWindowAdapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, layerNames);
            ListView listView = view.findViewById(R.id.lv_popupWindowLayerSwitcher_layerList);
            listView.setAdapter(popupWindowAdapter);

            listView.setOnItemClickListener(this);
            listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

            if (selectedPosition != -1) {
                listView.setItemChecked(selectedPosition, true);
            }
        }

        //listPopupWindow.showAsDropDown(anchorView);
        Context context = anchorView.getContext();

        if (context instanceof Activity) {
            DisplayMetrics displayMetrics = new DisplayMetrics();
            ((Activity) context).getWindowManager()
                    .getDefaultDisplay()
                    .getMetrics(displayMetrics);

            int xOffset = displayMetrics.widthPixels - Float.valueOf(anchorView.getX()).intValue();

            int[] screenPosition = new int[2];
            kujakuMapView.getLocationOnScreen(screenPosition);
            int yOffset = displayMetrics.heightPixels - (screenPosition[1] + kujakuMapView.getMeasuredHeight());

            listPopupWindow.showAtLocation(anchorView, Gravity.BOTTOM | Gravity.RIGHT, xOffset, yOffset);
        }
    }

    private int getPx(@NonNull Context context, float dpValue) {
        Resources r = context.getResources();
        float px = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dpValue,
                r.getDisplayMetrics()
        );

        return Float.valueOf(px).intValue();
    }

    public void showBaseLayer(@NonNull BaseLayer baseLayer) {
        if (style.isFullyLoaded()) {
            // Remove the previous baseLayer
            if (currentBaseLayer != null) {
                // Todo: Figure out this issue, where "Cannot Add Already Added Layer" or "Memory Violation"
                //removeBaseLayerFromMap(currentBaseLayer);
                kujakuMapView.disableLayer(currentBaseLayer);
                currentBaseLayer = null;
            }

            kujakuMapView.addLayer(baseLayer);
            currentBaseLayer = baseLayer;
        } else {
            Timber.e("The style has not fully loaded and the baseLayer %s of ID(%s) cannot be added"
                    , baseLayer.getDisplayName(), baseLayer.getId());
        }
    }

    private void removePreviousBaseLayer(@NonNull BaseLayer baseLayer) {
        kujakuMapView.removeLayer(baseLayer);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (parent.getSelectedItemPosition() == position) {
            return;
        }

        String displayName = (String) parent.getItemAtPosition(position);
        for (BaseLayer baseLayer: baseLayers) {
            if (baseLayer.getDisplayName().equals(displayName)) {

                // Disable the current checked item
                view.setSelected(true);
                parent.setSelection(position);
                showBaseLayer(baseLayer);
            }
        }
    }
}
