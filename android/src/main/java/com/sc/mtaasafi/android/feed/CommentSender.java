package com.sc.mtaasafi.android.feed;

import android.os.AsyncTask;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by Agree on 11/17/2014.
 */
public class CommentSender extends AsyncTask<JSONObject, Integer, JSONObject> {
    private static final String commentURL = "http://comments.biz.uk";
    AddCommentBar.CommentSendListener listener;
    CommentSender(AddCommentBar.CommentSendListener listener){
        this.listener = listener;
    }
    @Override
    protected JSONObject doInBackground(JSONObject... jsons){
        try{
            HttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(commentURL);
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");
            httpPost.setEntity(new StringEntity(jsons[0].toString()));
            HttpResponse response = httpClient.execute(httpPost);
            InputStream is = response.getEntity().getContent();
            String responseString = convertInputStreamToString(is);
            return new JSONObject(responseString);
        } catch (IOException e){
            e.printStackTrace();
        } catch (JSONException e){
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(JSONObject result) {
        if(result != null)
            listener.commentSentSuccess();
        else
            listener.commentSentFailure();
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
}
