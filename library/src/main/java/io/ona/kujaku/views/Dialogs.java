package io.ona.kujaku.views;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import io.ona.kujaku.R;
import io.ona.kujaku.utils.LocationSettingsHelper;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 29/01/2019
 */

public final class Dialogs {

    public static final void showDialogIfLocationDisabled(@NonNull Activity activity, @Nullable String dialogTitle, @Nullable String dialogMessage) {
        String finalDialogTitle = dialogTitle != null ? dialogTitle : activity.getString(R.string.location_service_disabled);
        String finalDialogMessage = dialogMessage != null ? dialogMessage : activity.getString(R.string.location_service_disabled_dialog_explanation);

        LocationSettingsHelper.checkLocationEnabled(activity, new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {
                final Status status = locationSettingsResult.getStatus();
                if (status.getStatusCode() == LocationSettingsStatusCodes.RESOLUTION_REQUIRED) {
                    new AlertDialog.Builder(activity)
                            .setTitle(finalDialogTitle)
                            .setMessage(finalDialogMessage)
                            .setPositiveButton(activity.getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                                @Override public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                            .show();
                }

            }
        });
    }
}
