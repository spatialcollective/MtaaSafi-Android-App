package com.sc.mtaa_safi.feed;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.Session;
import com.facebook.internal.SessionTracker;
import com.facebook.widget.FacebookDialog;
import com.facebook.widget.WebDialog;
import com.sc.mtaa_safi.R;
import com.sc.mtaa_safi.Report;
import com.sc.mtaa_safi.SystemUtils.NetworkUtils;
import com.sc.mtaa_safi.database.Contract;
import com.sc.mtaa_safi.feed.comments.Comment;
import com.sc.mtaa_safi.feed.comments.CommentAdapter;
import com.sc.mtaa_safi.feed.comments.CommentLayoutManager;
import com.sc.mtaa_safi.feed.comments.NewCommentLayout;
import com.sc.mtaa_safi.feed.comments.SyncComments;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;


public class ReportDetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    Report mReport;
    CommentAdapter mAdapter;
    String mCommentText;

    @Override
    public void onCreate(Bundle savedInstanceState){
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }

    public void setData(Report r) { mReport = r; }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedState) {
        View view = inflater.inflate(R.layout.fragment_report_detail, container, false);
        setUpHeader(view);
        if (savedState != null)
            mReport = new Report(savedState);
        setUpImagePager(view);
        if (NetworkUtils.isOnline(getActivity()))
            new SyncComments(getActivity(), mReport.serverId).execute();
        return view;
    }

    private void setUpHeader(View view) {
        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        ((MainActivity) getActivity()).setSupportActionBar(toolbar);
        ((MainActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationIcon(R.drawable.ic_action_back);
    }
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setDataInViews(view);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.report_detail, menu);
        MenuItem item = menu.findItem(R.id.menu_item_fb_share);
        if (!FacebookDialog.canPresentShareDialog(getActivity().getApplicationContext(),
                FacebookDialog.ShareDialogFeature.SHARE_DIALOG))
            item.setVisible(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()) {
            case R.id.menu_item_fb_share:
                createShareDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void createShareDialog(){
        FacebookDialog shareDialog = new FacebookDialog.ShareDialogBuilder(getActivity())
                .setName(mReport.content)
                .setLink(getActivity().getString(R.string.public_url)+"/mtaasafi/community/"+mReport.adminId+"/?format=json&selected="+mReport.serverId)
                .setDescription("This is in "+mReport.locationDescript)
                .setCaption("MtaaSafi")
                .setPicture(getActivity().getString(R.string.base_url) + "get_thumbnail/" + mReport.media.get(0) + "/" + 500)
                .setPlace(mReport.locationDescript)
                .build();
        shareDialog.present();
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
    }

    private void updateVote(VoteButton voter) { // (VoteButton) v.findViewById(R.id.voteInterface)
        voter.mServerId = mReport.serverId;
        voter.mReportUri = Report.getUri(mReport.dbId);
        voter.setCheckedState(mReport.upVoted, mReport.upVoteCount, null);
    }

    @Override
    public void onPause() {
        super.onPause();
        mCommentText = ((TextView) getView().findViewById(R.id.commentEditText)).getText().toString();
    }

    @Override
    public void onSaveInstanceState(Bundle outstate){
        super.onSaveInstanceState(outstate);
        if (mReport != null)
            outstate = mReport.saveState(outstate);
        if (mCommentText != null)
            outstate.putString(Contract.Comments.COLUMN_CONTENT, mCommentText);
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity) getActivity()).getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
    }
    @Override
    public void onStop() {
        super.onStop();
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
    }
    @Override public void onLoaderReset(Loader<Cursor> loader) {}

    private void setUpImagePager(View view) {
        ImagePagerAdapter ipa = new ImagePagerAdapter(mReport.media);
        ViewPager viewPager = (ViewPager) view.findViewById(R.id.image_pager);
        viewPager.setAdapter(ipa);
        viewPager.setOnPageChangeListener(new PageButtonListener(view, ipa, viewPager));
    }

    private class ImagePagerAdapter extends PagerAdapter {
        String[] mediaPaths;

        public ImagePagerAdapter(ArrayList<String> paths) {
            super();
            this.mediaPaths = paths.toArray(new String[paths.size()]);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Context c = getActivity();
            int width = ((MainActivity) c).getScreenWidth();
            int height = ((MainActivity) c).getScreenHeight()/2;
            ImageView imageView = new ImageView(c);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            String imageUrl = getActivity().getString(R.string.base_url) + "get_thumbnail/" + mediaPaths[position] + "/" + width + "x" + height;
            Picasso.with(c).load(imageUrl)
                    .placeholder(R.drawable.image_placeholder)
                    .error(R.drawable.image_error)
                    .into(imageView);
            ((ViewPager) container).addView(imageView, 0);
            return imageView;
        }

        @Override
        public int getCount() {
            return mediaPaths.length;
        }
        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == ((ImageView) object);
        }
        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            ((ViewPager) container).removeView((ImageView) object);
        }
    }

    private static class PageButtonListener extends ViewPager.SimpleOnPageChangeListener implements View.OnClickListener {
        ViewPager vp;
        View topView;
        int pageCount;

        public PageButtonListener(View view, ImagePagerAdapter adapter, ViewPager pager) {
            topView = view;
            pageCount = adapter.getCount();
            vp = pager;
            vp.setPageTransformer(true, new ZoomOutPageTransformer());
            view.findViewById(R.id.page_left).setOnClickListener(this);
            view.findViewById(R.id.page_right).setOnClickListener(this);
            view.findViewById(R.id.page_left).setVisibility(View.INVISIBLE);
            if (pageCount == 1)
                view.findViewById(R.id.page_right).setVisibility(View.INVISIBLE);
        }

        @Override
        public void onClick (View v) {
            if (v.getId() == R.id.page_left)
                vp.setCurrentItem(vp.getCurrentItem() - 1, true);
            else
                vp.setCurrentItem(vp.getCurrentItem() + 1, true);
        }

        @Override
        public void onPageSelected(int position) {
            if (position == 0)
                topView.findViewById(R.id.page_left).setVisibility(View.INVISIBLE);
            else
                topView.findViewById(R.id.page_left).setVisibility(View.VISIBLE);
            if (position == pageCount - 1)
                topView.findViewById(R.id.page_right).setVisibility(View.INVISIBLE);
            else
                topView.findViewById(R.id.page_right).setVisibility(View.VISIBLE);
        }
    }
}
