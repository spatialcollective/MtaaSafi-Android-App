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

public class NewsFeedFragment extends ListFragment
        implements LoaderManager.LoaderCallbacks<Cursor> {

    SimpleCursorAdapter mAdapter;
    ReportSelectedListener mCallback;

    int index, top;

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
        
        ((LinearLayout) view.findViewById(R.id.feedLL))
            .setPadding(0, ((MainActivity) getActivity()).getActionBarHeight(), 0, 0);

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
                        String distText;
                        if(distInMeters > 1000){
                            distText = Float.toString(distInMeters/1000);
                            if(distText.indexOf('.') !=-1) // show km within 1 dec pt
                                distText = distText.substring(0, distText.indexOf('.')+2);
                            if(distText.endsWith(".0"))// remove all that ".0" shit
                                distText = distText.substring(0, distText.length()-3);
                            distText += "km";
                        } else if(distInMeters > 30){
                            distText = Float.toString(distInMeters);
                            if(distText.indexOf('.') != -1){
                                distText = distText.substring(0, distText.indexOf('.'));
                            }
                            distText += "m";
                            // if distance is in meters meters, only show as an integer
                        } else {
                            distText = "here";
                        }
                        ((TextView)view).setText(distText);
                    }
                } else
                    return false;
                return true;
            }
        });
        updateSavedReportsBtn(view);
        setListAdapter(mAdapter);
        getLoaderManager().initLoader(0, null, this);
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
        updateSavedReportsBtn(getView());
    }
    @Override
    public void onPause() {
        super.onPause();
        // saveListPosition();
    }

    private void updateSavedReportsBtn(View view) {
        Button sendSavedReports = (Button) view.findViewById(R.id.savedReportsButton);
        int savedReportCt = NewReportActivity.getSavedReportCount(getActivity());
        if (savedReportCt > 0) {
            String buttonText = "Send " + savedReportCt + " saved report";
            if (savedReportCt > 1)
                buttonText += "s";
            sendSavedReports.setText(buttonText);
            sendSavedReports.setVisibility(View.VISIBLE);
        } else
            sendSavedReports.setVisibility(View.GONE);
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
        if (getView() != null)
            getView().findViewById(R.id.refreshingFeedView).setVisibility(View.VISIBLE);
    }
    public void endRefresh(){
        if (getView() != null)
            getView().findViewById(R.id.refreshingFeedView).setVisibility(View.GONE);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        startRefresh();
        return new CursorLoader(getActivity(), ReportContract.Entry.CONTENT_URI,
            Report.PROJECTION, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        endRefresh();
        Log.e("Feed Cursor", "My count is "+cursor.getCount());
        mAdapter.changeCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.changeCursor(null);
    }

}
