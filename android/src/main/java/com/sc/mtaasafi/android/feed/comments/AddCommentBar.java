package com.sc.mtaasafi.android.feed.comments;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.RemoteException;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.sc.mtaasafi.android.R;
import com.sc.mtaasafi.android.SystemUtils.NetworkUtils;
import com.sc.mtaasafi.android.database.Contract;
import com.sc.mtaasafi.android.newReport.SafiEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Agree on 11/17/2014.
 */
public class AddCommentBar extends LinearLayout {
    Button sendButton;
    SafiEditText editText;
    int defaultSendTextSize;
    String commentText;
    CommentListener listener;

    public interface CommentListener{
        void sendComment(String comment) throws JSONException;
        void commentActionFinished(JSONObject result) throws RemoteException, OperationApplicationException, JSONException;
    }

    public AddCommentBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    // needs to be called before user can press send
    public void setCommentListener(CommentListener listener){ this.listener = listener; }
    public void onFinishInflate(){
        editText = (SafiEditText) findViewById(R.id.commentEditText);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {}
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s,
                                      int start, int before, int count) { attemptEnableSend(s); }
        });
        sendButton = (Button) findViewById(R.id.sendComment);
        defaultSendTextSize = (int) sendButton.getTextSize();
        sendButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                try {  attemptSend(); }
                catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        sendButton.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch(motionEvent.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        sendButton.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                                (float) (defaultSendTextSize * 1.2));
                        ((Button)view).setTypeface(null, Typeface.BOLD);
                        break;
                    case MotionEvent.ACTION_UP:
                        ((Button)view).setTypeface(null, Typeface.NORMAL);
                        sendButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, defaultSendTextSize);
                        break;
                }
                return false;
            }
        });
        attemptEnableSend(editText.getText());
    }

    private void attemptEnableSend(CharSequence s){
        commentText = s.toString();
        if(commentText.isEmpty())
            disableSend();
        else
            enableSend();
        Log.e("Send button", "Comment empty: " + commentText.isEmpty()
                + ". Send button clickable: " + sendButton.isClickable());
    }
    private void enableSend(){
        sendButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
        sendButton.setTextColor(getResources().getColor(R.color.Coral));
        sendButton.setEnabled(true);
    }
    private void disableSend(){
        sendButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        sendButton.setTextColor(getResources().getColor(R.color.LightCoral));
        sendButton.setEnabled(false);
    }
    private void attemptSend() throws JSONException{
        if(NetworkUtils.isOnline(getContext()))
            send();
        else{
        // tell user she must be online to send
        }

    }
    private void send() throws JSONException {
        // send the server the comment, the username, the timestamp, the reportid
        sendButton.setTextColor(getResources().getColor(R.color.LightCoral));
        listener.sendComment(editText.getText().toString());
    }

    public void save(Bundle outstate){
        outstate.putString(Contract.Comments.COLUMN_CONTENT, commentText);
    }

    public void restore(Bundle instate){
        commentText = instate.getString(Contract.Comments.COLUMN_CONTENT);
        editText.setText(commentText);
    }

    public void clearText() { editText.setText(""); }

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
            throws RemoteException, OperationApplicationException, JSONException {
        JSONArray commentsArray = commentsData.getJSONArray(Contract.Comments.TABLE_NAME);
        if(commentsArray != null){
            ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>();
            // get report id from commentsData
            for(int i = 0; i < commentsArray.length(); i++) {
                JSONObject commentJSON = commentsArray.getJSONObject(i);
                batch.add(ContentProviderOperation.newInsert(Contract.Comments.COMMENTS_URI)
                        .withValue(Contract.Comments.COLUMN_SERVER_ID, commentJSON.getInt(Contract.Comments.COLUMN_SERVER_ID))
                        .withValue(Contract.Comments.COLUMN_REPORT_ID, commentJSON.getInt(Contract.Comments.COLUMN_REPORT_ID))
                        .withValue(Contract.Comments.COLUMN_TIMESTAMP, commentJSON.getLong(Contract.Comments.COLUMN_TIMESTAMP))
                        .withValue(Contract.Comments.COLUMN_USERNAME, commentJSON.getString(Contract.Comments.COLUMN_USERNAME))
                        .withValue(Contract.Comments.COLUMN_CONTENT, commentJSON.getString(Contract.Comments.COLUMN_CONTENT))
                        .build());
            }
            context.getContentResolver().applyBatch(Contract.CONTENT_AUTHORITY, batch);
            context.getContentResolver().notifyChange(Contract.Comments.COMMENTS_URI, null, false);
        }
    }

}
