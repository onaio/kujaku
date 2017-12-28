package io.ona.kujaku.utils;

import android.os.Build;
import android.view.View;

import java.util.Random;

/**
 * Created by Jason Rogena - jrogena@ona.io on 12/28/17.
 */

public class Views {
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
}
