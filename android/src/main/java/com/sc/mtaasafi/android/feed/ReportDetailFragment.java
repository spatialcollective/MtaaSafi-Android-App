package com.sc.mtaasafi.android.feed;

import android.database.Cursor;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.sc.mtaasafi.android.Report;
import com.sc.mtaasafi.android.R;
import com.sc.mtaasafi.android.database.Contract;

import java.text.SimpleDateFormat;


public class ReportDetailFragment extends android.support.v4.app.Fragment {

    private String content, location, time, user, mediaUrl1, mediaUrl2, mediaUrl3, distance;
    private int upvoteCount, serverId, dbId;
    private Location reportLocation;
    private boolean userVoted;
    public AQuery aq;
    public ViewPager viewPager;
    RelativeLayout bottomView;
    LinearLayout topView;
    private static String content_Key ="content",
        location_Key = "location",
        time_Key = "time",
        user_Key = "user",
        mediaUrl1_Key = "url1",
        mediaUrl2_Key = "url2",
        mediaUrl3_Key = "url3",
        serverId_Key = "serverId",
        dbId_Key = "dbId",
        lat_Key = "lat",
        lon_Key = "lon",
        userVoted_Key = "userVoted",
        upvoteCount_Key = "upvoteCt";

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        aq = new AQuery(getActivity());
    }

    public void setData(Cursor c) {
        content = c.getString(c.getColumnIndex(Contract.Entry.COLUMN_CONTENT));
        location = c.getString(c.getColumnIndex(Contract.Entry.COLUMN_LOCATION));
        time = getSimpleTimeStamp(c.getString(c.getColumnIndex(Contract.Entry.COLUMN_TIMESTAMP)));
        user = c.getString(c.getColumnIndex(Contract.Entry.COLUMN_USERNAME));
        if(user.equals(""))
            user = "Unknown user";
        mediaUrl1 = c.getString(c.getColumnIndex(Contract.Entry.COLUMN_MEDIAURL1));
        mediaUrl2 = c.getString(c.getColumnIndex(Contract.Entry.COLUMN_MEDIAURL2));
        mediaUrl3 = c.getString(c.getColumnIndex(Contract.Entry.COLUMN_MEDIAURL3));
        serverId = c.getInt(c.getColumnIndex(Contract.Entry.COLUMN_SERVER_ID));
        dbId = c.getInt(c.getColumnIndex(Contract.Entry.COLUMN_ID));
        userVoted = c.getInt(c.getColumnIndex(Contract.Entry.COLUMN_USER_UPVOTED)) > 0;
        upvoteCount = c.getInt(c.getColumnIndex(Contract.Entry.COLUMN_UPVOTE_COUNT));
        String latString = c.getString(c.getColumnIndex(Contract.Entry.COLUMN_LAT));
        String lonString = c.getString(c.getColumnIndex(Contract.Entry.COLUMN_LNG));
        reportLocation = new Location("report location");
        reportLocation.setLatitude(Double.parseDouble(latString));
        reportLocation.setLongitude(Double.parseDouble(lonString));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedState) {
        if(savedState != null)
            restoreMe(savedState);
        Location currentLocation = ((MainActivity) getActivity()).getLocation();
        if(currentLocation != null){
            distance = Report.getDistanceText(currentLocation, reportLocation);
        } else
            distance = "error";
        View view = inflater.inflate(R.layout.fragment_report_detail, container, false);
        return view;
    }
    
    private void restoreMe(Bundle instate){
        content = instate.getString(content_Key);
        location = instate.getString(location_Key);
        time = instate.getString(time_Key);
        user = instate.getString(user_Key);
        mediaUrl1 = instate.getString(mediaUrl1_Key);
        mediaUrl2 = instate.getString(mediaUrl2_Key);
        mediaUrl3 = instate.getString(mediaUrl3_Key);
        serverId = instate.getInt(serverId_Key);
        dbId = instate.getInt(dbId_Key);
        double lat = instate.getDouble(lat_Key);
        double lon = instate.getDouble(lon_Key);
        reportLocation = new Location("report_location");
        reportLocation.setLatitude(lat);
        reportLocation.setLongitude(lon);
        userVoted = instate.getBoolean(userVoted_Key);
        upvoteCount = instate.getInt(upvoteCount_Key);
    }
    @Override
    public void onSaveInstanceState(Bundle outstate){
        super.onSaveInstanceState(outstate);
        if(viewPager.getVisibility() == View.VISIBLE)
            updateTopVote();
        else
            updateBottomVote();
        outstate.putString(content_Key, content);
        outstate.putString(location_Key, location);
        outstate.putString(time_Key,time);
        outstate.putString(user_Key, user);
        outstate.putString(mediaUrl1_Key, mediaUrl1);
        outstate.putString(mediaUrl2_Key, mediaUrl2);
        outstate.putString(mediaUrl3_Key, mediaUrl3);
        outstate.putInt(serverId_Key, serverId);
        outstate.putInt(dbId_Key, dbId);
        outstate.putDouble(lat_Key, reportLocation.getLatitude());
        outstate.putDouble(lon_Key, reportLocation.getLongitude());
        outstate.putBoolean(userVoted_Key, userVoted);
        outstate.putInt(upvoteCount_Key, upvoteCount);
    }
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){
        topView = (LinearLayout) view.findViewById(R.id.top_layout);
        viewPager = (ViewPager) view.findViewById(R.id.viewpager);
        String[] mediaUrls ={mediaUrl1, mediaUrl2, mediaUrl3};
        viewPager.setAdapter(new ImageSlideAdapter(getChildFragmentManager(), mediaUrls));
        viewPager.setOffscreenPageLimit(3);
        viewPager.setPageTransformer(true, new ZoomOutPageTransformer());
        setClickListeners(view);
        updateView(view);
    }
    private void setClickListeners(View view) {
        view.findViewById(R.id.media1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                enterImageViewer(0);
            }
        });
        view.findViewById(R.id.media2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                enterImageViewer(1);
            }
        });
        view.findViewById(R.id.media3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                enterImageViewer(2);
            }
        });
    }

    private void enterImageViewer(int i){
        updateBottomVote();
        getActivity().getActionBar().hide();
        Animation slide_in_bottom = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_in_bottom);
        getView().setBackgroundColor(getResources().getColor(R.color.DarkSlateGray));
        viewPager.setCurrentItem(i);
        viewPager.setVisibility(View.VISIBLE);
        bottomView.setVisibility(View.VISIBLE);
        bottomView.startAnimation(slide_in_bottom);
        topView.setVisibility(View.INVISIBLE);
    }
    public void exitImageViewer(){
        getView().setBackgroundColor(getResources().getColor(R.color.White));
        updateTopVote();
        topView.setVisibility(View.VISIBLE);
        viewPager.setVisibility(View.INVISIBLE);
        Animation slide_out_bottom = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_out_bottom);
        bottomView.startAnimation(slide_out_bottom);
        bottomView.setVisibility(View.INVISIBLE);
        getActivity().getActionBar().show();
    }
    public void updateView(View view) {
        VoteInterface topVote = (VoteInterface) view.findViewById(R.id.topVote);
        topVote.updateData(upvoteCount, userVoted, dbId, serverId);
        ((TextView) view.findViewById(R.id.reportViewContent)).setText(content);
        ((TextView) view.findViewById(R.id.itemLocation)).setText(location);
        ((TextView) view.findViewById(R.id.itemDistance)).setText(distance);
        ((TextView) view.findViewById(R.id.reportViewTimeStamp)).setText(time);
        ((TextView) view.findViewById(R.id.reportViewUsername)).setText(user);

        bottomView = (RelativeLayout) view.findViewById(R.id.report_BottomView);
        updateBottomView(bottomView);
        int mediaHeight = ((MainActivity) getActivity()).getScreenHeight()/4;
        ImageView media1 = (ImageView) view.findViewById(R.id.media1);
        ImageView media2 = (ImageView) view.findViewById(R.id.media2);
        ImageView media3 = (ImageView) view.findViewById(R.id.media3);
        media1.getLayoutParams().height = mediaHeight;
        media1.requestLayout();
        media2.getLayoutParams().height = mediaHeight;
        media2.requestLayout();
        media3.getLayoutParams().height = mediaHeight;
        media3.requestLayout();
        aq.id(media1).image(mediaUrl1);
        aq.id(media2).image(mediaUrl2);
        aq.id(media3).image(mediaUrl3);
    }
    private void updateBottomView(View view){
        String bottomUserDisplay;
        if(user.length() > 16){
            bottomUserDisplay = user.substring(0, 14) + "...";
        } else
            bottomUserDisplay = user.toString();
        ((TextView) view.findViewById(R.id.bottomContent)).setText(content);
        ((TextView) view.findViewById(R.id.bottomUsername)).setText(bottomUserDisplay);
        ((TextView) view.findViewById(R.id.bottomTimestamp)).setText(getSimpleTimeStamp(time));
        ((TextView) view.findViewById(R.id.itemLocation)).setText(location);
        ((TextView) view.findViewById(R.id.itemDistance)).setText(distance);
        updateBottomVote();
    }
    private void updateTopVote(){
        VoteInterface bottomVote = (VoteInterface) bottomView.findViewById(R.id.bottomVote);
        VoteInterface topVote = (VoteInterface) getView().findViewById(R.id.topVote);
        userVoted = bottomVote.userVoted;
        upvoteCount = bottomVote.voteCount;
        topVote.updateData(bottomVote.voteCount, bottomVote.userVoted, dbId, serverId);
    }
    // called upon inflation as well as whenever the viewpager is about to become visible
    private void updateBottomVote(){
        VoteInterface bottomVote = (VoteInterface) bottomView.findViewById(R.id.bottomVote);
        bottomVote.setBottomMode();
        VoteInterface topVote = (VoteInterface) getView().findViewById(R.id.topVote);
        userVoted = topVote.userVoted;
        upvoteCount = topVote.voteCount;
        bottomVote.updateData(topVote.voteCount, topVote.userVoted, dbId, serverId);
    }
    private String getSimpleTimeStamp(String timestamp) {
        SimpleDateFormat fromFormat = new SimpleDateFormat("H:mm:ss dd-MM-yyyy");
        SimpleDateFormat displayFormat = new SimpleDateFormat("K:mm a  d MMM yy");
        try {
            return displayFormat.format(fromFormat.parse(timestamp));
        } catch (Exception e) {
            return timestamp;
        }
    }

    private class ImageSlideAdapter extends FragmentPagerAdapter {
        String[] mediaPaths;
        public ImageSlideAdapter(FragmentManager fm, String[] mediaPaths) {
            super(fm);
            this.mediaPaths = mediaPaths;
        }
        @Override
        public int getCount() {return mediaPaths.length;}
        @Override
        public Fragment getItem(int i) {
            ImageFragment iF = new ImageFragment();
            Bundle args = new Bundle();
            args.putString("mediaPath", mediaPaths[i]);
            iF.setArguments(args);
            return iF;
        }

    }
}
