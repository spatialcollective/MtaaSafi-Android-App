package com.sc.mtaasafi.android.feed;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.location.Location;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sc.mtaasafi.android.R;
import com.sc.mtaasafi.android.RecyclerViewCursorAdapter;
import com.sc.mtaasafi.android.Report;
import com.sc.mtaasafi.android.database.Contract;

import java.util.ArrayList;

public class FeedAdapter extends RecyclerViewCursorAdapter<FeedAdapter.ViewHolder> {
    ArrayList<Integer> upvoteList;
    public FeedAdapter(Context context, Cursor cursor) {
        super(context, cursor);
        upvoteList = new ArrayList<Integer>();
    }

    @Override
    public FeedAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.feed_item_view, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, Cursor c) {
        holder.mTitleView.setText(c.getString(c.getColumnIndex(Contract.Entry.COLUMN_CONTENT)));
        holder.mLocation.setText(c.getString(c.getColumnIndex(Contract.Entry.COLUMN_LOCATION)));
        holder.mVoteButton.mServerId = c.getInt(c.getColumnIndex(Contract.Entry.COLUMN_SERVER_ID));
        holder.mVoteButton.mReportUri = Report.getUri(c.getInt(c.getColumnIndex(Contract.Entry.COLUMN_ID)));
        setCheckedState(holder, c);
        setDistanceView(holder, c);
        addClick(holder, c);
    }

    private void addClick(ViewHolder holder, Cursor c) {
        final int pos = holder.getPosition();
        final Report report = new Report(c);
        final MainActivity activity = (MainActivity) getContext();
        holder.mListener = new FeedAdapter.ViewHolder.ViewHolderClicks() {
            public void detailClick(View caller) { activity.goToDetailView(report, pos); };
            public void upvoteClick(VoteButton b) { upvoteList.add(b.mServerId); }; };
    }

    private void setCheckedState(ViewHolder holder, Cursor c) {
        holder.mVoteButton.enabled = !(c.getInt(c.getColumnIndex(Contract.Entry.COLUMN_USER_UPVOTED)) > 0);
        if (upvoteList.contains(holder.mVoteButton.mServerId) && holder.mVoteButton.enabled) {
            holder.mVoteButton.setText(Integer.toString(c.getInt(c.getColumnIndex(Contract.Entry.COLUMN_UPVOTE_COUNT)) + 1));
            holder.mVoteButton.enabled = false;
        } else
            holder.mVoteButton.setText(Integer.toString(c.getInt(c.getColumnIndex(Contract.Entry.COLUMN_UPVOTE_COUNT))));

        holder.mVoteButton.setChecked(!holder.mVoteButton.enabled);
    }

    private void setDistanceView(ViewHolder holder, Cursor c) {
//        Location currentLocation = ((MainActivity) getContext()).getLocation();
//        if (currentLocation != null) {
//            String distText = Report.getDistanceText(currentLocation,
//                    c.getDouble(c.getColumnIndex(Contract.Entry.COLUMN_LAT)),
//                    c.getDouble(c.getColumnIndex(Contract.Entry.COLUMN_LNG)));
//            holder.mDist.setText(distText);
//            Resources resources = getContext().getResources();
//            if (distText.equals("here")) {
//                holder.mDist.setTextColor(resources.getColor(R.color.Coral));
//                holder.mDist.setCompoundDrawablesWithIntrinsicBounds(null, null, resources.getDrawable(R.drawable.marker_coral_small), null);
//            } else {
//                holder.mDist.setTextColor(resources.getColor(R.color.DarkGray));
//                holder.mDist.setCompoundDrawablesWithIntrinsicBounds(null, null, resources.getDrawable(R.drawable.marker_small), null);
//            }
//        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public ViewHolderClicks mListener;
        public TextView mTitleView, mLocation, mDist;
        public VoteButton mVoteButton;

        public static interface ViewHolderClicks {
            public void detailClick(View caller);
            public void upvoteClick(VoteButton vote);
        }

        public ViewHolder(View v) {
            super(v);
            mTitleView = (TextView) v.findViewById(R.id.itemTitle);
            mVoteButton = (VoteButton) v.findViewById(R.id.voteInterface);
            mLocation = (TextView) v.findViewById(R.id.itemLocation);
            mDist = (TextView) v.findViewById(R.id.itemDistance);

            v.setOnClickListener(this);
            mVoteButton.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (!(v instanceof VoteButton))
                mListener.detailClick(v);
            else
                mListener.upvoteClick((VoteButton) v);
        }
    }
}
