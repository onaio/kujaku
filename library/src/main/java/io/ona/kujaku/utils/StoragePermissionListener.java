package io.ona.kujaku.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;

import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.single.BasePermissionListener;

import io.ona.kujaku.R;

/**
 * Created by Emmanuel Otin - eo@novel-t.ch on 05/04/2019
 */
public class StoragePermissionListener extends BasePermissionListener {
    private final Context context;
    private final String title;
    private final String message;
    private final String positiveButtonText;

    public StoragePermissionListener(@NonNull Context context) {
        this.context = context;
        this.title = context.getString(R.string.storage_permission);
        this.message = context.getString(R.string.storage_permission_reason);
        this.positiveButtonText = context.getString(android.R.string.ok);
    }

    @Override public void onPermissionDenied(PermissionDeniedResponse response) {
        super.onPermissionDenied(response);

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
