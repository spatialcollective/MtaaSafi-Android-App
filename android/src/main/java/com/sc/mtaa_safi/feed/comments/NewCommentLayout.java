package com.sc.mtaa_safi.feed.comments;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;

import com.sc.mtaa_safi.feed.MainActivity;
import com.sc.mtaa_safi.newReport.SafiEditText;
import com.sc.mtaa_safi.R;
import com.sc.mtaa_safi.Report;
import com.sc.mtaa_safi.SystemUtils.NetworkUtils;
import com.sc.mtaa_safi.SystemUtils.PrefUtils;

import org.json.JSONException;

/**
 * Created by Agree on 11/17/2014.
 */
public class NewCommentLayout extends LinearLayout {
    public Comment mComment;

    Button mSendButton;
    SafiEditText mEditText;

    public NewCommentLayout(Context context, AttributeSet attrs) { 
        super(context, attrs);
        mComment = new Comment();
    }

    public void addData(Report report) {
        mComment.mUsername = PrefUtils.getPrefs(getContext()).getString(PrefUtils.USERNAME, "");
        mComment.mReportId = report.serverId;
    }

    @Override
    public void onFinishInflate() {
        mSendButton = (Button) findViewById(R.id.sendComment);
        mEditText = (SafiEditText) findViewById(R.id.commentEditText);
        mEditText.addTextChangedListener(new TextWatcher() {
            @Override public void afterTextChanged(Editable s) {}
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mComment.mText = s.toString();
                if (mComment.mText.isEmpty())
                    mSendButton.setEnabled(false);
                else
                    mSendButton.setEnabled(true);
            }
        });
        mSendButton.setOnClickListener(new OnClickListener() {
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

    private void attemptSend() throws JSONException {
        mSendButton.setEnabled(false);
//        InputMethodManager inputManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
//        inputManager.hideSoftInputFromWindow(
//                ((MainActivity) getContext()).getCurrentFocus().getWindowToken(),
//                InputMethodManager.HIDE_NOT_ALWAYS);
        mSendButton.setText("Sending");
        if (NetworkUtils.isOnline(getContext()) && !mComment.mText.isEmpty())
            new SyncComments(getContext(), mComment, this).execute();
        else // tell user she must be online to send
            mSendButton.setEnabled(true);
    }

    public void onSuccessfulSend() {
        mSendButton.setText("Send");
        mEditText.setText("");
        mSendButton.setEnabled(true);
    }

    public void onSendFailure() {
        mSendButton.setText("Send");
        mSendButton.setEnabled(true);
    }
}
