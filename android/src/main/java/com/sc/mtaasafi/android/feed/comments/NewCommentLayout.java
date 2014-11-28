package com.sc.mtaasafi.android.feed.comments;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;

import com.sc.mtaasafi.android.R;
import com.sc.mtaasafi.android.Report;
import com.sc.mtaasafi.android.SystemUtils.NetworkUtils;
import com.sc.mtaasafi.android.SystemUtils.PrefUtils;
import com.sc.mtaasafi.android.SystemUtils.URLs;
import com.sc.mtaasafi.android.database.Contract;
import com.sc.mtaasafi.android.feed.MainActivity;
import com.sc.mtaasafi.android.feed.ReportDetailFragment;
import com.sc.mtaasafi.android.newReport.SafiEditText;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

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
        InputMethodManager inputManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(
                ((MainActivity) getContext()).getCurrentFocus().getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);
        if (NetworkUtils.isOnline(getContext()) && !mComment.mText.isEmpty())
            new CommentSender(getContext(), mComment, this).execute();
        else // tell user she must be online to send
            mSendButton.setEnabled(true);
    }

    public void onSuccessfulSend() {
//        mFrag.insertComment();
        mEditText.setText("");
        mSendButton.setEnabled(true);
    }

    public void onSendFailure() {
        mSendButton.setEnabled(true);
    }
}
