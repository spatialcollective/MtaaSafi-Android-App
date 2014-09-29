package com.sc.mtaasafi.android;

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
        void onUpdateFailed();
    }

    public ServerCommCallbacks activity;
    public final static String writeURL = "http://app.spatialcollective.com/add_post",
                            readURL = "http://app.spatialcollective.com/get_posts";

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
        logResponse(httpResponse.getEntity().getContent());
    }

    public void getPosts(){
        FetchPosts fp = new FetchPosts();
        fp.execute(readURL);
    }

    private String GET(String url){
        InputStream inputStream;
        String result = "";
        try{
            HttpClient httpClient = new DefaultHttpClient();
            HttpResponse httpResponse = httpClient.execute(new HttpGet(url));
            inputStream = httpResponse.getEntity().getContent();

            if (inputStream != null)
                result = convertInputStreamToString(inputStream);
            else
                result = "Did not work";
        }catch (Exception e){
//            Log.d("InputStream", e.getLocalizedMessage());
            activity.onUpdateFailed();
        }
        return result;
    }

    private static String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line;
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;
        inputStream.close();
        return result;
    }

    private static void logResponse(InputStream inputStream) {
//        String result = convertInputStreamToString(inputStream);
//        Log.e(LogTags.BACKEND_W, result);
    }

    private class FetchPosts extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls){
            return GET(urls[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                JSONArray jsonArray = new JSONArray(result);
                int len = jsonArray.length();
                List<Report> listContent = new ArrayList<Report>(len);
//                Log.d("onPostExecute", "retrieved content list of length: " + len);
                if (listContent.size() == 1 && jsonArray.getJSONObject(0).getString("error") != null){
                    activity.onUpdateFailed();
                    return;
                }

                for (int i = 0; i < len; i++) {
                    try {
                        JSONObject json = jsonArray.getJSONObject(i);
                        listContent.add(new Report(json, null));
                    } catch (JSONException e) {
                        e.printStackTrace();
                        activity.onUpdateFailed();
                    } catch(Exception e) {
//                        Log.d("content", "JSON error");
                        activity.onUpdateFailed();
                    }
                }
                activity.onFeedUpdate(listContent);
            } catch (JSONException e) {
//                Log.d("JSONObject", e.getLocalizedMessage());
                activity.onUpdateFailed();
            }
        }
    }
}
