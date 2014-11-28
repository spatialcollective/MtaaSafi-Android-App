package com.sc.mtaasafi.android.feed;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sc.mtaasafi.android.R;
import com.sc.mtaasafi.android.Report;
import com.sc.mtaasafi.android.database.Contract;
import com.sc.mtaasafi.android.database.SyncUtils;

public class NewsFeedFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    ImageButton newReport;
    public final static String  SORT_RECENT = Contract.Entry.COLUMN_SERVER_ID + " DESC",
                                SORT_UPVOTES = Contract.Entry.COLUMN_UPVOTE_COUNT + " DESC";
    int index, top;
    String sortOrder = SORT_RECENT;
    public NewsFeedFragment() {}
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        index = top = 0;
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_feed, container, false);
        if (savedInstanceState != null) {
            index = savedInstanceState.getInt("index");
            top = savedInstanceState.getInt("top");
        }
        SwipeRefreshLayout refreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefresh);
        refreshLayout.setOnRefreshListener((MainActivity) getActivity());
        refreshLayout.setColorSchemeResources(R.color.Coral,
                                                R.color.White,
                                                R.color.Coral,
                                                R.color.White);
        return view;
    }
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycle);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new FeedAdapter(getActivity(), null);
        mRecyclerView.setAdapter(mAdapter);
        getLoaderManager().initLoader(0, null, this);
        setUpNewReportButton();
    }

    private void setUpNewReportButton(){
        newReport = (ImageButton) getView().findViewById(R.id.newReportButton);
        newReport.getLayoutParams().width = ((MainActivity) getActivity()).getScreenWidth()/4;
        newReport.getLayoutParams().height = ((MainActivity) getActivity()).getScreenWidth()/4;
        newReport.requestLayout();
        newReport.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                ((ViewGroup) v.getParent()).setClipChildren(false);
                if(event.getAction() == MotionEvent.ACTION_DOWN){
                    Animation scaleUp = new ScaleAnimation(1.0f, 1.2f, 1.0f, 1.2f,
                            Animation.RELATIVE_TO_SELF, .5f,
                            Animation.RELATIVE_TO_SELF, .5f);
                    scaleUp.setDuration(200);
                    scaleUp.setInterpolator(new AccelerateInterpolator());
                    v.startAnimation(scaleUp);

                } else {
                    Animation scaleDown = new ScaleAnimation(1.2f, 1.0f, 1.2f, 1.0f,
                            Animation.RELATIVE_TO_SELF,.5f,
                            Animation.RELATIVE_TO_SELF, .5f);
                    scaleDown.setDuration(201);
                    scaleDown.setInterpolator(new AccelerateInterpolator());
                    v.startAnimation(scaleDown);
                }
                return false;
            }
        });
    }
    @Override
    public void onSaveInstanceState(Bundle outstate){
        super.onSaveInstanceState(outstate);
        outstate.putInt("top", top);
        outstate.putInt("index", index);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        SyncUtils.CreateSyncAccount(activity);
    }

    public void refreshFailed(){
        View view = getView();
        if(view != null){
            ((SwipeRefreshLayout) view.findViewById(R.id.swipeRefresh)).setRefreshing(false);
            final LinearLayout refreshFailed = (LinearLayout) view.findViewById(R.id.refresh_failed_bar);
            Animation out = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_out_top);
            out.setStartOffset(1500);
            out.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {}
                @Override
                public void onAnimationEnd(Animation animation) {
                    refreshFailed.setVisibility(View.GONE);
                }
                @Override
                public void onAnimationRepeat(Animation animation) {}
            });
            refreshFailed.startAnimation(out);
            refreshFailed.setVisibility(View.VISIBLE);
        }
    }
    public void sortFeed(String sorting) {
        if (sorting != sortOrder) {
            sortOrder = sorting;
            getLoaderManager().restartLoader(0, null, this);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(), Contract.Entry.CONTENT_URI,
            Report.PROJECTION, Contract.Entry.COLUMN_PENDINGFLAG  + " < " + 0, null, sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        Log.e("Feed Cursor", "My count is " + cursor.getCount());
        ((FeedAdapter) mAdapter).swapCursor(cursor);
        View view = getView();
        if (view != null) {
            SwipeRefreshLayout refreshLayout =
                    (SwipeRefreshLayout) view.findViewById(R.id.swipeRefresh);
            if (refreshLayout.isRefreshing()) // refresh --> content displayed chronologically
                ((MainActivity) getActivity()).getSupportActionBar().setSelectedNavigationItem(0);
            refreshLayout.setRefreshing(false);
            if (cursor.getCount()==0)
                view.findViewById(R.id.refreshNotice).setVisibility(View.VISIBLE);
            else
                view.findViewById(R.id.refreshNotice).setVisibility(View.GONE);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) { ((FeedAdapter) mAdapter).swapCursor(null); }
}
