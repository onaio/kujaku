package io.ona.kujaku.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;

import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.listener.multi.BaseMultiplePermissionsListener;

import io.ona.kujaku.R;

public class KujakuMultiplePermissionListener extends BaseMultiplePermissionsListener {
    private final Context context;
    private final String title;
    private final String message;
    private final String positiveButtonText;

    public KujakuMultiplePermissionListener(@NonNull Context context) {
        this.context = context;
        this.title = context.getString(io.ona.kujaku.R.string.kujaku_permission);
        this.message = context.getString(R.string.kujaku_permission_reason);
        this.positiveButtonText = context.getString(android.R.string.ok);
    }

    @Override
    public void onPermissionsChecked(MultiplePermissionsReport report) {
        if (report.isAnyPermissionPermanentlyDenied() || !report.areAllPermissionsGranted()) {
            new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(positiveButtonText, new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                        if (context instanceof Activity) {
                            ((Activity) context).finish();
                        }
                    }
                })
                .show();
        }
    }
}
