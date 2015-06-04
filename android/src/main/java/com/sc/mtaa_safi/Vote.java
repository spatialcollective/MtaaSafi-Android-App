package com.sc.mtaa_safi;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.RemoteException;

import com.sc.mtaa_safi.database.Contract;

import java.util.ArrayList;

public class Vote {
    Context mContext;
    String mUsername;
    Uri mReportUri;
    double mTimeStamp, mLat, mLng;
    int mServerId, mVoteCount;

    public Vote(Context c, Uri reportUri, int serverId, int voteCount) {
        mContext = c;
        mReportUri = reportUri;
        mServerId = serverId;
        mVoteCount = voteCount;
    }

    public void setLocation(Location location) {
        mLat = location.getLatitude();
        mLng = location.getLongitude();
    }

    public void save() throws RemoteException, OperationApplicationException {
        ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>();
        batch.add(ContentProviderOperation.newUpdate(mReportUri)
                .withValue(Contract.Entry.COLUMN_USER_UPVOTED, 1)
                .withValue(Contract.Entry.COLUMN_UPVOTE_COUNT, mVoteCount)
                .build());
        batch.add(ContentProviderOperation.newInsert(Contract.UpvoteLog.UPVOTE_URI)
                .withValue(Contract.UpvoteLog.COLUMN_SERVER_ID, mServerId)
                .withValue(Contract.UpvoteLog.COLUMN_LAT, mLat)
                .withValue(Contract.UpvoteLog.COLUMN_LON, mLng)
                .build());
        mContext.getContentResolver().applyBatch(Contract.CONTENT_AUTHORITY, batch);
    }
}
