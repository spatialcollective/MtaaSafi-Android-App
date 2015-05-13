package com.sc.mtaa_safi.feed;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.github.ksoichiro.android.observablescrollview.ObservableRecyclerView;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ScrollState;
import com.sc.mtaa_safi.Feed;
import com.sc.mtaa_safi.R;
import com.sc.mtaa_safi.Report;
import com.sc.mtaa_safi.SystemUtils.NetworkUtils;
import com.sc.mtaa_safi.SystemUtils.Utils;
import com.sc.mtaa_safi.database.Contract;
import com.sc.mtaa_safi.database.SyncUtils;

public class NewsFeedFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor>, SwipeRefreshLayout.OnRefreshListener, ObservableScrollViewCallbacks, Feed.ResortListener {

    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    public final int PLACES_LOADER = 0, FEED_LOADER = 1;
    SimpleCursorAdapter placeAdapter;
    private Feed mFeed;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        mFeed = Feed.getInstance(getActivity());
        mFeed.setListener(this);
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_feed, container, false);
        SwipeRefreshLayout refreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefresh);
        refreshLayout.setOnRefreshListener(this);
        refreshLayout.setColorSchemeResources(R.color.Coral, R.color.mtaa_safi_blue);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        createToolbar(view);

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recycle);
        recyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);
        ((ObservableRecyclerView) recyclerView).setScrollViewCallbacks(this);

        mAdapter = new FeedAdapter(getActivity(), null);
        recyclerView.setAdapter(mAdapter);
        getLoaderManager().initLoader(FEED_LOADER, null, this);

        NavigationDrawer mDrawer = (NavigationDrawer) view.findViewById(R.id.drawer_layout);
        mDrawer.setupDrawer((Toolbar) view.findViewById(R.id.main_toolbar), this);
        if (mFeed.navPos != 0)
             mDrawer.selectNavItem(mFeed.navPos);
    }

    private Toolbar createToolbar(View view) {
        Toolbar toolbar = (Toolbar) view.findViewById(R.id.main_toolbar);
        ((MainActivity) getActivity()).setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_menu);
        ((TextView) view.findViewById(R.id.title)).setText(mFeed.title);
        addSortSpinner(view);
        return toolbar;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        SyncUtils.CreateSyncAccount(activity);
    }

    @Override
    public void onRefresh() {
        Activity act = getActivity();
        Location loc = ((MainActivity) act).getLocation();
        if (loc != null) {
            Utils.saveLocation(act, loc);
            attemptRefresh(act);
        } else
            refreshFailed();
    }

    private void attemptRefresh(Context c) {
        if (NetworkUtils.isOnline(c)) {
            SyncUtils.TriggerRefresh();
            if (getView() != null) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                    if (getView() != null)
                        ((SwipeRefreshLayout) getView().findViewById(R.id.swipeRefresh)).setRefreshing(false);
                    }
                }, 8000);
            }
        }
    }

    public void refreshFailed(){
        View view = getView();
        if (view != null) {
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

    @Override public void onScrollChanged(int scrollY, boolean firstScroll, boolean dragging) { }
    @Override public void onDownMotionEvent() { }
    @Override
    public void onUpOrCancelMotionEvent(ScrollState scrollState) {
        ActionBar ab = ((MainActivity) getActivity()).getSupportActionBar();
        if (scrollState == ScrollState.UP && ab.isShowing())
            ab.hide();
        else if (scrollState == ScrollState.DOWN && !ab.isShowing())
            ab.show();
    }

    public void setFeedToLocation(String name, long id) {
        Utils.saveSelectedAdmin(getActivity(), name, id);
        mFeed.setTitle(name, getView());
        getLoaderManager().restartLoader(FEED_LOADER, null, this);
        attemptRefresh(getActivity());
        ((SwipeRefreshLayout) getView().findViewById(R.id.swipeRefresh)).setRefreshing(true);
        refreshMessage(R.string.pull_refresh, true);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id == PLACES_LOADER)
            return new CursorLoader(getActivity(), Contract.Admin.ADMIN_URI,
                    new String[] { Contract.Admin.COLUMN_SERVER_ID, Contract.Admin.COLUMN_NAME },
                    null, null, Contract.Admin.COLUMN_NAME + " ASC");
        return new CursorLoader(getActivity(), Contract.Entry.CONTENT_URI,
                Report.PROJECTION, mFeed.feedContent, null, mFeed.sortOrder);
    }
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (loader.getId() == FEED_LOADER) {
            feedLoaded(cursor);
//            if (cursor.getCount() == 0)
//                refreshMessage(R.string.sorry_nothing, false);
        } else
            placeAdapter.swapCursor(cursor);
    }
    private void feedLoaded(Cursor cursor) {
        ((FeedAdapter) mAdapter).swapCursor(cursor);
        View view = getView();
        if (view != null) {
            SwipeRefreshLayout refreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefresh);
            refreshLayout.setRefreshing(false);
            if (cursor.getCount() == 0)
                view.findViewById(R.id.refreshNotice).setVisibility(View.VISIBLE);
            else
                view.findViewById(R.id.refreshNotice).setVisibility(View.GONE);
        }
    }
    @Override
    public void onLoaderReset(Loader<Cursor> loader) { 
        if (loader.getId() == FEED_LOADER)
            ((FeedAdapter) mAdapter).swapCursor(null);
        else
            placeAdapter.swapCursor(null);
    }

    private void addSortSpinner(View v) {
        Spinner spin = ((Spinner) v.findViewById(R.id.feed_sorter));
        ArrayAdapter mSpinnerAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.feed_sort_choices, R.layout.spinner_header);
        mSpinnerAdapter.setDropDownViewResource(R.layout.spinner_item);
        spin.setAdapter(mSpinnerAdapter);
        spin.setSelection(mFeed.navIndex);
        spin.setOnItemSelectedListener(mFeed.new SortListener());
    }
    public void triggerResort() {
        getLoaderManager().restartLoader(FEED_LOADER, null, this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main_activity, menu);
        updateUploadAction(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        MainActivity act = (MainActivity) getActivity();
        switch (item.getItemId()) {
            case R.id.upload: act.uploadSavedReports(); return true;
            default: return super.onOptionsItemSelected(item);
        }
    }

    private void updateUploadAction(Menu menu) {
        int drawable = 0;
        switch (Utils.getSavedReportCount(getActivity())) {
            case 0: menu.findItem(R.id.upload).setVisible(false); break;
            case 1: drawable = R.drawable.button_uploadsaved1; break;
            case 2: drawable = R.drawable.button_uploadsaved2; break;
            case 3: drawable = R.drawable.button_uploadsaved3; break;
            case 4: drawable = R.drawable.button_uploadsaved4; break;
            case 5: drawable = R.drawable.button_uploadsaved5; break;
            case 6: drawable = R.drawable.button_uploadsaved6; break;
            case 7: drawable = R.drawable.button_uploadsaved7; break;
            case 8: drawable = R.drawable.button_uploadsaved8; break;
            case 9: drawable = R.drawable.button_uploadsaved9; break;
            default: drawable = R.drawable.button_uploadsaved9plus;
        }
        if (drawable != 0) {
            menu.findItem(R.id.upload).setIcon(drawable);
            menu.findItem(R.id.upload).setVisible(true);
        }
    }

    private void refreshMessage(int messageId, Boolean showArrow){
        final ImageView imageView = (ImageView) getView().findViewById(R.id.doneButton);
        if (showArrow)
            imageView.setVisibility(View.VISIBLE);
        else
            imageView.setVisibility(View.INVISIBLE);
        final TextView textView = (TextView) getView().findViewById(R.id.pullDownText);
        textView.setText(getString(messageId));
    }
}
