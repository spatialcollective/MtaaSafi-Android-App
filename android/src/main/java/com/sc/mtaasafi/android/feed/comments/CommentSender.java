package com.sc.mtaasafi.android.feed.comments;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.Context;
import android.content.OperationApplicationException;
import android.os.AsyncTask;
import android.os.RemoteException;
import android.util.Log;
import android.widget.LinearLayout;

import com.sc.mtaasafi.android.SystemUtils.NetworkUtils;
import com.sc.mtaasafi.android.SystemUtils.URLs;
import com.sc.mtaasafi.android.database.Contract;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by Agree on 11/17/2014.
 */
public class CommentSender extends AsyncTask<JSONObject, Integer, Integer> {
    Context mContext;
    Comment mComment;
    NewCommentLayout mLayout;

    public CommentSender(Context context, Comment comment, NewCommentLayout layout) {
        mContext = context;
        mComment = comment;
        mLayout = layout;
    }

    @Override
    protected Integer doInBackground(JSONObject... jsons) {
        try {
            mComment.setTime(System.currentTimeMillis(), mContext);
            JSONObject response = sendToServer(mComment.getJson());
            addNewCommentsToDb(response);
            return 1;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    private JSONObject sendToServer(JSONObject json) throws IOException {
        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(URLs.SEND_COMMENT);
        httpPost.setHeader("Accept", "application/json");
        httpPost.setHeader("Content-type", "application/json");
        httpPost.setEntity(new StringEntity(json.toString()));
        HttpResponse response = httpClient.execute(httpPost);
        return NetworkUtils.convertHttpResponseToJSON(response);
    }

    public void addNewCommentsToDb(JSONObject commentsData)
                throws JSONException, RemoteException, OperationApplicationException {
        JSONArray commentsArray = commentsData.getJSONArray(Contract.Comments.TABLE_NAME);
        if (commentsArray != null) {
            ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>();
            for (int i = 0; i < commentsArray.length(); i++)
                mComment.getContentProviderOp(commentsArray.getJSONObject(i), batch);
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
