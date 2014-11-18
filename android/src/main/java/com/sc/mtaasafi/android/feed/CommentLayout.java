package com.sc.mtaasafi.android.feed;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.OperationApplicationException;
import android.os.RemoteException;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.support.v4.widget.SimpleCursorAdapter;

import com.sc.mtaasafi.android.R;
import com.sc.mtaasafi.android.database.Contract;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Agree on 11/18/2014.
 */
public class CommentLayout extends LinearLayout {
    SimpleCursorAdapter mAdapter;
    AddCommentBar addComment;
    private final static String REFRESH_KEY= "refresh";

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
        String sort = Contract.Comments.COLUMN_TIMESTAMP + " DESC";
        getContext().getContentResolver().query(Contract.Comments.COMMENTS_URI,
                                                null, selection, null, sort);
    }
    public void refreshComments(){
       // get all new comments for this report from the server
    }
    public void commentSendSuccess(JSONArray response) throws RemoteException, OperationApplicationException {
        // extract all the recent comments and add them to your database
        ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>();
        for(int i = 0; i < response.length(); i++) {
            batch.add(ContentProviderOperation.newInsert(Contract.Comments.COMMENTS_URI)
                    .withValue(Contract.Comments.COLUMN_REPORT_ID, 1)
                    .withValue(Contract.Comments.COLUMN_TIMESTAMP, 0000)
                    .withValue(Contract.Comments.COLUMN_USERNAME, "blabla")
                    .withValue(Contract.Comments.COLUMN_SERVER_ID, 1).build());
        }
        getContext().getContentResolver().applyBatch(Contract.CONTENT_AUTHORITY, batch);
    }
}
