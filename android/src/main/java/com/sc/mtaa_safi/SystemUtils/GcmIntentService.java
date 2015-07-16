package com.sc.mtaa_safi.SystemUtils;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentProviderClient;
import android.content.ContentProviderOperation;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.sc.mtaa_safi.R;
import com.sc.mtaa_safi.Report;
import com.sc.mtaa_safi.database.SyncUtils;
import com.sc.mtaa_safi.feed.MainActivity;
import com.sc.mtaa_safi.feed.comments.Comment;
import com.sc.mtaa_safi.settings.SettingsActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class GcmIntentService extends IntentService {
    public static final int REPORT_UPDATE = 1, NEW_REPORT = 2, 
                            RESET_NEW = 3, RESET_UPDATE = 4, 
                            MULTIPLE_UPDATE = -2, MULTIPLE_NEW = -3;
    private static final String NEW_COMMENT = " new comment", NEW_VOTE = " new upvote", YOURS = " on your reports",
                                MSG= "msg", TYPE = "type", COMMENT = "comment", VOTE = "upvote", NEW = "new";
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
                    if (extras.containsKey(MSG) && !extras.getString(MSG).isEmpty())
                        handleNotification(extras.getString(MSG));
                } catch (JSONException e) { e.printStackTrace(); }
//            else if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType))
//            else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType))
        }
        GcmBroadcastReceiver.completeWakefulIntent(intent); // Release the wake lock
    }

    private void handleNotification(String msg) throws JSONException {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        JSONObject msg_data = new JSONObject(msg);
        String type = "";
        if (msg_data.has(TYPE))
            type = msg_data.getString(TYPE).trim();
        if (type.equals(NEW)) {
            msg_data = updateNew(msg_data);
            if (sharedPref.getBoolean(SettingsActivity.NEW, true))
                sendNotification(msg_data);
        } else {
            msg_data = updateUpdates(msg_data, sharedPref);
            if (type.equals(COMMENT) && sharedPref.getBoolean(SettingsActivity.COMMENTS, true))
                sendNotification(msg_data);
            if (type.equals(VOTE) && sharedPref.getBoolean(SettingsActivity.VOTES, true))
                sendNotification(msg_data);
        }
    }

    private void sendNotification(JSONObject msg_data) throws JSONException {
        NotificationCompat.Builder mBuilder = buildNotification(msg_data);
        mBuilder.setContentIntent(buildIntent(msg_data));

        mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(notificationType, mBuilder.build());
    }

    private JSONObject updateUpdates(JSONObject msg_data, SharedPreferences prefs) throws JSONException {
        notificationType = REPORT_UPDATE;
        if (!update_message.isEmpty())
            update_message += " | ";
        update_message += msg_data.getString("message");
        if (msg_data.getString(TYPE).trim().equals(COMMENT)) {
            if (prefs.getBoolean(SettingsActivity.COMMENTS, true))
                numComments++;
            new Comment(msg_data.getJSONObject("data"), msg_data.getInt("reportId"), getBaseContext()).save();
        }
        if (msg_data.getString(TYPE).trim().equals(VOTE) && prefs.getBoolean(SettingsActivity.COMMENTS, true))
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
        Report report = new Report(msg_data.getJSONObject("data"), -1, new ArrayList<String>(), getBaseContext());
        report.save(getBaseContext(), false);

        notificationType = NEW_REPORT;
        if (!new_message.isEmpty())
            new_message += " | ";
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
        if (msg_data.getString(TYPE).trim().equals(NEW))
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