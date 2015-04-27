package com.sc.mtaa_safi.SystemUtils;

import android.app.IntentService;
import android.app.Notification;
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
import com.sc.mtaa_safi.database.SyncUtils;
import com.sc.mtaa_safi.feed.MainActivity;

import org.json.JSONException;
import org.json.JSONObject;

public class GcmIntentService extends IntentService {
    public static final int REPORT_UPDATE = 1, NEW_REPORT = 2, RESET_NEW = 3, RESET_UPDATE = 4, MULTIPLE_UPDATE = -2, MULTIPLE_NEW = -3;
    private static final String NEW_COMMENT = " new comment", NEW_VOTE = " new upvote", YOURS = " on your reports";
    private static int numComments = 0, numUpvotes = 0,  numNew = 0, notificationType = REPORT_UPDATE;
    private static String new_message = "", update_message = "";
    private NotificationManager mNotificationManager;
    public GcmIntentService() { super("GcmIntentService"); }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty()) { // has effect of unparcelling Bundle
            if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType))
                try {
                    sendNotification(extras.getString("msg"));
                } catch (JSONException e) { e.printStackTrace(); }
//            else if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType))
//            else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType))
        }
        GcmBroadcastReceiver.completeWakefulIntent(intent); // Release the wake lock
    }

    private void sendNotification(String msg) throws JSONException {
        JSONObject msg_data  = new JSONObject(msg);
        if (msg_data.getString("type").trim().equals("new"))
            msg_data = updateNew(msg_data);
        else
            msg_data = updateUpdates(msg_data);

        NotificationCompat.Builder mBuilder = buildNotification(msg_data);
        mBuilder.setContentIntent(buildIntent(msg_data));

        mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(notificationType, mBuilder.build());
    }

    private JSONObject updateUpdates(JSONObject msg_data) throws JSONException {
        notificationType = REPORT_UPDATE;
        if (!update_message.isEmpty())
            update_message += ", ";
        update_message += msg_data.getString("message");
        if (msg_data.getString("type").trim().equals("comment"))
            numComments++;
        if (msg_data.getString("type").trim().equals("upvote"))
            numUpvotes++;
        if (numComments + numUpvotes <= 1)
            return msg_data;

        return updateUpdateText(msg_data);
    }

    private JSONObject updateUpdateText(JSONObject msg_data) throws JSONException {
        String comments = "", upvotes = "";
        if (numComments > 0)
            comments = numComments + NEW_COMMENT;
        if (numComments > 1)
            comments = comments + "s";
        if (numComments > 0 && numUpvotes > 0)
            comments = comments + ", ";
        if (numUpvotes > 0)
            upvotes = numUpvotes + NEW_VOTE;
        if (numUpvotes > 1)
            upvotes = upvotes + "s";

        msg_data.put("title", comments + upvotes + YOURS);
        msg_data.put("message", update_message);
        msg_data.put("reportId", MULTIPLE_UPDATE);
        return msg_data;
    }

    private JSONObject updateNew(JSONObject msg_data) throws JSONException {
        if (NetworkUtils.isOnline(this))
            SyncUtils.TriggerRefresh();
        notificationType = NEW_REPORT;
        if (!new_message.isEmpty())
            new_message += ", ";
        new_message += msg_data.getString("message");
        if (++numNew > 1) {
            msg_data.put("message", new_message);
            msg_data.put("reportId", MULTIPLE_NEW);
            msg_data.put("title", numNew + " new reports near you");
        }
        return msg_data;
    }

    private PendingIntent buildIntent(JSONObject msg_data) throws JSONException {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("reportId", msg_data.getInt("reportId"));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        return PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
    }

    private PendingIntent createOnDismissIntent(int notificationId) {
        Intent intent = new Intent(this, GcmBroadcastReceiver.class);
        intent.putExtra("notificationId", notificationId);
        return PendingIntent.getBroadcast(this, notificationId, intent, PendingIntent.FLAG_CANCEL_CURRENT);
    }

    private NotificationCompat.Builder buildNotification(JSONObject msg_data) throws JSONException {
        int dismiss_type = RESET_UPDATE;
        if (msg_data.getString("type").trim().equals("new"))
            dismiss_type = RESET_NEW;
        return new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.notification_logo)
                .setColor(getResources().getColor(R.color.mtaa_safi_blue))
                .setDefaults(Notification.DEFAULT_SOUND)
                .setAutoCancel(true)
                .setDeleteIntent(createOnDismissIntent(dismiss_type))
                .setStyle(new NotificationCompat.BigTextStyle().bigText(msg_data.getString("message")))
                .setContentTitle(msg_data.getString("title"))
                .setContentText(msg_data.getString("message"));
    }

    public static void resetAll() {
        GcmIntentService.resetNew();
        GcmIntentService.resetUpdate();
    }
    public static void resetUpdate() {
        GcmIntentService.numComments = 0;
        GcmIntentService.numUpvotes = 0;
        GcmIntentService.update_message = "";
    }
    public static void resetNew() {
        GcmIntentService.numNew = 0;
        GcmIntentService.new_message = "";
    }
}