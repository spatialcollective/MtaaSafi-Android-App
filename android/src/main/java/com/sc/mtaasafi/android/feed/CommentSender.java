package com.sc.mtaasafi.android.feed;

import android.content.OperationApplicationException;
import android.os.AsyncTask;
import android.os.RemoteException;

import com.sc.mtaasafi.android.SystemUtils.NetworkUtils;
import com.sc.mtaasafi.android.SystemUtils.URLs;

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
import java.net.URL;

/**
 * Created by Agree on 11/17/2014.
 */
public class CommentSender extends AsyncTask<JSONObject, Integer, JSONObject> {
    CommentLayout.CommentListener listener;
    CommentSender(CommentLayout.CommentListener listener){
        this.listener = listener;
    }
    @Override
    protected JSONObject doInBackground(JSONObject... jsons){
        try{
            HttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(URLs.SEND_COMMENT);
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");
            httpPost.setEntity(new StringEntity(jsons[0].toString()));
            HttpResponse response = httpClient.execute(httpPost);
            InputStream is = response.getEntity().getContent();
            String responseString = NetworkUtils.convertInputStreamToString(is);
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
        try {
            listener.commentActionFinished(result);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (OperationApplicationException e) {
            e.printStackTrace();
        }
    }
}
