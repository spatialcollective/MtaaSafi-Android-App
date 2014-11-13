package com.sc.mtaasafi.android.uploading;

import android.accounts.NetworkErrorException;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.sc.mtaasafi.android.R;
import com.sc.mtaasafi.android.SystemUtils.LogTags;
import com.sc.mtaasafi.android.Report;
import com.sc.mtaasafi.android.SystemUtils.PrefUtils;
import com.sc.mtaasafi.android.database.Contract;
import com.sc.mtaasafi.android.newReport.NewReportActivity;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
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

public class ReportUploader extends AsyncTask<Integer, Integer, Integer> {

    Report pendingReport;
    int screenW;
    Context mContext;
    ReportUploadingFragment mFragment;
    String canceller;

    private static final String CANCEL_SESSION = "user", DELETE_BUTTON = "delete";

    private static final String BASE_WRITE_URL = "http://app.spatialcollective.com/add_post",
            NEXT_REPORT_PIECE_KEY = "nextfield",
            REPORT_ID_KEY = "id",
            OUTPUT_KEY = "output";

    public ReportUploader(Context context, Report report, ReportUploadingFragment frag) {
        mContext = context;
        mFragment = frag;
        pendingReport = report;
        screenW = PrefUtils.getPrefs(context).getObject(PrefUtils.SCREEN_WIDTH, Integer.class);
        canceller = "";
    }

    @Override
    protected Integer doInBackground(Integer... p) {
        JSONObject serverResponse = null;
        try {
            for (int i = 0; i < 4; i++) {
                if (isCancelled())
                    return 0;
                // verifyUploadProgress();
                if (pendingReport.pendingState == 0)
                    serverResponse = writeTextToServer();
                else if (pendingReport.pendingState > 0)
                    serverResponse = writePicToServer();
                if (serverResponse != null)
                    updateDB(serverResponse);
            }
            return 1;
        } catch (Exception e) {
            cancel(true);
            e.printStackTrace();
        }
        return -1;
    }

    private JSONObject writeTextToServer() throws IOException, JSONException {
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(BASE_WRITE_URL);
        httpPost.setHeader("Accept", "application/json");
        httpPost.setHeader("Content-type", "application/json");
        httpPost.setEntity(new StringEntity(pendingReport.getJsonStringRep()));
        return processResponse(httpclient.execute(httpPost));
    }

    private JSONObject writePicToServer() throws IOException, JSONException {
        String urlString = BASE_WRITE_URL + "_from_stream/" + pendingReport.serverId + "/" + screenW + "/";
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
        String response = convertInputStreamToString(is);
        is.close();
        urlConnection.disconnect();
        return new JSONObject(response);
    }
    
    private void updateDB(JSONObject response) throws JSONException {
        pendingReport.pendingState = response.getInt(NEXT_REPORT_PIECE_KEY);
        publishProgress(pendingReport.pendingState);
        ContentValues updateValues = new ContentValues();
        File localPicToDelete = null;
        if (pendingReport.pendingState == 1) {
            updateValues.put(Contract.Entry.COLUMN_LOCATION, response.getString(OUTPUT_KEY));
            updateValues.put(Contract.Entry.COLUMN_SERVER_ID, response.getInt(REPORT_ID_KEY));
            pendingReport.serverId = response.getInt(REPORT_ID_KEY);
        } else if (pendingReport.pendingState == 2){
            localPicToDelete = new File(pendingReport.mediaPaths.get(0));
            updateValues.put(Contract.Entry.COLUMN_MEDIAURL1, response.getString(OUTPUT_KEY));
        } else if (pendingReport.pendingState == 3){
            localPicToDelete = new File(pendingReport.mediaPaths.get(1));
            updateValues.put(Contract.Entry.COLUMN_MEDIAURL2, response.getString(OUTPUT_KEY));
        } else if (pendingReport.pendingState == -1){
            localPicToDelete = new File(pendingReport.mediaPaths.get(2));
            updateValues.put(Contract.Entry.COLUMN_MEDIAURL3, response.getString(OUTPUT_KEY));
        }
        if(localPicToDelete != null)
            localPicToDelete.delete();
        updateValues.put(Contract.Entry.COLUMN_PENDINGFLAG, pendingReport.pendingState);
        if (pendingReport.pendingState > 0)
            updateValues.put(Contract.Entry.COLUMN_UPLOAD_IN_PROGRESS, 1);
        else
            updateValues.put(Contract.Entry.COLUMN_UPLOAD_IN_PROGRESS, 0);

        Uri reportUri = Contract.Entry.CONTENT_URI.buildUpon()
                    .appendPath(Integer.toString(pendingReport.dbId)).build();
        mContext.getContentResolver().update(reportUri, updateValues, null, null);
    }

    private String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        StringBuilder result = new StringBuilder(inputStream.available());
        String line;
        while((line = bufferedReader.readLine()) != null)
            result.append(line);
        inputStream.close();
        return result.toString();
    }

    private JSONObject processResponse(HttpResponse response) throws JSONException, IOException {
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode >= 400)
            cancel(true);
        String responseString = EntityUtils.toString(response.getEntity(), "UTF-8");
        return new JSONObject(responseString);
    }

    protected void onProgressUpdate(Integer... progress) { }//mFragment.reportUploadProgress(progress[0]); }
    @Override
    protected void onPostExecute(Integer result) {
        if(!isCancelled())
            mFragment.reportUploadSuccess();
    }

    public void cancelSession(){
        cancel(true);
        canceller = CANCEL_SESSION;
    }
    public void deleteReport(){
        cancel(true);
        canceller = DELETE_BUTTON;
    }
    @Override
    protected void onCancelled() {
        ContentValues updateValues = new ContentValues();
        updateValues.put(Contract.Entry.COLUMN_UPLOAD_IN_PROGRESS, 0);
        Uri reportUri = Contract.Entry.CONTENT_URI.buildUpon()
                    .appendPath(Integer.toString(pendingReport.dbId)).build();
        mContext.getContentResolver().update(reportUri, updateValues, null, null);
        if (canceller.equals(CANCEL_SESSION))
            mFragment.onSessionCancelled();
        else if(canceller.equals(DELETE_BUTTON))
            mFragment.onPendingReportDeleted();
        else
            mFragment.changeHeader("Error", R.color.DarkRed, ReportUploadingFragment.HIDE_CANCEL);
    }
}
