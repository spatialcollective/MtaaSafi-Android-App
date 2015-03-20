package com.sc.mtaa_safi.login;

import android.content.Context;
import android.os.AsyncTask;

import com.sc.mtaa_safi.R;
import com.sc.mtaa_safi.SystemUtils.NetworkUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

/**
 * Created by ishuah on 3/18/15.
 */
public class UserDataUploader extends AsyncTask<Integer, Integer, Integer> {
    JSONObject mUserdata;
    Context mContext;

    int canceller = -1;
    public static final int NETWORK_ERROR = 2;

    public UserDataUploader(Context context, JSONObject userdata){
        mContext = context;
        mUserdata = userdata;
    }

    @Override
    protected Integer doInBackground(Integer... p){
        JSONObject serverResponse;
        int result = 1;
        try {
            serverResponse =sendToServer();
            if (serverResponse != null && serverResponse.has("error"))
                result = -1;
        } catch (IOException | JSONException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return result;
    }

    private JSONObject sendToServer() throws IOException, JSONException, NoSuchAlgorithmException {
        JSONObject response = NetworkUtils.makeRequest(mContext.getString(R.string.signin), "post", mUserdata);
        if (response != null && response.has("error") && response.getInt("error") > 400)
            cancelSession(NETWORK_ERROR);
        return response;
    }

    public Integer cancelSession(int reason) {
        canceller = reason;
        cancel(true);
        return reason;
    }

}
