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

public class FeedAdapter extends RecyclerViewCursorAdapter<FeedAdapter.ViewHolder> {

    public FeedAdapter(Context context, Cursor cursor) { super(context, cursor); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView mTitleView, mLocation, mDist;
        public VoteButton mVoteButton;

        public ViewHolder(View v) {
            super(v);
            mTitleView = (TextView) v.findViewById(R.id.itemTitle);
            mVoteButton = (VoteButton) v.findViewById(R.id.voteInterface);
            mLocation = (TextView) v.findViewById(R.id.itemLocation);
            mDist = (TextView) v.findViewById(R.id.itemDistance);
        }
    }

    @Override
    public FeedAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.feed_item_view, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, Cursor c) {
        holder.mTitleView.setText(c.getString(c.getColumnIndex(Contract.Entry.COLUMN_CONTENT)));
        holder.mLocation.setText(c.getString(c.getColumnIndex(Contract.Entry.COLUMN_LOCATION)));
        holder.mVoteButton.setText(Integer.toString(c.getInt(c.getColumnIndex(Contract.Entry.COLUMN_UPVOTE_COUNT))));
        if (c.getInt(c.getColumnIndex(Contract.Entry.COLUMN_USER_UPVOTED)) > 0)
            holder.mVoteButton.setChecked(true);
        else
            holder.mVoteButton.setChecked(false);

        Location currentLocation = ((MainActivity) getContext()).getLocation();
        if (currentLocation != null) {
            String distText = Report.getDistanceText(currentLocation,
                    c.getDouble(c.getColumnIndex(Contract.Entry.COLUMN_LAT)),
                    c.getDouble(c.getColumnIndex(Contract.Entry.COLUMN_LNG)));
            holder.mDist.setText(distText);
            Resources resources = getContext().getResources();
            if (distText.equals("here")) {
                holder.mDist.setTextColor(resources.getColor(R.color.Coral));
                holder.mDist.setCompoundDrawablesWithIntrinsicBounds(null, null, resources.getDrawable(R.drawable.marker_coral_small), null);
            } else {
                holder.mDist.setTextColor(resources.getColor(R.color.DarkGray));
                holder.mDist.setCompoundDrawablesWithIntrinsicBounds(null, null, resources.getDrawable(R.drawable.marker_small), null);
            }
        }
    }
}
