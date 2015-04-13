package com.sc.mtaa_safi.SystemUtils;

import android.content.Context;
import android.os.AsyncTask;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.sc.mtaa_safi.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class RegisterWithGcm extends AsyncTask<String, String, String> {
    Context mContext;
    GoogleCloudMessaging gcm;
    String regid;
    public RegisterWithGcm(Context context, GoogleCloudMessaging gcm) {
        mContext = context;
        this.gcm = gcm;
    }

    @Override
    protected String doInBackground(String... params) {
        String msg = "";
        try {
            if (gcm == null)
                gcm = GoogleCloudMessaging.getInstance(mContext);

            regid = gcm.register(Utils.SENDER_ID);
            msg = "Device registered, registration ID=" + regid;

            sendRegistrationIdToBackend();
            if (!isCancelled())
                Utils.setRegistrationId(mContext, regid);
        } catch (Exception ex) {
            msg = "Error :" + ex.getMessage();
            // If there is an error, don't just keep trying to register.
            // Require the user to click a button again, or perform exponential back-off.
        }
        return msg;
    }

    private void sendRegistrationIdToBackend() throws JSONException, IOException {
        JSONObject json = new JSONObject();
        json.put("reg_id", this.regid);
        json.put("name", Utils.getUserName(mContext));
        json.put("dev_id", "1234");
        JSONObject response = NetworkUtils.makeRequest(mContext.getString(R.string.registerId), "post", json);
        if (response != null && response.has("error") && response.getInt("error") > 400)
            cancel(true);
    }

    @Override
    protected void onPostExecute(String msg) {
//        mDisplay.append(msg + "\n");
    }
}
