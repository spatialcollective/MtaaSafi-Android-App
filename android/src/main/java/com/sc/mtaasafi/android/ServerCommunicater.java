package com.sc.mtaasafi.android;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
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
    }

    public ServerCommCallbacks activity;
    public final static String writeURL = "http://app.spatialcollective.com/add_post",
                            readURL = "http://app.spatialcollective.com/get_posts/";

    ServerCommunicater(ServerCommCallbacks activity){
        this.activity = activity;
    }

    // Asynchronously push the post to the server
    public void post(Report report){
        WritePost writePost = new WritePost(report);
        writePost.execute(writeURL);
    }

    private class WritePost extends AsyncTask<String, Void, String>{
        Report report;
        WritePost(Report post) {
            report = post;
        }

        @Override
        protected String doInBackground(String... urls) {
            try {
                writeToServer(urls[0], report);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private static void writeToServer(String wURL, Report report) throws JSONException, IOException {
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(wURL);
        httpPost.setHeader("Accept", "application/json");
        httpPost.setHeader("Content-type", "application/json");

        StringEntity entity = new StringEntity(report.getJson().toString());
        httpPost.setEntity(entity);
        HttpResponse httpResponse = httpclient.execute(httpPost);
    }

    public void getPosts(){
        FetchPosts fp = new FetchPosts();
        Log.e(LogTags.BACKEND_W, readURL + activity.getScreenWidth());
        fp.execute(readURL + activity.getScreenWidth());
    }

    private JSONArray GET(String url){
        InputStream inputStream;
        String resultString = "error";

        try {
            HttpClient httpClient = new DefaultHttpClient();
            HttpResponse httpResponse = httpClient.execute(new HttpGet(url));
            inputStream = httpResponse.getEntity().getContent();
            if (inputStream != null)
                resultString = convertInputStreamToString(inputStream);
        } catch (Exception e) {
            activity.onUpdateFailed();
        }
        backupDataToFile(resultString);
        return convertStringToJson(resultString);
    }

    private void backupDataToFile(String dataString) {
//        try {
//            outputStream = openFileOutput("serverBackup", Context.MODE_PRIVATE);
//            outputStream.write(dataString.getBytes());
//            outputStream.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
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

    private JSONArray convertStringToJson(String input) {
        try {
            JSONArray jsonArray = new JSONArray(input);
            if (jsonArray.length() == 1 && jsonArray.getJSONObject(0).getString("error") != null)
                activity.onUpdateFailed();
            return jsonArray;
        } catch (JSONException e) {
            activity.onUpdateFailed();
        }
        return new JSONArray();
    }

    private class FetchPosts extends AsyncTask<String, Void, JSONArray> {
        @Override
        protected JSONArray doInBackground(String... urls) {
            return GET(urls[0]);
        }

        @Override
        protected void onPostExecute(JSONArray result) {
            int len = result.length();
            List<Report> listContent = new ArrayList<Report>(len);

            try {
                for (int i = 0; i < len; i++)
                    listContent.add(new Report(result.getJSONObject(i), null));
            } catch (JSONException e) {
                activity.onUpdateFailed();
            } catch (Exception e) {
                e.printStackTrace();
                activity.onUpdateFailed();
            }
            activity.onFeedUpdate(listContent);
        }
    }
}
