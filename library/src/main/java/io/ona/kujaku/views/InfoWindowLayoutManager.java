package io.ona.kujaku.views;

import android.content.Context;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 29/11/2017.
 */

public class InfoWindowLayoutManager extends LinearLayoutManager {

    private Context mContext;
    private int screenWidth = 0;
    private OnScrollListener onScrollListener;

    public InfoWindowLayoutManager(Context context, int screenWidth) {
        super(context);
        mContext = context;
        this.screenWidth = screenWidth;
    }

    public InfoWindowLayoutManager(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
        mContext = context;
    }

    public InfoWindowLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mContext = context;
    }

    @Override
    public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int position) {
        //super.smoothScrollToPosition(recyclerView, state, position);

        /*Smooth scroll to the specified adapter position.

        To support smooth scrolling,
        1. Override this method,
        2. Create your RecyclerView.SmoothScroller instance
        3. Call startSmoothScroll(SmoothScroller).*/

        //Create your RecyclerView.SmoothScroller instance? Check.
        LinearSmoothScroller smoothScroller = new LinearSmoothScroller(mContext) {

            //Automatically implements this method on instantiation.
            @Override
            public PointF computeScrollVectorForPosition(int targetPosition) {
                return InfoWindowLayoutManager.this.computeScrollVectorForPosition
                        (targetPosition);
            }

            @Override
            public int calculateDxToMakeVisible(View view, int snapPreference) {
                int initialDX = super.calculateDxToMakeVisible(view, snapPreference);
                return initialDX + (screenWidth/2);
            }

            @Override
            protected void onStop() {
                super.onStop();

                if (onScrollListener != null) {
                    onScrollListener.onFinishedScrolling();
                }
            }
        };

        //Docs do not tell us anything about this,
        //but we need to set the position we want to scroll to.
        smoothScroller.setTargetPosition(position);

        //Call startSmoothScroll(SmoothScroller)? Check.
        startSmoothScroll(smoothScroller);
    }

    public OnScrollListener getOnScrollListener() {
        return onScrollListener;
    }

    public void setOnScrollListener(OnScrollListener onScrollListener) {
        this.onScrollListener = onScrollListener;
    }

    public void removeOnScrollListener() {
        this.onScrollListener = null;
    }

    public static interface OnScrollListener {
        void onFinishedScrolling();
    }
}
