package com.sc.mtaasafi.android;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.MultiAutoCompleteTextView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.androidquery.AQuery;
import com.androidquery.util.Progress;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.model.GraphObject;
import com.sc.mtaasafi.android.adapter.FeedAdapter;

import java.util.List;

public class NewsFeedFragment extends ListFragment {
    private ProgressBar progressBar;
    FeedAdapter mAdapter;
    RelativeLayout mLayout;
    ReportSelectedListener mCallback;
    MainActivity mActivity;
    AQuery aq;
    int index;
    int top;
    
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        aq = new AQuery(getActivity());
        mActivity = (MainActivity) getActivity();
        mAdapter = new FeedAdapter(mActivity, this);
        setListAdapter(mAdapter);
        index = top = 0;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_news_feed, container, false);
        mLayout = (RelativeLayout) view.findViewById(R.id.news_feed);
        mLayout.setPadding(0, mActivity.getActionBarHeight(), 0, 0);

        progressBar = (ProgressBar) view.findViewById(R.id.feedProgress);
        if(savedInstanceState !=null){
            index = savedInstanceState.getInt("index");
            top = savedInstanceState.getInt("top");
        }
        updateFeed();
        return view;
    }

    @Override
    public void onListItemClick(ListView l, View view, int position, long id) {
        super.onListItemClick(l, view, position, id);
//        Log.e(LogTags.FEEDADAPTER, "CLICKED FEED ITEM!!!!");
        Report r = mAdapter.mReports.get(position);
        mCallback.goToDetailView(r);
    }

    public void updateFeed(){
        // TODO: setup conditional that only turns on progressBar if there are new posts from server
        progressBar.setVisibility(View.VISIBLE);
        mActivity.updateFeed(this);
    }
    public interface ReportSelectedListener {
        public void goToDetailView(Report report);
    }

    private void onSessionStateChange(Session session, SessionState state, Exception exception) {
        if (state.isOpened()) {
        } else if (state.isClosed()) {
        }
    }

    @Override
    public void onResume(){
        super.onResume();

        restoreListPosition();
        Session session = Session.getActiveSession();
        if (session != null && (session.isOpened() || session.isClosed())){
            onSessionStateChange(session, session.getState(), null);
        }
    }

    public void onFeedUpdate(List<Report> allReports){
        progressBar.setVisibility(View.INVISIBLE);
        mAdapter.updateItems(allReports);
    }

    public void alertFeedUpdate(){
        mAdapter.notifyDataSetChanged();
    }

    public void saveListPosition(){
        index = getListView().getFirstVisiblePosition();
        View v = getListView().getChildAt(0);
        top = (v == null) ? 0 : v.getTop();
        Bundle bundle = new Bundle();
        bundle.putInt("index", index);
        bundle.putInt("top", top);
        onSaveInstanceState(bundle);
    }

    public void restoreListPosition(){
        getListView().setSelectionFromTop(index, top);
    }
    public void onPause(){
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
}
