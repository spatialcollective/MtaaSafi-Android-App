package com.sc.mtaasafi.android.feed;

import android.accounts.Account;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.SyncStatusObserver;
import android.database.Cursor;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.sc.mtaasafi.android.R;
import com.sc.mtaasafi.android.Report;
import com.sc.mtaasafi.android.SystemUtils.ComplexPreferences;
import com.sc.mtaasafi.android.SystemUtils.PrefUtils;
import com.sc.mtaasafi.android.database.AuthenticatorService;
import com.sc.mtaasafi.android.database.ReportContract;
import com.sc.mtaasafi.android.database.SyncUtils;
import com.sc.mtaasafi.android.newReport.NewReportActivity;

import java.util.List;

public class NewsFeedFragment extends ListFragment
        implements LoaderManager.LoaderCallbacks<Cursor> {

    SimpleCursorAdapter mAdapter;
    private Object mSyncObserverHandle;

    ReportSelectedListener mCallback;
    int index;
    int top;
    private final static int SAVED_REPORT_BUTTON_ID = 100;
    public String[] PROJECTION = new String[] {
        ReportContract.Entry._ID,
        ReportContract.Entry.COLUMN_TITLE,
        ReportContract.Entry.COLUMN_DETAILS,
        ReportContract.Entry.COLUMN_TIMESTAMP,
        ReportContract.Entry.COLUMN_LAT,
        ReportContract.Entry.COLUMN_LNG,
        ReportContract.Entry.COLUMN_USERNAME,
        ReportContract.Entry.COLUMN_MEDIAURL1,
        ReportContract.Entry.COLUMN_MEDIAURL2,
        ReportContract.Entry.COLUMN_MEDIAURL3
    };
    public String[] FROM_COLUMNS = new String[] {
        ReportContract.Entry.COLUMN_TITLE,
        ReportContract.Entry.COLUMN_DETAILS,
        ReportContract.Entry.COLUMN_TIMESTAMP,
        ReportContract.Entry.COLUMN_LAT,
        ReportContract.Entry.COLUMN_LNG
    };
    private static final int[] TO_FIELDS = new int[] {
        R.id.itemDetails,
        R.id.itemTitle,
        R.id.timeElapsed,
        R.id.itemDistance,
        R.id.itemDistance
    };

    public NewsFeedFragment() {}
        
    public interface ReportSelectedListener {
        public void goToDetailView(Cursor c, int position);
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        index = top = 0;
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_news_feed, container, false);
        LinearLayout feedLL = (LinearLayout) view.findViewById(R.id.feedLL);
        feedLL.setPadding(0, ((MainActivity) getActivity()).getActionBarHeight(), 0, 0);
        view.findViewById(R.id.savedReportsButton).setOnClickListener(new View.OnClickListener(){
            @Override
            public  void onClick(View view){
                ((MainActivity) getActivity()).uploadSavedReports();
            }
        });
        if (savedInstanceState != null) {
             index = savedInstanceState.getInt("index");
             top = savedInstanceState.getInt("top");
        }
        return view;
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mAdapter = new SimpleCursorAdapter(getActivity(), R.layout.feed_item_view,
            null, FROM_COLUMNS, TO_FIELDS, 0);
        mAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Cursor cursor, int i) {
                if (i == cursor.getColumnIndex(ReportContract.Entry.COLUMN_TIMESTAMP))
                    ((TextView)view).setText(Report.getElapsedTime(cursor.getString(i)));
                else if (i == cursor.getColumnIndex(ReportContract.Entry.COLUMN_LNG)){
                    Location reportLocation = new Location("ReportLocation");
                    reportLocation.setLatitude(Double.parseDouble(cursor.getString(i-1)));
                    reportLocation.setLongitude(Double.parseDouble(cursor.getString(i)));
                    Location currentLocation = ((MainActivity)getActivity()).getLocation();
                    if(currentLocation != null){
                        float distInMeters = reportLocation.distanceTo(currentLocation);
                        Log.i("View Binder", "Distance in m: " + distInMeters);
                        String distText;
                        if(distInMeters > 1000)
                            distText = Float.toString(distInMeters/1000).substring(0, 2) + "km";
                        else
                            distText = Float.toString(distInMeters).substring(0, 2) + "m";
                        distText.replaceAll("\\."+0, "");
                        ((TextView)view).setText(distText + " km");
                    }
                } else
                    return false;
                return true;
            }
        });
        setListAdapter(mAdapter);
        getLoaderManager().initLoader(0, null, this);
    }

    @Override public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

   @Override
   public void onListItemClick(ListView l, View view, int position, long id) {
       super.onListItemClick(l, view, position, id);
       Cursor c = (Cursor) mAdapter.getItem(position);
       mCallback.goToDetailView(c, position);
   }

    @Override
    public void onResume(){
        super.onResume();
        ComplexPreferences cp = PrefUtils.getPrefs(getActivity());
        View view = getView();
        int savedReports = NewReportActivity.getSavedReportCount(getActivity());
        if (savedReports > 0){
            Button sendSavedReports = (Button) view.findViewById(R.id.savedReportsButton);
            sendSavedReports.setVisibility(View.VISIBLE);
            String buttonText = "Send " + savedReports + " saved report";
            if(savedReports > 1)
                buttonText += "s";
            sendSavedReports.setText(buttonText);
        }
        else
            view.findViewById(R.id.savedReportsButton).setVisibility(View.GONE);
        mSyncStatusObserver.onStatusChanged(0);
        final int mask = ContentResolver.SYNC_OBSERVER_TYPE_PENDING |
                ContentResolver.SYNC_OBSERVER_TYPE_ACTIVE;
        mSyncObserverHandle = ContentResolver.addStatusChangeListener(mask, mSyncStatusObserver);
        // restoreListPosition();
    }
    @Override
    public void onPause() {
        super.onPause();
        if (mSyncObserverHandle != null) {
            ContentResolver.removeStatusChangeListener(mSyncObserverHandle);
            mSyncObserverHandle = null;
        }
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
    public void startRefresh(){
        if(getView() != null){
            getView().findViewById(R.id.refreshingFeedView).setVisibility(View.VISIBLE);
        }
    }
    public void endRefresh(){
        if(getView() != null){
            getView().findViewById(R.id.refreshingFeedView).setVisibility(View.GONE);
        }
    }
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        startRefresh();
        return new CursorLoader(getActivity(), ReportContract.Entry.CONTENT_URI,
            PROJECTION, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        endRefresh();
        mAdapter.changeCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.changeCursor(null);
    }

    private SyncStatusObserver mSyncStatusObserver = new SyncStatusObserver() {
        @Override
        public void onStatusChanged(int which) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("GETing", "Begin network synchronization in frag");
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
