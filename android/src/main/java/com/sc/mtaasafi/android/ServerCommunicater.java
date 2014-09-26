package com.sc.mtaasafi.android;

import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

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
        void onFeedUpdate(List<PostData> posts);
        void onUpdateFailed();
    }
    /*
    {
"unique_id": String,
"title":String,
"details": String,
"timestamp":String,
"username": String,
"latitude": Float,
"longitude": Float,
"type": String,
"mediaURL": String,
"userPicURL": String,
}
    */

    public ServerCommCallbacks activity;
    public final static String titleName = "title";
    public final static String detailName = "details";
    public final static String userName = "user";
    public final static String timestampName = "timestamp";
    public final static String latName = "latitude";
    public final static String lonName = "longitude";
    public final static String mediaName = "mediaURL";
    public final static String profilePicURL = "userPicURL";
    public final static String errorName = "error";
    public final static String writeURL = "http://app.spatialcollective.com/add_post";
    private final static String readURL = "http://app.spatialcollective.com/get_posts";

    ServerCommunicater(ServerCommCallbacks activity){
        this.activity = activity;
    }
    // Asynchronously push the post to the server
    public void post(PostData postData){
        WritePost writePost = new WritePost(postData);
        writePost.execute(writeURL);
    }

    private class WritePost extends AsyncTask<String, Void, String>{
        PostData postData;
        WritePost(PostData post){
            postData = post;
        }

        @Override
        protected String doInBackground(String... urls) {
            try {
                writeToServer(urls[0], postData);
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private static void writeToServer(String wURL, PostData postData) throws JSONException, IOException {
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(wURL);
        httpPost.setHeader("Accept", "application/json");
        httpPost.setHeader("Content-type", "application/json");

        StringEntity entity = new StringEntity(toJSON(postData).toString());
        httpPost.setEntity(entity);
        HttpResponse httpResponse = httpclient.execute(httpPost);
        InputStream inputStream = httpResponse.getEntity().getContent();
        String result = convertInputStreamToString(inputStream);
        Log.e(LogTags.BACKEND_W, result);
    }

    // Convert POJO PostData to JSON
    private static JSONObject toJSON(PostData postData){
        try {
            JSONObject json = new JSONObject();
            json.put(userName, postData.userName);
            json.put(timestampName, postData.timestamp);
            json.put(latName, postData.latitude);
            json.put(lonName, postData.longitude);
            json.put(titleName, postData.title);
            json.put(detailName, postData.details);
            if(postData.picture != null){
                String encodedImage = Base64.encodeToString(postData.picture, Base64.DEFAULT);
                json.put(mediaName, encodedImage);
            }
            Log.e(LogTags.JSON, json.toString());
            return json;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.e(LogTags.JSON, "Failed to convert data to JSON");
        return null;
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

            if(inputStream != null)
                result = convertInputStreamToString(inputStream);
            else
                result = "Did not work";
        }catch (Exception e){
            Log.d("InputStream", e.getLocalizedMessage());
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

    private class FetchPosts extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls){
            return GET(urls[0]);
        }

        @Override
        protected void onPostExecute(String result){
            try{
                JSONArray jsonArray = new JSONArray(result);
                int len = jsonArray.length();
                List<PostData> listContent = new ArrayList<PostData>(len);
                Log.e("onPostExecute", "retrieved content list of length: " + len);
                JSONObject firstJSON = jsonArray.getJSONObject(0);
                if(listContent.size() == 1 && firstJSON.getString(errorName) != null){
                    activity.onUpdateFailed();
                    return;
                }
                for(int i = 0; i<len; i++){
                    try{
                        JSONObject json = jsonArray.getJSONObject(i);
                        try {
                            String title = json.getString(titleName);
                            String detail = json.getString(detailName);
                            String timeCreated = json.getString(timestampName);
                            String mediaURL = json.getString(mediaName);
                            String usn = json.getString(userName);
                            double lat = json.getLong(latName);
                            double lon = json.getLong(lonName);
                            PostData pd = new PostData(usn,
                                                        timeCreated,
                                                        lat, lon,
                                                        title,
                                                        detail,
                                                        mediaURL,
                                                        null
                                                        );
                            Log.e(LogTags.BACKEND_R, pd.title + " " + pd.details);
                            listContent.add(pd);
                            } catch (JSONException e) {
                            e.printStackTrace();
                            activity.onUpdateFailed();
                        }
                    }catch(Exception e){
                        Log.d("content", "JSON error");
                        activity.onUpdateFailed();
                    }
                }
                activity.onFeedUpdate(listContent);
            }catch (JSONException e){
                Log.d("JSONObject", e.getLocalizedMessage());
                activity.onUpdateFailed();
            }

        }
    }
}
