package io.ona.kujaku.helpers;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.util.ArrayList;
import java.util.List;

import io.ona.kujaku.utils.KujakuMultiplePermissionListener;
import timber.log.Timber;

public class PermissionsHelper {

    public static void checkPermissions(String TAG, Context context) {
        if (context instanceof Activity) {
            List<String> permissions = new ArrayList<>();
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);

            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S) {
                permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
            final Activity activity = (Activity) context;

            MultiplePermissionsListener dialogMultiplePermissionListener = new KujakuMultiplePermissionListener(activity);

            Dexter.withActivity(activity)
                    .withPermissions(permissions)
                    .withListener(dialogMultiplePermissionListener)
                    .check();

        } else {
            Timber.tag(TAG).wtf("KujakuMapView was not started in an activity!! This is very bad or it is being used in tests. We are going to ignore the permissions check! Good luck");
        }
    }
}
