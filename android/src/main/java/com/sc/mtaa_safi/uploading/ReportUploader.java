package com.sc.mtaa_safi.uploading;

import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.RemoteException;
import android.util.Log;

import com.sc.mtaa_safi.R;
import com.sc.mtaa_safi.Report;
import com.sc.mtaa_safi.SystemUtils.NetworkUtils;
import com.sc.mtaa_safi.SystemUtils.Utils;
import com.sc.mtaa_safi.database.Contract;
import com.sc.mtaa_safi.feed.tags.ReportTagJunction;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

public class ReportUploader extends AsyncTask<Integer, Integer, Integer> {

    Context mContext;
    ReportUploadingFragment mFragment;
    Report pendingReport;
    int screenW;

    public ArrayList<String> localMedia = new ArrayList<String>();
    int canceller = -1;

    public static final int CANCEL_SESSION = 0, DELETE_BUTTON = 1, NETWORK_ERROR = 2;

    public ReportUploader(Context context, Report report, ReportUploadingFragment frag) {
        mContext = context;
        mFragment = frag;
        pendingReport = report;
        localMedia = pendingReport.media;
        screenW = Utils.getScreenWidth(context);
    }

    @Override
    protected Integer doInBackground(Integer... p) {
        JSONObject serverResponse = null;
        try {
            for (int i = 0; i < pendingReport.media.size() + 1; i++) {
                if (isCancelled()) {
                    updateProgressStopped();
                    return canceller;
                }
                if (pendingReport.pendingState == 0)
                    serverResponse = writeTextToServer();
                else if (pendingReport.pendingState > 0)
                    serverResponse = writePicToServer();
                if (serverResponse != null && !serverResponse.has("error"))
                    updateDB(serverResponse);
            }
            return 1;
        } catch (IOException e) {
            e.printStackTrace();
            return cancelSession(NETWORK_ERROR);
        } catch (Exception e) { e.printStackTrace(); }
        return -1;
    }

    private JSONObject writeTextToServer() throws IOException, JSONException, NoSuchAlgorithmException {
        JSONObject report = pendingReport.getJsonRep();
        report.put("userId", Utils.getUserId(mContext));

        JSONArray tags = ReportTagJunction.getReportTags(mContext, pendingReport.dbId);
        if (tags.length() != 0)
            report.put("tags", ReportTagJunction.getReportTags(mContext, pendingReport.dbId));

        JSONObject response = NetworkUtils.makeRequest(mContext.getString(R.string.base_write) + "/", "post", report);
        if (response.has("error"))
            cancelSession(NETWORK_ERROR);
        return response;
    }

    private JSONObject writePicToServer() throws IOException, JSONException {
        String urlString = mContext.getString(R.string.base_write) + "_from_stream/" + pendingReport.serverId + "/" + screenW + "/";
        byte[] data = pendingReport.getBytesForPic(pendingReport.pendingState - 1);
        return NetworkUtils.streamRequest(urlString, data);
    }
    
    private void updateDB(JSONObject response) throws JSONException, RemoteException, OperationApplicationException {
        ContentValues updateValues = pendingReport.updateValues(response);

        if (pendingReport.pendingState == -1) {
            deleteLocalPics();
            Utils.saveSavedReportCount(mContext,Utils.getSavedReportCount(mContext) - 1);
        }

        Uri reportUri = Contract.Entry.CONTENT_URI.buildUpon().appendPath(Integer.toString(pendingReport.dbId)).build();
        if (mContext.getContentResolver().query(reportUri, null, null, null, null).getCount() > 0)
            mContext.getContentResolver().update(reportUri, updateValues, null, null);
        else {
            cancel(true);
            Log.e("Cancelled!", "From UpdateDB");
        }
    }

    private void deleteLocalPics() {
        for (int picPos = 0; picPos < localMedia.size(); picPos++) {
            File picFile = new File(localMedia.get(picPos));
            if (picFile != null)
                picFile.delete();
        }
    }

    @Override
    protected void onPostExecute(Integer result) { mFragment.reportUploadSuccess(); }

    public Integer cancelSession(int reason) {
        canceller = reason;
        cancel(true);
        return reason;
    }

    private void updateProgressStopped() {
        ContentValues updateValues = new ContentValues();
        updateValues.put(Contract.Entry.COLUMN_UPLOAD_IN_PROGRESS, 0);
        Uri reportUri = Contract.Entry.CONTENT_URI.buildUpon()
                .appendPath(Integer.toString(pendingReport.dbId)).build();
        if (mContext.getContentResolver().query(reportUri, null, null, null, null).getCount() > 0)
            mContext.getContentResolver().update(reportUri, updateValues, null, null);
    }

    @Override
    protected void onCancelled(Integer result) {
        if (result == CANCEL_SESSION)
            mFragment.changeHeader("Upload Cancelled", R.color.Crimson, ReportUploadingFragment.SHOW_RETRY);
        else if(result == NETWORK_ERROR)
            mFragment.changeHeader("Connection Error: Retry?", R.color.DarkRed, ReportUploadingFragment.SHOW_RETRY);
        else if (result != DELETE_BUTTON)
            mFragment.changeHeader("Error", R.color.DarkRed, ReportUploadingFragment.SHOW_RETRY);
    }
}
