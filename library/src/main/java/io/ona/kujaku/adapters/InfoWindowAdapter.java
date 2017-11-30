package io.ona.kujaku.adapters;

import android.app.Activity;
import android.graphics.Color;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import io.ona.kujaku.R;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 24/11/2017.
 */

public class InfoWindowAdapter extends RecyclerView.Adapter<InfoWindowAdapter.InfoWindowViewHolder> {

    private Activity activity;
    private ArrayList<InfoWindowObject> items;
    private OnClickListener onClickListener;
    private static InfoWindowAdapter instance;
    private RecyclerView recyclerView;

    private int lastSelectedPosition = -1;
    private int currentSelectedPosition = -1;
    private static final String TAG = InfoWindowAdapter.class.getSimpleName();

    private float unselectedOpacity = 0.5f;
    private float selectedOpacity = 1.0f;
    private int selectedSizeDifferenceDp = 10;
    private int animateResizeDuration = 1000;
    private ViewGroup.MarginLayoutParams layoutParams;

    private boolean defaultMarginAndLayoutSet = false;
    private boolean globalListenerSet = false;

    private int defaultWidth = -1;
    private int defaultHeight = -1;

    public InfoWindowAdapter(@NonNull Activity activity, @NonNull LinkedHashMap<String, InfoWindowObject> items, @NonNull RecyclerView recyclerView) {
        this(activity, new ArrayList<InfoWindowObject>(items.values()), recyclerView);
    }

    public InfoWindowAdapter(@NonNull Activity activity, @NonNull ArrayList<InfoWindowObject> items, @NonNull RecyclerView recyclerView) {
        this.activity = activity;
        this.items = items;
        this.recyclerView = recyclerView;
        instance = this;
    }

    @Override
    public InfoWindowViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final LinearLayout linearLayout = (LinearLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.info_window_card_item, parent, false);

        if (!globalListenerSet) {
            linearLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    defaultWidth = linearLayout.getMeasuredWidth();
                    defaultHeight = linearLayout.getMeasuredHeight();

                    if (!defaultMarginAndLayoutSet) {
                        layoutParams = (ViewGroup.MarginLayoutParams) linearLayout.getLayoutParams();
                        defaultMarginAndLayoutSet = true;
                    }

                    linearLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
            });
            globalListenerSet = true;
        }

        CardView cardView = linearLayout.findViewById(R.id.cv_bottomInfoWindow_cardView);
        TextView nameTv = linearLayout.findViewById(R.id.tv_bottomInfoWindow_name);
        TextView ageTv = linearLayout.findViewById(R.id.tv_bottomInfoWindow_age);
        TextView weightTv = linearLayout.findViewById(R.id.tv_bottomInfoWindow_weight);
        TextView vaccineTv = linearLayout.findViewById(R.id.tv_bottomInfoWindow_vaccine);

        TextView labelNameTv = linearLayout.findViewById(R.id.tv_bottomInfoWindow_labelName);
        TextView labelAgeTv = linearLayout.findViewById(R.id.tv_bottomInfoWindow_labelAge);
        TextView labelWeightTv = linearLayout.findViewById(R.id.tv_bottomInfoWindow_labelWeight);
        TextView labelVaccineTv = linearLayout.findViewById(R.id.tv_bottomInfoWindow_labelVaccine);

        InfoWindowViewHolder infoWindowViewHolder = new InfoWindowViewHolder(linearLayout, cardView,
                nameTv, ageTv, weightTv, vaccineTv, labelNameTv, labelAgeTv, labelWeightTv, labelVaccineTv);
        return infoWindowViewHolder;
    }

    @Override
    public void onBindViewHolder(InfoWindowViewHolder holder, int position) {
        InfoWindowObject infoWindowObject = items.get(position);

        if (infoWindowObject.getPosition() == position) {
            try {
                holder.setData(infoWindowObject.getJsonObject());
                holder.onBind(position);
            } catch (JSONException e) {
                Log.e(TAG, Log.getStackTraceString(e));
            }
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public OnClickListener getOnClickListener() {
        return onClickListener;
    }

    public void setOnClickListener(OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    public static class InfoWindowViewHolder extends RecyclerView.ViewHolder {

        private LinearLayout linearLayout;
        private CardView cardView;
        private TextView nameTv;
        private TextView ageTv;
        private TextView weightTv;
        private TextView vaccineTv;

        TextView labelNameTv;
        TextView labelAgeTv;
        TextView labelWeightTv;
        TextView labelVaccineTv;

        private int width = -1;
        private int height = -1;

        public InfoWindowViewHolder(@NonNull final LinearLayout linearLayout, @NonNull  CardView cardView, @NonNull  TextView nameTv,
                                    @NonNull TextView ageTv, @NonNull  TextView weightTv, @NonNull  TextView vaccineTv,
                                    @NonNull TextView nameLabel, @NonNull TextView ageLabel, @NonNull  TextView weightLabel, @NonNull  TextView vaccineLabel) {
            super(linearLayout);
            this.linearLayout = linearLayout;
            this.cardView = cardView;
            this.nameTv = nameTv;
            this.ageTv = ageTv;
            this.weightTv = weightTv;
            this.vaccineTv = vaccineTv;

            labelNameTv = nameLabel;
            labelAgeTv = ageLabel;
            labelWeightTv = weightLabel;
            labelVaccineTv = vaccineLabel;
        }

        public void onBind(final int position) {
            if (instance.getCurrentSelectedPosition() != position) {
                resetView();
            }
            linearLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    OnClickListener onClickListener = instance.getOnClickListener();
                    instance.focusOnPosition(position);
                    if (onClickListener != null) {
                        onClickListener.onClick(v, position);
                    }
                }
            });
        }

        private void resetView() {
            linearLayout.setAlpha(instance.unselectedOpacity) ;
            /*if (instance.layoutParams != null) {
                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) cardView.getLayoutParams();
                layoutParams.height = instance.defaultHeight;
                layoutParams.width = instance.defaultWidth;

                // Set the default margins also
                layoutParams.setMargins(
                        instance.layoutParams.leftMargin,
                        instance.layoutParams.topMargin,
                        instance.layoutParams.rightMargin,
                        instance.layoutParams.bottomMargin
                );

                cardView.setLayoutParams(layoutParams);
            }*/
        }

        public void setData(JSONObject jsonObject) throws JSONException {
            String[] priorityFields = new String[] {
                    "first_name",
                    "Birth_Weight",
                    "Place_Birth",
                    "zeir_id"
            };
            TextView[] tvs = new TextView[]{nameTv, ageTv, weightTv, vaccineTv};
            TextView[] tvLabels = new TextView[]{labelNameTv, labelAgeTv, labelWeightTv, labelVaccineTv};
            int fromPriorityFieldIndex = 0;

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

        }

        public void select() {
            linearLayout.setAlpha(instance.selectedOpacity);
            int increasePx = (int) getPx(instance.selectedSizeDifferenceDp);

            // Resize the widget
            //instance.resizeView(linearLayout, (int) getPx(instance.selectedSizeDifferenceDp), instance.defaultWidth, instance.defaultHeight);
            /*instance.resizeView(cardView,
                    instance.defaultWidth + increasePx,
                    instance.defaultHeight + increasePx);
            instance.changeMargin(cardView, (int) -getPx(instance.selectedSizeDifferenceDp), instance.layoutParams);*/
        }

        public void unselect() {
            //linearLayout.setBackgroundColor(instance.unselectedColor);
            linearLayout.setAlpha(instance.unselectedOpacity);

            // Reduce the size
            //instance.resizeView(linearLayout, (int) -getPx(instance.selectedSizeDifferenceDp), width, height);
            /*instance.resizeView(cardView,
                    instance.defaultWidth,
                    instance.defaultHeight);
            instance.changeMargin(cardView, (int) getPx(instance.selectedSizeDifferenceDp), instance.layoutParams);*/
        }

        private float getPx(int dp) {
            return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, linearLayout.getResources().getDisplayMetrics());
        }

        private String humanizeFieldName(String fieldName) {
            // Replace underscores with spaces
            // Capitalize the words
            fieldName = fieldName.replace("_", " ");
            fieldName = capitalize(fieldName);

            // Todo: Capitalize all words

            // Change abbreviations to Uppercase
            String[] abbreviations = new String[] {
                    "id",
                    "zeir"
            };

            for (String abbreviation: abbreviations) {
                fieldName = fieldName.replace(abbreviation, abbreviation.toUpperCase());
            }

            return fieldName;
        }

        private String capitalize(String name) {
            return name.substring(0, 1).toUpperCase() + name.substring(1);
        }
    }

    public static interface OnClickListener {

        void onClick(View v, int position);
    }

    private void activateSelectedItem(int position) {
        InfoWindowViewHolder infoWindowViewHolder = (InfoWindowAdapter.InfoWindowViewHolder) recyclerView.findViewHolderForAdapterPosition(position);
        if (infoWindowViewHolder != null) {
            infoWindowViewHolder.select();
        }
    }

    private void deactivatePreviouslySelectedItem(int lastSelectedPosition) {
        InfoWindowViewHolder infoWindowViewHolder = (InfoWindowAdapter.InfoWindowViewHolder) recyclerView.findViewHolderForAdapterPosition(lastSelectedPosition);
        if (infoWindowViewHolder != null) {
            infoWindowViewHolder.unselect();
        }
    }

    private void resizeView(@NonNull final View view, final int sizeIncreaseInPixels, int width, int height) {
        ViewGroup.LayoutParams params = view.getLayoutParams();
        params.width = (int) (width + (sizeIncreaseInPixels * 2));
        params.height = (int) (height + (sizeIncreaseInPixels * 2));
        view.setLayoutParams(params);
    }

    private void resizeView(@NonNull View view, int finalWidth, int finalHeight) {
        ViewGroup.LayoutParams params = view.getLayoutParams();
        params.width = finalWidth;
        params.height = finalHeight;
        view.setLayoutParams(params);
    }

    private void changeMargin(@NonNull View view, final int sizeIncreaseInPixels, ViewGroup.MarginLayoutParams layoutParams) {
        if (view != null && view instanceof LinearLayout) {
            RecyclerView.LayoutParams layoutParams1 = (RecyclerView.LayoutParams) view.getLayoutParams();
            if (layoutParams1 != null && layoutParams != null) {
                layoutParams1.setMargins(
                        layoutParams.leftMargin,
                        layoutParams.topMargin + sizeIncreaseInPixels,
                        layoutParams.rightMargin,
                        layoutParams.bottomMargin
                );
                view.setLayoutParams(layoutParams1);
            }
        }
    }

    public void focusOnPosition(int position) {
        currentSelectedPosition = position;
        if (lastSelectedPosition != position) {
            if (lastSelectedPosition > -1) {
                deactivatePreviouslySelectedItem(instance.lastSelectedPosition);
            }
            activateSelectedItem(position);
            lastSelectedPosition = position;

        }
    }

    public int getCurrentSelectedPosition() {
        return currentSelectedPosition;
    }

    public void setCurrentSelectedPosition(int currentSelectedPosition) {
        this.currentSelectedPosition = currentSelectedPosition;
    }
}
