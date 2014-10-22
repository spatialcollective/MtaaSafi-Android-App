package com.sc.mtaasafi.android;

import android.content.Entity;
import android.os.AsyncTask;
import android.util.Log;

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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

public class ReportUploader extends AsyncTask<Integer, Integer, Integer> {

    ReportUploadingFragment mFragment;
    Report pendingReport;
    private static final String BASE_WRITE_URL = "http://app.spatialcollective.com/add_post",
            NEXT_REPORT_PIECE_KEY = "nextfield",
            REPORT_ID_KEY = "pid",
            PIC_HASHES_KEY = "pic_hashes";

    public ReportUploader(ReportUploadingFragment fragment, Report report) {
        mFragment = fragment;
        pendingReport = report;
    }

    @Override
    protected Integer doInBackground(Integer... i) {
        Log.e(LogTags.BACKEND_W, "ServerCommunicater.writePost");
        return writeReportToServer();
    }

    private void updateProgress(int nextPiece) {
        Integer[] progress = new Integer[1];
        progress[0] = Integer.valueOf(nextPiece);
        publishProgress(progress);
    }

    protected void onProgressUpdate(Integer... progress) {
        mFragment.updatePostProgress(progress[0], pendingReport);
    }

    @Override
    protected void onPostExecute(Integer result) {
        Log.e(LogTags.BACKEND_W, "onPostExecute: " + result);
        if (result == -1)
            mFragment.uploadSuccess();
        else
            mFragment.uploadFailure("Unknown Error");
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
    private JSONArray queryServerFor(int reportId){
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
            e.printStackTrace();
            Log.e(LogTags.BACKEND_W, "IO Exception!!!!");
            // tell the user there was a IOException
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(LogTags.JSON, "JSON Exception!!!!");
            // tell the user there was a JSON error
        }
        return null;
    }

    private void writeNewReport(Report report){
        try {
            writeNextPieceToServer(report, 0);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(LogTags.BACKEND_W, "JSON FAILURE");
            mFragment.uploadFailure("JSON");
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(LogTags.BACKEND_W, "IO FAILURE");
            mFragment.uploadFailure("IO");
        }
    }

    @Override
    public void onCancelled(){
        mFragment.onFailure("");
    }

    // recursive function that sends a new report to the server one piece at a time
    private void writeNextPieceToServer(Report report, int pieceKey) throws JSONException, IOException, FileNotFoundException {
        JSONObject piece;
        pendingReport = report;
        updateProgress(pieceKey);
        if(pieceKey == -1)
            return;
        else if (pieceKey == 0)
            piece = report.getJsonForText();
        else
            piece = report.getJsonForPic(pieceKey-1);
        HttpResponse response = sendPiece(report.id, piece.toString());
        JSONObject responseJSON = processResponse(response);
        report.id = responseJSON.getInt(REPORT_ID_KEY);
        pieceKey = responseJSON.getInt(NEXT_REPORT_PIECE_KEY);
        writeNextPieceToServer(report, pieceKey);
    }

    // For each picture in the interrupted report, check if it was uploaded to the server previously.
    // if not, upload it.
    private void writeInterruptedReport(Report report, String picHashes){
        int progress = picHashes.length() + 1;
        for(int i = 0; i < progress + 1; i++) // make the uploading interface reflect how many pics left to upload
            updateProgress(i);
        try {
            for(int i = 0; i < report.picPaths.size(); i++){
                // if the server's pic hashes don't contain the SHA1 for picture i, send the picture
                if(!picHashes.contains(report.getSHA1forPic(i))){
                    sendPiece(report.id, report.getJsonForPic(i).toString());
                    progress++;
                    updateProgress(progress);
                }
            }
            updateProgress(-1);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

    }

    // sends the piece to the server at the report's write url
    // upon success the server sends back as a response the report's id and the next piece it expects
    private HttpResponse sendPiece(int reportId, String piece) throws IOException {
        HttpClient httpclient = new DefaultHttpClient();
        String writeUrl = BASE_WRITE_URL;
        if (reportId != 0)
            writeUrl += "/" + reportId + "/";
        HttpPost httpPost = new HttpPost(writeUrl);
        httpPost.setHeader("Accept", "application/json");
        httpPost.setHeader("Content-type", "application/json");
        httpPost.setEntity(new StringEntity(piece));
        return httpclient.execute(httpPost);
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

}