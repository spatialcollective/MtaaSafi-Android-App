package com.sc.mtaa_safi.feed;

import android.app.ActionBar;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.sc.mtaa_safi.R;
import com.sc.mtaa_safi.Report;
import com.sc.mtaa_safi.SystemUtils.NetworkUtils;
import com.sc.mtaa_safi.database.Contract;
import com.sc.mtaa_safi.feed.comments.Comment;
import com.sc.mtaa_safi.feed.comments.CommentAdapter;
import com.sc.mtaa_safi.feed.comments.CommentLayoutManager;
import com.sc.mtaa_safi.feed.comments.NewCommentLayout;
import com.sc.mtaa_safi.feed.comments.SyncComments;

import java.text.SimpleDateFormat;


public class ReportDetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    Report mReport;
    CommentAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
    }

    public void setData(Report r) { mReport = r; }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedState) {
        View view = inflater.inflate(R.layout.fragment_report_detail, container, false);
        setUpHeader(view);
        if (savedState != null)
            mReport = new Report(savedState);
        if (NetworkUtils.isOnline(getActivity()))
            new SyncComments(getActivity(), mReport.serverId).execute();
        return view;
    }

    private void setUpHeader(View view) {
        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        ((MainActivity) getActivity()).setSupportActionBar(toolbar);
        ((MainActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setDataInViews(view);
    }

    private void setDataInViews(View view) {
        updateVote((VoteButton) view.findViewById(R.id.topVote));
        updateDetails(view.findViewById(R.id.top_layout));
        addComments(view);
    }

    private void updateDetails(View view) {
        ((TextView) view.findViewById(R.id.r_meta)).setText(mReport.userName + "  ·  " + createHumanReadableTimestamp());
        ((TextView) view.findViewById(R.id.r_content)).setText(mReport.content);
        ((TextView) view.findViewById(R.id.itemLocation)).setText(mReport.locationDescript);

        int width = ((MainActivity) getActivity()).getScreenWidth();
        int height = ((MainActivity) getActivity()).getScreenHeight()/2;
        AQuery aq = new AQuery(getActivity());
        String imageUrl = getActivity().getString(R.string.base_url) + "get_thumbnail/" + mReport.media.get(0) + "/" + width;
        ImageView iv = (ImageView) view.findViewById(R.id.leadImage);
        aq.id(iv).image(imageUrl).animate(R.anim.abc_fade_in);
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
        return new SimpleDateFormat("MMM dd' '''yy' at 'HH:mm")
                .format(new java.util.Date(mReport.timeStamp));
    }

    private void addComments(View view) {
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.comments);
        recyclerView.setLayoutManager(new CommentLayoutManager(getActivity()));

        mAdapter = new CommentAdapter(getActivity(), null);
        recyclerView.setAdapter(mAdapter);
        getLoaderManager().initLoader(0, null, this);

        ((NewCommentLayout) view.findViewById(R.id.new_comment_standalone)).addData(mReport.serverId);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(), Contract.Comments.COMMENTS_URI,
            Comment.PROJECTION, Comment.getSelection(mReport.serverId), null, Comment.DEFAULT_SORT);
    }
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mAdapter.swapCursor(cursor);
        ((NewCommentLayout) getView().findViewById(R.id.new_comment_standalone)).requestLayout();
    }
    @Override public void onLoaderReset(Loader<Cursor> loader) {}

// ==========================   Images   ===============================
    private void setUpViewPager(View view) {
//        viewPager = (ViewPager) view.findViewById(R.id.viewpager);
//        viewPager.setAdapter(new ImageSlideAdapter(getChildFragmentManager(), mReport.media.toArray(new String[mReport.media.size()])));
//        viewPager.setOffscreenPageLimit(3);
//        viewPager.setPageTransformer(true, new ZoomOutPageTransformer());
    }

//    private class ImageSlideAdapter extends FragmentPagerAdapter {
//        String[] mediaPaths;
//        public ImageSlideAdapter(FragmentManager fm, String[] mediaPaths) {
//            super(fm);
//            this.mediaPaths = mediaPaths;
//        }
//        @Override
//        public int getCount() { return mediaPaths.length; }
//        @Override
//        public Fragment getItem(int i) {
//            ImageFragment iF = new ImageFragment();
//            Bundle args = new Bundle();
//            args.putString("mediaPath", mediaPaths[i]);
//            iF.setArguments(args);
//            return iF;
//        }
//    }
}
