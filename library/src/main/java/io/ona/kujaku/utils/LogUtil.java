package io.ona.kujaku.utils;

import android.util.Log;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 31/07/2018
 */

public class LogUtil {

    public static final void e(String TAG, Exception e) {
        if (BuildConfig.DEBUG) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
    }

    public static final void e(String TAG, String message) {
        if (BuildConfig.DEBUG) {
            Log.e(TAG, message);
        }
    }

    public static final void i(String TAG, String message) {
        if (BuildConfig.DEBUG) {
            Log.i(TAG, message);
        }
    }

    public static final void eWithoutCrashlytics(String TAG, String message) {
        if (BuildConfig.DEBUG) {
            Log.e(TAG, message);
        }
    }

    public static final void eWithoutCrashlytics(String TAG, Exception e) {
        if (BuildConfig.DEBUG) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
    }
}
