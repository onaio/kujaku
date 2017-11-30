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
    private static final String TAG = InfoWindowAdapter.class.getSimpleName();

    private int unselectedOpacity = 50;
    private int selectedOpacity = 255;
    private int selectedSizeDifferenceDp = 10;
    private int animateResizeDuration = 1000;
    @ColorInt private int unselectedColor = Color.WHITE;
    @ColorInt private int selectedColor;

    public InfoWindowAdapter(@NonNull Activity activity, @NonNull LinkedHashMap<String, InfoWindowObject> items, @NonNull RecyclerView recyclerView) {
        this(activity, new ArrayList<InfoWindowObject>(items.values()), recyclerView);
    }

    public InfoWindowAdapter(@NonNull Activity activity, @NonNull ArrayList<InfoWindowObject> items, @NonNull RecyclerView recyclerView) {
        this.activity = activity;
        this.items = items;
        this.recyclerView = recyclerView;
        instance = this;

        selectedColor = activity.getResources().getColor(R.color.infoWindowItemSelected);
    }

    @Override
    public InfoWindowViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LinearLayout linearLayout = (LinearLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.info_window_card_item, parent, false);

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
        private ViewGroup.MarginLayoutParams layoutParams;

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
            labelWeightTv = weightTv;
            labelVaccineTv = vaccineLabel;

            linearLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    width = linearLayout.getMeasuredWidth();
                    height = linearLayout.getMeasuredHeight();

                    layoutParams = (ViewGroup.MarginLayoutParams) linearLayout.getLayoutParams();
                }
            });
        }

        public void onBind(final int position) {
            resetView();
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
            linearLayout.setAlpha(0.5f);
            /*if (height > -1 && width > -1) {
                RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) linearLayout.getLayoutParams();
                layoutParams.height = height;
                layoutParams.width = width;

                // Set the default margins also
                layoutParams.setMargins(
                        this.layoutParams.leftMargin,
                        this.layoutParams.topMargin,
                        this.layoutParams.rightMargin,
                        this.layoutParams.bottomMargin
                );

                linearLayout.setLayoutParams(layoutParams);
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
                            labelTv.setText(humanizeFieldName(priorityField));
                            tv.setText(propertiesJSON.getString(priorityField));
                            fromPriorityFieldIndex = j + 1;
                            break;
                        } else {
                            // Select a substitude field or disable the text view
                            labelTv.setVisibility(View.GONE);
                            tv.setVisibility(View.GONE);
                            //tv.setText(propertiesJSON.getString("id"));
                        }

                    }
                }
            }

        }

        public void select() {
            linearLayout.setAlpha(1.0f);

            // Resize the widget
            //instance.resizeView(linearLayout, (int) getPx(instance.selectedSizeDifferenceDp), width, height);
            //instance.changeMargin(linearLayout, (int) -getPx(instance.selectedSizeDifferenceDp), layoutParams);
        }

        public void unselect() {
            //linearLayout.setBackgroundColor(instance.unselectedColor);
            linearLayout.setAlpha(0.5f);

            // Reduce the size
            //instance.resizeView(linearLayout, (int) -getPx(instance.selectedSizeDifferenceDp), width, height);
            //instance.changeMargin(linearLayout, (int) getPx(instance.selectedSizeDifferenceDp), layoutParams);

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

    private void changeMargin(@NonNull View view, final int sizeIncreaseInPixels, ViewGroup.MarginLayoutParams layoutParams) {
        /*ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) view.getLayoutParams();

        marginLayoutParams.setMargins(
                marginLayoutParams.leftMargin,
                marginLayoutParams.topMargin + sizeIncreaseInPixels,
                marginLayoutParams.rightMargin,
                marginLayoutParams.bottomMargin + sizeIncreaseInPixels
        );*/
        if (view instanceof LinearLayout && view != null) {
            RecyclerView.LayoutParams layoutParams1 = (RecyclerView.LayoutParams) view.getLayoutParams();
            if (layoutParams1 != null && layoutParams != null) {
                layoutParams1.setMargins(
                        layoutParams.leftMargin,
                        layoutParams.topMargin + sizeIncreaseInPixels,
                        layoutParams.rightMargin,
                        layoutParams.bottomMargin + sizeIncreaseInPixels
                );
                view.setLayoutParams(layoutParams1);
            }
        }
    }

    public void focusOnPosition(int position) {
        if (lastSelectedPosition != position) {
            if (lastSelectedPosition > -1) {
                deactivatePreviouslySelectedItem(instance.lastSelectedPosition);
            }
            activateSelectedItem(position);
            lastSelectedPosition = position;

        }
    }

}
