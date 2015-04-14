package com.sc.mtaa_safi.SystemUtils;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.Context;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.sc.mtaa_safi.R;
import com.sc.mtaa_safi.feed.MainActivity;

public class GcmIntentService extends IntentService {
    private static final int NOTIFICATION_ID = 1;
    private NotificationManager mNotificationManager;

    public GcmIntentService() { super("GcmIntentService"); }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty()) { // has effect of unparcelling Bundle
            if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType))
                sendNotification(extras.getString("msg"));
//            else if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType))
//            else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType))
        }
        GcmBroadcastReceiver.completeWakefulIntent(intent); // Release the wake lock
    }

    private void sendNotification(String msg) {
        mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("reportId", 33049);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder mBuilder =  new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.notification_logo)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
                .setContentTitle(getResources().getString(R.string.app_name))
                .setContentText(msg);

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }
}
