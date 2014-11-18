package com.sc.mtaasafi.android.feed;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.os.RemoteException;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.support.v4.widget.SimpleCursorAdapter;

import com.sc.mtaasafi.android.R;
import com.sc.mtaasafi.android.database.Contract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Agree on 11/18/2014.
 */
public class CommentLayout extends LinearLayout {
    SimpleCursorAdapter mAdapter;
    AddCommentBar addComment;
    private final static String REFRESH_KEY= "refresh";
    public static final String USERNAME = "username",
            COMMENT = "comment",
            TIMESTAMP = "timestamp",
            REPORT_ID = "id";

    public final String[] FROM_COLUMNS = new String[]{
        Contract.Comments.COLUMN_CONTENT,
        Contract.Comments.COLUMN_USERNAME,
        Contract.Comments.COLUMN_TIMESTAMP
    };
    public final int[] TO_FIELDS = new int[]{
        R.id.commentText,
        R.id.commentUserName,
        R.id.commentTime
    };

    public interface CommentListener{
        void sendComment(String comment) throws JSONException;
        void commentActionFinished(JSONObject result) throws RemoteException, OperationApplicationException;
    }

    public CommentLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    @Override
    public void onFinishInflate(){
        // populate the
        mAdapter = new SimpleCursorAdapter(getContext(), R.layout.comment_view, null,
                                            FROM_COLUMNS, TO_FIELDS, 0);
        addComment = (AddCommentBar) findViewById(R.id.add_comment_bar);
    }

    public void updateCommentsList(int reportId){
        String selection = Contract.Comments.COLUMN_REPORT_ID + " = " + reportId;
        String sort = Contract.Comments.COLUMN_TIMESTAMP + " ASC";
        Cursor c = getContext().getContentResolver().query(Contract.Comments.COMMENTS_URI,
                null, selection, null, sort);
        mAdapter.changeCursor(c);
    }

    public void refreshComments(int reportId, CommentListener listener) throws JSONException {
       // get all new comments for this report from the server
        long sinceTime = getLastCommentTimeStamp(reportId, getContext());
        new CommentRefresher(listener).execute(new JSONObject().put("ReportId", reportId)
                .put("Since", sinceTime));
    }

    public static long getLastCommentTimeStamp(int reportId, Context context) {
        String[] projection = new String[1];
        projection[0] = Contract.Comments.COLUMN_TIMESTAMP;
        String selection = Contract.Comments.COLUMN_REPORT_ID + " = " + reportId;
        String sortOrder = Contract.Comments.COLUMN_TIMESTAMP + " ASC";
        Cursor c = context.getContentResolver().query(Contract.Comments.COMMENTS_URI, projection,
                selection, null, sortOrder);
        if (c.moveToLast())
            return c.getLong(c.getColumnIndex(Contract.Comments.COLUMN_TIMESTAMP));
        else
            return 0;
    }
    public static void updateCommentsTable(JSONObject commentsData, Context context)
            throws RemoteException, OperationApplicationException {
        ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>();
        // get report id from commentsData
        for(int i = 0; i < commentsData.length(); i++) {
            batch.add(ContentProviderOperation.newInsert(Contract.Comments.COMMENTS_URI)
                    .withValue(Contract.Comments.COLUMN_REPORT_ID, 1)
                    .withValue(Contract.Comments.COLUMN_TIMESTAMP, 0000)
                    .withValue(Contract.Comments.COLUMN_USERNAME, "blabla")
                    .withValue(Contract.Comments.COLUMN_CONTENT, "contentntnenenent").build());
        }
        context.getContentResolver().applyBatch(Contract.CONTENT_AUTHORITY, batch);
    }
}
