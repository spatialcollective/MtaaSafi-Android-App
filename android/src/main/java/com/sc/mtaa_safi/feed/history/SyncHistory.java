package com.sc.mtaa_safi.feed.history;

import android.content.Context;
import android.os.AsyncTask;

import com.sc.mtaa_safi.R;
import com.sc.mtaa_safi.SystemUtils.NetworkUtils;
import com.sc.mtaa_safi.SystemUtils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by ishuah on 5/20/15.
 */
public class SyncHistory extends AsyncTask<Void, Integer, JSONObject> {

    Context mContext;
    int mReportId = 0;
    int canceller = -1;
    public static final int NETWORK_ERROR = 2;

    public SyncHistory(Context context, int reportId){
        mContext = context;
        mReportId = reportId;
    }

    @Override
    protected JSONObject doInBackground(Void... voids) {
        JSONObject result = new JSONObject();
        try {
            result = getReportHistory();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

    private JSONObject getReportHistory() throws IOException, JSONException {
        JSONObject response = NetworkUtils.makeRequest(mContext.getString(R.string.base_url) + "get_child_reports/" + Utils.getUserId(mContext) + "/" + mReportId + "/", "get", null);
        if (response.has("error") && response.getInt("error") >= 400)
            cancelSession(NETWORK_ERROR);
        return response;
    }

    public Integer cancelSession(int reason) {
        canceller = reason;
        cancel(true);
        return reason;
    }
}
