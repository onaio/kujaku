package io.ona.kujaku.notifications;

import android.app.NotificationManager;
import android.content.Context;
import android.support.annotation.NonNull;

import io.ona.kujaku.R;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 17/01/2018.
 */

public class KujakuNotificationImplClass2 extends KujakuNotification {
    public static final String CHANNEL_ID = "KUJAKU_NOTIFICATION_IMPL_CHANNEL";

    public KujakuNotificationImplClass2() {}

    public KujakuNotificationImplClass2(@NonNull Context context) {
        this.context = context;
        createNotificationChannel(NotificationManager.IMPORTANCE_LOW, context.getString(R.string.download_progress_channel_name), CHANNEL_ID, context.getString(R.string.download_progress_channel_description));
    }
}
