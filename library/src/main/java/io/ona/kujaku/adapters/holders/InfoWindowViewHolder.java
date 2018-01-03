package io.ona.kujaku.adapters.holders;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import io.ona.kujaku.R;
import io.ona.kujaku.adapters.InfoWindowAdapter;
import io.ona.kujaku.adapters.InfoWindowObject;
import io.ona.kujaku.utils.Views;
import utils.config.InfoWindowConfig;
import utils.config.KujakuConfig;
import utils.helpers.MapBoxStyleHelper;

public class InfoWindowViewHolder extends RecyclerView.ViewHolder
        implements InfoWindowObject.OnFocusChangeListener, View.OnClickListener {
    private static final String TAG = InfoWindowViewHolder.class.getSimpleName();
    private static final float UNSELECTED_OPACITY = 0.5f;
    private static final float SELECTED_OPACITY = 1.0f;
    private static final long ANIMATION_DURATION = 200;

    private final InfoWindowAdapter adapter;
    private final CardView cardView;
    private final ArrayList<TextView> propertyLabels;
    private final ArrayList<TextView> propertyValues;
    private InfoWindowConfig infoWindowConfig;
    private InfoWindowObject currentInfoWindowObject;
    private LinearLayout canvasLayout;

    public InfoWindowViewHolder(@NonNull InfoWindowAdapter adapter, @NonNull CardView cardView) {
        super(cardView);
        this.adapter = adapter;
        this.cardView = cardView;
        this.propertyLabels = new ArrayList<>();
        this.propertyValues = new ArrayList<>();
        canvasLayout = cardView.findViewById(R.id.ll_canvas_layout);

        this.cardView.setOnClickListener(this);
    }

    /**
     * Resets the stored configuration as well as all the views affected by the configuration.
     *
     * @param config    The new configuration to be applied
     * @throws JSONException If unable to parse the new configuration
     */
    public void setInfoWindowConfig(@NonNull InfoWindowConfig config)
            throws JSONException {
        this.infoWindowConfig = config;
        removeDynamicViews();
        JSONArray visibleProperties = infoWindowConfig.getVisibleProperties();
        if (visibleProperties != null) {
            for (int i = 0; i < visibleProperties.length(); i++) {
                createPropertyViews(visibleProperties.getJSONObject(i));
            }
        }
    }

    private void removeDynamicViews() {
        canvasLayout.removeAllViews();
        propertyLabels.clear();
        propertyValues.clear();
    }

    /**
     * Creates views for a GeoJSON property meant to be visible, as defined in the
     * {@link utils.config.InfoWindowConfig}
     *
     * @param property The GeoJSON property to be displayed as defined in
     *                 {@code utils.helpers.MapBoxStyleHelper.KujakuConfig.InfoWindowConfig.addVisibleProperty}
     */
    private void createPropertyViews(JSONObject property) throws JSONException {
        String label = property
                .getString(InfoWindowConfig.KEY_VP_LABEL);
        String id = property.getString(InfoWindowConfig.KEY_VP_ID);

        LinearLayout itemCanvas = (LinearLayout) LayoutInflater
                .from(adapter.getContext()).inflate(R.layout.item_info_window, canvasLayout, true);
        itemCanvas.setId(Views.generateViewId());

        TextView labelTV = (TextView) itemCanvas.findViewById(R.id.tv_label);
        labelTV.setId(Views.generateViewId());
        labelTV.setText(label + ":");
        labelTV.setTag(id);
        propertyLabels.add(labelTV);

        TextView valueTV = (TextView) itemCanvas.findViewById(R.id.tv_value);
        valueTV.setId(Views.generateViewId());
        valueTV.setTag(id);
        propertyValues.add(valueTV);
    }

    public void setData(InfoWindowObject infoWindowObject) throws JSONException {
        this.currentInfoWindowObject = infoWindowObject;
        this.currentInfoWindowObject.setOnFocusChangeListener(this);
        JSONObject jsonObject = infoWindowObject.getJsonObject();

        if (jsonObject.has("properties")) {
            JSONObject propertiesJSON = jsonObject.getJSONObject("properties");

            for (TextView curField : propertyValues) {
                String propertyId = (String) curField.getTag();
                if (propertyId != null && propertiesJSON.has(propertyId)) {
                    curField.setText(propertiesJSON.getString(propertyId));
                } else {
                    curField.setText(null);
                }
            }
        }

        updateFocusViews();
    }

    private void startCardViewWidthAnimation(int endWidth,
                                             Animator.AnimatorListener animatorListener) {
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

    private void startCardViewAlphaAnimation(float endAlpha,
                                             Animator.AnimatorListener animatorListener) {
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

    private void startCardViewHeightAnimation(int endHeight,
                                              Animator.AnimatorListener animatorListener) {
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

    private void startCardViewVMarginAnimation(int endMargin,
                                               Animator.AnimatorListener animatorListener) {
        final ViewGroup.MarginLayoutParams layoutParams =
                (ViewGroup.MarginLayoutParams) cardView.getLayoutParams();
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

    private void startTextSizeAnimation(float endTextSize,
                                        Animator.AnimatorListener animatorListener) {
        ValueAnimator anim =
                ValueAnimator.ofFloat(propertyLabels.get(0).getPaint().getTextSize(), endTextSize);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float animatedValue = (Float) valueAnimator.getAnimatedValue();
                for (TextView curTextView : propertyLabels) {
                    curTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, animatedValue);
                }

                for (TextView curTextView : propertyValues) {
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

    private void startCanvasPaddingAnimation(int endPadding,
                                             Animator.AnimatorListener animatorListener) {
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