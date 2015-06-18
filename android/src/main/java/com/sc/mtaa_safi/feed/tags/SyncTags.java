package com.sc.mtaa_safi.feed.tags;

import android.content.Context;
import android.os.AsyncTask;

import com.sc.mtaa_safi.R;
import com.sc.mtaa_safi.SystemUtils.NetworkUtils;
import com.sc.mtaa_safi.SystemUtils.URLConstants;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by ishuah on 6/3/15.
 */
public class SyncTags extends AsyncTask<Void, Integer, JSONObject>{

    Context mContext;
    int canceller = -1;
    public static final int NETWORK_ERROR = 2;

    public SyncTags(Context context){
        mContext = context;
    }

    @Override
    protected JSONObject doInBackground(Void... voids) {
        JSONObject result = new JSONObject();
        try {
            result = getTags();
        }catch (JSONException e){
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    private JSONObject getTags() throws IOException, JSONException{
        JSONObject response = NetworkUtils.makeRequest(URLConstants.buildURL(mContext, URLConstants.TAG_GET_URL), "get", null);
        if (response.has("error") && response.getInt("error") >= 400)
            cancelSession(NETWORK_ERROR);
        return  response;
    }

    public Integer cancelSession(int reason) {
        canceller = reason;
        cancel(true);
        return reason;
    }
}
