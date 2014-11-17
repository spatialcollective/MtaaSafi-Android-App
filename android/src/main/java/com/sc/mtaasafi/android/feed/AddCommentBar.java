package com.sc.mtaasafi.android.feed;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.sc.mtaasafi.android.R;
import com.sc.mtaasafi.android.SystemUtils.ComplexPreferences;
import com.sc.mtaasafi.android.SystemUtils.PrefUtils;
import com.sc.mtaasafi.android.newReport.SafiEditText;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Agree on 11/17/2014.
 */
public class AddCommentBar extends LinearLayout {
    ImageButton sendButton;
    SafiEditText editText;
    int reportId;
    String commentText;
    CommentSendListener listener;
    public interface CommentSendListener{
        void sendCommentPressed();
        void commentSentSuccess();
        void commentSentFailure();
    }
    private static final String USERNAME = "username",
                                COMMENT = "comment",
                                TIMESTAMP = "timestamp",
                                REPORT_ID = "id";
    public AddCommentBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    // needs to be called before user can press send
    public void setListener(CommentSendListener listener){
        this.listener = listener;
    }
    public void onFinishInflate(){
        editText = (SafiEditText) findViewById(R.id.commentEditText);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                attemptEnableSend(s);
            }
        });
        sendButton = (ImageButton) findViewById(R.id.sendComment);
        sendButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    attemptSend();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void attemptEnableSend(CharSequence s){
        if(!s.toString().isEmpty()){
            sendButton.setImageResource(R.drawable.button_comment_clickable);
            sendButton.setClickable(true);
        } else {
            sendButton.setImageResource(R.drawable.button_comment_unclickable);
            sendButton.setClickable(false);
        }
        commentText = s.toString();
    }
    private void attemptSend() throws JSONException{
        if(true)
            send();
        else{// tell user she must be online to send
        }

    }
    private void send() throws JSONException {
        // send the server the comment, the username, the timestamp, the reportid
        JSONObject commentData = new JSONObject();
        ComplexPreferences cp = PrefUtils.getPrefs(getContext());
        commentData.put(USERNAME, cp.getString(PrefUtils.USERNAME, ""));
        commentData.put(TIMESTAMP, System.currentTimeMillis());
        commentData.put(REPORT_ID, reportId);
        commentData.put(COMMENT, commentText);
        new CommentSender(listener).execute(commentData);
    }

    // called by the AsyncTask handling this comment.... NO should be the
    public void sendCommentSuccess(){
        // update the database for comments about that particular post
        // update the fragment for that post's newly added comments
        // clear the comment bar
    }
    public void save(Bundle args){
        args.putString(COMMENT, commentText);
    }

    public void restore(Bundle args){
        commentText = args.getString(COMMENT);
        editText.setText(commentText);
    }
}
