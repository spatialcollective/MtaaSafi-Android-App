package com.sc.mtaasafi.android.adapter;

import android.accounts.Account;
import android.app.Activity;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.SyncResult;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.sc.mtaasafi.android.Report;
import com.sc.mtaasafi.android.SystemUtils.ComplexPreferences;
import com.sc.mtaasafi.android.SystemUtils.LogTags;
import com.sc.mtaasafi.android.SystemUtils.PrefUtils;
import com.sc.mtaasafi.android.database.ReportContract;

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
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.TreeSet;

/* This class is instantiated in {@link SyncService}, which also binds SyncAdapter to the system.
 * SyncAdapter should only be initialized in SyncService, never anywhere else. */
class SyncAdapter extends AbstractThreadedSyncAdapter {

    public static final String TAG = "SyncAdapter";
    private static final String FEED_URL = "http://app.spatialcollective.com/fetch_reports/";
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
            Log.i(TAG, "Streaming data from network: " + FEED_URL);
            updateLocalFeedData(getServerIds(), syncResult);
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

        final ContentResolver contentResolver = getContext().getContentResolver();
        ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>();

        // Get list of all items
        Log.i(TAG, "Fetching local entries for merge");
        String[] projection = new String[1];
        projection[0] = ReportContract.Entry.COLUMN_SERVER_ID;
        Cursor c = contentResolver.query(ReportContract.Entry.CONTENT_URI, projection, ReportContract.Entry.COLUMN_PENDINGFLAG + " = -1", null, null);
        assert c != null;
        Log.i(TAG, "Found " + c.getCount() + " local entries. Computing merge solution...");
        TreeSet<Integer> dbIds = new TreeSet<Integer>();
        while (c.moveToNext()) {
            syncResult.stats.numEntries++;
            dbIds.add(c.getInt(0));
        }
        c.close();
        // for each id from the server, if it is in the local DB,
        // remove the id from both the DB ids and the server ids
        showToast("Added to dbIds: " + dbIds.size() + " entries");
        Log.i(LogTags.BACKEND_W, "Added to dbIds: " + dbIds.size() + " entries");
        for(int i = 0; i < serverIds.size(); i++) {
            if (dbIds.remove(serverIds.get(i)))
                serverIds.remove(i);
        }

        Log.e(LogTags.BACKEND_R, "Deleting " + dbIds.size() + " DB entries");
        // delete all of the reports in the DB which the server didn't also have
        Integer dbIdToDelete = dbIds.pollFirst();
        Uri toDeleteUri;
        while(dbIdToDelete != null) {
            if(dbIdToDelete != 0){
                toDeleteUri = ReportContract.Entry.CONTENT_URI.buildUpon()
                        .appendPath(Integer.toString(dbIdToDelete)).build();
                batch.add(ContentProviderOperation.newDelete(toDeleteUri).build());
                Log.i(TAG, "Scheduled delete: " + toDeleteUri);
                syncResult.stats.numDeletes++;
            }
            dbIdToDelete = dbIds.pollFirst();
        }
        // fetch the new reports, which the local DB didn't have, from the server and write them to DB
        writeNewReportsToDB(getNewReportsFromServer(serverIds), batch, syncResult);
    }

    private void writeNewReportsToDB(JSONArray newReports, ArrayList<ContentProviderOperation> batch,
                                 SyncResult syncResult)
            throws RemoteException, OperationApplicationException {
        try {
            Log.i(TAG, "Got " + newReports.length() + " Json objects in response");

            showToast("Got " + newReports.length() + " Json objects in response");
            for (int i = 0; i < newReports.length(); i++) {
                JSONObject entry = newReports.getJSONObject(i);
                JSONArray mediaURLsJSON = entry.getJSONArray("mediaURLs");
                ArrayList<String> mediaURLs = new ArrayList<String>();
                for (int j = 0; j < mediaURLsJSON.length(); j++)
                    mediaURLs.add(mediaURLsJSON.get(j).toString());

                batch.add(ContentProviderOperation.newInsert(ReportContract.Entry.CONTENT_URI)
                        .withValue(ReportContract.Entry.COLUMN_SERVER_ID, entry.getString(ReportContract.Entry.COLUMN_SERVER_ID))
                        .withValue(ReportContract.Entry.COLUMN_TITLE, entry.getString(ReportContract.Entry.COLUMN_TITLE))
                        .withValue(ReportContract.Entry.COLUMN_DETAILS, entry.getString(ReportContract.Entry.COLUMN_DETAILS))
                        .withValue(ReportContract.Entry.COLUMN_TIMESTAMP, entry.getString(ReportContract.Entry.COLUMN_TIMESTAMP))
                        .withValue(ReportContract.Entry.COLUMN_LAT, entry.getString(ReportContract.Entry.COLUMN_LAT))
                        .withValue(ReportContract.Entry.COLUMN_LNG, entry.getString(ReportContract.Entry.COLUMN_LNG))
                        .withValue(ReportContract.Entry.COLUMN_USERNAME, entry.getString(ReportContract.Entry.COLUMN_USERNAME))
                        .withValue(ReportContract.Entry.COLUMN_MEDIAURL1, mediaURLs.get(0))
                        .withValue(ReportContract.Entry.COLUMN_MEDIAURL2, mediaURLs.get(1))
                        .withValue(ReportContract.Entry.COLUMN_MEDIAURL3, mediaURLs.get(2))
                        .build());
                syncResult.stats.numInserts++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.i(TAG, "Merge solution ready. Applying batch update");
        showToast("Merge solution ready. Applying batch update.");
        mContentResolver.applyBatch(ReportContract.CONTENT_AUTHORITY, batch);
        mContentResolver.notifyChange(ReportContract.Entry.CONTENT_URI, null, false);
    }
    private void showToast(final String message){
//        Activity ac = (Activity) getContext();
//                ac.runOnUiThread(new Runnable() {
//            public void run() {
//                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
//            }
//        });
    }
// retrieves from server a list of the objects
    private JSONArray getNewReportsFromServer(ArrayList serverIds) throws IOException, JSONException{
        String fetchReportsURL = FEED_URL + cp.getObject(PrefUtils.SCREEN_WIDTH, Integer.class) + "/";
        return convertStringToJson(getFromServer(fetchReportsURL, serverIds.toString()));
    }

    // retrieves from the server a list of ids that are within some radius of the user's current location
    private ArrayList getServerIds() throws IOException, JSONException{
        ComplexPreferences cp = PrefUtils.getPrefs(getContext());
        Location cachedLocation = cp.getObject(PrefUtils.LOCATION, Location.class);
        JSONObject locationJSON = new JSONObject()
                                    .put("latitude", cachedLocation.getLatitude())
                                    .put("longitude", cachedLocation.getLongitude());
        String responseString = getFromServer(FEED_URL, locationJSON.toString());
        String[] responseStringArray =  responseString
                                        .replaceAll("\\[", "").replaceAll("\\]", "")
                                        .split(", ");
        ArrayList serverIds = new ArrayList();
        for(String id : responseStringArray)
            serverIds.add(Integer.parseInt(id));
        return serverIds;
    }

    private String getFromServer(String url, String entity) throws IOException {
        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(url);
        httpPost.setHeader("Accept", "application/json");
        httpPost.setHeader("Content-type", "application/json");
        httpPost.setEntity(new StringEntity(entity));
        HttpResponse response = httpClient.execute(httpPost);
        if (response.getStatusLine().getStatusCode() > 400) { /*TODO: alert for statuses > 400*/ }
        InputStream is = response.getEntity().getContent();
        String responseString = convertInputStreamToString(is);
        is.close();
        return responseString;
        // final URL location = new URL(url);
        // HttpURLConnection conn = (HttpURLConnection) location.openConnection();
        // conn.setReadTimeout(NET_READ_TIMEOUT_MILLIS /* milliseconds */);
        // conn.setConnectTimeout(NET_CONNECT_TIMEOUT_MILLIS /* milliseconds */);
        // conn.setRequestMethod("POST");
        // conn.setDoInput(true);
        // conn.connect();
        // String responseString = convertInputStreamToString(conn.getInputStream());
        // conn.disconnect();
        // return responseString;
    }

    private static String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        StringBuilder result = new StringBuilder(inputStream.available());
        String line;

        while((line = bufferedReader.readLine()) != null)
            result.append(line);
        inputStream.close();
        return result.toString();
    }

    private JSONArray convertStringToJson(String input) throws JSONException {
        JSONArray jsonArray = new JSONArray(input);
        if (jsonArray.length() == 1 && jsonArray.getJSONObject(0).getString("error") != null)
            throw new JSONException("Server returned error");
        return jsonArray;
    }
}