package com.sc.mtaasafi.android.feed;

import android.content.OperationApplicationException;
import android.os.AsyncTask;
import android.os.RemoteException;

import com.sc.mtaasafi.android.database.Contract;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Agree on 11/18/2014.
 */
public class CommentRefresher extends AsyncTask<JSONObject, Integer, JSONObject> {
    CommentLayout.CommentListener listener;
    CommentRefresher(CommentLayout.CommentListener listener){
        this.listener = listener;
    }
    @Override
    protected JSONObject doInBackground(JSONObject... jsonObjects) {
        try {
            int serverId = jsonObjects[0].getInt(Contract.Comments.COLUMN_SERVER_ID);
            long sinceTimeStamp = jsonObjects[0].getLong(Contract.Comments.COLUMN_TIMESTAMP);
            // ask the server for all comments for report == serverId, since timestamp

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
    public void onPostExecute(JSONObject result){
        try {
            listener.commentActionFinished(result);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (OperationApplicationException e) {
            e.printStackTrace();
        }
    }
}
