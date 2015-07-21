package com.sc.mtaa_safi.feed.detail.comments;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.sc.mtaa_safi.R;
import com.sc.mtaa_safi.SystemUtils.Utils;
import com.sc.mtaa_safi.common.AsyncUploader;
import com.sc.mtaa_safi.newReport.SafiEditText;

import org.json.JSONException;

public class NewCommentLayout extends LinearLayout {
    private Context mContext;
    public Comment mComment;

    Button mSendButton;
    SafiEditText mEditText;

    public NewCommentLayout(Context context, AttributeSet attrs) { 
        super(context, attrs);
        mComment = new Comment(context);
        mContext = context;
    }

    public void addData(int reportId) {
        mComment.mUsername = Utils.getUserName(mContext);
        mComment.mReportId = reportId;
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
                mComment.mText = s.toString().trim();
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
        if (!mComment.mText.isEmpty())
            new AsyncUploader(getContext(), mComment).execute();
        reset();
    }

    private void reset() {
        mSendButton.setEnabled(true);
        mSendButton.setText("Send");
        int reportId = mComment.mReportId;
        mComment = new Comment(mContext);
        addData(reportId);
        mEditText.setText("");
    }
}
