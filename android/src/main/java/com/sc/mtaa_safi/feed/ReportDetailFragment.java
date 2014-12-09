package com.sc.mtaa_safi.feed;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.database.Cursor;
import android.graphics.Point;
import android.graphics.Rect;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.sc.mtaa_safi.Report;
import com.sc.mtaa_safi.R;
import com.sc.mtaa_safi.SystemUtils.PrefUtils;
import com.sc.mtaa_safi.database.Contract;
import com.sc.mtaa_safi.feed.comments.Comment;
import com.sc.mtaa_safi.feed.comments.CommentAdapter;
import com.sc.mtaa_safi.feed.comments.CommentsFragment;
import com.sc.mtaa_safi.feed.comments.NewCommentLayout;

import java.text.SimpleDateFormat;


public class ReportDetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    Report mReport;
    NewCommentLayout mNewComment;
    Location currentLocation;
    private LinearLayout[] latestComments;

    public AQuery aq;
    public ViewPager viewPager;

    public static final String USERNAME = "username", REFRESH_KEY= "refresh", COMMENT = "comment";

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        aq = new AQuery(getActivity());
    }
    public void setData(Report r) { mReport = r; }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedState) {
        View view = inflater.inflate(R.layout.fragment_report_detail_v2, container, false);
        if (savedState != null)
            mReport = new Report(savedState);
        currentLocation = ((MainActivity) getActivity()).getLocation();
        return view;
    }
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setUpViewPager(view);
        setDataInViews(view);
    }

    private void setDataInViews(View view) {
        updateVote((VoteButton) view.findViewById(R.id.topVote));
        updateDetails(view.findViewById(R.id.top_layout));
        addComments(view);
    }

    private void updateDetails(View view) {
        ((TextView) view.findViewById(R.id.r_username)).setText(mReport.userName);
        ((TextView) view.findViewById(R.id.r_content)).setText(mReport.content);
        ((TextView) view.findViewById(R.id.r_timestamp)).setText(createHumanReadableTimestamp());
        ((TextView) view.findViewById(R.id.itemLocation)).setText(mReport.locationDescript);
        getView().findViewById(R.id.seeMoreComments).setVisibility(View.VISIBLE);
        getView().findViewById(R.id.seeMoreComments).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CommentsFragment commentsFragment = new CommentsFragment(mReport);
                commentsFragment.show(getActivity().getSupportFragmentManager(), "COMMENTS");
            }
        });
    }

    private void updateVote(VoteButton voter) {
        voter.mServerId = mReport.serverId;
        voter.mReportUri = Report.getUri(mReport.dbId);
        voter.setCheckedState(mReport.upVoted, mReport.upVoteCount, null);
    }

    @Override
    public void onSaveInstanceState(Bundle outstate){
        super.onSaveInstanceState(outstate);
        String commentText = ((TextView) getView().findViewById(R.id.commentEditText)).getText().toString();
        if (commentText != null)
            outstate.putString(Contract.Comments.COLUMN_CONTENT, commentText);
        outstate = mReport.saveState(outstate);
    }

    @Override
    public void onResume() {
        super.onResume();
        ((ActionBarActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((MainActivity) getActivity()).getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
    }
    @Override
    public void onStop() {
        super.onStop();
        ((ActionBarActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        ((MainActivity) getActivity()).getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
    }
    private String createHumanReadableTimestamp() {
        return new SimpleDateFormat("H:mm:ss dd-MM-yyyy")
                .format(new java.util.Date(mReport.timeStamp));
    }

    private void addComments(View view) {
        mNewComment = (NewCommentLayout) view.findViewById(R.id.new_comment_bar);
        mNewComment.addData(mReport);
        latestComments = new LinearLayout[5];
        latestComments[0] = (LinearLayout) view.findViewById(R.id.comment1);
        latestComments[1] = (LinearLayout) view.findViewById(R.id.comment2);
        latestComments[2] = (LinearLayout) view.findViewById(R.id.comment3);
        latestComments[3] = (LinearLayout) view.findViewById(R.id.comment4);
        latestComments[4] = (LinearLayout) view.findViewById(R.id.comment5);
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(), Contract.Comments.COMMENTS_URI,
            Comment.PROJECTION, Comment.getSelection(mReport.serverId), null, Comment.DEFAULT_SORT);
    }
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if(cursor.getCount() > 5) {
            if (getView() != null)
                getView().findViewById(R.id.seeMoreComments).setVisibility(View.VISIBLE);
            for (int i = 0; i < cursor.getCount() - 5; i++) {
                cursor.moveToNext();
            }
        } else
            getView().findViewById(R.id.seeMoreComments).setVisibility(View.GONE);
        while(cursor.moveToNext()){
            LinearLayout comment = latestComments[cursor.getPosition() - (cursor.getCount() - 5)];
            String timeElapsed = PrefUtils.getElapsedTime(cursor.getLong(cursor.getColumnIndex(Contract.Comments.COLUMN_TIMESTAMP)));
            String commentText = cursor.getString(cursor.getColumnIndex(Contract.Comments.COLUMN_CONTENT));
            String commentUserName = cursor.getString(cursor.getColumnIndex(Contract.Comments.COLUMN_USERNAME));
            ((TextView) comment.findViewById(R.id.commentText)).setText(commentText);
            ((TextView) comment.findViewById(R.id.commentUserName)).setText(commentUserName);
            ((TextView) comment.findViewById(R.id.commentTime)).setText(timeElapsed);
            comment.setVisibility(View.VISIBLE);
        }
    }
    @Override public void onLoaderReset(Loader<Cursor> loader) {}

// ==========================   Images   ===============================
    private void setUpViewPager(View view) {
        viewPager = (ViewPager) view.findViewById(R.id.viewpager);
        viewPager.setAdapter(new ImageSlideAdapter(getChildFragmentManager(), mReport.mediaPaths.toArray(new String[mReport.mediaPaths.size()])));
        viewPager.setOffscreenPageLimit(3);
        viewPager.setPageTransformer(true, new ZoomOutPageTransformer());
    }

    private class ImageSlideAdapter extends FragmentPagerAdapter {
        String[] mediaPaths;
        public ImageSlideAdapter(FragmentManager fm, String[] mediaPaths) {
            super(fm);
            this.mediaPaths = mediaPaths;
        }
        @Override
        public int getCount() { return mediaPaths.length; }
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
