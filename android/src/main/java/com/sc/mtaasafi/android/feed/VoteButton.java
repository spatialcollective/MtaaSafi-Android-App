package com.sc.mtaasafi.android.feed;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.location.Location;
import android.net.Uri;
import android.util.AttributeSet;
import android.widget.CompoundButton;

import com.sc.mtaasafi.android.database.Contract;

import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class VoteButton extends CompoundButton {
    public Uri mReportUri;
    public int mServerId;
    public boolean enabled;

    public VoteButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setOnCheckedChangeListener(new MyListener());
    }

    private class MyListener implements CompoundButton.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(CompoundButton view, boolean isChecked) {
            if (enabled && isChecked) {
                enabled = false;
                Location location = ((MainActivity) getContext()).getLocation();
                int voteCount = Integer.parseInt(getText().toString());
                setText(voteCount + 1 + "");
                FireAndForgetExecutor.exec(new SaveUpvote(getContext(), mReportUri,
                        location.getLatitude(), location.getLongitude(), mServerId, voteCount));
            } else if (!enabled && !isChecked) {
                setChecked(true);
            }
        }
    }

    public void setCheckedState(boolean upvoted, int upvoteCount, ArrayList<Integer> upvoteList) {
        this.enabled = !upvoted;
        if (!upvoteList.isEmpty() && upvoteList.contains(this.mServerId) && this.enabled) {
            this.setText(Integer.toString(upvoteCount + 1));
            this.enabled = false;
        } else
            this.setText(Integer.toString(upvoteCount));

        this.setChecked(!this.enabled);
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
                    .withValue(Contract.Entry.COLUMN_UPVOTE_COUNT, voteCount + 1)
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
