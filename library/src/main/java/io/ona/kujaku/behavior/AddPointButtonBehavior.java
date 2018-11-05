package io.ona.kujaku.behavior;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;

/**
 * @author Vincent Karuri
 */
public class AddPointButtonBehavior extends CoordinatorLayout.Behavior<ImageButton> {

    public AddPointButtonBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, ImageButton child, View dependency) {
        return dependency instanceof android.support.v7.widget.CardView;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, ImageButton child, View dependency) {
        // set views bottom up
        dependency.setBottom(parent.getBottom() - 40);
        dependency.setTop(dependency.getBottom() - dependency.getLayoutParams().height);
        child.setBottom(dependency.getTop() - 40);
        child.setTop(child.getBottom() - child.getLayoutParams().height);
        return true;
    }
}
