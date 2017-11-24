package io.ona.kujaku.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

    private ArrayList<InfoWindowObject> items;
    private OnClickListener onClickListener;
    private static InfoWindowAdapter instance;
    private RecyclerView recyclerView;

    private int lastSelectedPosition = 0;
    private static final String TAG = InfoWindowAdapter.class.getSimpleName();

    public InfoWindowAdapter(@NonNull LinkedHashMap<String, InfoWindowObject> items, @NonNull RecyclerView recyclerView) {
        this(new ArrayList<InfoWindowObject>(items.values()), recyclerView);
    }

    public InfoWindowAdapter(@NonNull ArrayList<InfoWindowObject> items, @NonNull RecyclerView recyclerView) {
        this.items = items;
        this.recyclerView = recyclerView;
        instance = this;
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
            this.cardView = cardView;
            this.nameTv = nameTv;
            this.ageTv = ageTv;
            this.weightTv = weightTv;
            this.vaccineTv = vaccineTv;
        }

        public void onBind(final int position) {
            linearLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    instance.deactivatePreviouslySelectedItem(instance.lastSelectedPosition);
                    instance.activateSelectedItem(position);
                    instance.lastSelectedPosition = position;

                    OnClickListener onClickListener = instance.getOnClickListener();
                    if (onClickListener != null) {
                        onClickListener.onClick(v, position);
                    }
                }
            });
        }

        public void setData(JSONObject jsonObject) throws JSONException {
            String[] priorityFields = new String[] {
                    "name",
                    "age",
                    "weight",
                    "vaccine"
            };
            TextView[] tvs = new TextView[] {nameTv, ageTv, weightTv, vaccineTv};

            for(int i = 0; i < priorityFields.length; i++) {
                TextView tv = tvs[i];

                for(String priorityField: priorityFields) {
                    if (jsonObject.has(priorityField)) {
                        tv.setText(jsonObject.getString(priorityField));
                        break;
                    } else {
                        // Select a substitude field or disable the text view
                        tv.setVisibility(View.GONE);
                    }
                }
            }

        }

        public void select() {}

        public void unselect() {}
    }

    public static interface OnClickListener {

        void onClick(View v, int position);
    }

    private void activateSelectedItem(int position) {
        ((InfoWindowAdapter.InfoWindowViewHolder) recyclerView.findViewHolderForAdapterPosition(position))
                .select();
    }

    private void deactivatePreviouslySelectedItem(int lastSelectedPosition) {
        ((InfoWindowAdapter.InfoWindowViewHolder) recyclerView.findViewHolderForAdapterPosition(lastSelectedPosition))
                .unselect();
    }

}
