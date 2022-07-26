package io.ona.kujaku.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import androidx.annotation.NonNull;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.util.Random;

import io.ona.kujaku.exceptions.ContextUnavailableException;

/**
 * Created by Jason Rogena - jrogena@ona.io on 12/28/17.
 */

public class Views {
    public static final String TAG = Views.class.getName();

    private static final int MAX_RANDOM_ID = 32131;
    private static final int MIN_RANDOM_ID = 2342;

    public static int generateViewId() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return View.generateViewId();
        } else {
            Random random = new Random();
            return random.nextInt(MAX_RANDOM_ID - MIN_RANDOM_ID) + MIN_RANDOM_ID;
        }
    }

    public static void changeDrawable(@NonNull View view, int drawableId) {
        Context context;
        if (view.getContext() != null) {
            context = view.getContext().getApplicationContext();
        } else {
            LogUtil.e(TAG, new ContextUnavailableException());
            return;
        }

        if (view instanceof ImageButton || view instanceof ImageView) {
            Drawable focusedIcon;
            if (Build.VERSION.SDK_INT >= 21) {
                focusedIcon = context.getResources().getDrawable(drawableId, null);
            } else {
                focusedIcon = context.getResources().getDrawable(drawableId);
            }

            if (view instanceof ImageButton) {
                ImageButton imageButton = (ImageButton) view;
                imageButton.setImageDrawable(focusedIcon);
            } else if (view instanceof ImageView) {
                ImageView imageView = (ImageView) view;
                imageView.setImageDrawable(focusedIcon);
            }
        }
    }
}
