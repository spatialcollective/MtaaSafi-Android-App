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
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;

import com.sc.mtaasafi.android.ReportContract;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
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
import java.util.HashMap;
import java.util.List;

/* This class is instantiated in {@link SyncService}, which also binds SyncAdapter to the system.
 * SyncAdapter should only be initialized in SyncService, never anywhere else. */
class SyncAdapter extends AbstractThreadedSyncAdapter {
    public static final String TAG = "SyncAdapter";
    private static final String FEED_URL = "http://app.spatialcollective.com/get_posts/" + 400; //screenwidth;
    private static final int NET_CONNECT_TIMEOUT_MILLIS = 15000;  // 15 seconds
    private static final int NET_READ_TIMEOUT_MILLIS = 10000;  // 10 seconds

    private final ContentResolver mContentResolver;

    // Project used when querying content provider. Returns all known fields.
    private static final String[] PROJECTION = new String[] {
        ReportContract.Entry._ID,
        ReportContract.Entry.COLUMN_TITLE,
        ReportContract.Entry.COLUMN_DETAILS,
        ReportContract.Entry.COLUMN_TIMESTAMP,
        ReportContract.Entry.COLUMN_LAT,
        ReportContract.Entry.COLUMN_LNG,
        ReportContract.Entry.COLUMN_USERNAME,
        ReportContract.Entry.COLUMN_PICS,
        ReportContract.Entry.COLUMN_MEDIAURLS };

    // Constants representing column positions from PROJECTION.
    public static final int COLUMN_ID = 0,
        COLUMN_ENTRY_ID = 1,
        COLUMN_TITLE = 2,
        COLUMN_DETAILS = 3,
        COLUMN_TIMESTAMP = 4,
        COLUMN_LAT = 5,
        COLUMN_LNG = 6,
        COLUMN_USERNAME = 7,
        COLUMN_PICS = 8,
        COLUMN_MEDIAURLS = 9;

    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        Log.i(TAG, "Constructing");
        mContentResolver = context.getContentResolver();
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
            final URL location = new URL(FEED_URL);
            InputStream stream = null;

            try {
                Log.i(TAG, "Streaming data from network: " + location);
                stream = downloadUrl(FEED_URL);
                updateLocalFeedData(stream, syncResult);
            } finally {
                if (stream != null)
                    stream.close();
            }
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
        }
        Log.i(TAG, "Network synchronization complete");
    }

    /**
     * <p>This is where incoming data is persisted, committing the results of a sync. In order to
     * minimize (expensive) disk operations, we compare incoming data with what's already in our
     * database, and compute a merge. Only changes (insert/update/delete) will result in a database
     * write.
     *
     * <p>As an additional optimization, we use a batch operation to perform all database writes at
     * once.
     *
     * <p>Merge strategy:
     * 1. Get cursor to all items in feed<br/>
     * 2. For each item, check if it's in the incoming data.<br/>
     *    a. YES: Remove from "incoming" list. Check if data has mutated, if so, perform
     *            database UPDATE.<br/>
     *    b. NO: Schedule DELETE from database.<br/>
     * (At this point, incoming database only contains missing items.)<br/>
     * 3. For any items remaining in incoming list, ADD to database.
     */

    // Above is ideal situation. For now just drop the db and re-add everything.
    public void updateLocalFeedData(final InputStream stream, final SyncResult syncResult)
            throws IOException, XmlPullParserException, RemoteException,
            OperationApplicationException, ParseException {

        final ContentResolver contentResolver = getContext().getContentResolver();
        ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>();

        // Get list of all items
        Log.i(TAG, "Fetching local entries for merge");
        Cursor c = contentResolver.query(ReportContract.Entry.CONTENT_URI, PROJECTION, null, null, null); // Get all entries
        assert c != null;
        Log.i(TAG, "Found " + c.getCount() + " local entries. Computing merge solution...");

        // Find stale data
        int id;
        String entryId;
        String title;
        String details;
        long published;
        while (c.moveToNext()) {
            syncResult.stats.numEntries++;
            id = c.getInt(COLUMN_ID);
            // entryId = c.getString(COLUMN_ENTRY_ID);
            // title = c.getString(COLUMN_TITLE);
            // link = c.getString(COLUMN_LINK);
            // published = c.getLong(COLUMN_PUBLISHED);
            // FeedParser.Entry match = entryMap.get(entryId);
            // if (match != null) {
            //     // Entry exists. Remove from entry map to prevent insert later.
            //     entryMap.remove(entryId);
            //     // Check to see if the entry needs to be updated
            //     Uri existingUri = ReportContract.Entry.CONTENT_URI.buildUpon()
            //             .appendPath(Integer.toString(id)).build();
            //     if ((match.title != null && !match.title.equals(title)) ||
            //             (match.link != null && !match.link.equals(link)) ||
            //             (match.published != published)) {
            //         // Update existing record
            //         Log.i(TAG, "Scheduling update: " + existingUri);
            //         batch.add(ContentProviderOperation.newUpdate(existingUri)
            //                 .withValue(ReportContract.Entry.COLUMN_TITLE, title)
            //                 .withValue(ReportContract.Entry.COLUMN_DETAILS, link)
            //                 .withValue(ReportContract.Entry.COLUMN_TIMESTAMP, published)
            //                 .build());
            //         syncResult.stats.numUpdates++;
            //     } else {
            //         Log.i(TAG, "No action: " + existingUri);
            //     }
            // } else {
                // Entry doesn't exist. Remove it from the database.
                Uri deleteUri = ReportContract.Entry.CONTENT_URI.buildUpon()
                        .appendPath(Integer.toString(id)).build();
                Log.i(TAG, "Scheduling delete: " + deleteUri);
                batch.add(ContentProviderOperation.newDelete(deleteUri).build());
                syncResult.stats.numDeletes++;
            // }
        }
        c.close();

        try {
            String serverString = convertInputStreamToString(stream);
            JSONArray jsonData = convertStringToJson(serverString);
            int len = jsonData.length();
            Log.i(TAG, "Got " + Integer.toString(len) + " Json objects in response");
            for (int i = 0; i < len; i++) {
                JSONObject entry = jsonData.getJSONObject(i);
                Log.i(TAG, "Entry: " + entry.toString());
                batch.add(ContentProviderOperation.newInsert(ReportContract.Entry.CONTENT_URI)
                    .withValue(ReportContract.Entry.COLUMN_TITLE, entry.getString(ReportContract.Entry.COLUMN_TITLE))
                    .withValue(ReportContract.Entry.COLUMN_DETAILS, entry.getString(ReportContract.Entry.COLUMN_DETAILS))
                    .withValue(ReportContract.Entry.COLUMN_TIMESTAMP, entry.getString(ReportContract.Entry.COLUMN_TIMESTAMP))
                    .withValue(ReportContract.Entry.COLUMN_LAT, entry.getString(ReportContract.Entry.COLUMN_LAT))
                    .withValue(ReportContract.Entry.COLUMN_LNG, entry.getString(ReportContract.Entry.COLUMN_LNG))
                    .withValue(ReportContract.Entry.COLUMN_USERNAME, entry.getString(ReportContract.Entry.COLUMN_USERNAME))
                    .withValue(ReportContract.Entry.COLUMN_PICS, "")
                    .withValue(ReportContract.Entry.COLUMN_MEDIAURLS, "")
                    .build());
                syncResult.stats.numInserts++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.i(TAG, "Merge solution ready. Applying batch update");
        mContentResolver.applyBatch(ReportContract.CONTENT_AUTHORITY, batch);
        mContentResolver.notifyChange(ReportContract.Entry.CONTENT_URI, null, false); // IMPORTANT: Do not sync to network (last arg)
        // This sample doesn't support uploads, but if *your* code does, make sure you set
        // syncToNetwork=false in the line above to prevent duplicate syncs.
    }


    private InputStream downloadUrl(String url) throws IOException {
        HttpClient httpClient = new DefaultHttpClient();
        HttpResponse httpResponse = httpClient.execute(new HttpGet(url));
        InputStream inputStream = httpResponse.getEntity().getContent();
        return inputStream;
    }
    // Given a string representation of a URL, sets up a connection and gets an input stream.
    // private InputStream downloadUrl(final URL url) throws IOException {
    //     HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    //     conn.setReadTimeout(NET_READ_TIMEOUT_MILLIS /* milliseconds */);
    //     conn.setConnectTimeout(NET_CONNECT_TIMEOUT_MILLIS /* milliseconds */);
    //     conn.setRequestMethod("GET");
    //     conn.setDoInput(true);
    //     // Starts the query
    //     conn.connect();
    //     return conn.getInputStream();
    // }

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