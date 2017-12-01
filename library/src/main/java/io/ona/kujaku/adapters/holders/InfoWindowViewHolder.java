package io.ona.kujaku.adapters.holders;

import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import io.ona.kujaku.R;
import io.ona.kujaku.adapters.InfoWindowAdapter;
import io.ona.kujaku.adapters.InfoWindowObject;

public class InfoWindowViewHolder extends RecyclerView.ViewHolder implements InfoWindowObject.OnFocusChangeListener, View.OnClickListener {
    private final InfoWindowAdapter adapter;
    private static final float UNSELECTED_OPACITY = 0.5f;
    private static final float SELECTED_OPACITY = 1.0f;
    private static final int SELECTED_SIZE_DIFFERENCE_DP = 10;
    private static final int ANIMATE_RESIZE_DURATION = 1000;

    private InfoWindowObject currentInfoWindowObject;

    private View parent;
    private CardView cardView;
    private TextView nameTv;
    private TextView ageTv;
    private TextView weightTv;
    private TextView vaccineTv;
    private TextView labelNameTv;
    private TextView labelAgeTv;
    private TextView labelWeightTv;
    private TextView labelVaccineTv;

    public InfoWindowViewHolder(@NonNull InfoWindowAdapter adapter, View parent) {
        super(parent);

        this.adapter = adapter;

        cardView = parent.findViewById(R.id.cv_bottomInfoWindow_cardView);
        nameTv = parent.findViewById(R.id.tv_bottomInfoWindow_name);
        ageTv = parent.findViewById(R.id.tv_bottomInfoWindow_age);
        weightTv = parent.findViewById(R.id.tv_bottomInfoWindow_weight);
        vaccineTv = parent.findViewById(R.id.tv_bottomInfoWindow_vaccine);

        labelNameTv = parent.findViewById(R.id.tv_bottomInfoWindow_labelName);
        labelAgeTv = parent.findViewById(R.id.tv_bottomInfoWindow_labelAge);
        labelWeightTv = parent.findViewById(R.id.tv_bottomInfoWindow_labelWeight);
        labelVaccineTv = parent.findViewById(R.id.tv_bottomInfoWindow_labelVaccine);
        this.parent = parent;
        this.parent.setOnClickListener(this);
    }

    public void setData(InfoWindowObject infoWindowObject) throws JSONException {
        this.currentInfoWindowObject = infoWindowObject;
        this.currentInfoWindowObject.setOnFocusChangeListener(this);
        String[] priorityFields = new String[]{
                "first_name",
                "Birth_Weight",
                "Place_Birth",
                "zeir_id"
        };
        TextView[] tvs = new TextView[]{nameTv, ageTv, weightTv, vaccineTv};
        TextView[] tvLabels = new TextView[]{labelNameTv, labelAgeTv, labelWeightTv, labelVaccineTv};
        int fromPriorityFieldIndex = 0;

        JSONObject jsonObject = infoWindowObject.getJsonObject();

        if (jsonObject.has("properties")) {
            JSONObject propertiesJSON = jsonObject.getJSONObject("properties");

            for (int i = 0; i < tvs.length; i++) {
                TextView tv = tvs[i];
                TextView labelTv = tvLabels[i];

                for (int j = fromPriorityFieldIndex; j < priorityFields.length; j++) {
                    String priorityField = priorityFields[j];

                    if (propertiesJSON.has(priorityField)) {
                        labelTv.setText(humanizeFieldName(priorityField) + ": ");
                        tv.setText(propertiesJSON.getString(priorityField));
                        fromPriorityFieldIndex = j + 1;
                        break;
                    } else {
                        // Select a substitude field or disable the TextView(s)
                        labelTv.setVisibility(View.GONE);
                        tv.setVisibility(View.GONE);
                    }

                }
            }
        }
        updateFocusViews();
    }

    public void select() {
        parent.setAlpha(SELECTED_OPACITY);
        int increasePx = (int) getPx(SELECTED_SIZE_DIFFERENCE_DP);
    }

    public void unselect() {
        parent.setAlpha(UNSELECTED_OPACITY);
    }

    private float getPx(int dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, parent.getResources().getDisplayMetrics());
    }

    private String humanizeFieldName(String fieldName) {
        // Replace underscores with spaces
        // Capitalize the words
        fieldName = fieldName.replace("_", " ");
        fieldName = capitalize(fieldName);

        // Todo: Capitalize all words

        // Change abbreviations to Uppercase
        String[] abbreviations = new String[]{
                "id",
                "zeir"
        };

        for (String abbreviation : abbreviations) {
            fieldName = fieldName.replace(abbreviation, abbreviation.toUpperCase());
        }

        return fieldName;
    }

    private String capitalize(String name) {
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    private void updateFocusViews() {
        if (currentInfoWindowObject.isFocused()) {
            select();
        } else {
            unselect();
        }
    }

    @Override
    public void onFocusChanged(InfoWindowObject object) {
        if (currentInfoWindowObject == object) {
            updateFocusViews();
        }
    }

    @Override
    public void onClick(View v) {
        if (v == parent && currentInfoWindowObject != null) {
            adapter.focusOnPosition(currentInfoWindowObject.getPosition());
        }
    }
}