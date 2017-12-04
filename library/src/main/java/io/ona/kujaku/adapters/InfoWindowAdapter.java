package io.ona.kujaku.adapters;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import io.ona.kujaku.R;
import io.ona.kujaku.activities.MapActivity;
import io.ona.kujaku.adapters.holders.InfoWindowViewHolder;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 24/11/2017.
 */

public class InfoWindowAdapter extends RecyclerView.Adapter<InfoWindowViewHolder> {
    private static final String TAG = InfoWindowAdapter.class.getSimpleName();

    private Activity activity;
    private ArrayList<InfoWindowObject> items;
    private static InfoWindowAdapter instance;
    private RecyclerView recyclerView;

    private int lastSelectedPosition = -1;
    private int currentSelectedPosition = -1;

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
        final CardView inflatedView = (CardView) LayoutInflater.from(parent.getContext()).inflate(R.layout.info_window_card_item, parent, false);
        InfoWindowViewHolder infoWindowViewHolder = new InfoWindowViewHolder(this, inflatedView);
        return infoWindowViewHolder;
    }

    @Override
    public void onBindViewHolder(InfoWindowViewHolder holder, int position) {
        InfoWindowObject infoWindowObject = items.get(position);

        if (infoWindowObject.getPosition() == position) {
            try {
                holder.setData(infoWindowObject);
            } catch (JSONException e) {
                Log.e(TAG, Log.getStackTraceString(e));
            }
        }
    }

    public Context getContext() {
        return this.activity;
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void focusOnPosition(int position) {
        currentSelectedPosition = position;
        if (lastSelectedPosition != position) {
            if (lastSelectedPosition > -1) {
                items.get(lastSelectedPosition).setFocused(false);
            }
            lastSelectedPosition = position;
        }
        items.get(position).setFocused(true);
        if (activity instanceof MapActivity) {
            ((MapActivity) activity).focusOnFeature(position);
        }
    }
}
