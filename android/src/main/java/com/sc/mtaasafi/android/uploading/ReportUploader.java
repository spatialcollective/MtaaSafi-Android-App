package com.sc.mtaasafi.android.uploading;

import android.accounts.NetworkErrorException;
import android.os.AsyncTask;
import android.util.Log;

import com.sc.mtaasafi.android.SystemUtils.LogTags;
import com.sc.mtaasafi.android.Report;
import com.sc.mtaasafi.android.newReport.NewReportActivity;
import com.sc.mtaasafi.android.uploading.ReportUploadingFragment;

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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ReportUploader extends AsyncTask<Integer, Integer, Integer> {

    ReportUploadingFragment mFragment;
    Report pendingReport;
    boolean fragmentAvailable;
    int progress, screenW;
    private static final String BASE_WRITE_URL = "http://app.spatialcollective.com/add_post",
            NEXT_REPORT_PIECE_KEY = "nextfield",
            REPORT_ID_KEY = "id",
            PIC_HASHES_KEY = "pic_hashes",
            OUTPUT_KEY = "output";

    public ReportUploader(ReportUploadingFragment fragment, Report report) {
        mFragment = fragment;
        pendingReport = report;
        NewReportActivity nra = (NewReportActivity) mFragment.getActivity();
        screenW = nra.getScreenWidth();
        fragmentAvailable = true;
        Log.e("FRAG AVAIL", "Fragment is available: " + fragmentAvailable);
    }

    @Override
    protected Integer doInBackground(Integer... p) {
        for(int i = 0; i < 4; i++){
            if(isCancelled())
                break;
            else if(pendingReport.mediaPaths.get(2).contains("http:")){
                progress = -1;
                updateProgress();
                break;
            }
            else if(pendingReport.mediaPaths.get(1).contains("http:"))
                i = progress = 3;
            else if(pendingReport.mediaPaths.get(0).contains("http:"))
                i = progress = 2;
            else if(pendingReport.serverId != 0 && queryServerFor(pendingReport) !=404)
                i = progress = 1;
            else
                i = progress = 0;
            updateProgress();
            Log.e("New loop", "Progress is: " + progress);
            if(progress==0)
                writeTextToServer();
            else if(progress>0)
                writePicToServer();
        }
        return -1;
    }
    private void writePicToServer(){
        try {
            Log.e("New loop", "Writing pic, progress: "  + progress);
            JSONObject response =
                    sendPiece(pendingReport.serverId, pendingReport.getBytesForPic(progress-1));
            updateReportFields(response);
        } catch (IOException e) {
            ioException(e);
        } catch (JSONException e) {
            jsonException(e);
        }
    }
    private void writeTextToServer(){
        try {
            Log.e("New loop", "Writing text, progress: "  + progress);
            JSONObject response = sendPiece(pendingReport.getJsonForText().toString());
            updateReportFields(response);
        } catch (IOException e) {
            ioException(e);
        } catch (JSONException e) {
            jsonException(e);
        }
    }
    // ==== Communicating with the Uploading Fragment ====
    private void updateProgress() {
        Integer[] progressUpdate = new Integer[1];
        progressUpdate[0] = Integer.valueOf(progress);
        publishProgress(progress);
        // special case, because onProgressUpdate isn't called if thread is cancelled
//        if(fragmentAvailable && progress == -1 && isCancelled())
//            mFragment.stillUploaded();
    }
    protected void onProgressUpdate(Integer... progress) {
        if(fragmentAvailable){
//            mFragment.updatePostProgress(progress[0], pendingReport);
            Log.e("Update Progress", "fragment was called. Progress: " + progress[0]);
        }
        else
            Log.e("Update Progress", "fragment was NOT//NOT called. Progress: " + progress );
    }
    // called by the reportUploadingFragment when it gets destroyed
    public void setfragmentAvailable(boolean isIt){
        fragmentAvailable = isIt;
        Log.e("FRAG AVAIL", "FRAG IS AVAILABLE CALLED!!!!!!!!!!!" + fragmentAvailable);
        if(fragmentAvailable){
            Integer[] progressUpdate = new Integer[1];
            progressUpdate[0] = this.progress;
            publishProgress(progressUpdate);
        }
    }
    @Override
    protected void onPostExecute(Integer result) {
        Log.e(LogTags.BACKEND_W, "onPostExecute: " + result);
        if(fragmentAvailable){
//            if (result == -1)
//                mFragment.uploadSuccess();
//            else
//                mFragment.uploadFailure("Unknown Error");
        }
    }
    @Override
    protected void onCancelled(){
//        if(fragmentAvailable)
//            mFragment.cancelConfirmed();
    }
//    // if the report has a server id that the server recognizes, it's an interrupted report
//    // upload the first pic in the array that isn't a web URL
//    // if the server throws you a 404--it's a new report, upload the report text then upload the pics
//    private int writeReportToServer() {
//        if(queryServerFor(pendingReport) == 404)
//            writeNewReport(pendingReport);
//        else
//            writeInterruptedReport(pendingReport);
//        return -1;
//    }
//
    // returns the status code from the server for a given report's server id
    // 404 means it's a report not on the server (new)
    private int queryServerFor(Report report){
        try{
            HttpResponse response = new DefaultHttpClient()
                    .execute(new HttpGet(BASE_WRITE_URL + "/" + report.serverId + "/"));
            return response.getStatusLine().getStatusCode();
        } catch (ClientProtocolException e) {
//            if(fragmentAvailable)
//                mFragment.uploadFailure("Connection error");
        } catch (IOException e) {
            e.printStackTrace();
//            if(fragmentAvailable)
//                mFragment.uploadFailure("Connection error");
        }
        return 0;
    }
//
//    private void writeNewReport(Report report){
//        try {
//            Log.e(LogTags.BACKEND_W, "Writing a new report...");
//            writeNextPieceToServer(report, null);
//        } catch (JSONException e) {
//            jsonException(e);
//        } catch (IOException e) {
//            ioException(e);
//        } catch (NetworkErrorException e){
//            networkErrorException(e);
//        }
//    }
//
//
//    // recursive function that sends a new report to the server one piece at a time
//    private void writeNextPieceToServer(Report report, JSONObject response) throws JSONException, IOException, NetworkErrorException {
//        JSONObject piece;
//        pendingReport = report;
//        if(response != null)
//            progress = response.getInt(NEXT_REPORT_PIECE_KEY);
//        else
//            progress = 0;
//        Log.e("PIECE 2 SERVER", "Writing piece to server! Piece: " + progress + " is fragment available: " + fragmentAvailable);
//        if(progress == -1){
//            return;
//        } else if (progress == 0){
//            updateProgress();
//            updateReportFields(response);
//            piece = report.getJsonForText();
//            JSONObject responseJSON = sendPiece(piece.toString());
//            report.serverId = responseJSON.getInt(REPORT_ID_KEY);
//            writeNextPieceToServer(report, responseJSON);
//        } else{
//            writeNextPic(report, response);
//        }
//    }
//    // TODO: Fix when the SHA1 hashes are ready
//    // For each picture in the interrupted report, check if it was uploaded to the server previously.
//    // if not, upload it.
//    private void writeInterruptedReport(Report report){
//        // go through the report's media paths. The first path that isn't a web URL is the next pic to upload
//        Log.e("Interrupted report", "Report was interrupted, resending");
//        for(int i =0; i < report.mediaPaths.size(); i++){
//            Log.e("Interrupted report", "Media path: " + i + ". " + report.mediaPaths.get(i));
//            if(!report.mediaPaths.get(i).contains("http:")){
//                progress = i+1;
//                break;
//            }
//        }
//        if(progress > 0) try{
//                JSONObject json = new JSONObject().put(NEXT_REPORT_PIECE_KEY, progress);
//                writeNextPic(report, json);
//            } catch (JSONException e) {
//                jsonException(e);
//            } catch (IOException e) {
//                ioException(e);
//            }
//        else
//            progress = -1;
//        // go into the loop to finish this
//    }
//
//    private void writeNextPic(Report report, JSONObject response) throws IOException, JSONException {
//        progress = response.getInt(NEXT_REPORT_PIECE_KEY);
//        updateProgress();
//        updateReportFields(response);
//        if(progress != -1){
//            JSONObject responseJSON = sendPiece(report.serverId, report.getBytesForPic(progress - 1));
//            writeNextPic(report, responseJSON);
//        }
//    }

    // =============================== Low-Level Functions=================================
    // (They who do the heavy lifting)
    private void updateReportFields(JSONObject response){
        try{
            if(response != null){
                Log.e("SERVER OUTPUT JSON : ", response.toString());
                String serverOutput = response.getString(OUTPUT_KEY);
                Log.e("SERVER OUTPUT: ", serverOutput);
                int nextField = response.getInt(NEXT_REPORT_PIECE_KEY);
                if(nextField > 1)
                    pendingReport.mediaPaths.set(nextField - 2,serverOutput);
                else if(nextField == -1){
                    pendingReport.mediaPaths.set(2, serverOutput);
                    progress = -1;
                    Log.e("nextField ==-1: ", "Next field is negative one");
                    updateProgress();
                }
                else if(nextField == 1){
                    pendingReport.title = serverOutput;
                    pendingReport.serverId = response.getInt(REPORT_ID_KEY);
                }
            }
        } catch (JSONException e) {
            jsonException(e);
        }
    }

    // sends the piece to the server at the report's write url
    // upon success the server sends back as a response the report's id and the next piece it expects
    private JSONObject sendPiece(String piece) throws IOException, JSONException {
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(BASE_WRITE_URL);
        httpPost.setHeader("Accept", "application/json");
        httpPost.setHeader("Content-type", "application/json");
        httpPost.setEntity(new StringEntity(piece));
        return processResponse(httpclient.execute(httpPost));
    }

    private JSONObject sendPiece(long reportId, byte[] pic) throws IOException, JSONException {
        String urlString = BASE_WRITE_URL + "_from_stream/" + reportId + "/" + screenW + "/";
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
            InputStream is = urlConnection.getInputStream();
            String response = convertInputStreamToString(is);
            is.close();
            return new JSONObject(response);
        } finally {
//            urlConnection.disconnect();
//            HttpClient httpClient = new DefaultHttpClient();
//            HttpResponse httpResponse = httpClient.execute(new HttpGet(urlString));
//            return processResponse(httpResponse);
        }
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

    // checks the response from the server for errors. If none, returns the JSON object the server sent back
    private JSONObject processResponse(HttpResponse response) throws JSONException, IOException {
        int statusCode = response.getStatusLine().getStatusCode();
//        if (statusCode >= 400 && statusCode < 500)
//            if(fragmentAvailable)
//                mFragment.uploadFailure("Client error");
//        else if (statusCode >= 500 && statusCode < 600)
//            if(fragmentAvailable)
//                mFragment.uploadFailure("Server error");
        String responseString = EntityUtils.toString(response.getEntity(), "UTF-8");
        return new JSONObject(responseString);
    }

    private void jsonException(JSONException e){
        e.printStackTrace();
        Log.e(LogTags.BACKEND_W, "JSON FAILURE");
//        if(fragmentAvailable)
//            mFragment.uploadFailure("JSON error");

    }
    private void networkErrorException(NetworkErrorException e){
        e.printStackTrace();
        Log.e(LogTags.BACKEND_W, "NETWORK FAILURE");
//        if(fragmentAvailable)
//            mFragment.uploadFailure("Network error");

    }
    private void ioException(IOException e){
        e.printStackTrace();
        Log.e(LogTags.BACKEND_W, "IO FAILURE");
//        if(fragmentAvailable)
//            mFragment.uploadFailure("IO");
    }
}
