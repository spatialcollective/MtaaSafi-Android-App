package com.sc.mtaa_safi.adapter;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentProviderOperation;
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
import com.sc.mtaa_safi.SystemUtils.URLConstants;
import com.sc.mtaa_safi.SystemUtils.Utils;
import com.sc.mtaa_safi.database.Contract;
import com.sc.mtaa_safi.feed.comments.Comment;
import com.sc.mtaa_safi.feed.tags.Tag;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;

/* This class is instantiated in {@link SyncService}, which also binds SyncAdapter to the system.
 * SyncAdapter should only be initialized in SyncService, never anywhere else. */
class SyncAdapter extends AbstractThreadedSyncAdapter {
    public static final String TAG = "SyncAdapter";
    private Context mContext;
    private String[] projection = { Contract.Entry.COLUMN_ID, Contract.Entry.COLUMN_SERVER_ID };

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
        JSONObject responseJson = NetworkUtils.makeRequest(URLConstants.buildURL(mContext, URLConstants.LOCATION_GET_URL), "get", null);
        Community.addCommunities(responseJson, provider);
    }

    private void updateTags(ContentProviderClient provider) throws IOException, JSONException, RemoteException, OperationApplicationException {

        JSONObject responseJSON = NetworkUtils.makeRequest(URLConstants.buildURL(mContext, URLConstants.TAG_GET_URL), "get", null);
        Tag.addTags(responseJSON, provider);
    }

    private JSONObject downloadReports(String type) throws IOException, JSONException {
        String args = "&userId=" + Utils.getUserId(this.getContext());
        if (type.equals("all"))
            args += "&all=true";
        else if (type.equals("user"))
            args += "&userOnly=true";
        if (Utils.getSelectedAdminId(this.getContext()) == -1) {
            Location location = Utils.getCoarseLocation(this.getContext());
            if (location != null && location.getTime() != 0)
                args += "&lng=" + location.getLongitude() + "&lat=" + location.getLatitude();
        } else
            args += "&adminId=" + Utils.getSelectedAdminId(this.getContext());
        return contactServer(args);
    }

    private JSONObject contactServer(String whichReports) throws IOException, JSONException {
        return NetworkUtils.makeRequest(URLConstants.buildURL(this.getContext(), URLConstants.REPORT_GET_URL + whichReports), "get", null);
    }

    public void updateLocalData(JSONObject serverData, ContentProviderClient provider, final SyncResult syncResult)
            throws IOException, XmlPullParserException, RemoteException,
            OperationApplicationException, ParseException, JSONException {

        ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>();

        updateReports(serverData, provider, syncResult, batch);
        Log.i(TAG, "Merge solution ready. Applying batch update");
        provider.applyBatch(batch);
        sanityCheck(serverData, provider, syncResult);
    }

    private void updateReports(JSONObject serverData, ContentProviderClient provider, final SyncResult syncResult, ArrayList batch) throws
            IOException, XmlPullParserException, RemoteException, OperationApplicationException, ParseException, JSONException {

        HashMap<Integer, Report> reportMap = createReportMap(serverData);
        Cursor c = provider.query(Contract.Entry.CONTENT_URI, projection, createCursorFilter(serverData), null, null);
        deleteUpdatedReports(reportMap, c, syncResult, batch);
        saveReports(reportMap, syncResult, batch);
        c.close();
    }
    private void deleteUpdatedReports(HashMap<Integer, Report> reportMap, Cursor c, SyncResult syncResult, ArrayList<ContentProviderOperation> batch) {
        while (c.moveToNext()) {
            syncResult.stats.numEntries++;
            int serverId = c.getInt(c.getColumnIndex(Contract.Entry.COLUMN_SERVER_ID));
            Report match = reportMap.get(serverId);
            if (match != null) {
                batch.add(ContentProviderOperation.newDelete(Report.getUri(c.getInt(0))).build());
                syncResult.stats.numDeletes++;
            }
        }
    }
    private void saveReports(HashMap<Integer, Report> reportMap, SyncResult syncResult, ArrayList<ContentProviderOperation> batch)
            throws RemoteException, OperationApplicationException, JSONException {
        for (Report r : reportMap.values()) {
            batch = r.createContentProviderOperation(batch);
            syncResult.stats.numInserts++;
        }
    }
    public void saveCommentsToDb(JSONObject commentsData) throws JSONException, RemoteException, OperationApplicationException {
        JSONArray commentsArray = commentsData.getJSONArray(Contract.Comments.TABLE_NAME);
        if (commentsArray != null) {
            ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>();
            for (int i = 0; i < commentsArray.length(); i++)
                Comment.getContentProviderOp(commentsArray.getJSONObject(i), batch, mContext);
            mContext.getContentResolver().applyBatch(Contract.CONTENT_AUTHORITY, batch);
            mContext.getContentResolver().notifyChange(Contract.Comments.COMMENTS_URI, null, false);
        }
    }
    private String createCursorFilter(JSONObject serverData) throws JSONException {
        String cursorFilter = Contract.Entry.COLUMN_PENDINGFLAG + " = -1";
        if (serverData.getJSONObject("meta").has("nearby_admins")) {
            String rawString = serverData.getJSONObject("meta").getString("nearby_admins");
            cursorFilter = cursorFilter + " AND " + Utils.createCursorAdminList(rawString);
            Utils.saveNearbyAdmins(mContext, rawString);
        } else if (Utils.getSelectedAdminId(this.getContext()) != -1) {
            cursorFilter += " AND " + Contract.Entry.COLUMN_ADMIN_ID + " = " + Utils.getSelectedAdminId(this.getContext());
        }
        return cursorFilter;
    }

    private void sanityCheck(JSONObject serverData, ContentProviderClient provider, SyncResult syncResult) throws
            IOException, XmlPullParserException, RemoteException,
            OperationApplicationException, ParseException, JSONException {
        syncResult.stats.numDeletes =  syncResult.stats.numInserts = syncResult.stats.numUpdates = 0;
        if (!db_Is_Sane(syncResult, serverData))
            updateLocalData(downloadReports("all"), provider, syncResult);
        if (!userHasOwnReports(serverData, provider))
            updateLocalData(downloadReports("user"), provider, syncResult);
    }
    private boolean db_Is_Sane(SyncResult syncResult, JSONObject serverData) throws JSONException {
        long totalCount = syncResult.stats.numEntries + syncResult.stats.numInserts - syncResult.stats.numDeletes;
        Log.v(TAG, "Cursor: " + totalCount + " Server Actual: " + serverData.getJSONObject("meta").getLong("actual_total"));
        if (totalCount >= serverData.getJSONObject("meta").getLong("actual_total"))
            return true;
        return false;
    }
    private boolean userHasOwnReports(JSONObject serverData, ContentProviderClient provider) throws JSONException, RemoteException {
        Cursor c = provider.query(Contract.Entry.CONTENT_URI, projection, Contract.Entry.COLUMN_USERID + " = " + Utils.getUserId(this.getContext()), null, null);
        if (c.moveToFirst()) {
            int userReportCount = c.getCount();
            c.close();
            if (!serverData.getJSONObject("meta").has("user_report_count") || serverData.getJSONObject("meta").getLong("user_report_count") <= userReportCount)
                return true;
        }
        return false;
    }

    private HashMap<Integer, Report> createReportMap(JSONObject serverData) throws JSONException {
        JSONArray reports = serverData.getJSONArray("objects");
        String rawUpvoteList = "";
        if (serverData.getJSONObject("meta").has("user_upvotes"))
            rawUpvoteList = serverData.getJSONObject("meta").getString("user_upvotes");
        HashMap<Integer, Report> map = new HashMap<>();
        for (int i = 0; i < reports.length(); i++) {
            Report report = new Report(reports.getJSONObject(i), -1, Utils.toStringList(rawUpvoteList), mContext);
            map.put(report.serverId, report);
        }
        return map;
    }

    private void syncFromServer(ContentProviderClient provider, SyncResult syncResult) {
        try {
            updatePlaces(provider);
            updateTags(provider);
            JSONObject reports = downloadReports("normal");
            if (reports != null)
                updateLocalData(reports, provider, syncResult);
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
        Log.v(TAG, "Network synchronization complete");
    }
}