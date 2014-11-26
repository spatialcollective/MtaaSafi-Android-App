package com.sc.mtaasafi.android.feed.comments;

import android.content.OperationApplicationException;
import android.os.AsyncTask;
import android.os.RemoteException;
import android.util.Log;

import com.sc.mtaasafi.android.SystemUtils.NetworkUtils;
import com.sc.mtaasafi.android.SystemUtils.URLs;
import com.sc.mtaasafi.android.database.Contract;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by Agree on 11/18/2014.
 */
public class CommentRefresher extends AsyncTask<JSONObject, Integer, JSONObject> {
    AddCommentBar.CommentListener listener;
    public CommentRefresher(AddCommentBar.CommentListener listener){
        this.listener = listener;
    }
    @Override
    protected JSONObject doInBackground(JSONObject... jsonObjects) {
        try {
            int serverId = jsonObjects[0].getInt("ReportId");
            long sinceTimeStamp = jsonObjects[0].getLong("Since");
            String refreshURL = URLs.REFRESH_COMMENT + serverId + "/";
            if(sinceTimeStamp != 0)
                refreshURL += sinceTimeStamp + "/";
            Log.e("Refresh comment URL", refreshURL);
            HttpClient client = new DefaultHttpClient();
            return NetworkUtils.convertHttpResponseToJSON(client.execute(new HttpGet(refreshURL)));
        } catch (JSONException e) { e.printStackTrace();}
          catch (ClientProtocolException e) { e.printStackTrace(); }
          catch (IOException e) { e.printStackTrace(); }
        return null;
    }

    public void onPostExecute(JSONObject result){
        try { listener.commentActionFinished(result); }
        catch (RemoteException e) { e.printStackTrace(); }
        catch (OperationApplicationException e) {  e.printStackTrace(); }
        catch (JSONException e) { e.printStackTrace(); }
    }
}
