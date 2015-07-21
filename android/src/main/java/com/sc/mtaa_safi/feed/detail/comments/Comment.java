package com.sc.mtaa_safi.feed.detail.comments;

import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;

import com.sc.mtaa_safi.database.Contract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class Comment {
    Context mContext;
    String mText, mUsername;
    int mId, mReportId, mServerId = -1;
    Long mTimeStamp;

    public Comment(Context c) {
        mContext = c;
    }

    public Comment(Cursor c, Context context) {
        mContext = context;
        mId = c.getInt(c.getColumnIndex(Contract.Comments._ID));
        mText = c.getString(c.getColumnIndex(Contract.Comments.COLUMN_CONTENT));
        mReportId = c.getInt(c.getColumnIndex(Contract.Comments.COLUMN_REPORT_ID));
        mTimeStamp = c.getLong(c.getColumnIndex(Contract.Comments.COLUMN_TIMESTAMP));
        mUsername = c.getString(c.getColumnIndex(Contract.Comments.COLUMN_USERNAME));
    }

    public Comment(JSONObject c, Context context) throws JSONException {
        mContext = context;
        mId = c.getInt("clientId");
        mServerId = c.getInt("id");

        mText = c.getString(Contract.Comments.COLUMN_CONTENT);
        mUsername = c.getString(Contract.Comments.COLUMN_USERNAME);
        mReportId = c.getInt(Contract.Comments.COLUMN_REPORT_ID);
        mTimeStamp = c.getLong(Contract.Comments.COLUMN_TIMESTAMP);
        if (c.has("owner"))
            mUsername = c.getJSONObject("owner").getString(Contract.Comments.COLUMN_USERNAME);
    }

    public Comment(JSONObject c, int reportId, Context context) throws JSONException {
        mContext = context;
        mReportId = reportId;
        mServerId = c.getInt("id");
        mText = c.getString(Contract.Comments.COLUMN_CONTENT);
        mTimeStamp = c.getLong(Contract.Comments.COLUMN_TIMESTAMP);
        if (c.has("owner"))
            mUsername = c.getJSONObject("owner").getString(Contract.Comments.COLUMN_USERNAME);
    }

	public static final String[] PROJECTION = new String[] {
		    Contract.Comments._ID,
            Contract.Comments.COLUMN_SERVER_ID,
            Contract.Comments.COLUMN_REPORT_ID,
            Contract.Comments.COLUMN_CONTENT,
            Contract.Comments.COLUMN_USERNAME,
            Contract.Comments.COLUMN_TIMESTAMP };

	public static final String DEFAULT_SORT = Contract.Comments.COLUMN_TIMESTAMP + " ASC";

	public static String getSelection(int reportId) {
		return Contract.Comments.COLUMN_REPORT_ID + " = " + reportId + " AND "
			+ Contract.Comments.COLUMN_USERNAME + " NOT NULL AND "
			+ Contract.Comments.COLUMN_TIMESTAMP + " NOT NULL AND "
			+ Contract.Comments.COLUMN_CONTENT + " NOT NULL";
	}

    public JSONObject getJson() throws JSONException {
        JSONObject commentData = new JSONObject();
        commentData.put(Contract.Comments.COLUMN_CONTENT, mText)
                    .put("clientId", mId)
                    .put(Contract.Comments.COLUMN_USERNAME, mUsername)
                    .put(Contract.Comments.COLUMN_TIMESTAMP, mTimeStamp)
                    .put(Contract.Comments.COLUMN_REPORT_ID, mReportId);
        return commentData;
    }

    public Uri save(long time) {
        mTimeStamp = time;
        return this.save();
    }
    public Uri save() {
        return mContext.getContentResolver().insert(Contract.Comments.COMMENTS_URI, getContentValues());
    }

    public ContentValues getContentValues() {
        ContentValues commentValues = new ContentValues();
        commentValues.put(Contract.Comments.COLUMN_CONTENT, mText);
        commentValues.put(Contract.Comments.COLUMN_SERVER_ID, mServerId);
        commentValues.put(Contract.Comments.COLUMN_USERNAME, mUsername);
        commentValues.put(Contract.Comments.COLUMN_TIMESTAMP, mTimeStamp);
        commentValues.put(Contract.Comments.COLUMN_REPORT_ID, mReportId);
        return commentValues;
    }

    public static void getContentProviderOp(JSONObject commentJSON, ArrayList<ContentProviderOperation> batch, Context context)
                throws JSONException {
        Cursor c = context.getContentResolver().query(Contract.Comments.COMMENTS_URI, Comment.PROJECTION,
                Contract.Comments.COLUMN_SERVER_ID + " = " + commentJSON.getInt(Contract.Comments.COLUMN_SERVER_ID),
                null, Comment.DEFAULT_SORT);
        if (!(c.getCount() > 0))
            batch.add(ContentProviderOperation.newInsert(Contract.Comments.COMMENTS_URI)
               .withValue(Contract.Comments.COLUMN_SERVER_ID, commentJSON.getInt(Contract.Comments.COLUMN_SERVER_ID))
               .withValue(Contract.Comments.COLUMN_REPORT_ID, commentJSON.getInt(Contract.Comments.COLUMN_REPORT_ID))
               .withValue(Contract.Comments.COLUMN_TIMESTAMP, commentJSON.getLong(Contract.Comments.COLUMN_TIMESTAMP))
               .withValue(Contract.Comments.COLUMN_USERNAME, commentJSON.getString(Contract.Comments.COLUMN_USERNAME))
               .withValue(Contract.Comments.COLUMN_CONTENT, commentJSON.getString(Contract.Comments.COLUMN_CONTENT))
               .build());
    }

    public static void saveServerIds(Cursor c, JSONArray comments, Context context) throws JSONException, RemoteException, OperationApplicationException {
        HashMap<Integer, Comment> map = Comment.createCommentMap(comments, context);
        ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>();
        c.moveToFirst();
        do {
            Comment match = map.get(c.getInt(c.getColumnIndex(Contract.Comments._ID)));
            if (match != null)
                batch.add(ContentProviderOperation.newInsert(Contract.Comments.COMMENTS_URI).withValues(match.getContentValues()).build()); // This only works because of unique constraint in db
        } while (c.moveToNext());
        context.getContentResolver().applyBatch(Contract.CONTENT_AUTHORITY, batch);
    }

    private static HashMap<Integer, Comment> createCommentMap(JSONArray comments, Context context) throws JSONException {
        HashMap<Integer, Comment> map = new HashMap<>();
        for (int i = 0; i < comments.length(); i++) {
            Comment comment = new Comment(comments.getJSONObject(i), context);
            map.put(comment.mId, comment);
        }
        return map;
    }
}
