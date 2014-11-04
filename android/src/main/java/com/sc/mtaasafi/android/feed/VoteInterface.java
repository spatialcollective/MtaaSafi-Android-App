package com.sc.mtaasafi.android.feed;

import android.app.Activity;
import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sc.mtaasafi.android.R;
import com.sc.mtaasafi.android.Report;
import com.sc.mtaasafi.android.SystemUtils.ComplexPreferences;
import com.sc.mtaasafi.android.SystemUtils.PrefUtils;
import com.sc.mtaasafi.android.database.ReportContract;
import com.sc.mtaasafi.android.newReport.NewReportActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Agree on 10/10/2014.
 */

public class VoteInterface extends LinearLayout {
    TextView voteCountTV;
    ImageButton upvote;
    private static final String UPVOTE_DATA_KEY = "upvote_data";
    public VoteInterface(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void onFinishInflate(){
        voteCountTV = (TextView) findViewById(R.id.upvoteCount);
        upvote = (ImageButton) findViewById(R.id.upvoteButton);
        upvote.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>();

                    // tell the reports table that you upvoted the report
                    int reportDBId = (Integer) view.getTag();
                    ContentProviderOperation.Builder userUpvotedUpdate =
                            ContentProviderOperation.newUpdate(Report.uriFor(reportDBId));
                    userUpvotedUpdate.withValue(ReportContract.Entry.COLUMN_USER_UPVOTED, 1);
                    batch.add(userUpvotedUpdate.build());
                    ContentProviderOperation.Builder upvoteCountUpdate =
                            ContentProviderOperation.newUpdate(Report.uriFor(reportDBId));
                    int currentVoteCount = Integer.parseInt(voteCountTV.getText().toString());
                    upvoteCountUpdate.withValue(ReportContract.Entry.COLUMN_UPVOTE_COUNT, currentVoteCount+1);

                    // TODO: check for multiple reports entered in the db? (maybe)
                    // tell the upvote log table that you upvoted the report
                    int reportServerId = (Integer) voteCountTV.getTag();
                    ContentProviderOperation.Builder upvoteOperation =
                            ContentProviderOperation.newInsert(ReportContract.UpvoteLog.UPVOTE_URI);
                    upvoteOperation.withValue(ReportContract.UpvoteLog.COLUMN_SERVER_ID, reportServerId);
                    batch.add(upvoteOperation.build());

                    getContext().getContentResolver().applyBatch(ReportContract.CONTENT_AUTHORITY, batch);
                    getContext().getContentResolver()
                            .notifyChange(ReportContract.UpvoteLog.UPVOTE_URI, null, false);
                    voteCountTV.setTextColor(getResources().getColor(R.color.mtaa_safi_blue));
                } catch (RemoteException e) {
                    e.printStackTrace();
                } catch (OperationApplicationException e) {
                    e.printStackTrace();
                }
                // tell the upvote log table you voted

            }
        });
    }

    public static JSONObject recordUpvoteLog(Context context, JSONObject jsonRecord){
        try {
            ArrayList<Integer> upvoteIds = getUpvotesFromDb(context);
            if(upvoteIds.size() > 0){
                JSONObject upvoteData = new JSONObject();
                ComplexPreferences cp = PrefUtils.getPrefs(context);
                String userName = cp.getString(PrefUtils.USERNAME, "");
                if(!userName.equals("")){
                    userName = PrefUtils.trimUsername(userName);
                    upvoteData.put("username", userName);
                    JSONArray upvoteIdsJSON = new JSONArray();
                    for(int id : upvoteIds){
                        upvoteIdsJSON.put(id);
                    }
                    upvoteData.put("ids", upvoteIds);
                    jsonRecord.put(UPVOTE_DATA_KEY, upvoteData);
                }
            }
            return jsonRecord;
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (OperationApplicationException e) {
            e.printStackTrace();
        }
        return null;
    }
    // return an arraylist of server ids of reports the user has upvoted that the server has not yet received
    // delete duplicate entries in the upvote log table
    private static ArrayList getUpvotesFromDb(Context context) throws RemoteException, OperationApplicationException {
        Cursor upvoteLog = getUpvoteLog(context);
        ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>();
        ArrayList upvoteIds = new ArrayList();
        while(upvoteLog.moveToNext()){
            int serverId = upvoteLog.getInt(upvoteLog.getColumnIndex(ReportContract.UpvoteLog.COLUMN_SERVER_ID));
            if(upvoteIds.contains(serverId)){ // schedule duplicates of ids for deletion
                Log.i("getUPVOTES", "Upvote id: " + serverId + " was a duplicate, deleting");
                Uri toDeleteUri = ReportContract.UpvoteLog.UPVOTE_URI.buildUpon()
                        .appendPath(Integer.toString(upvoteLog.getInt(upvoteLog.getColumnIndex(ReportContract.UpvoteLog.COLUMN_ID)))).build();
                batch.add(ContentProviderOperation.newDelete(toDeleteUri).build());
            } else { // add the server id to the upvoteIds list
                Log.i("getUPVOTES", "ADDED " + serverId + " to upvoteIds");
                upvoteIds.add(serverId);
            }
        }
        upvoteLog.close();
        context.getContentResolver().applyBatch(ReportContract.CONTENT_AUTHORITY, batch);
        context.getContentResolver().notifyChange(ReportContract.UpvoteLog.UPVOTE_URI, null, false);
        return upvoteIds;
    }

    // takes JSON responses from the server and updates the user's upvote log and report tables
    // if there is any upvote data in the response
    public static void updateUpvoteData(Context context, JSONObject serverResponse) throws JSONException, RemoteException, OperationApplicationException {
        if(serverResponse.toString().contains(UPVOTE_DATA_KEY)){
            JSONArray upvoteData = serverResponse.getJSONArray(UPVOTE_DATA_KEY);
            Cursor upvoteLog = getUpvoteLog(context);
            Cursor reportData = getReportUpvoteData(context);
            ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>();
            // for each serverId that the server received upvotes for, delete the entry in the upvote log
            // and update the entry in the report table (if they exist)
            for(int i = 0; i < upvoteData.length(); i++){
                JSONObject upvoteJson = upvoteData.getJSONObject(i);
                int upvoteServerId = upvoteJson.getInt("id");
                int upvoteCount = upvoteJson.getInt("upvote_count");
                ContentProviderOperation deleteFromUpvoteLog = attemptScheduleDelete(upvoteServerId, upvoteLog);
                ContentProviderOperation updateInReportTable = attemptScheduleUpdate(upvoteServerId, upvoteCount, reportData);
                if(deleteFromUpvoteLog != null)
                    batch.add(deleteFromUpvoteLog);
                if(updateInReportTable != null)
                    batch.add(updateInReportTable);
            }
            upvoteLog.close();
            reportData.close();
            context.getContentResolver().applyBatch(ReportContract.CONTENT_AUTHORITY, batch);
            context.getContentResolver().notifyChange(ReportContract.UpvoteLog.UPVOTE_URI, null, false);
        } else {
            Log.e("UPDATE UPVOTE DATA", "Server didn't have any upvote data for me! Cool!");
        }
    }

    private static Cursor getUpvoteLog(Context context){
        return context.getContentResolver()
                .query(ReportContract.UpvoteLog.UPVOTE_URI, null, null, null, null);
    }
    // delete the entry with the given serverId if it exists in the upvoteLog
    private static ContentProviderOperation attemptScheduleDelete(int serverId, Cursor upvoteLog){
        int column_id = upvoteLog.getColumnIndex(ReportContract.UpvoteLog.COLUMN_ID);
        int column_serverId = upvoteLog.getColumnIndex(ReportContract.UpvoteLog.COLUMN_SERVER_ID);
        while(upvoteLog.moveToNext()){ // Performance note: upvoteLog should never have > 10 entries
            int upvoteLogServerId = upvoteLog.getInt(column_serverId);
            if(upvoteLogServerId == serverId){
                Uri toDeleteUri = uriFor(upvoteLog.getInt(column_id));
                return ContentProviderOperation.newDelete(toDeleteUri).build();
            }
        }
        return null;
    }

    private static Cursor getReportUpvoteData(Context context){
        String[] projection = new String[2];
        projection[0] = ReportContract.Entry.COLUMN_ID;
        projection[1] = ReportContract.Entry.COLUMN_SERVER_ID;
        String[] selectionArgs = new String[1];
        selectionArgs[0] = "0";
        return context.getContentResolver()
                .query(ReportContract.Entry.CONTENT_URI, projection,
                        ReportContract.Entry.COLUMN_USER_UPVOTED + " = ?",
                        selectionArgs, null);
    }

    private static ContentProviderOperation attemptScheduleUpdate(int serverId, int upvoteCount, Cursor reportData){
        int column_id = reportData.getColumnIndex(ReportContract.Entry.COLUMN_ID);
        int column_server_id = reportData.getColumnIndex(ReportContract.Entry.COLUMN_SERVER_ID);
        while(reportData.moveToNext()){
            int reportTableServerId = reportData.getInt(column_server_id);
            if(reportTableServerId == serverId){
                Uri toUpdateUri = Report.uriFor(reportData.getInt(column_id));
                return ContentProviderOperation.newUpdate(toUpdateUri)
                                        .withValue(ReportContract.Entry.COLUMN_USER_UPVOTED, 1)
                                        .withValue(ReportContract.Entry.COLUMN_UPVOTE_COUNT, upvoteCount).build();
            }
        }
        return null;
    }

    public static Uri uriFor(int dbId){
        return ReportContract.UpvoteLog.UPVOTE_URI.buildUpon().appendPath(Integer.toString(dbId)).build();
    }
}
