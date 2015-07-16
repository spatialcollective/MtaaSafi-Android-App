package com.sc.mtaa_safi.SystemUtils;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

public class GcmBroadcastReceiver extends WakefulBroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        int notificationId = intent.getExtras().getInt("notificationId");
        if (notificationId != GcmIntentService.RESET_NEW && notificationId != GcmIntentService.RESET_UPDATE) {
            // Explicitly specify that GcmIntentService will handle the intent.
            ComponentName comp = new ComponentName(context.getPackageName(), GcmIntentService.class.getName());
            // Start the service, keeping the device awake while it is launching.
            startWakefulService(context, (intent.setComponent(comp)));
//            setResultCode(Activity.RESULT_OK);
        } else if (notificationId == GcmIntentService.RESET_UPDATE)
            GcmIntentService.resetUpdate();
        else
            GcmIntentService.resetNew();
    }
}
