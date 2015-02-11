package com.sc.mtaa_safi.feed;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.GravityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v4.widget.SwipeRefreshLayout;
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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.sc.mtaa_safi.R;
import com.sc.mtaa_safi.Report;
import com.sc.mtaa_safi.SystemUtils.NetworkUtils;
import com.sc.mtaa_safi.SystemUtils.Utils;
import com.sc.mtaa_safi.database.Contract;
import com.sc.mtaa_safi.database.SyncUtils;

public class NewsFeedFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor>, SwipeRefreshLayout.OnRefreshListener  {

    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    public final static String  SORT_RECENT = Contract.Entry.COLUMN_SERVER_ID + " DESC",
                                SORT_UPVOTES = Contract.Entry.COLUMN_UPVOTE_COUNT + " DESC";
    private final int PLACES_LOADER = 0, FEED_LOADER = 1;
    int index, top, navIndex = 0;
    String sortOrder = SORT_RECENT;

    SimpleCursorAdapter placeAdapter;
    private DrawerLayout mDrawerLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        index = top = 0;
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_feed, container, false);
        getActivity().setTitle(Utils.getSelectedAdminName(getActivity()));

        if (savedInstanceState != null) {
            index = savedInstanceState.getInt("index");
            top = savedInstanceState.getInt("top");
            sortOrder = savedInstanceState.getString("sortOrder");
            navIndex = savedInstanceState.getInt("navIndex");
        }
        createToolbar(view);

        SwipeRefreshLayout refreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefresh);
        refreshLayout.setOnRefreshListener(this);
        refreshLayout.setColorSchemeResources(R.color.Coral, R.color.mtaa_safi_blue);
        return view;
    }

    private void createToolbar(View view) {
        Toolbar toolbar = (Toolbar) view.findViewById(R.id.main_toolbar);
        ((MainActivity) getActivity()).setSupportActionBar(toolbar);
        addSortSpinner(view);

        mDrawerLayout = (DrawerLayout) view.findViewById(R.id.drawer_layout);
        ListView drawerList = (ListView) view.findViewById(R.id.location_list);
        placeAdapter = new SimpleCursorAdapter(getActivity(), R.layout.drawer_list_item, 
            null, new String[] { Contract.Admin.COLUMN_NAME }, new int[] { R.id.place_name }, 0);
        drawerList.setAdapter(placeAdapter);
        drawerList.setOnItemClickListener(new DrawerItemClickListener());
        view.findViewById(R.id.nearby).setOnClickListener(new StaticItemClickListener());
        getLoaderManager().initLoader(PLACES_LOADER, null, this);
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override public void onItemClick(AdapterView parent, View view, int pos, long id) {
            setFeedToLocation((String) ((TextView) view).getText(), id);
        }
    }
    private class StaticItemClickListener implements View.OnClickListener {
        @Override public void onClick(View v) {
            setFeedToLocation(getResources().getString(R.string.nearby), -1);
        }
    }
    public void setFeedToLocation(String name, long id) {
        Utils.saveSelectedAdmin(getActivity(), name, id);
        ((SwipeRefreshLayout) getView().findViewById(R.id.swipeRefresh)).setRefreshing(true);
        getActivity().setTitle(name);
        mDrawerLayout.closeDrawer(GravityCompat.END);
        attemptRefresh(getActivity());
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(getView().findViewById(R.id.right_drawer));
        menu.findItem(R.id.choose_location).setVisible(!drawerOpen);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recycle);
        recyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new FeedAdapter(getActivity(), null);
        recyclerView.setAdapter(mAdapter);
        getLoaderManager().initLoader(FEED_LOADER, null, this);
    }

    @Override
    public void onSaveInstanceState(Bundle outstate){
        super.onSaveInstanceState(outstate);
        outstate.putInt("top", top);
        outstate.putInt("index", index);
        outstate.putString("sortOrder", sortOrder);
        outstate.putInt("navIndex", navIndex);
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
                        ((SwipeRefreshLayout) getView().findViewById(R.id.swipeRefresh)).setRefreshing(false);
                    }
                }, 2000);
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

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id == PLACES_LOADER)
            return new CursorLoader(getActivity(), Contract.Admin.ADMIN_URI,
                    new String[] { Contract.Admin.COLUMN_SERVER_ID, Contract.Admin.COLUMN_NAME },
                    null, null, Contract.Admin.COLUMN_NAME + " ASC");
        return new CursorLoader(getActivity(), Contract.Entry.CONTENT_URI,
                Report.PROJECTION, Contract.Entry.COLUMN_PENDINGFLAG  + " < " + 0, null, sortOrder);
    }
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (loader.getId() == FEED_LOADER)
            feedLoaded(cursor);
        else
            placeAdapter.swapCursor(cursor);
    }
    private void feedLoaded(Cursor cursor) {
        ((FeedAdapter) mAdapter).swapCursor(cursor);
        View view = getView();
        if (view != null) {
            Log.e("News feed", "Has view");
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
                R.array.feed_sort_choices, android.R.layout.simple_spinner_dropdown_item);
        spin.setAdapter(mSpinnerAdapter);
        spin.setSelection(navIndex);
        spin.setOnItemSelectedListener(new SortListener());
    }
    public void sortFeed(String sorting) {
        if (sorting != sortOrder) {
            sortOrder = sorting;
            getLoaderManager().restartLoader(FEED_LOADER, null, this);
        }
    }
    private class SortListener implements AdapterView.OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            navIndex = pos;
            if (pos == 0) sortFeed(SORT_RECENT);
            else sortFeed(SORT_UPVOTES);
        }
        public void onNothingSelected(AdapterView<?> parent) { }
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
            case R.id.choose_location: mDrawerLayout.openDrawer(GravityCompat.END); return true;
            case R.id.upload: act.uploadSavedReports(); return true;
            default: return super.onOptionsItemSelected(item);
        }
    }

    private void updateUploadAction(Menu menu) {
        int drawable = 0;
        switch (getSavedReportCount(getActivity())) {
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

    public static int getSavedReportCount(Activity ac){
        String[] projection = new String[1];
        projection[0] = Contract.Entry.COLUMN_ID;
        Cursor c = ac.getContentResolver().query(Contract.Entry.CONTENT_URI,
                projection, Contract.Entry.COLUMN_PENDINGFLAG + " >= 0 ", null, null);
        int count = c.getCount();
        c.close();
        return count;
    }
}
