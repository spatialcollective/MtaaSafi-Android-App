package com.sc.mtaa_safi.login;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.sc.mtaa_safi.R;
import com.sc.mtaa_safi.SystemUtils.NetworkUtils;
import com.sc.mtaa_safi.SystemUtils.URLConstants;
import com.sc.mtaa_safi.SystemUtils.Utils;

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
            serverResponse = sendToServer();
            if (isCancelled())
                return -1;

            if (serverResponse.has("userId")) {
                Utils.setUserId(mContext, serverResponse.getInt("userId"));
                Log.i("UserDataUploader", String.valueOf(Utils.getUserId(mContext)));
            }

        } catch (IOException | JSONException | NoSuchAlgorithmException | NullPointerException e) {
            e.printStackTrace();
        }

        return result;
    }

    private JSONObject sendToServer() throws IOException, JSONException, NoSuchAlgorithmException {
        Log.e("UserDataUploader", String.valueOf(mUserdata));

        JSONObject response = NetworkUtils.makeRequest(URLConstants.buildURL(mContext, URLConstants.SIGN_IN_URL), "post", mUserdata);
        Log.e("UserDataUploader", String.valueOf(response));
        if (response != null && response.has("error"))
            cancelSession(NETWORK_ERROR);
        return response;
    }

    public Integer cancelSession(int reason) {
        canceller = reason;
        cancel(true);
        return reason;
    }

}
