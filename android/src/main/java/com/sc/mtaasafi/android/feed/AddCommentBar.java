package com.sc.mtaasafi.android.feed;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
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
    Button sendButton;
    SafiEditText editText;
    int reportId, defaultSendTextSize;
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
            public void afterTextChanged(Editable s) {}
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                attemptEnableSend(s);
            }
        });
        sendButton = (Button) findViewById(R.id.sendComment);
        defaultSendTextSize = (int) sendButton.getTextSize();
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
        if(!s.toString().isEmpty())
            enableSend();
        else
            disableSend();
        commentText = s.toString();
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
        if(true)
            send();
        else{// tell user she must be online to send
        }

    }
    private void send() throws JSONException {
        // send the server the comment, the username, the timestamp, the reportid
        JSONObject commentData = new JSONObject();
        ComplexPreferences cp = PrefUtils.getPrefs(getContext());
        commentData.put(USERNAME, cp.getString(PrefUtils.USERNAME, ""))
                    .put(TIMESTAMP, System.currentTimeMillis())
                    .put(REPORT_ID, reportId)
                    // put the timestamp of the last comment I have for this gosh dang report
                    .put(COMMENT, commentText);
        Log.e("SendComment!", commentData.toString());
        sendButton.setTextColor(getResources().getColor(R.color.LightCoral));
//        new CommentSender(listener).execute(commentData);
    }

    // called by the AsyncTask handling this comment.... NO should be the
    public void sendCommentSuccess(){
        // update the database for comments about that particular post
        // update the fragment for that post's newly added comments
        // clear the comment bar
    }
    public void save(Bundle outstate){
        outstate.putString(COMMENT, commentText);
    }

    public void restore(Bundle instate){
        commentText = instate.getString(COMMENT);
        editText.setText(commentText);
    }
}
