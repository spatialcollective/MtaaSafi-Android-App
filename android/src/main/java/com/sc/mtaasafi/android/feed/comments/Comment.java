package com.sc.mtaasafi.android.feed.comments;

import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.os.RemoteException;

import com.sc.mtaasafi.android.R;
import com.sc.mtaasafi.android.database.Contract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class Comment {
    String mText, mUsername;
    int mReportId, mServerId;
    double mTimeStamp, mTimeSince;

	public static final String[] FROM_COLUMNS = new String[]{
        Contract.Comments.COLUMN_CONTENT,
        Contract.Comments.COLUMN_USERNAME,
        Contract.Comments.COLUMN_TIMESTAMP
    };
    public static final int[] TO_FIELDS = new int[]{
            R.id.commentText,
            R.id.commentUserName,
            R.id.commentTime
    };
	public static final String[] PROJECTION = new String[] {
		Contract.Comments._ID,
		Contract.Comments.COLUMN_CONTENT,
		Contract.Comments.COLUMN_USERNAME,
		Contract.Comments.COLUMN_TIMESTAMP };

	public static final String DEFAULT_SORT = Contract.Comments.COLUMN_TIMESTAMP + " ASC";
    
	public static String getSelection(int reportId) {
		return Contract.Comments.COLUMN_REPORT_ID + " = "+ reportId + " AND "
			+ Contract.Comments.COLUMN_USERNAME + " NOT NULL AND "
			+ Contract.Comments.COLUMN_TIMESTAMP + " NOT NULL AND "
			+ Contract.Comments.COLUMN_CONTENT + " NOT NULL";
	}

    public Comment setTime(double timeStamp, Context context) {
        mTimeStamp = timeStamp;
        mTimeSince = getLastCommentTimeStamp(mServerId, context);
        return this;
    }

    public JSONObject getJson() throws JSONException {
        JSONObject commentData = new JSONObject();
        commentData.put(Contract.Comments.COLUMN_CONTENT, mText)
                    .put(Contract.Comments.COLUMN_USERNAME, mUsername)
                    .put(Contract.Comments.COLUMN_TIMESTAMP, mTimeStamp)
                    .put("last_comment_timestamp", mTimeSince)
                    .put(Contract.Comments.COLUMN_REPORT_ID, mServerId);
        return commentData;
    }

    public ContentValues getContentValues() {
        ContentValues commentValues = new ContentValues();
        commentValues.put(Contract.Comments.COLUMN_CONTENT, mText);
        commentValues.put(Contract.Comments.COLUMN_USERNAME, mUsername);
        commentValues.put(Contract.Comments.COLUMN_TIMESTAMP, mTimeStamp);
        commentValues.put(Contract.Comments.COLUMN_SERVER_ID, mServerId);
        return commentValues;
    }

    public void getContentProviderOp(JSONObject commentJSON, ArrayList<ContentProviderOperation> batch)
                throws JSONException {
        batch.add(ContentProviderOperation.newInsert(Contract.Comments.COMMENTS_URI)
           .withValue(Contract.Comments.COLUMN_SERVER_ID, commentJSON.getInt(Contract.Comments.COLUMN_SERVER_ID))
           .withValue(Contract.Comments.COLUMN_REPORT_ID, commentJSON.getInt(Contract.Comments.COLUMN_REPORT_ID))
           .withValue(Contract.Comments.COLUMN_TIMESTAMP, commentJSON.getLong(Contract.Comments.COLUMN_TIMESTAMP))
           .withValue(Contract.Comments.COLUMN_USERNAME, commentJSON.getString(Contract.Comments.COLUMN_USERNAME))
           .withValue(Contract.Comments.COLUMN_CONTENT, commentJSON.getString(Contract.Comments.COLUMN_CONTENT))
           .build());
    }

    public static long getLastCommentTimeStamp(int reportId, Context context) {
        Cursor c = context.getContentResolver().query(Contract.Comments.COMMENTS_URI, Comment.PROJECTION,
                Contract.Comments.COLUMN_REPORT_ID + " = " + reportId, null, Comment.DEFAULT_SORT);
        if (c.moveToLast()) {
            long mostRecentTimeStamp = c.getLong(c.getColumnIndex(Contract.Comments.COLUMN_TIMESTAMP));
            c.close();
            return mostRecentTimeStamp;
        } else  return 0;
    }
}
