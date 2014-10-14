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
        void onServerResponse(int reportId, int nextField);
    }

    public ServerCommCallbacks activity;
    private int currentField;
    private static final int    NEW_REPORT = 0,
                                POST_SUCCESS = -1;

    private static final String BASE_WRITE_URL = "http://app.spatialcollective.com/add_post",
                                READ_URL = "http://app.spatialcollective.com/get_posts/",
                                NEXT_FIELD_KEY = "nextfield",
                                REPORT_ID_KEY = "pid";

    ServerCommunicater(ServerCommCallbacks activity){
        this.activity = activity;

    }

    // Asynchronously push the post to the server
    public void post(int reportId, int fieldToSend, Report report){
        Log.e(LogTags.BACKEND_W, "ServerCommunicater.post");
        WritePostText writePost = new WritePostText(report, reportId);
        writePost.execute(fieldToSend);
    }

    private class WritePostText extends AsyncTask<Integer, Void, String>{
        Report report;
        int reportId;
        WritePostText(Report report, int reportId) {
            this.report = report;
            this.reportId = reportId;
        }
        @Override
        protected String doInBackground(Integer... fieldsToSend) {
            try {
                Log.e(LogTags.BACKEND_W, "ServerCommunicater.writePost");
                writeToServer(reportId, fieldsToSend[0], report);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    // takes postId and the field # to be sent to the server and and a JSON of the contents to be sent for that field
    // postId: 0 ==> new post write URL has no postID attached, postId > 0 ==> writeURL += postId
    // CurrentField: 0 ==> reportText, currentField > 0 ==> picture(currentField - 1);
    // recursively goes through the contents of the report, sending one field at a time and updating
    // mainActivity about its progress
    private void writeToServer(int reportId, int fieldToSend, Report report) throws JSONException, IOException {
        Log.e(LogTags.BACKEND_W, "field to send: " + fieldToSend);
        if(fieldToSend != POST_SUCCESS){
            HttpClient httpclient = new DefaultHttpClient();
            String writeUrl = BASE_WRITE_URL;
            JSONObject contents;
            if(reportId != NEW_REPORT){
                writeUrl += "/" + reportId + "/";
            }
            contents = report.getJsonForField(fieldToSend);
            HttpPost httpPost = new HttpPost(writeUrl);
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");
            httpPost.setEntity(new StringEntity(contents.toString()));
            Log.e(LogTags.BACKEND_W, "httpclient starting for field: " + fieldToSend + " write url: " + writeUrl);
            HttpResponse response = httpclient.execute(httpPost);
            String responseString = EntityUtils.toString(response.getEntity(), "UTF-8");
            JSONObject responseJSON = new JSONObject(responseString);
            int nextField = responseJSON.getInt(NEXT_FIELD_KEY);
            activity.onServerResponse(reportId, nextField);
            if(nextField != POST_SUCCESS){
                reportId = responseJSON.getInt(REPORT_ID_KEY);
                writeToServer(reportId, nextField, report);
            }
        }
        else{
            Log.e(LogTags.BACKEND_W, "ServerCommunicater.writeToServer: httpclient finished, in second else");
            activity.onServerResponse(reportId, fieldToSend);
        }
        Log.e(LogTags.BACKEND_W, "ServerCommunicater.writeToServer: httpclient finished");
    }

    public void getPosts(){
        FetchPosts fp = new FetchPosts();
        fp.execute(READ_URL + activity.getScreenWidth());
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
