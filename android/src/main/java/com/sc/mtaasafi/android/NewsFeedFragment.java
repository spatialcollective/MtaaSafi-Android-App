package com.sc.mtaasafi.android;

import android.accounts.Account;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.SyncStatusObserver;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.MultiAutoCompleteTextView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.androidquery.AQuery;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.model.GraphObject;

import java.util.List;

public class NewsFeedFragment extends ListFragment
        implements LoaderManager.LoaderCallbacks<Cursor> {

    SimpleCursorAdapter mAdapter;
    private Object mSyncObserverHandle;
    ListView mListView;
    private Menu mOptionsMenu;

    private ProgressBar progressBar;
    ReportSelectedListener mCallback;
    MainActivity mActivity;
    AQuery aq;
    int index;
    int top;

    public String[] PROJECTION = new String[] {
        ReportContract.Entry._ID,
        ReportContract.Entry.COLUMN_TITLE,
        ReportContract.Entry.COLUMN_DETAILS,
        ReportContract.Entry.COLUMN_TIMESTAMP
    };
    public String[] FROM_COLUMNS = new String[] {
        ReportContract.Entry.COLUMN_TITLE,
        ReportContract.Entry.COLUMN_DETAILS,
        ReportContract.Entry.COLUMN_TIMESTAMP
    };
    private static final int[] TO_FIELDS = new int[] {
        R.id.itemTitle,
        R.id.itemDetails,
        R.id.timeElapsed
    };

    public NewsFeedFragment() {}
        
    public interface ReportSelectedListener {
        public void goToDetailView(Report report, int position);
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        index = top = 0;
        super.onCreate(savedInstanceState);
        aq = new AQuery(getActivity());
        mActivity = (MainActivity) getActivity();
        setHasOptionsMenu(true);
        // getLoaderManager().initLoader(0, null, this).forceLoad();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_news_feed, container, false);

        mListView = (ListView) view.findViewById(android.R.id.list);

        // mListView.setAdapter(mAdapter);

        // mListView = (RelativeLayout) view.findViewById(R.id.news_feed);
        // mListView.setPadding(0, mActivity.getActionBarHeight(), 0, 0);
        progressBar = (ProgressBar) view.findViewById(R.id.feedProgress);
        progressBar.setVisibility(View.VISIBLE);
        // if (savedInstanceState != null) {
        //     index = savedInstanceState.getInt("index");
        //     top = savedInstanceState.getInt("top");
        // }
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAdapter = new SimpleCursorAdapter(
            getActivity(),              // Current context
            R.layout.feed_item_view,    // Layout for individual rows
            null,                       // Cursor
            FROM_COLUMNS,               // Cursor columns to use
            TO_FIELDS,                  // Layout fields to use
            0                           // No flags
        );

//        For use in setting view values that are not straighforward (e.g. timestamp)
        mAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Cursor cursor, int i) {
                // if (i == COLUMN_PUBLISHED) {
                //     // Convert timestamp to human-readable date
                //     Time t = new Time();
                //     t.set(cursor.getLong(i));
                //     ((TextView) view).setText(t.format("%Y-%m-%d %H:%M"));
                //     return true;
                // } else {
                    // Let SimpleCursorAdapter handle other fields automatically
                    return false;
                // }
            }
        });

        setListAdapter(mAdapter);
        // setEmptyText(getText(R.string.loading));
        getLoaderManager().initLoader(0, null, this);
    }

    @Override public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

//    @Override
//    public void onListItemClick(ListView l, View view, int position, long id) {
//        super.onListItemClick(l, view, position, id);
//        Cursor c = (Cursor) mAdapter.getItem(position);
//
////        mCallback.goToDetailView(c, position);
//    }

    @Override
    public void onResume(){
        super.onResume();
        mSyncStatusObserver.onStatusChanged(0);

        // Watch for sync state changes
        final int mask = ContentResolver.SYNC_OBSERVER_TYPE_PENDING |
                ContentResolver.SYNC_OBSERVER_TYPE_ACTIVE;
        mSyncObserverHandle = ContentResolver.addStatusChangeListener(mask, mSyncStatusObserver);

        // datasource.open();
        // restoreListPosition();
    }
    @Override
    public void onPause() {
        super.onPause();
        if (mSyncObserverHandle != null) {
            ContentResolver.removeStatusChangeListener(mSyncObserverHandle);
            mSyncObserverHandle = null;
        }
        // datasource.close();
        // saveListPosition();
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // If the user clicks the "Refresh" button.
            case R.id.menu_refresh:
                SyncUtils.TriggerRefresh();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
//        return new NewsFeedLoader(getActivity());
        return new CursorLoader(
             getActivity(),   // Parent activity context
             ReportContract.Entry.CONTENT_URI,        // Table to query
             PROJECTION,     // Projection to return
             null,            // No selection clause
             null,            // No selection arguments
             null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
//        Bundle args = new Bundle();
//        for (int i = 0; i < data.size(); i++) {
//            if (data.get(i) != null)
//                datasource.createReport(data.get(i).saveState(args));
//        }
//
//        List<Report> values = datasource.getAllReports();

        // mAdapter.updateItems(data);
        mAdapter.changeCursor(cursor);
        progressBar.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.changeCursor(null);
    }

    private SyncStatusObserver mSyncStatusObserver = new SyncStatusObserver() {
        /** Callback invoked with the sync adapter status changes. */
        @Override
        public void onStatusChanged(int which) {
            getActivity().runOnUiThread(new Runnable() {
                /**
                 * The SyncAdapter runs on a background thread. To update the UI, onStatusChanged()
                 * runs on the UI thread.
                 */
                @Override
                public void run() {
                    Log.d("GETing", "Begin network synchronization in frag");
                    // Create a handle to the account that was created by
                    // SyncService.CreateSyncAccount(). This will be used to query the system to
                    // see how the sync status has changed.
                    Account account = AuthenticatorService.GetAccount();
//                    if (account == null) {
                        // GetAccount() returned an invalid value. This shouldn't happen, but
                        // we'll set the status to "not refreshing".
//                        setRefreshActionButtonState(false);
//                        return;
//                    }

                    // Test the ContentResolver to see if the sync adapter is active or pending.
                    // Set the state of the refresh button accordingly.
                    boolean syncActive = ContentResolver.isSyncActive(
                            account, ReportContract.CONTENT_AUTHORITY);
                    boolean syncPending = ContentResolver.isSyncPending(
                            account, ReportContract.CONTENT_AUTHORITY);
//                    setRefreshActionButtonState(syncActive || syncPending);
                }
            });
        }
    };
}
