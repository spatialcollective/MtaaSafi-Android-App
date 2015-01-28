package com.sc.mtaa_safi.feed.comments;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.OperationApplicationException;
import android.os.AsyncTask;
import android.os.RemoteException;
import android.util.Log;

import com.sc.mtaa_safi.R;
import com.sc.mtaa_safi.SystemUtils.NetworkUtils;
import com.sc.mtaa_safi.database.Contract;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Agree on 11/17/2014.
 */
public class SyncComments extends AsyncTask<JSONObject, Integer, Integer> {
    Context mContext;
    Comment mComment;
    NewCommentLayout mLayout;
    int mReportId = 0;
    boolean isSending = true;

    public SyncComments(Context context, Comment comment, NewCommentLayout layout) {
        mContext = context;
        mComment = comment;
        mLayout = layout;
    }

    public SyncComments(Context context, int reportId) {
        mContext = context;
        isSending = false;
        mReportId = reportId;
    }

    @Override
    protected Integer doInBackground(JSONObject... jsons) {
        JSONObject response;
        try {
            if (isSending)
                response = sendToServer(mComment.setTime(System.currentTimeMillis(), mContext).getJson());
            else
                response = getFromServer();
            addNewCommentsToDb(response);
            return 1;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    private JSONObject sendToServer(JSONObject json) throws IOException {
        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(mContext.getString(R.string.send_comment));
        httpPost.setHeader("Accept", "application/json");
        httpPost.setHeader("Content-type", "application/json");
        httpPost.setEntity(new StringEntity(json.toString()));
        HttpResponse response = httpClient.execute(httpPost);
        return NetworkUtils.convertHttpResponseToJSON(response);
    }

    private JSONObject getFromServer() throws IOException {
        HttpClient httpClient = new DefaultHttpClient();
        HttpResponse response = httpClient.execute(new HttpGet(mContext.getString(R.string.refresh_comment) + "/" + mReportId));
        return NetworkUtils.convertHttpResponseToJSON(response);
    }

    public void addNewCommentsToDb(JSONObject commentsData)
                throws JSONException, RemoteException, OperationApplicationException {
        JSONArray commentsArray = commentsData.getJSONArray(Contract.Comments.TABLE_NAME);
        if (commentsArray != null) {
            ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>();
            for (int i = 0; i < commentsArray.length(); i++)
                Comment.getContentProviderOp(commentsArray.getJSONObject(i), batch, mContext);
            mContext.getContentResolver().applyBatch(Contract.CONTENT_AUTHORITY, batch);
            mContext.getContentResolver().notifyChange(Contract.Comments.COMMENTS_URI, null, false);
       }
    }

    @Override
    protected void onPostExecute(Integer result) {
        if (mLayout != null) {
            if (result == 1) mLayout.onSuccessfulSend();
            else mLayout.onSendFailure();
        }
    }
}
