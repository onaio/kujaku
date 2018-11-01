package io.ona.kujaku.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    /**
     * Returns {@link android.content.pm.PermissionInfo#PROTECTION_DANGEROUS} permissions which
     * have not been requested yet/denied by the user from the list of {@link Permissions#CRITICAL_PERMISSIONS}
     * required
     *
     * @param context
     * @return list of unauthorised permissions
     */
    public static String[] getUnauthorizedCriticalPermissions(Context context) {
        List<String> unauthorizedPermissions = new ArrayList<>();
        for (String curPermission : CRITICAL_PERMISSIONS) {
            if (!check(context, curPermission)) {
                unauthorizedPermissions.add(curPermission);
            }
        }

        return unauthorizedPermissions.toArray(new String[]{});
    }

    /**
     * Checks if a specific application permission is authorised
     *
     * @param context
     * @param permission Permission name from constants in {@link android.Manifest.permission}
     * @return  {@code TRUE} if the permission is authorised
     *          {@code FALSE} if the permission is not authorised
     */
    public static boolean check(Context context, String permission) {
        if (ContextCompat.checkSelfPermission(context, permission)
                != PackageManager.PERMISSION_GRANTED) {
            return false;
        }

        return true;
    }

    /**
     * Requests for a certain permission from the user by showing a System Dialog box
     *
     * @param activity
     * @param permissions Permission names to request from the user
     * @param requestCode Request code that will be returned on {@link Activity#onActivityResult(int, int, Intent)}
     */
    public static void request(Activity activity, String[] permissions, int requestCode) {
        ActivityCompat.requestPermissions(activity, permissions, requestCode);
    }
}
