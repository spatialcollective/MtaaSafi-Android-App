package com.sc.mtaa_safi.common;

import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.RemoteException;

import com.sc.mtaa_safi.SystemUtils.NetworkUtils;
import com.sc.mtaa_safi.SystemUtils.URLConstants;
import com.sc.mtaa_safi.SystemUtils.Utils;
import com.sc.mtaa_safi.Vote;
import com.sc.mtaa_safi.database.Contract;
import com.sc.mtaa_safi.feed.detail.comments.Comment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class AsyncUploader extends AsyncTask<JSONObject, Integer, Integer> {
    Context mContext;
    Comment mComment = null;
    Vote mVote = null;

    int canceller = -1;
    public static final int NETWORK_ERROR = 2;

    public AsyncUploader(Context context, Comment comment) {
        mContext = context;
        mComment = comment;
    }
    public AsyncUploader(Context context, Vote vote) {
        mContext = context;
        mVote = vote;
    }

    @Override
    protected Integer doInBackground(JSONObject... jsons) {
        try {
            saveToDb();
            if (!NetworkUtils.isOnline(mContext))
                cancelSession(NETWORK_ERROR);
            if (!isCancelled())
                sendUpvotes();
            if (!isCancelled())
                sendComments();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    private void sendUpvotes() throws RemoteException, OperationApplicationException, JSONException, IOException {
        Cursor upvoteLog = mContext.getContentResolver().query(Contract.UpvoteLog.UPVOTE_URI, null, null, null, null);
        if (upvoteLog.getCount() > 0) {

            JSONObject response = send(URLConstants.buildURL(mContext, URLConstants.UPVOTE_POST_URL), upvoteLog, "votes");
            if (response.has("error"))
                cancelSession(NETWORK_ERROR);
            else
                mContext.getContentResolver().delete(Contract.UpvoteLog.UPVOTE_URI, null, null);
        }
        upvoteLog.close();
    }

    private void sendComments() throws RemoteException, OperationApplicationException, JSONException, IOException {
        Cursor unsentComments = mContext.getContentResolver().query(Contract.Comments.COMMENTS_URI, Comment.PROJECTION, Contract.Comments.COLUMN_SERVER_ID + " = -1", null, null);
        if (unsentComments.getCount() > 0) {

            JSONObject response = send(URLConstants.buildURL(mContext, URLConstants.COMMENT_POST_URL), unsentComments, "comments");
            if (response.has("error"))
                cancelSession(NETWORK_ERROR);
            else
                Comment.saveServerIds(unsentComments, response.getJSONArray("comments"), mContext);
        }
        unsentComments.close();
    }

    private JSONObject send(String url, Cursor c, String type) throws RemoteException, OperationApplicationException, JSONException, IOException {
        JSONObject data = new JSONObject().put("userId", Utils.getUserId(mContext));
        data.put("items", new JSONArray());
        while (c.moveToNext()) {
            if (type == "comments")
                data.accumulate("items", new Comment(c, mContext).getJson());
            else if (type == "votes")
                data.accumulate("items", c.getInt(c.getColumnIndex(Contract.UpvoteLog.COLUMN_SERVER_ID)));
        }
        if (data.getJSONArray("items").length() == 0) {
            return new JSONObject().put("error", "No Data to Send");
        } else
            return NetworkUtils.makeRequest(url, "post", data);
    }

    private void saveToDb() throws JSONException, RemoteException, OperationApplicationException {
        if (mComment != null)
            mComment.save(System.currentTimeMillis());
        else if (mVote != null)
            mVote.save();
    }

    public Integer cancelSession(int reason) {
        canceller = reason;
        cancel(true);
        return reason;
    }
}