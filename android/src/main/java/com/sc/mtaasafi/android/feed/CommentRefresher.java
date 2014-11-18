package com.sc.mtaasafi.android.feed;

import android.os.AsyncTask;

import com.sc.mtaasafi.android.database.Contract;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Agree on 11/18/2014.
 */
public class CommentRefresher extends AsyncTask<JSONObject, Integer, JSONObject> {

    @Override
    protected JSONObject doInBackground(JSONObject... jsonObjects) {
        try {
            int serverId = jsonObjects[0].getInt(Contract.Comments.COLUMN_SERVER_ID);
            long timeStamp = jsonObjects[0].getLong(Contract.Comments.COLUMN_TIMESTAMP);
            // ask the server for all comments for report == serverId, since timestamp

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
    public void onPostExecute(JSONObject result){
        if(result != null)
            result.toString();
            // tell the listener, ie the fragment, that you're done!
        // tell the fragment things didn't
    }
}
