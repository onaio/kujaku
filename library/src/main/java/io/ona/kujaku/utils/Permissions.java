package io.ona.kujaku.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jason Rogena - jrogena@ona.io on 11/7/17.
 */

public class Permissions {
    private static final String[] CRITICAL_PERMISSIONS = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    public static String[] getUnauthorizedCriticalPermissions(Context context) {
        List<String> unauthorizedPermissions = new ArrayList<>();
        for (String curPermission : CRITICAL_PERMISSIONS) {
            if (!check(context, curPermission)) {
                unauthorizedPermissions.add(curPermission);
            }
        }

        return unauthorizedPermissions.toArray(new String[]{});
    }

    public static boolean check(Context context, String permission) {
        if (ContextCompat.checkSelfPermission(context, permission)
                != PackageManager.PERMISSION_GRANTED) {
            return false;
        }

        return true;
    }

    public static void request(Activity activity, String[] permissions, int requestCode) {
        ActivityCompat.requestPermissions(activity, permissions, requestCode);
    }
}
