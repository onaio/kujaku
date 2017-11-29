package io.ona.kujaku.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
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
import java.util.HashMap;
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

        InfoWindowViewHolder infoWindowViewHolder = new InfoWindowViewHolder(linearLayout, cardView, nameTv, ageTv, weightTv, vaccineTv);
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

        public InfoWindowViewHolder(@NonNull LinearLayout linearLayout,@NonNull  CardView cardView,@NonNull  TextView nameTv,
                                    @NonNull TextView ageTv,@NonNull  TextView weightTv,@NonNull  TextView vaccineTv) {
            super(linearLayout);
            this.linearLayout = linearLayout;
            this.cardView = cardView;
            this.nameTv = nameTv;
            this.ageTv = ageTv;
            this.weightTv = weightTv;
            this.vaccineTv = vaccineTv;
        }

        public void onBind(final int position) {
            linearLayout.setAlpha(0.5f);
            //instance.changeMargin(linearLayout, (int) getPx(instance.selectedSizeDifferenceDp));
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

        public void setData(JSONObject jsonObject) throws JSONException {
            String[] priorityFields = new String[] {
                    "first_name",
                    "Birth_Weight",
                    "Place_Birth",
                    "zeir_id"
            };
            TextView[] tvs = new TextView[]{nameTv, ageTv, weightTv, vaccineTv};
            int fromPriorityFieldIndex = 0;

            if (jsonObject.has("properties")) {
                JSONObject propertiesJSON = jsonObject.getJSONObject("properties");

                for (int i = 0; i < tvs.length; i++) {
                    TextView tv = tvs[i];

                    for (int j = fromPriorityFieldIndex; j < priorityFields.length; j++) {
                        String priorityField = priorityFields[j];

                        if (propertiesJSON.has(priorityField)) {
                            tv.setText(propertiesJSON.getString(priorityField));
                            fromPriorityFieldIndex = j + 1;
                            break;
                        } else {
                            // Select a substitude field or disable the text view
                            tv.setVisibility(View.GONE);
                            //tv.setText(propertiesJSON.getString("id"));
                        }

                    }
                }
            }

        }

        public void select() {
            //linearLayout.setBackgroundColor(instance.selectedColor);
            //cardView.setBackgroundColor(instance.selectedColor);
            linearLayout.setAlpha(1.0f);

            // Resize the widget
            instance.resizeView(linearLayout, (int) getPx(instance.selectedSizeDifferenceDp));
            instance.changeMargin(linearLayout, (int) -getPx(instance.selectedSizeDifferenceDp));
        }

        public void unselect() {
            //linearLayout.setBackgroundColor(instance.unselectedColor);
            linearLayout.setAlpha(0.5f);

            // Reduce the size
            instance.resizeView(linearLayout, (int) -getPx(instance.selectedSizeDifferenceDp));
            instance.changeMargin(linearLayout, (int) getPx(instance.selectedSizeDifferenceDp));

        }

        private float getPx(int dp) {
            return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, linearLayout.getResources().getDisplayMetrics());
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

    private void resizeView(@NonNull final View view, final int sizeIncreaseInPixels) {
        /*ViewGroup.LayoutParams params = view.getLayoutParams();
        params.width = (int) (params.width + sizeIncreaseInPixels);
        params.height = (int) (params.height + sizeIncreaseInPixels);
        view.setLayoutParams(params);*/

        /*if (view instanceof LinearLayout || view instanceof CardView) {
            int paddingTop = view.getPaddingTop();
            int paddingBottom = view.getPaddingBottom();
            int paddingLeft = view.getPaddingLeft();
            int paddingRight = view.getPaddingRight();

            view.setPadding(
                    paddingLeft + sizeIncreaseInPixels,
                    paddingTop + sizeIncreaseInPixels,
                    paddingRight + sizeIncreaseInPixels,
                    paddingBottom + sizeIncreaseInPixels
            );
        }*/

        /*view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {

            }
        });*/

        int height = view.getMeasuredHeight();
        int width = view.getMeasuredWidth();

        ViewGroup.LayoutParams params = view.getLayoutParams();
        params.width = (int) (width + (sizeIncreaseInPixels * 2));
        params.height = (int) (height + (sizeIncreaseInPixels * 2));
        view.setLayoutParams(params);
    }

    private void changeMargin(@NonNull View view, final int sizeIncreaseInPixels) {
        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) view.getLayoutParams();

        marginLayoutParams.setMargins(
                marginLayoutParams.leftMargin,
                marginLayoutParams.topMargin + sizeIncreaseInPixels,
                marginLayoutParams.rightMargin,
                marginLayoutParams.bottomMargin + sizeIncreaseInPixels
        );
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
