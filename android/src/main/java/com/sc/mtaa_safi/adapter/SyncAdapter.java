package com.sc.mtaa_safi.adapter;

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

import com.sc.mtaa_safi.Community;
import com.sc.mtaa_safi.R;
import com.sc.mtaa_safi.Report;
import com.sc.mtaa_safi.SystemUtils.NetworkUtils;
import com.sc.mtaa_safi.SystemUtils.Utils;
import com.sc.mtaa_safi.database.Contract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.util.ArrayList;

/* This class is instantiated in {@link SyncService}, which also binds SyncAdapter to the system.
 * SyncAdapter should only be initialized in SyncService, never anywhere else. */
class SyncAdapter extends AbstractThreadedSyncAdapter {

    public static final String TAG = "SyncAdapter";
    private Context mContext;

    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mContext = context;
    }

    // The syncResult argument allows you to pass information back to the method that triggered the sync.
    @Override
    public void onPerformSync(Account account, Bundle extras, String authority,
                              ContentProviderClient provider, SyncResult syncResult) {
        syncFromServer(provider, syncResult);
    }


    private void updatePlaces(ContentProviderClient provider) throws IOException, JSONException, RemoteException, OperationApplicationException {
        Location cachedLocation = Utils.getLocation(mContext);
        Log.i(TAG, "Streaming data from network: " + this.getContext().getString(R.string.location_data));
        if (cachedLocation != null && cachedLocation.getTime() != 0) {
            JSONObject responseJson = NetworkUtils.makeRequest(this.getContext().getString(R.string.location_data), "get", null);
            Community.addCommunities(responseJson, provider);
        }
    }

    private ArrayList getServerIds() throws IOException, JSONException {
        if (Utils.getSelectedAdminId(mContext) == -1)
            return getIdsForCoords();
        else
            return getIdsForAdmin();
    }

    private ArrayList getIdsForCoords() throws IOException, JSONException {
        Location cachedLocation = Utils.getLocation(mContext);
        Log.e(TAG, "cachedLocation: " + cachedLocation);
        ArrayList serverIds = new ArrayList();
        if (cachedLocation != null && cachedLocation.getTime() != 0) {
            JSONObject responseJSON = NetworkUtils.makeRequest(this.getContext().getString(R.string.feed) + Utils.getUserId(mContext) + "/" + cachedLocation.getLongitude() + "/" + cachedLocation.getLatitude() + "/", "get", null);
            JSONArray serverIdsJSON = responseJSON.getJSONArray("ids");
            for (int i = 0; i < serverIdsJSON.length(); i++)
                serverIds.add(serverIdsJSON.getInt(i));
        }
        return serverIds;
    }

    private ArrayList getIdsForAdmin() throws IOException, JSONException {
        JSONObject responseJSON = NetworkUtils.makeRequest(this.getContext().getString(R.string.feed) + Utils.getUserId(mContext) + "/" + Utils.getSelectedAdminId(mContext) + "/", "get", null);
        ArrayList serverIds = new ArrayList();
        JSONArray serverIdsJSON = responseJSON.getJSONArray("ids");
        for (int i = 0; i < serverIdsJSON.length(); i++)
            serverIds.add(serverIdsJSON.getInt(i));
        return serverIds;
    }

    public void updateLocalFeedData(ArrayList serverIds, ContentProviderClient provider, final SyncResult syncResult)
            throws IOException, XmlPullParserException, RemoteException,
            OperationApplicationException, ParseException, JSONException {

        ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>();
        String[] projection = {Contract.Entry.COLUMN_ID, Contract.Entry.COLUMN_SERVER_ID};
        Cursor c = provider.query(Contract.Entry.CONTENT_URI, projection,
                           Contract.Entry.COLUMN_PENDINGFLAG + " = -1", null, null);
        while (c.moveToNext()) {
            int serverId = c.getInt(c.getColumnIndex(Contract.Entry.COLUMN_SERVER_ID));
            if (!serverIds.contains(serverId)) {
                batch.add(ContentProviderOperation.newDelete(Report.getUri(c.getInt(0))).build());
                syncResult.stats.numEntries++;
                syncResult.stats.numDeletes++;
            } else {
                serverIds.remove(serverIds.indexOf(serverId));
            }
        }
        writeNewReports(getNewReportsFromServer(serverIds, provider), batch, provider, syncResult);
        c.close();

        Log.i(TAG, "Merge solution ready. Applying batch update");
        provider.applyBatch(batch);
    }

    private void writeNewReports(JSONObject serverResponse, ArrayList<ContentProviderOperation> batch, ContentProviderClient provider, SyncResult syncResult)
            throws RemoteException, OperationApplicationException, JSONException {
        if (serverResponse == null)
            return;
        JSONArray reportsArray = serverResponse.getJSONArray("reports");
        for (int i = 0; i < reportsArray.length(); i++) {
            Report report = new Report(reportsArray.getJSONObject(i), -1);
            batch = report.createContentProviderOperation(batch);
            syncResult.stats.numEntries++;
            syncResult.stats.numInserts++;
            provider.delete(Contract.UpvoteLog.UPVOTE_URI, null, null);
        }
    }

    private JSONObject getNewReportsFromServer(ArrayList serverIds, ContentProviderClient provider) throws
            IOException, JSONException, OperationApplicationException, RemoteException {

        if (!Utils.getUserName(mContext).isEmpty() && !serverIds.isEmpty()) {
            String fetchReportsURL = this.getContext().getString(R.string.feed_details) + Utils.getScreenWidth(mContext) + "/";
            JSONObject fetchRequest = new JSONObject().put("userId", Utils.getUserId(mContext))
                                                      .put("ids", new JSONArray());
            for (int i=0; i < serverIds.size(); i++)
                fetchRequest.accumulate("ids", serverIds.get(i));
            fetchRequest = addNewUpvotes(fetchRequest, provider);
            Log.i("FETCH_REQUEST", fetchRequest.toString());
            return NetworkUtils.makeRequest(fetchReportsURL, "post", fetchRequest);
        }
        return null;
    }

    private JSONObject addNewUpvotes(JSONObject fetchRequest, ContentProviderClient provider) throws RemoteException, OperationApplicationException, JSONException {
        JSONObject upvoteData = new JSONObject().put("userId", Utils.getUserId(mContext));
        upvoteData.put("ids", new JSONArray());

        Cursor upvoteLog = provider.query(Contract.UpvoteLog.UPVOTE_URI, null, null, null, null);
        while (upvoteLog.moveToNext())
            upvoteData.accumulate("ids", upvoteLog.getInt(upvoteLog.getColumnIndex(Contract.UpvoteLog.COLUMN_SERVER_ID)));
        fetchRequest.put("upvote_data", upvoteData);
        upvoteLog.close();
        return fetchRequest;
    }

    private void syncFromServer(ContentProviderClient provider, SyncResult syncResult) {
        try {
            updatePlaces(provider);
            ArrayList serverIds = getServerIds();
            if (serverIds != null)
                updateLocalFeedData(serverIds,  provider, syncResult);
            else // TODO : handle null location errors, which will occur first time user opens app
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
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.e(TAG, "Network synchronization complete");
    }
}
