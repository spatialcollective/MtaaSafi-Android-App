package com.sc.mtaasafi.android.newReport;

import android.accounts.NetworkErrorException;
import android.os.AsyncTask;
import android.util.Log;

import com.sc.mtaasafi.android.SystemUtils.LogTags;
import com.sc.mtaasafi.android.Report;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.NoSuchAlgorithmException;

public class ReportUploader extends AsyncTask<Integer, Integer, Integer> {

    ReportUploadingFragment mFragment;
    Report pendingReport;
    boolean fragmentAvailable;
    int progress;
    private static final String BASE_WRITE_URL = "http://app.spatialcollective.com/add_post",
            NEXT_REPORT_PIECE_KEY = "nextfield",
            REPORT_ID_KEY = "id",
            PIC_HASHES_KEY = "pic_hashes";

    public ReportUploader(ReportUploadingFragment fragment, Report report) {
        mFragment = fragment;
        pendingReport = report;
        fragmentAvailable = true;
    }

    @Override
    protected Integer doInBackground(Integer... i) {
        Log.e(LogTags.BACKEND_W, "ServerCommunicater.writePost");
        return writeReportToServer();
    }

    private void updateProgress(int nextPiece) {
        this.progress = nextPiece;
        Integer[] progress = new Integer[1];
        progress[0] = Integer.valueOf(nextPiece);
        publishProgress(progress);
    }

    protected void onProgressUpdate(Integer... progress) {
        if(fragmentAvailable)
            mFragment.updatePostProgress(progress[0], pendingReport);
    }

    @Override
    protected void onPostExecute(Integer result) {
        Log.e(LogTags.BACKEND_W, "onPostExecute: " + result);
        if(fragmentAvailable){
            if (result == -1)
                mFragment.uploadSuccess();
            else
                mFragment.uploadFailure("Unknown Error");
        }
    }
    // ask the server if it has this pending report
    // if it doesn't, upload it as anew report
    // if it does, upload it as an interrupted report
    private int writeReportToServer() {
        JSONArray picHashes = queryServerFor(pendingReport.id);
        if(picHashes == null)
            writeNewReport(pendingReport);
        else
            writeInterruptedReport(pendingReport, picHashes.toString());

        return -1;
    }

    // ask the server if it has a (partially) uploaded report with id reportId
    // returns null if there's no such report on server
    // returns an array of hashes of the report's pictures on the server if the server has the report
    private JSONArray queryServerFor(long reportId){
        try {
            HttpResponse response = new DefaultHttpClient()
                    .execute(new HttpGet(BASE_WRITE_URL + "/" + reportId + "/"));
            if(response.getStatusLine().getStatusCode() == 404) // report with reportId doesn't exist
                return null;
            else{ // return the array of hashed pics' encoded byte arrays
                JSONObject jsonResponse = new JSONObject(EntityUtils.toString(response.getEntity(), "UTF-8"));
                return jsonResponse.getJSONArray(PIC_HASHES_KEY);
            }
        } catch (IOException e) {
            ioException(e);
        } catch (JSONException e) {
            jsonException(e);
        }
        return null;
    }

    private void writeNewReport(Report report){
        try {
            Log.e(LogTags.BACKEND_W, "Writing a new report...");
            writeNextPieceToServer(report, 0);
        } catch (JSONException e) {
            jsonException(e);
        } catch (IOException e) {
            ioException(e);
        } catch (NetworkErrorException e){
            networkErrorException(e);
        }
    }

    // called by the reportUploadingFragment when it gets destroyed
    public void setfragmentAvailable(boolean isIt){
        fragmentAvailable = isIt;
        if(fragmentAvailable){
            Integer[] progressUpdate = new Integer[1];
            progressUpdate[0] = this.progress;
            publishProgress(progressUpdate);
        }
    }

    @Override
    public void onCancelled(){
        if(fragmentAvailable)
            mFragment.onFailure("", this);
    }

    // recursive function that sends a new report to the server one piece at a time
    private void writeNextPieceToServer(Report report, int pieceKey) throws JSONException, IOException, NetworkErrorException {
        JSONObject piece;
        pendingReport = report;
        updateProgress(pieceKey);
        Log.e("PIECE 2 SERVER", "Writing piece to server! Piece: " + pieceKey);
        if(pieceKey == -1)
            return;
        else if (pieceKey == 0){
            piece = report.getJsonForText();
            JSONObject responseJSON = sendPiece(report.id, piece.toString());
            report.id = responseJSON.getInt(REPORT_ID_KEY);
            pieceKey = responseJSON.getInt(NEXT_REPORT_PIECE_KEY);
            writeNextPieceToServer(report, pieceKey);
        }
        else{
            byte[] picBytes = report.getBytesForPic(pieceKey - 1);
            JSONObject responseJSON = sendPiece(report.id, picBytes);
            report.id = responseJSON.getInt(REPORT_ID_KEY);
            pieceKey = responseJSON.getInt(NEXT_REPORT_PIECE_KEY);
            writeNextPieceToServer(report, pieceKey);
        }
    }

    // For each picture in the interrupted report, check if it was uploaded to the server previously.
    // if not, upload it.
    private void writeInterruptedReport(Report report, String picHashes){
        Log.e(LogTags.BACKEND_W, "Interrupted report with " + picHashes + " pic hashes");
        progress = (picHashes.length() - picHashes.replace(",", "").length())+1;
        if(progress > 3){
            updateProgress(-1);
            return;
        }
        for(int i = 0; i < progress + 1; i++) // make the uploading interface reflect how many pics left to upload
            updateProgress(i);
        try {
            Log.e("INTERRUPTED", "Pic paths found: " + report.picPaths.size());
            int i = 0;
            while(progress != -1 && i < report.picPaths.size()){
                // if the server's pic hashes don't contain the SHA1 for picture i, send the picture
                Log.e("INTERRUPTED", "Enterred interrrupted loop!");
                if(!picHashes.contains(report.getSHA1forPic(i))){
                    JSONObject responseJson = sendPiece(report.id, report.getBytesForPic(progress));
                    Log.e("INTERRUPTED", "Server said: " + responseJson.getInt(NEXT_REPORT_PIECE_KEY));
                    progress = responseJson.getInt(NEXT_REPORT_PIECE_KEY);
                    updateProgress(progress);
                    if(responseJson.getInt(NEXT_REPORT_PIECE_KEY) == -1){
                        updateProgress(-1);
                        break;
                    }
                }
                i++;
            }
        } catch (JSONException e) {
            jsonException(e);
        } catch (IOException e) {
            ioException(e);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    // sends the piece to the server at the report's write url
    // upon success the server sends back as a response the report's id and the next piece it expects
    private JSONObject sendPiece(long reportId, String piece) throws IOException, JSONException {
        HttpClient httpclient = new DefaultHttpClient();
        String writeUrl = BASE_WRITE_URL;
        if (reportId != 0)
            writeUrl += "/" + reportId + "/";
        HttpPost httpPost = new HttpPost(writeUrl);
        httpPost.setHeader("Accept", "application/json");
        httpPost.setHeader("Content-type", "application/json");
        httpPost.setEntity(new StringEntity(piece));
        return processResponse(httpclient.execute(httpPost));
    }

    private JSONObject sendPiece(long reportId, byte[] pic) throws IOException, JSONException {
        String urlString = BASE_WRITE_URL + "_from_stream/" + reportId + "/";
        URL url = new URL(urlString);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try{
            urlConnection.setInstanceFollowRedirects(false);
            urlConnection.setConnectTimeout(15000);
            urlConnection.setDoOutput(true);
            urlConnection.setChunkedStreamingMode(0);
            urlConnection.connect();
            DataOutputStream out = new DataOutputStream(urlConnection.getOutputStream());
            out.write(pic);
            out.flush();
            out.close();
            int responseCode = urlConnection.getResponseCode();
        } finally {
            urlConnection.disconnect();
            HttpClient httpClient = new DefaultHttpClient();
            HttpResponse httpResponse = httpClient.execute(new HttpGet(urlString));
            return processResponse(httpResponse);
        }
    }

    // checks the response from the server for errors. If none, returns the JSON object the server sent back
    private JSONObject processResponse(HttpResponse response) throws JSONException, IOException {
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode >= 400 && statusCode < 500)
            mFragment.uploadFailure("Client error");
        else if (statusCode >= 500 && statusCode < 600)
            mFragment.uploadFailure("Server error");
        String responseString = EntityUtils.toString(response.getEntity(), "UTF-8");
        return new JSONObject(responseString);
    }
    private void jsonException(JSONException e){
        e.printStackTrace();
        Log.e(LogTags.BACKEND_W, "JSON FAILURE");
        mFragment.uploadFailure("JSON error");

    }
    private void networkErrorException(NetworkErrorException e){
        e.printStackTrace();
        Log.e(LogTags.BACKEND_W, "NETWORK FAILURE");
        mFragment.uploadFailure("Network error");

    }
    private void ioException(IOException e){
        e.printStackTrace();
        Log.e(LogTags.BACKEND_W, "IO FAILURE");
        mFragment.uploadFailure("IO");
    }
}
