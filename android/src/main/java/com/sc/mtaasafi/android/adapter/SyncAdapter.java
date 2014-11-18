package com.sc.mtaasafi.android.adapter;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.SyncResult;
import android.database.Cursor;
import android.location.Location;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;

import com.sc.mtaasafi.android.Report;
import com.sc.mtaasafi.android.SystemUtils.ComplexPreferences;
import com.sc.mtaasafi.android.SystemUtils.NetworkUtils;
import com.sc.mtaasafi.android.SystemUtils.PrefUtils;
import com.sc.mtaasafi.android.SystemUtils.URLs;
import com.sc.mtaasafi.android.database.Contract;
import com.sc.mtaasafi.android.feed.VoteInterface;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.util.ArrayList;

/* This class is instantiated in {@link SyncService}, which also binds SyncAdapter to the system.
 * SyncAdapter should only be initialized in SyncService, never anywhere else. */
class SyncAdapter extends AbstractThreadedSyncAdapter {

    public static final String TAG = "SyncAdapter";
    private static final int NET_CONNECT_TIMEOUT_MILLIS = 15000;  // 15 seconds
    private static final int NET_READ_TIMEOUT_MILLIS = 10000;  // 10 seconds

    private final ContentResolver mContentResolver;
    private ComplexPreferences cp;
    // Project used when querying content provider. Returns all known fields.
    // Constants representing column positions from PROJECTION.

    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        Log.i(TAG, "Constructing");
        mContentResolver = context.getContentResolver();
        cp = PrefUtils.getPrefs(context);
    }

    public SyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        Log.i(TAG, "Constructing");
        mContentResolver = context.getContentResolver();
    }

    // The syncResult argument allows you to pass information back to the method that triggered the sync.
    @Override
    public void onPerformSync(Account account, Bundle extras, String authority,
                              ContentProviderClient provider, SyncResult syncResult) {
        Log.i(TAG, "Beginning network synchronization");
        try {
            Log.i(TAG, "Streaming data from network: " + URLs.FEED);
            ArrayList serverIds = getServerIds();
            if(serverIds != null)
                updateLocalFeedData(serverIds, syncResult);
            else //throw a fit?
                 // TODO : handle null location errors, which will occur first time user opens app
                return;
        } catch (MalformedURLException e) {
            Log.wtf(TAG, "Feed URL is malformed", e);
            syncResult.stats.numParseExceptions++;
            return;
        } catch (IOException e) {
            Log.e(TAG, "Error reading from network: " + e.toString());
            syncResult.stats.numIoExceptions++;
            return;
        } catch (XmlPullParserException e) {
            Log.e(TAG, "Error parsing feed: " + e.toString());
            syncResult.stats.numParseExceptions++;
            return;
        } catch (ParseException e) {
            Log.e(TAG, "Error parsing feed: " + e.toString());
            syncResult.stats.numParseExceptions++;
            return;
        } catch (RemoteException e) {
            Log.e(TAG, "Error updating database: " + e.toString());
            syncResult.databaseError = true;
            return;
        } catch (OperationApplicationException e) {
            Log.e(TAG, "Error updating database: " + e.toString());
            syncResult.databaseError = true;
            return;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.i(TAG, "Network synchronization complete");
    }

    public void updateLocalFeedData(final ArrayList serverIds, final SyncResult syncResult)
            throws IOException, XmlPullParserException, RemoteException,
            OperationApplicationException, ParseException, JSONException {

        ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>();
        ArrayList<Integer> dbIds = getDbIds(syncResult);
        for (int i = 0; i < dbIds.size(); i++) {
            batch.add(ContentProviderOperation.newDelete(Report.getUri(dbIds.get(i))).build());
            syncResult.stats.numDeletes++;
        }
        writeNewReports(getNewReportsFromServer(serverIds), batch, syncResult);
        Log.i(TAG, "Merge solution ready. Applying batch update");
        getDbIds(syncResult);
        mContentResolver.applyBatch(Contract.CONTENT_AUTHORITY, batch);
        mContentResolver.notifyChange(Contract.Entry.CONTENT_URI, null, false);
    }

    private void writeNewReports(JSONObject serverResponse, ArrayList<ContentProviderOperation> batch, SyncResult syncResult)
            throws RemoteException, OperationApplicationException, JSONException {
        if (serverResponse == null)
            return; // TODO: add error statement
        JSONArray reportsArray = serverResponse.getJSONArray("reports");
        for (int i = 0; i < reportsArray.length(); i++) {
            Report report = new Report(reportsArray.getJSONObject(i), -1);
            batch.add(ContentProviderOperation
                    .newInsert(Contract.Entry.CONTENT_URI)
                    .withValues(report.getContentValues())
                    .build());
            syncResult.stats.numInserts++;
        }
        VoteInterface.onUpvotesRecorded(getContext(), serverResponse);
    }

    private JSONObject getNewReportsFromServer(ArrayList serverIds) throws IOException, JSONException{
        String fetchReportsURL = URLs.FEED + cp.getObject(PrefUtils.SCREEN_WIDTH, Integer.class) + "/";
        ComplexPreferences cp = PrefUtils.getPrefs(getContext());
        String username = cp.getString(PrefUtils.USERNAME, "");
        if(!username.isEmpty()){
            JSONObject fetchRequest = new JSONObject().put("username", PrefUtils.trimUsername(username))
                                                      .put("ids", new JSONArray());
            for(int i=0; i < serverIds.size(); i++)
                fetchRequest.accumulate("ids", serverIds.get(i));
            VoteInterface.recordUpvoteLog(getContext(), fetchRequest);
            Log.i("FETCH_REQUEST", fetchRequest.toString());
            return makeRequest(fetchReportsURL, fetchRequest);
        }
        return null;
    }

    // retrieves from the server a list of ids that are within some radius of the user's current location
    private ArrayList getServerIds() throws IOException, JSONException{
        ComplexPreferences cp = PrefUtils.getPrefs(getContext());
        Location cachedLocation = cp.getObject(PrefUtils.LOCATION, Location.class);
        if(cachedLocation != null){
            JSONObject locationJSON = new JSONObject()
                    .put("latitude", cachedLocation.getLatitude())
                    .put("longitude", cachedLocation.getLongitude());
            String responseString = makeRequest(URLs.FEED, locationJSON).toString();
            String[] responseStringArray =  responseString
                                            .replaceAll("\\[", "").replaceAll("\\]", "")
                                            .split(", ");
            ArrayList serverIds = new ArrayList();
            for(String id : responseStringArray)
                serverIds.add(Integer.parseInt(id));
            return serverIds;
        } else
            return null;

    }

    private ArrayList<Integer> getDbIds(SyncResult syncResult){
        // Get list of all items
        Log.i(TAG, "getting local entries for merge");
        String[] projection = new String[1];
        projection[0] = Contract.Entry.COLUMN_ID;
        Cursor c = getContext().getContentResolver() // Get all entries that aren't pending reports
                    .query(Contract.Entry.CONTENT_URI, projection,
                    Contract.Entry.COLUMN_MEDIAURL3 + " LIKE 'http%'", null, null);
        assert c != null;
        ArrayList<Integer> dbIds = new ArrayList<Integer>();
        while (c.moveToNext()) {
            syncResult.stats.numEntries++;
            dbIds.add(c.getInt(0));
        }
        c.close();
        return dbIds;
    }

    private JSONObject makeRequest(String url, JSONObject entity) throws IOException {
        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(url);
        httpPost.setHeader("Accept", "application/json");
        httpPost.setHeader("Content-type", "application/json");
        httpPost.setEntity(new StringEntity(entity.toString()));
        HttpResponse response = httpClient.execute(httpPost);
        if (response.getStatusLine().getStatusCode() > 400) { /*TODO: alert for statuses > 400*/ }
        return NetworkUtils.convertHttpResponseToJSON(response);
    }
}
