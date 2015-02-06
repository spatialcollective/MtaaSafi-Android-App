package com.sc.mtaa_safi.feed;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.DrawerLayout;
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

import com.sc.mtaa_safi.R;
import com.sc.mtaa_safi.Report;
import com.sc.mtaa_safi.database.Contract;
import com.sc.mtaa_safi.database.SyncUtils;
import com.sc.mtaa_safi.uploading.UploadingActivity;

public class NewsFeedFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    public final static String  SORT_RECENT = Contract.Entry.COLUMN_SERVER_ID + " DESC",
                                SORT_UPVOTES = Contract.Entry.COLUMN_UPVOTE_COUNT + " DESC";
    int index, top, navIndex = 0;
    String sortOrder = SORT_RECENT;

    String[] mPlaceTitles;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private CharSequence mDrawerTitle = "Choose a Location to View";
    private CharSequence mTitle;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        index = top = 0;
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_feed, container, false);

        if (savedInstanceState != null) {
            index = savedInstanceState.getInt("index");
            top = savedInstanceState.getInt("top");
            sortOrder = savedInstanceState.getString("sortOrder");
            navIndex = savedInstanceState.getInt("navIndex");
        }
        createToolbar(view);

        SwipeRefreshLayout refreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefresh);
        refreshLayout.setOnRefreshListener((MainActivity) getActivity());
        refreshLayout.setColorSchemeResources(R.color.Coral, R.color.White, R.color.mtaa_safi_blue);
        return view;
    }

    private void createToolbar(View view) {
        Toolbar toolbar = (Toolbar) view.findViewById(R.id.main_toolbar);
        ((MainActivity) getActivity()).setSupportActionBar(toolbar);
        ((MainActivity) getActivity()).getSupportActionBar().setDisplayShowTitleEnabled(false);
        addSortSpinner(view);

        mPlaceTitles = getResources().getStringArray(R.array.dummy_places);
        ListView mDrawerList = (ListView) view.findViewById(R.id.right_drawer);

        mDrawerList.setAdapter(new ArrayAdapter<String>(getActivity(), R.layout.drawer_list_item, mPlaceTitles));
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.drawable.ic_drawer, R.string.drawer_open, R.string.drawer_close) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                getActionBar().setTitle(mTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getActionBar().setTitle(mDrawerTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };

        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {
            selectItem(view, position);
        }
    }

    private void selectItem(View view, int position) {
        // Highlight the selected item, update the title, and close the drawer
        ListView drawerList = (ListView) view.findViewById(R.id.right_drawer);
        drawerList.setItemChecked(position, true);
        getActivity().setTitle(mPlaceTitles[position]);
        ((DrawerLayout) view.findViewById(R.id.drawer_layout)).closeDrawer(view.findViewById(R.id.right_drawer));
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
    public void onCreateOptionsMenu(
            Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main_activity, menu);
        addUploadAction(menu);
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

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(), Contract.Entry.CONTENT_URI,
            Report.PROJECTION, Contract.Entry.COLUMN_PENDINGFLAG  + " < " + 0, null, sortOrder);
    }
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        Log.i("Feed Cursor", "My count is " + cursor.getCount());
        ((FeedAdapter) mAdapter).swapCursor(cursor);
        View view = getView();
        if (view != null) {
            SwipeRefreshLayout refreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefresh);
//            if (refreshLayout.isRefreshing()) // refresh --> content displayed chronologically
//                ((Spinner) v.findViewById(R.id.feed_sorter)).setSelection(0);
            refreshLayout.setRefreshing(false);
            if (cursor.getCount() == 0)
                view.findViewById(R.id.refreshNotice).setVisibility(View.VISIBLE);
            else
                view.findViewById(R.id.refreshNotice).setVisibility(View.GONE);
        }
    }
    @Override
    public void onLoaderReset(Loader<Cursor> loader) { ((FeedAdapter) mAdapter).swapCursor(null); }

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
            getLoaderManager().restartLoader(0, null, this);
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

    private void addUploadAction(Menu menu){
        int savedReportCt = getSavedReportCount(getActivity());
        int drawable = 0;
        switch (savedReportCt) {
            case 1: drawable = R.drawable.button_uploadsaved1; break;
            case 2: drawable = R.drawable.button_uploadsaved2; break;
            case 3: drawable = R.drawable.button_uploadsaved3; break;
            case 4: drawable = R.drawable.button_uploadsaved4; break;
            case 5: drawable = R.drawable.button_uploadsaved5; break;
            case 6: drawable = R.drawable.button_uploadsaved6; break;
            case 7: drawable = R.drawable.button_uploadsaved7; break;
            case 8: drawable = R.drawable.button_uploadsaved8; break;
            case 9: drawable = R.drawable.button_uploadsaved9; break;
            default:
                if (savedReportCt > 9)
                    drawable = R.drawable.button_uploadsaved9plus;
        }
        if (drawable != 0)
            menu.add(0, 0, 0, "Upload Saved Reports").setIcon(drawable)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
    }

    public static int getSavedReportCount(Activity ac){
        String[] projection = new String[1];
        projection[0] = Contract.Entry.COLUMN_ID;
        Cursor c = ac.getContentResolver().query(
                Contract.Entry.CONTENT_URI,
                projection,
                Contract.Entry.COLUMN_PENDINGFLAG + " >= 0 ", null, null);
        int count = c.getCount();
        c.close();
        return count;
    }

}
