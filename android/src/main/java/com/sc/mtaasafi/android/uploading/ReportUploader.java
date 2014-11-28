package com.sc.mtaasafi.android.uploading;

import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.RemoteException;
import android.util.Log;

import com.sc.mtaasafi.android.R;
import com.sc.mtaasafi.android.Report;
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
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.TimeoutException;

public class ReportUploader extends AsyncTask<Integer, Integer, Integer> {

    Report pendingReport;
    int screenW;
    Context mContext;
    ReportUploadingFragment mFragment;
    int canceller = -1;

    public static final int CANCEL_SESSION = 0, DELETE_BUTTON = 1, NETWORK_ERROR = 2;
    private static final String BASE_WRITE_URL = "http://app.spatialcollective.com/add_post",
            NEXT_REPORT_PIECE_KEY = "nextfield",
            REPORT_ID_KEY = "id",
            OUTPUT_KEY = "output";

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
            for (int i = 0; i < 4; i++) {
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
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (OperationApplicationException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
            canceller = NETWORK_ERROR;
            cancel(true);
            return NETWORK_ERROR;
        }
        return -1;
    }

    private JSONObject writeTextToServer() throws IOException, JSONException {
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(URLs.BASE_WRITE);
        httpPost.setHeader("Accept", "application/json");
        httpPost.setHeader("Content-type", "application/json");
        // send along user's upvote data, if any, with the report text
        JSONObject withUpvoteData = VoteInterface.recordUpvoteLog(
                mFragment.getActivity(), new JSONObject(pendingReport.getJsonStringRep()));
        httpPost.setEntity(new StringEntity(withUpvoteData.toString()));
        return processResponse(httpclient.execute(httpPost));
    }

    private JSONObject writePicToServer() throws IOException, JSONException {
        String urlString = URLs.BASE_WRITE + "_from_stream/" + pendingReport.serverId + "/" + screenW + "/";
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
        VoteInterface.onUpvotesRecorded(mFragment.getActivity(), response);
        pendingReport.pendingState = response.getInt(NEXT_REPORT_PIECE_KEY);
        ContentValues updateValues = new ContentValues();
        if (pendingReport.pendingState == 1) {
            updateValues.put(Contract.Entry.COLUMN_LOCATION, response.getString(OUTPUT_KEY));
            updateValues.put(Contract.Entry.COLUMN_SERVER_ID, response.getInt(REPORT_ID_KEY));
            pendingReport.serverId = response.getInt(REPORT_ID_KEY);
        } else if (pendingReport.pendingState == 2) {
            deleteLocalPic(0);
            updateValues.put(Contract.Entry.COLUMN_MEDIAURL1, response.getString(OUTPUT_KEY));
        } else if (pendingReport.pendingState == 3) {
            deleteLocalPic(1);
            updateValues.put(Contract.Entry.COLUMN_MEDIAURL2, response.getString(OUTPUT_KEY));
        } else if (pendingReport.pendingState == 4) {
            deleteLocalPic(2);
            updateValues.put(Contract.Entry.COLUMN_MEDIAURL3, response.getString(OUTPUT_KEY));
        }
        if(pendingReport.pendingState > pendingReport.mediaPaths.size())
            pendingReport.pendingState = -1;
        if (pendingReport.pendingState > 0)
            updateValues.put(Contract.Entry.COLUMN_UPLOAD_IN_PROGRESS, 1);
        else
            updateValues.put(Contract.Entry.COLUMN_UPLOAD_IN_PROGRESS, 0);

        updateValues.put(Contract.Entry.COLUMN_PENDINGFLAG, pendingReport.pendingState);
        Uri reportUri = Contract.Entry.CONTENT_URI.buildUpon()
                    .appendPath(Integer.toString(pendingReport.dbId)).build();
        if (mContext.getContentResolver().query(reportUri, null, null, null, null).getCount() > 0)
            mContext.getContentResolver().update(reportUri, updateValues, null, null);
        else{
            cancel(true);
            Log.e("Cancelled!", "From UpdateDB");
        }
    }

    private void deleteLocalPic(int picPos) {
        File picFile = new File(pendingReport.mediaPaths.get(picPos));
        if (picFile != null)
            picFile.delete();
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
    }
}
