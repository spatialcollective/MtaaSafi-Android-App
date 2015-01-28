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
import com.sc.mtaa_safi.SystemUtils.PrefUtils;
import com.sc.mtaa_safi.database.Contract;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.NoSuchAlgorithmException;

public class ReportUploader extends AsyncTask<Integer, Integer, Integer> {

    Report pendingReport;
    int screenW;
    Context mContext;
    ReportUploadingFragment mFragment;
    int canceller = -1;

    public static final int CANCEL_SESSION = 0, DELETE_BUTTON = 1, NETWORK_ERROR = 2;

    public ReportUploader(Context context, Report report, ReportUploadingFragment frag) {
        mContext = context;
        mFragment = frag;
        pendingReport = report;
        screenW = PrefUtils.getPrefs(context).getObject(PrefUtils.SCREEN_WIDTH, Integer.class);
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
                if (serverResponse != null)
                    updateDB(serverResponse);
            }
            return 1;
        } catch (IOException e) {
            e.printStackTrace();
            canceller = NETWORK_ERROR;
            cancel(true);
            return NETWORK_ERROR;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    private JSONObject writeTextToServer() throws IOException, JSONException, NoSuchAlgorithmException {
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(mContext.getString(R.string.base_write) + "/");
        httpPost.setHeader("Accept", "application/json");
        httpPost.setHeader("Content-type", "application/json");
        Log.e("Write Text:", pendingReport.getJsonStringRep());
        // send along user's upvote data, if any, with the report text
//        JSONObject withUpvoteData = VoteInterface.recordUpvoteLog(
//                mFragment.getActivity(), new JSONObject(pendingReport.getJsonStringRep())).toString();
        httpPost.setEntity(new StringEntity(pendingReport.getJsonStringRep()));
        return processResponse(httpclient.execute(httpPost));
    }

    private JSONObject writePicToServer() throws IOException, JSONException {
        String urlString = mContext.getString(R.string.base_write) + "_from_stream/" + pendingReport.serverId + "/" + screenW + "/";
        Log.e("Pic URL", urlString);
        URL url = new URL(urlString);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setInstanceFollowRedirects(false);
        urlConnection.setConnectTimeout(15000);
        urlConnection.setDoOutput(true);
        urlConnection.setChunkedStreamingMode(0);
        urlConnection.connect();
        DataOutputStream out = new DataOutputStream(urlConnection.getOutputStream());
        byte[] bytes = pendingReport.getBytesForPic(pendingReport.pendingState - 1);
        Log.e("BYTES 2 SERVER", "Bytes being sent:" + bytes.length);
        out.write(bytes);
        out.flush();
        out.close();
        InputStream is = urlConnection.getInputStream();
        String response = NetworkUtils.convertInputStreamToString(is);
        is.close();
        urlConnection.disconnect();
        return new JSONObject(response);
    }
    
    private void updateDB(JSONObject response) throws JSONException, RemoteException, OperationApplicationException {
        ContentValues updateValues = pendingReport.updateValues(response);

        if (pendingReport.pendingState == -1)
            deleteLocalPics();

        Uri reportUri = Contract.Entry.CONTENT_URI.buildUpon()
                    .appendPath(Integer.toString(pendingReport.dbId)).build();
        if (mContext.getContentResolver().query(reportUri, null, null, null, null).getCount() > 0)
            mContext.getContentResolver().update(reportUri, updateValues, null, null);
        else {
            cancel(true);
            Log.e("Cancelled!", "From UpdateDB");
        }
    }

    private void deleteLocalPics() {
        for (int picPos = 0; picPos < pendingReport.media.size(); picPos++) {
            File picFile = new File(pendingReport.media.get(picPos));
            if (picFile != null)
                picFile.delete();
        }
    }

    private JSONObject processResponse(HttpResponse response) throws JSONException, IOException {
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode >= 400){
            Log.e("Cancelled!", "From ProcessRequest");
            cancel(true);
        }
        return NetworkUtils.convertHttpResponseToJSON(response);
    }

    @Override
    protected void onPostExecute(Integer result) { mFragment.reportUploadSuccess(); }

    public void cancelSession(int reason) {
        canceller = reason;
        cancel(true);
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
        else if (result == DELETE_BUTTON)
            mFragment.beamUpFirstReport();
        else if(result == NETWORK_ERROR)
            mFragment.changeHeader("Connection Error: Retry?", R.color.DarkRed, ReportUploadingFragment.SHOW_RETRY);
        else
            mFragment.changeHeader("Error", R.color.DarkRed, ReportUploadingFragment.SHOW_RETRY);
    }
}
