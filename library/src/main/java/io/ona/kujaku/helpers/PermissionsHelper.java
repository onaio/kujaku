package io.ona.kujaku.helpers;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import io.ona.kujaku.utils.KujakuMultiplePermissionListener;

public class PermissionsHelper {

    public static void checkPermissions(String TAG, Context context) {
        if (context instanceof Activity) {
            final Activity activity = (Activity) context;

            MultiplePermissionsListener dialogMultiplePermissionListener = new KujakuMultiplePermissionListener(activity);

            Dexter.withActivity(activity)
                    .withPermissions(Manifest.permission.ACCESS_FINE_LOCATION, (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) ? Manifest.permission.MANAGE_EXTERNAL_STORAGE : Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    .withListener(dialogMultiplePermissionListener)
                    .check();

        } else {
            Log.wtf(TAG, "KujakuMapView was not started in an activity!! This is very bad or it is being used in tests. We are going to ignore the permissions check! Good luck");
        }
    }
}
