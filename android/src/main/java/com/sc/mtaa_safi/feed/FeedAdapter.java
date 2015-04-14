package com.sc.mtaa_safi.feed;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sc.mtaa_safi.R;
import com.sc.mtaa_safi.RecyclerViewCursorAdapter;
import com.sc.mtaa_safi.Report;
import com.sc.mtaa_safi.database.Contract;
import com.squareup.picasso.Picasso;

import java.io.FileNotFoundException;
import java.util.ArrayList;

public class FeedAdapter extends RecyclerViewCursorAdapter<FeedAdapter.ViewHolder> {
    ArrayList<Integer> upvoteList;
    private Gson gson = new Gson();

    public FeedAdapter(Context context, Cursor cursor) {
        super(context, cursor);
        upvoteList = new ArrayList<Integer>();
    }

    @Override
    public FeedAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.feed_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, Cursor c) {
        setTitle(holder.mTitleView, c);
        holder.mLocation.setText(c.getString(c.getColumnIndex(Contract.Entry.COLUMN_HUMAN_LOC)));
        holder.mVoteButton.mServerId = c.getInt(c.getColumnIndex(Contract.Entry.COLUMN_SERVER_ID));
        holder.mVoteButton.mReportUri = Report.getUri(c.getInt(c.getColumnIndex(Contract.Entry.COLUMN_ID)));
        holder.mVoteButton.setCheckedState(c.getInt(c.getColumnIndex(Contract.Entry.COLUMN_USER_UPVOTED)) > 0,
                c.getInt(c.getColumnIndex(Contract.Entry.COLUMN_UPVOTE_COUNT)), upvoteList);

        addImage(holder, c);
        setDistanceView(holder, c);
        addClick(holder, c);
    }

    private void setTitle(TextView view, Cursor c) {
        view.setText(c.getString(c.getColumnIndex(Contract.Entry.COLUMN_CONTENT)));
        int status = c.getInt(c.getColumnIndex(Contract.Entry.COLUMN_STATUS));
        int drawable;
        if (status == 1)
            drawable = R.drawable.status_progress_selected;
        else if (status == 2)
            drawable = R.drawable.status_fixed_selected;
        else
            drawable = R.drawable.status_broken_selected;

//        view.setCompoundDrawablesWithIntrinsicBounds(null, null, getContext().getResources().getDrawable(drawable), null);
    }

    private void addImage(ViewHolder holder, Cursor c) {
        ArrayList<String> imagesJson = gson.fromJson(c.getString(c.getColumnIndex(Contract.Entry.COLUMN_MEDIA)), new TypeToken<ArrayList<String>>() {
        }.getType());
        String imageUrl = "";
        try {
            if (imagesJson != null && !imagesJson.get(0).isEmpty()) {
                Integer.parseInt(imagesJson.get(0));
                imageUrl = getContext().getString(R.string.base_url) + "get_thumbnail/" + imagesJson.get(0) + "/76x76";
                Picasso.with(getContext()).load(imageUrl)
                        .placeholder(R.drawable.image_placeholder)
                        .into(holder.mLeadImage);
            }
        } catch (NumberFormatException e) {
            try {
                Picasso.with(getContext()).load(imagesJson.get(0))
                        .placeholder(R.drawable.image_placeholder)
                        .into(holder.mLeadImage);
            } catch (Exception ex) { }
        } catch (Exception ex) { }
    }

    private void addClick(ViewHolder holder, Cursor c) {
        final int pos = holder.getPosition();
        final Report report = new Report(c);
        final MainActivity activity = (MainActivity) getContext();
        holder.mListener = new FeedAdapter.ViewHolder.ViewHolderClicks() {
            public void detailClick(View caller) { activity.goToDetailView(report); };
            public void upvoteClick(VoteButton b) { upvoteList.add(b.mServerId); }; };
    }

    private void setDistanceView(ViewHolder holder, Cursor c) {
//        Location currentLocation = ((MainActivity) getContext()).findLocation();
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
        public TextView mTitleView, mLocation;
        public ImageView mLeadImage;
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
            mLeadImage = (ImageView) v.findViewById(R.id.leadImage);
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
