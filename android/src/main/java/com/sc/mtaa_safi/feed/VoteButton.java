package com.sc.mtaa_safi.feed;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.graphics.Outline;
import android.graphics.Typeface;
import android.location.Location;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.CompoundButton;

import com.sc.mtaa_safi.R;
import com.sc.mtaa_safi.database.Contract;

import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class VoteButton extends CompoundButton {
    public Uri mReportUri;
    public int mServerId;
    public boolean enabled = true;

    public VoteButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setOnCheckedChangeListener(new MyListener());
        this.setOnTouchListener(new MyTouchListener());
    }

    private class MyTouchListener implements OnTouchListener {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                Animation scaleUp = new ScaleAnimation(1.0f, 1.2f, 1.0f, 1.2f,
                        Animation.RELATIVE_TO_SELF, .5f,
                        Animation.RELATIVE_TO_SELF, .5f);
                scaleUp.setDuration(200);
                scaleUp.setInterpolator(new AccelerateInterpolator());
                v.startAnimation(scaleUp);
            } else {
                Animation scaleDown = new ScaleAnimation(1.2f, 1.0f, 1.2f, 1.0f,
                        Animation.RELATIVE_TO_SELF,.5f,
                        Animation.RELATIVE_TO_SELF, .5f);
                scaleDown.setDuration(201);
                scaleDown.setInterpolator(new AccelerateInterpolator());
                v.startAnimation(scaleDown);
            }
            return false;
        }
    }

    private class MyListener implements CompoundButton.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(CompoundButton view, boolean isChecked) {
            Log.e("Vote btn", "checked state is: " + isChecked);
            if (enabled && isChecked) {
                enabled = false;
                int voteCount = Integer.parseInt(getText().toString()) + 1;
                Location location = ((MainActivity) getContext()).getLocation();
                if (location != null)
                    FireAndForgetExecutor.exec(new SaveUpvote(getContext(), mReportUri,
                            location.getLatitude(), location.getLongitude(), mServerId, voteCount));
                else FireAndForgetExecutor.exec(new SaveUpvote(getContext(), mReportUri,
                            0, 0, mServerId, voteCount));
                setToUp(voteCount);
            } else if (!enabled && !isChecked) {
                setChecked(true);
            }
        }
    }

    public void setCheckedState(boolean upvoted, int upvoteCount, ArrayList<Integer> upvoteList) {
        this.enabled = !upvoted;
        Log.e("Vote btn", "setting to on: " + enabled);
        if (upvoteList != null && !upvoteList.isEmpty() && upvoteList.contains(this.mServerId) && this.enabled) {
            setToUp(upvoteCount + 1);
            this.enabled = false;
        } else
            this.setText(Integer.toString(upvoteCount));
        this.setChecked(!this.enabled);
    }

    public void setToUp(int count) {
        setText(count + "");
        setTextColor(getResources().getColor(R.color.mtaa_safi_blue));
        setTypeface(Typeface.DEFAULT_BOLD);
    }

    public void setToDown() {
        int voteCount = Integer.parseInt(getText().toString()) - 1;
        setText(voteCount + "");
        setTextColor(getResources().getColor(R.color.mediumGrey));
        setTypeface(Typeface.DEFAULT);
    }

    @Override
    public void onFinishInflate() {
        if (isChecked()) setToUp(Integer.parseInt(getText().toString()));
        else setToDown();
    }

    public static class SaveUpvote implements Runnable {
        Context mContext;
        Uri mReportUri;
        double mLat, mLng;
        int mServerId, mVoteCount;

        public SaveUpvote(Context context, Uri reportUri, double lat, double lng, int serverId, int voteCount) {
            mContext = context;
            mReportUri = reportUri;
            mLat = lat;
            mLng = lng;
            mServerId = serverId;
            mVoteCount = voteCount;
        }

        @Override
        public void run() {
            ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>();
            batch.add(addUpvoteForUpload(mServerId, mLat, mLng));
            batch.add(updateReportDb(mReportUri, mVoteCount));
            try {
                mContext.getContentResolver().applyBatch(Contract.CONTENT_AUTHORITY, batch);
            } catch (Exception e) { }
        }

        private ContentProviderOperation updateReportDb(Uri reportUri, int voteCount) {
            return ContentProviderOperation.newUpdate(reportUri)
                    .withValue(Contract.Entry.COLUMN_USER_UPVOTED, 1)
                    .withValue(Contract.Entry.COLUMN_UPVOTE_COUNT, voteCount)
                    .build();
        }

        private ContentProviderOperation addUpvoteForUpload(int serverId, double lat, double lng) {
            return ContentProviderOperation.newInsert(Contract.UpvoteLog.UPVOTE_URI)
                    .withValue(Contract.UpvoteLog.COLUMN_SERVER_ID, serverId)
                    .withValue(Contract.UpvoteLog.COLUMN_LAT, lat)
                    .withValue(Contract.UpvoteLog.COLUMN_LON, lng)
                    .build();
        }
    }

    public static class FireAndForgetExecutor {
        private static Executor executor = Executors.newFixedThreadPool(5);
        public static void exec(Runnable command) { executor.execute(command); }
    }
}
