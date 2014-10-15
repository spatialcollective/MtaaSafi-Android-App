package com.sc.mtaasafi.android;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpEntity;
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

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Agree on 9/4/2014.
 * ServerCommunicater receives posts written by the user from the main activity.
 * It also gives the main activity posts to put in the FeedFragment.
 * ServerCommunicater takes posts at POJOs, converts them to JSON-formatted strings and
 * then pushes them to the server via ServerRelay, which communicates directly with the server
 * at the byte level.
 */
public class ServerCommunicater {

    public interface ServerCommCallbacks{
        void onFeedUpdate(List<Report> posts);
        int getScreenWidth();
        void onUpdateFailed();
        void backupDataToFile(String dataString) throws IOException;
        String getJsonStringFromFile() throws IOException;
        void updatePendingReportProgress(int progress);
        void onReportUploadSuccess();
        void onUploadFailed(String failMessage);
    }

    public ServerCommCallbacks activity;
    private int currentField;

    private static final String BASE_WRITE_URL = "http://app.spatialcollective.com/add_post",
                                READ_URL = "http://app.spatialcollective.com/get_posts/",
                                NEXT_REPORT_PIECE_KEY = "nextfield",
                                REPORT_ID_KEY = "pid";

    ServerCommunicater(ServerCommCallbacks activity){
        this.activity = activity;
    }

    public void postNewReport(Report report) { new SendReport().execute(report); }

    private class SendReport extends AsyncTask<Report, Integer, Integer> {

        @Override
        protected Integer doInBackground(Report... report) {
            try {
                Log.e(LogTags.BACKEND_W, "ServerCommunicater.writePost");
                return writeReportToServer(report[0]);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onProgressUpdate(Integer... progress) {
            activity.updatePendingReportProgress(progress[0]);
        }

        @Override
        protected void onPostExecute(Integer result) {
            Log.e(LogTags.BACKEND_W, "onPostExecute");
            if (result == -1)
                activity.onReportUploadSuccess();
            else
                activity.onUploadFailed("Unknown Error");
        }

        private int writeReportToServer(Report report) throws JSONException, IOException {
            try {
                HttpResponse response = sendRequest(0, report.getJsonForText().toString());
                JSONObject jsonResponse = processResponse(response);
                int nextPieceKey = jsonResponse.getInt(NEXT_REPORT_PIECE_KEY);
                report.id = jsonResponse.getInt(REPORT_ID_KEY);
                while (nextPieceKey != -1) {
                    jsonResponse = writePieceToServer(report, nextPieceKey);
                    nextPieceKey = jsonResponse.getInt(NEXT_REPORT_PIECE_KEY);
                }
                updateProgress(nextPieceKey);
                return nextPieceKey;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return 0;
        }

        private void updateProgress(int nextPiece) {
            Integer[] progress = new Integer[1];
            progress[0] = Integer.valueOf(nextPiece);
            publishProgress(progress);
        }

        private JSONObject writePieceToServer(Report report, int nextPieceKey) throws JSONException, IOException, FileNotFoundException {
            HttpResponse response = sendRequest(report.id, report.getJsonForPic(nextPieceKey).toString());
            updateProgress(nextPieceKey);
            return processResponse(response);
        }

        private JSONObject processResponse(HttpResponse response) throws JSONException, IOException {
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode >= 400 && statusCode < 500)
                activity.onUploadFailed("Client error");
            else if (statusCode >= 500 && statusCode < 600)
                activity.onUploadFailed("Server error");
            String responseString = EntityUtils.toString(response.getEntity(), "UTF-8");
            return new JSONObject(responseString);
        }

        private HttpResponse sendRequest(int reportId, String contents) throws IOException {
            HttpClient httpclient = new DefaultHttpClient();
            String writeUrl = BASE_WRITE_URL;
            if (reportId != 0)
                writeUrl += "/" + reportId + "/";
            HttpPost httpPost = new HttpPost(writeUrl);
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");
            httpPost.setEntity(new StringEntity(contents));
            return httpclient.execute(httpPost);
        }
    }

    public void getPosts(){
        new FetchPosts().execute(READ_URL + activity.getScreenWidth());
    }

    private List<Report> GET(String url) {
        String resultString;
        JSONArray resultJson;

        try {
            resultString = getDataFromServer(url);
            resultJson = convertStringToJson(resultString);
            activity.backupDataToFile(resultString);
            return createReportsFromJson(resultJson);
        } catch (Exception ex) {
            ex.printStackTrace();
            try {
                resultString = activity.getJsonStringFromFile();
                resultJson = convertStringToJson(resultString);
                return createReportsFromJson(resultJson);
            } catch (Exception e) {
                activity.onUpdateFailed();
            }
        }
        return new ArrayList<Report>();
    }

    private String getDataFromServer(String url) throws IOException {
        HttpClient httpClient = new DefaultHttpClient();
        HttpResponse httpResponse = httpClient.execute(new HttpGet(url));
        InputStream inputStream = httpResponse.getEntity().getContent();
        if (inputStream != null)
            return convertInputStreamToString(inputStream);
        return "error";
    }

    private List<Report> createReportsFromJson(JSONArray jsonData) throws JSONException {
        int len = jsonData.length();
        List<Report> listContent = new ArrayList<Report>(len);
        for (int i = 0; i < len; i++)
            listContent.add(new Report(jsonData.getJSONObject(i)));
        return listContent;
    }

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

    private class FetchPosts extends AsyncTask<String, Void, List<Report>> {

        @Override
        protected List<Report> doInBackground(String... urls) {
            return GET(urls[0]);
        }

        @Override
        protected void onPostExecute(List<Report> result) {
            activity.onFeedUpdate(result);
        }
    }
}
