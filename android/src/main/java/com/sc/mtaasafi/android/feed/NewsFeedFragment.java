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
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;

import com.sc.mtaasafi.android.R;
import com.sc.mtaasafi.android.Report;
import com.sc.mtaasafi.android.database.Contract;
import com.sc.mtaasafi.android.database.SyncUtils;

public class NewsFeedFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    ReportSelectedListener mCallback;
    public final static String  SORT_RECENT = Contract.Entry.COLUMN_SERVER_ID + " DESC",
                                SORT_UPVOTES = Contract.Entry.COLUMN_UPVOTE_COUNT + " DESC",
                                SORT_KEY = "sorting";
    int index, top;
    public NewsFeedFragment() {}
        
    public interface ReportSelectedListener { public void goToDetailView(Report r, int position); }
    
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
    }

   // @Override
   // public void onListItemClick(ListView l, View view, int position, long id) {
   //     super.onListItemClick(l, view, position, id);
   //     Report r = new Report((Cursor) mAdapter.getItem(position));
   //     mCallback.goToDetailView(r, position);
   // }

    @Override
    public void onResume(){
        super.onResume();
        // restore default ordering
        sortFeed(SORT_RECENT);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        SyncUtils.CreateSyncAccount(activity);
        try { // This makes sure that the container activity has implemented the callback interface.
            mCallback = (ReportSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement ReportSelectedListener");
        }
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
    public void sortFeed(String sorting){
        Bundle args = new Bundle();
        args.putString(SORT_KEY, sorting);
//        NewsFeedFragment nff = ((MainActivity) getActivity()).getNewsFeedFragment();
//        nff.getLoaderManager().restartLoader(0, args, nff);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
//        String sortOrder = null;
//        if(args != null)
//                sortOrder = args.getString(SORT_KEY);
//        if(sortOrder == null) // default is by time TODO: sort by epoch time
//            sortOrder = Contract.Entry.COLUMN_SERVER_ID + " DESC";
//        Log.e("Sort order: ", sortOrder);
        String selection = Contract.Entry.COLUMN_PENDINGFLAG  + " < " + 0;
        return new CursorLoader(getActivity(), Contract.Entry.CONTENT_URI,
            Report.PROJECTION, selection, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        Log.e("Feed Cursor", "My count is " + cursor.getCount());
        ((FeedAdapter) mAdapter).changeCursor(cursor);
        View view = getView();
        if (view != null) {
            SwipeRefreshLayout refreshLayout =
                    (SwipeRefreshLayout) view.findViewById(R.id.swipeRefresh);
            if(refreshLayout.isRefreshing())
            // refresh --> content displayed chronologically
                ((MainActivity) getActivity()).getSupportActionBar().setSelectedNavigationItem(0);
            refreshLayout.setRefreshing(false);
            if(cursor.getCount()==0)
                view.findViewById(R.id.refreshNotice).setVisibility(View.VISIBLE);
            else
                view.findViewById(R.id.refreshNotice).setVisibility(View.GONE);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        ((FeedAdapter) mAdapter).changeCursor(null);
    }
}
