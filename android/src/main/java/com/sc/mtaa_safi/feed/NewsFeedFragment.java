package com.sc.mtaa_safi.feed;

import android.accounts.Account;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.SyncStatusObserver;
import android.database.Cursor;
import android.os.Bundle;
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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.github.ksoichiro.android.observablescrollview.ObservableRecyclerView;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ScrollState;
import com.sc.mtaa_safi.R;
import com.sc.mtaa_safi.Report;
import com.sc.mtaa_safi.SystemUtils.NetworkUtils;
import com.sc.mtaa_safi.SystemUtils.Utils;
import com.sc.mtaa_safi.database.AuthenticatorService;
import com.sc.mtaa_safi.database.Contract;
import com.sc.mtaa_safi.database.SyncUtils;

public class NewsFeedFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor>, SwipeRefreshLayout.OnRefreshListener, ObservableScrollViewCallbacks, Feed.ResortListener {

    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private Object mSyncObserverHandle;
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
        createNavDrawer(view);
        createFeedRecycler(view);
    }

    private void createNavDrawer(View view) {
        NavigationDrawer mDrawer = (NavigationDrawer) view.findViewById(R.id.drawer_layout);
        mDrawer.setupDrawer((Toolbar) view.findViewById(R.id.main_toolbar), this);
        if (mFeed.navPos != 0)
            mDrawer.selectNavItem(mFeed.navPos);
    }

    private void createFeedRecycler(View view) {
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recycle);
        recyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);
        ((ObservableRecyclerView) recyclerView).setScrollViewCallbacks(this);

        mAdapter = new FeedAdapter(getActivity(), null);
        recyclerView.setAdapter(mAdapter);
        getLoaderManager().initLoader(FEED_LOADER, null, this);
    }

    private Toolbar createToolbar(View view) {
        Toolbar toolbar = (Toolbar) view.findViewById(R.id.main_toolbar);
        ((MainActivity) getActivity()).setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_menu);
        ((TextView) view.findViewById(R.id.title)).setText(mFeed.title);
        addSortSpinner(view);
        return toolbar;
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

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        SyncUtils.CreateSyncAccount(activity);
    }
    @Override
    public void onResume() {
        super.onResume();
        mSyncStatusObserver.onStatusChanged(0);
        final int mask = ContentResolver.SYNC_OBSERVER_TYPE_PENDING | ContentResolver.SYNC_OBSERVER_TYPE_ACTIVE;
        mSyncObserverHandle = ContentResolver.addStatusChangeListener(mask, mSyncStatusObserver);
    }
    @Override
    public void onPause() {
        super.onPause();
        if (mSyncObserverHandle != null) {
            ContentResolver.removeStatusChangeListener(mSyncObserverHandle);
            mSyncObserverHandle = null;
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

    public void triggerReload() {
        getLoaderManager().restartLoader(FEED_LOADER, null, this);
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
            ((FeedAdapter) mAdapter).swapCursor(cursor);
            View view = getView();
            if (view != null) {
                ((SwipeRefreshLayout) view.findViewById(R.id.swipeRefresh)).setRefreshing(false);
                if (cursor.getCount() == 0)
                    setEmptyState(view);
                else
                    view.findViewById(R.id.empty_state).setVisibility(View.GONE);
            }
        } else
            placeAdapter.swapCursor(cursor);
    }
    @Override
    public void onLoaderReset(Loader<Cursor> loader) { 
        if (loader.getId() == FEED_LOADER)
            ((FeedAdapter) mAdapter).swapCursor(null);
        else
            placeAdapter.swapCursor(null);
    }

    private void setEmptyState(View view) {
        String error = Utils.getFeedError(getActivity());
        if (!error.isEmpty() && Utils.getSelectedAdminId(getActivity()) == -1 && !mFeed.feedContent.equals(Feed.LOAD_USER)) {
            view.findViewById(R.id.empty_refresh).setVisibility(View.GONE);
            view.findViewById(R.id.empty_nearby).setVisibility(View.VISIBLE);
        } else {
            view.findViewById(R.id.empty_refresh).setVisibility(View.VISIBLE);
            view.findViewById(R.id.empty_nearby).setVisibility(View.GONE);
        }
        view.findViewById(R.id.empty_state).setVisibility(View.VISIBLE);
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

    @Override
    public void onRefresh() {
        Activity act = getActivity();
        if (((MainActivity) act).hasCoarseLocation()) {
            if (NetworkUtils.isOnline(act))
                SyncUtils.AttemptRefresh(act);
            else
                refreshFailed(R.string.refresh_network_fail);
        } else
            refreshFailed(R.string.refresh_loc_fail);
    }
    public void refreshFailed(int reasonId) {
        View view = getView();
        if (view != null) {
            final LinearLayout refreshFailed = (LinearLayout) view.findViewById(R.id.refresh_failed_bar);
            ((TextView) refreshFailed.findViewById(R.id.failText)).setText(getResources().getString(reasonId));
            Animation out = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_out_top);
            out.setStartOffset(3000);
            out.setAnimationListener(new Animation.AnimationListener() {
                @Override public void onAnimationStart(Animation animation) {}
                @Override public void onAnimationRepeat(Animation animation) {}
                @Override public void onAnimationEnd(Animation animation) {
                    refreshFailed.setVisibility(View.GONE);
                }
            });
            refreshFailed.startAnimation(out);
            refreshFailed.setVisibility(View.VISIBLE);
            setRefreshState(false);
        }
    }
    public void setRefreshState(boolean refreshing) {
        View view = getView();
        if (view != null) {
            ((SwipeRefreshLayout) view.findViewById(R.id.swipeRefresh)).setRefreshing(refreshing);
            if (!refreshing && mAdapter.getItemCount() == 0)
                setEmptyState(view);
        }
    }
    private SyncStatusObserver mSyncStatusObserver = new SyncStatusObserver() {
        @Override /** Callback invoked with the sync adapter status changes. */
        public void onStatusChanged(int which) {
        getActivity().runOnUiThread(new Runnable() {
        @Override
        public void run() {
            Account account = AuthenticatorService.GetAccount();
            boolean syncActive = ContentResolver.isSyncActive(account, Contract.CONTENT_AUTHORITY);
            boolean syncPending = ContentResolver.isSyncPending(account, Contract.CONTENT_AUTHORITY);
            setRefreshState(syncActive || syncPending);
        }
        });
        }
    };
}