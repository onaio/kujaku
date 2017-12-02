package io.ona.kujaku.adapters.holders;

import android.animation.ValueAnimator;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
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
    private CardView cardView;
    private TextView nameTv;
    private TextView ageTv;
    private TextView weightTv;
    private TextView vaccineTv;
    private TextView labelNameTv;
    private TextView labelAgeTv;
    private TextView labelWeightTv;
    private TextView labelVaccineTv;

    public InfoWindowViewHolder(@NonNull InfoWindowAdapter adapter, CardView cardView) {
        super(cardView);

        this.adapter = adapter;
        this.cardView = cardView;
        nameTv = cardView.findViewById(R.id.tv_bottomInfoWindow_name);
        ageTv = cardView.findViewById(R.id.tv_bottomInfoWindow_age);
        weightTv = cardView.findViewById(R.id.tv_bottomInfoWindow_weight);
        vaccineTv = cardView.findViewById(R.id.tv_bottomInfoWindow_vaccine);
        labelNameTv = cardView.findViewById(R.id.tv_bottomInfoWindow_labelName);
        labelAgeTv = cardView.findViewById(R.id.tv_bottomInfoWindow_labelAge);
        labelWeightTv = cardView.findViewById(R.id.tv_bottomInfoWindow_labelWeight);
        labelVaccineTv = cardView.findViewById(R.id.tv_bottomInfoWindow_labelVaccine);
        this.cardView.setOnClickListener(this);
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

    private void startCardViewWidthAnimation(int endWidth) {
        final ViewGroup.LayoutParams layoutParams = cardView.getLayoutParams();
        ValueAnimator anim = ValueAnimator.ofInt(layoutParams.width, endWidth);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int val = (Integer) valueAnimator.getAnimatedValue();
                layoutParams.width = val;
                cardView.setLayoutParams(layoutParams);
            }
        });
        anim.setDuration(200);
        anim.start();
    }

    private void startCardViewAlphaAnimation(float endAlpha) {
        ValueAnimator anim = ValueAnimator.ofFloat(cardView.getAlpha(), endAlpha);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float val = (Float) valueAnimator.getAnimatedValue();
                cardView.setAlpha(val);
            }
        });
        anim.setDuration(200);
        anim.start();
    }

    private void startCardViewHeightAnimation(int endHeight) {
        final ViewGroup.LayoutParams layoutParams = cardView.getLayoutParams();
        ValueAnimator anim = ValueAnimator.ofInt(layoutParams.height, endHeight);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int val = (Integer) valueAnimator.getAnimatedValue();
                layoutParams.height = val;
                cardView.setLayoutParams(layoutParams);
            }
        });
        anim.setDuration(200);
        anim.start();
    }

    private void startCardViewVMarginAnimation(int endMargin) {
        final ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) cardView.getLayoutParams();
        ValueAnimator anim = ValueAnimator.ofInt(layoutParams.bottomMargin, endMargin);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int val = (Integer) valueAnimator.getAnimatedValue();
                layoutParams.bottomMargin = val;
                layoutParams.topMargin = val;
                cardView.setLayoutParams(layoutParams);
            }
        });
        anim.setDuration(200);
        anim.start();
    }

    public void select() {
        startCardViewWidthAnimation(adapter.getContext().getResources().getDimensionPixelSize(R.dimen.info_window_focus_width));
        startCardViewAlphaAnimation(SELECTED_OPACITY);
        startCardViewHeightAnimation(adapter.getContext().getResources().getDimensionPixelSize(R.dimen.info_window_focus_height));
        startCardViewVMarginAnimation(adapter.getContext().getResources().getDimensionPixelSize(R.dimen.info_window_focus_v_margin));
    }

    public void unselect() {
        startCardViewWidthAnimation(adapter.getContext().getResources().getDimensionPixelSize(R.dimen.info_window_nonfocus_width));
        startCardViewAlphaAnimation(UNSELECTED_OPACITY);
        startCardViewHeightAnimation(adapter.getContext().getResources().getDimensionPixelSize(R.dimen.info_window_nonfocus_height));
        startCardViewVMarginAnimation(adapter.getContext().getResources().getDimensionPixelSize(R.dimen.info_window_nonfocus_v_margin));
    }

    private int getPx(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, cardView.getResources().getDisplayMetrics());
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
        if (v == cardView && currentInfoWindowObject != null) {
            adapter.focusOnPosition(currentInfoWindowObject.getPosition());
        }
    }
}