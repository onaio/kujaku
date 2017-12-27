package io.ona.kujaku.adapters.holders;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import io.ona.kujaku.R;
import io.ona.kujaku.adapters.InfoWindowAdapter;
import io.ona.kujaku.adapters.InfoWindowObject;

public class InfoWindowViewHolder extends RecyclerView.ViewHolder implements InfoWindowObject.OnFocusChangeListener, View.OnClickListener {
    private static final String TAG = InfoWindowViewHolder.class.getSimpleName();
    private final InfoWindowAdapter adapter;
    private static final float UNSELECTED_OPACITY = 0.5f;
    private static final float SELECTED_OPACITY = 1.0f;
    private static final long ANIMATION_DURATION = 200;

    private InfoWindowObject currentInfoWindowObject;
    private CardView cardView;
    private LinearLayout canvasLayout;
    private ArrayList<TextView> textViews;
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
        this.textViews = new ArrayList<>();
        canvasLayout = cardView.findViewById(R.id.ll_canvas_layout);
        nameTv = cardView.findViewById(R.id.tv_bottomInfoWindow_name);
        textViews.add(nameTv);
        ageTv = cardView.findViewById(R.id.tv_bottomInfoWindow_age);
        textViews.add(ageTv);
        weightTv = cardView.findViewById(R.id.tv_bottomInfoWindow_weight);
        textViews.add(weightTv);
        vaccineTv = cardView.findViewById(R.id.tv_bottomInfoWindow_vaccine);
        textViews.add(vaccineTv);
        labelNameTv = cardView.findViewById(R.id.tv_bottomInfoWindow_labelName);
        textViews.add(labelNameTv);
        labelAgeTv = cardView.findViewById(R.id.tv_bottomInfoWindow_labelAge);
        textViews.add(labelAgeTv);
        labelWeightTv = cardView.findViewById(R.id.tv_bottomInfoWindow_labelWeight);
        textViews.add(labelWeightTv);
        labelVaccineTv = cardView.findViewById(R.id.tv_bottomInfoWindow_labelVaccine);
        textViews.add(labelVaccineTv);
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

    private void startCardViewWidthAnimation(int endWidth, Animator.AnimatorListener animatorListener) {
        final ViewGroup.LayoutParams layoutParams = cardView.getLayoutParams();
        final ValueAnimator anim = ValueAnimator.ofInt(layoutParams.width, endWidth);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int val = (Integer) valueAnimator.getAnimatedValue();
                layoutParams.width = val;
                cardView.setLayoutParams(layoutParams);
            }
        });
        if (animatorListener != null) {
            anim.addListener(animatorListener);
        }
        anim.setDuration(ANIMATION_DURATION);
        anim.start();
    }

    private void startCardViewAlphaAnimation(float endAlpha, Animator.AnimatorListener animatorListener) {
        ValueAnimator anim = ValueAnimator.ofFloat(cardView.getAlpha(), endAlpha);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float val = (Float) valueAnimator.getAnimatedValue();
                cardView.setAlpha(val);
            }
        });
        if (animatorListener != null) {
            anim.addListener(animatorListener);
        }
        anim.setDuration(ANIMATION_DURATION);
        anim.start();
    }

    private void startCardViewHeightAnimation(int endHeight, Animator.AnimatorListener animatorListener) {
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
        if (animatorListener != null) {
            anim.addListener(animatorListener);
        }
        anim.setDuration(ANIMATION_DURATION);
        anim.start();
    }

    private void startCardViewVMarginAnimation(int endMargin, Animator.AnimatorListener animatorListener) {
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
        if (animatorListener != null) {
            anim.addListener(animatorListener);
        }
        anim.setDuration(ANIMATION_DURATION);
        anim.start();
    }

    private void startTextSizeAnimation(float endTextSize, Animator.AnimatorListener animatorListener) {
        ValueAnimator anim = ValueAnimator.ofFloat(textViews.get(0).getPaint().getTextSize(), endTextSize);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float animatedValue = (Float) valueAnimator.getAnimatedValue();
                for (TextView curTextView : textViews) {
                    curTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, animatedValue);
                }
            }
        });
        if (animatorListener != null) {
            anim.addListener(animatorListener);
        }
        anim.setDuration(ANIMATION_DURATION);
        anim.start();
    }

    private void startCanvasPaddingAnimation(int endPadding, Animator.AnimatorListener animatorListener) {
        ValueAnimator anim = ValueAnimator.ofInt(canvasLayout.getPaddingTop(), endPadding);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int animatedValue = (Integer) valueAnimator.getAnimatedValue();
                canvasLayout.setPadding(animatedValue, animatedValue, animatedValue, animatedValue);
            }
        });
        if (animatorListener != null) {
            anim.addListener(animatorListener);
        }
        anim.setDuration(ANIMATION_DURATION);
        anim.start();
    }

    private void moveCardToCenterScreen() {
        adapter.getRecyclerView().scrollToPosition(currentInfoWindowObject.getPosition());
    }

    public void select() {
        moveCardToCenterScreen();
        startCardViewWidthAnimation(adapter.getContext().getResources().getDimensionPixelSize(R.dimen.info_window_focus_width), null);
        startCardViewAlphaAnimation(SELECTED_OPACITY, null);
        startCardViewHeightAnimation(adapter.getContext().getResources().getDimensionPixelSize(R.dimen.info_window_focus_height), null);
        startCardViewVMarginAnimation(adapter.getContext().getResources().getDimensionPixelSize(R.dimen.info_window_focus_v_margin), null);
        startTextSizeAnimation(adapter.getContext().getResources().getDimensionPixelSize(R.dimen.info_window_focus_text_size), null);
        startCanvasPaddingAnimation(adapter.getContext().getResources().getDimensionPixelSize(R.dimen.info_window_focus_padding), new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (!currentInfoWindowObject.isFocused()) {
                    unselect();
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }

    public void unselect() {
        startCardViewWidthAnimation(adapter.getContext().getResources().getDimensionPixelSize(R.dimen.info_window_nonfocus_width), null);
        startCardViewAlphaAnimation(UNSELECTED_OPACITY, null);
        startCardViewHeightAnimation(adapter.getContext().getResources().getDimensionPixelSize(R.dimen.info_window_nonfocus_height), null);
        startCardViewVMarginAnimation(adapter.getContext().getResources().getDimensionPixelSize(R.dimen.info_window_nonfocus_v_margin), null);
        startTextSizeAnimation(adapter.getContext().getResources().getDimensionPixelSize(R.dimen.info_window_nonfocus_text_size), null);
        startCanvasPaddingAnimation(adapter.getContext().getResources().getDimensionPixelSize(R.dimen.info_window_nonfocus_padding), new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (currentInfoWindowObject.isFocused()) {
                    select();
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
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