package com.sc.mtaa_safi.feed;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sc.mtaa_safi.RecyclerViewCursorAdapter;
import com.sc.mtaa_safi.Report;
import com.sc.mtaa_safi.R;
import com.sc.mtaa_safi.SystemUtils.PrefUtils;
import com.sc.mtaa_safi.database.Contract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.jar.JarException;

public class FeedAdapter extends RecyclerViewCursorAdapter<FeedAdapter.ViewHolder> {
    ArrayList<Integer> upvoteList;
    AQuery aq;
    private Gson gson = new Gson();

    public FeedAdapter(Context context, Cursor cursor) {
        super(context, cursor);
        aq = new AQuery(context);
        upvoteList = new ArrayList<Integer>();
    }

    @Override
    public FeedAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.feed_card, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, Cursor c) {
        holder.mTitleView.setText(c.getString(c.getColumnIndex(Contract.Entry.COLUMN_CONTENT)));
        holder.mLocation.setText(c.getString(c.getColumnIndex(Contract.Entry.COLUMN_HUMAN_LOC)));
        holder.mVoteButton.mServerId = c.getInt(c.getColumnIndex(Contract.Entry.COLUMN_SERVER_ID));
        holder.mVoteButton.mReportUri = Report.getUri(c.getInt(c.getColumnIndex(Contract.Entry.COLUMN_ID)));
        holder.mVoteButton.setCheckedState(c.getInt(c.getColumnIndex(Contract.Entry.COLUMN_USER_UPVOTED)) > 0,
                c.getInt(c.getColumnIndex(Contract.Entry.COLUMN_UPVOTE_COUNT)), upvoteList);

        ArrayList<String> imagesJson = gson.fromJson(c.getString(c.getColumnIndex(Contract.Entry.COLUMN_MEDIA)), new TypeToken<ArrayList<String>>() {
        }.getType());
        String imageUrl = "";
        try {
            if (imagesJson != null && !imagesJson.get(0).isEmpty())
                imageUrl = R.string.base_url + "get_thumbnail/" + Integer.parseInt(imagesJson.get(0)) + "/76x76";
        } catch (NumberFormatException e) {
            imageUrl = imagesJson.get(0);
        }
        
        aq.id(holder.mLeadImage).image(imageUrl);
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
