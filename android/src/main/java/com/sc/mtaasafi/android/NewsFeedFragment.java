package com.sc.mtaasafi.android;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.androidquery.AQuery;
import com.sc.mtaasafi.android.adapter.FeedAdapter;

import java.util.List;

public class NewsFeedFragment extends ListFragment
        implements LoaderManager.LoaderCallbacks<List<Report>> {

    FeedAdapter mAdapter;
    ReportSelectedListener mCallback;
    MainActivity mActivity;
    AQuery aq;
    int index;
    int top;

    public interface ReportSelectedListener {
        public void goToDetailView(Report report);
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        index = top = 0;
        super.onCreate(savedInstanceState);
        aq = new AQuery(getActivity());
        mActivity = (MainActivity) getActivity();
        mAdapter = new FeedAdapter(mActivity, this);
        setListAdapter(mAdapter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_news_feed, container, false);
        RelativeLayout mLayout = (RelativeLayout) view.findViewById(R.id.news_feed);
        mLayout.setPadding(0, mActivity.getActionBarHeight(), 0, 0);
        if (savedInstanceState != null) {
            index = savedInstanceState.getInt("index");
            top = savedInstanceState.getInt("top");
        }
        refreshFeed();
        return view;
    }

    @Override public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onListItemClick(ListView l, View view, int position, long id) {
        super.onListItemClick(l, view, position, id);
        Report r = mAdapter.mReports.get(position);
        mCallback.goToDetailView(r);
    }

    @Override
    public void onResume(){
        super.onResume();
        restoreListPosition();
    }

    public void saveListPosition() {
        index = getListView().getFirstVisiblePosition();
        View v = getListView().getChildAt(0);
        top = (v == null) ? 0 : v.getTop();
        Bundle bundle = new Bundle();
        bundle.putInt("index", index);
        bundle.putInt("top", top);
        onSaveInstanceState(bundle);
    }

    public void restoreListPosition() {
        getListView().setSelectionFromTop(index, top);
    }

    public void onPause() {
        super.onPause();
        saveListPosition();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try { // This makes sure that the container activity has implemented the callback interface.
            mCallback = (ReportSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement ReportSelectedListener");
        }
    }
    public void refreshFeed(){
        getLoaderManager().initLoader(0, null, this).forceLoad();
    }

    @Override
    public Loader<List<Report>> onCreateLoader(int id, Bundle args) {
        return new NewsFeedLoader(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<List<Report>> loader, List<Report> data) {
        mAdapter.updateItems(data);
    }

    @Override
    public void onLoaderReset(Loader<List<Report>> loader) {
    }
}
