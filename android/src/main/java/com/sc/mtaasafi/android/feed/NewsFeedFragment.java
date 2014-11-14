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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.sc.mtaasafi.android.R;
import com.sc.mtaasafi.android.Report;
import com.sc.mtaasafi.android.SystemUtils.ComplexPreferences;
import com.sc.mtaasafi.android.SystemUtils.PrefUtils;
import com.sc.mtaasafi.android.database.AuthenticatorService;
import com.sc.mtaasafi.android.database.Contract;
import com.sc.mtaasafi.android.database.SyncUtils;
import com.sc.mtaasafi.android.newReport.NewReportActivity;

public class NewsFeedFragment extends ListFragment
        implements LoaderManager.LoaderCallbacks<Cursor> {

    SimpleCursorAdapter mAdapter;
    ReportSelectedListener mCallback;

    int index, top;
    public String[] FROM_COLUMNS = new String[] {
            Contract.Entry.COLUMN_ID,
            Contract.Entry.COLUMN_USER_UPVOTED,
            Contract.Entry.COLUMN_UPVOTE_COUNT,
            Contract.Entry.COLUMN_SERVER_ID,
            Contract.Entry.COLUMN_LOCATION,
            Contract.Entry.COLUMN_CONTENT,
            Contract.Entry.COLUMN_LAT,
            Contract.Entry.COLUMN_LNG
    };
    private static final int[] TO_FIELDS = new int[] {
            R.id.upvoteButton,
            R.id.voteInterface,
            R.id.upvoteCount,
            R.id.upvoteCount,
            R.id.itemLocation,
            R.id.itemTitle,
            R.id.itemDistance,
            R.id.itemDistance
    };

    public NewsFeedFragment() {}
        
    public interface ReportSelectedListener {
        public void goToDetailView(Report r, int position);
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        index = top = 0;
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_news_feed, container, false);
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
        ImageButton recentTab = (ImageButton) view.findViewById(R.id.recent_tab_button);
        recentTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // make recent tab button and underscore blue
                ((ImageButton) view).setImageResource(R.drawable.recent_posts_clicked);
                View myParent = (View) view.getParent();
                ImageView underscore = (ImageView) myParent.findViewById(R.id.recent_tab_underscore);
                underscore.setBackgroundColor(getResources().getColor(R.color.mtaa_safi_blue));
                // make the popular tab button and underscore gray
                View myGrandParent = (View) myParent.getParent();
                ImageButton otherTab = (ImageButton) myGrandParent.findViewById(R.id.popular_tab_button);
                otherTab.setImageResource(R.drawable.popular_reports_unclicked);
                ImageView otherUnderscore = (ImageView) myGrandParent.findViewById(R.id.popular_tab_underscore);
                otherUnderscore.setBackgroundColor(getResources().getColor(R.color.White));
                // sort the feed
                Bundle args = new Bundle();
                args.putString("SORT", "recent");
                NewsFeedFragment nff = ((MainActivity) view.getContext()).getNewsFeedFragment();
                nff.getLoaderManager().restartLoader(0, args, nff);
            }
        });
        ImageButton popularTab = (ImageButton) view.findViewById(R.id.popular_tab_button);
        popularTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // make popular tab button and underscore blue
                ((ImageButton) view).setImageResource(R.drawable.popular_reports_clicked);
                View myParent = (View) view.getParent();
                ImageView underscore = (ImageView) myParent.findViewById(R.id.popular_tab_underscore);
                underscore.setBackgroundColor(getResources().getColor(R.color.mtaa_safi_blue));
                // make the recent tab button and underscore gray
                View myGrandParent = (View) myParent.getParent();
                ImageButton otherTab = (ImageButton) myGrandParent.findViewById(R.id.recent_tab_button);
                otherTab.setImageResource(R.drawable.recent_posts_unclicked);
                ImageView otherUnderscore = (ImageView) myGrandParent.findViewById(R.id.recent_tab_underscore);
                otherUnderscore.setBackgroundColor(getResources().getColor(R.color.White));
                // sort the feed
                Bundle args = new Bundle();
                args.putString("SORT", "popular");
                NewsFeedFragment nff = ((MainActivity) view.getContext()).getNewsFeedFragment();
                nff.getLoaderManager().restartLoader(0, args, nff);
            }
        });

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
                if (i == cursor.getColumnIndex(Contract.Entry.COLUMN_ID))
                    view.setTag(cursor.getInt(i));
                else if (i == cursor.getColumnIndex(Contract.Entry.COLUMN_USER_UPVOTED)){
                    TextView upvoteTV = (TextView) view.findViewById(R.id.upvoteCount);
                    ImageButton upvoteButton = (ImageButton) view.findViewById(R.id.upvoteButton);
                    Log.i("BINDING userVoted", "Uservoted on this: " + cursor.getInt(i)
                        + ". Server id:" + cursor.getInt(cursor.getColumnIndex(Contract.Entry.COLUMN_SERVER_ID)));
                    VoteInterface vi = (VoteInterface) view;
                    vi.feedMode = true;
                    if(cursor.getInt(i) > 0){
                        upvoteButton.setImageResource(R.drawable.button_upvote_clicked);
                        upvoteTV.setTextColor(getResources().getColor(R.color.mtaa_safi_blue));
                    } else {
                        upvoteButton.setImageResource(R.drawable.button_upvote_unclicked);
                        upvoteTV.setTextColor(getResources().getColor(R.color.DarkGray));
                    }
                } else if(i == cursor.getColumnIndex(Contract.Entry.COLUMN_SERVER_ID)) {
                    view.setTag(cursor.getInt(i));
                } else if(i == cursor.getColumnIndex(Contract.Entry.COLUMN_UPVOTE_COUNT)){
                    ((TextView) view).setText(Integer.toString(cursor.getInt(i)));
                } else if (i == cursor.getColumnIndex(Contract.Entry.COLUMN_LNG)){ // set the distance
                    Location currentLocation = ((MainActivity) getActivity()).getLocation();
                    if(currentLocation != null){
                        Location reportLocation = new Location("ReportLocation");
                        reportLocation.setLatitude(cursor.getDouble(i-1));
                   	    reportLocation.setLongitude(cursor.getDouble(i));
                        String distText = Report.getDistanceText(currentLocation, reportLocation);
                        ((TextView)view).setText(distText);
                        if(distText.equals("here")){
                            ((TextView)view).setTextColor(getResources().getColor(R.color.Coral));
                            View parent = (View) view.getParent();
                            ((ImageView)parent.findViewById(R.id.markerIcon)).setImageResource(R.drawable.marker_coral);
                        } else{
                            ((TextView)view).setTextColor(getResources().getColor(R.color.DarkGray));
                            View parent = (View) view.getParent();
                            ((ImageView)parent.findViewById(R.id.markerIcon)).setImageResource(R.drawable.marker);
                        }
                    }
               } else
                    return false;
                return true;
            }
        });
        updateSavedReportsBtn(view);
        setListAdapter(mAdapter);
    }

   @Override
   public void onListItemClick(ListView l, View view, int position, long id) {
       super.onListItemClick(l, view, position, id);
       Report r = new Report((Cursor) mAdapter.getItem(position));
       mCallback.goToDetailView(r, position);
   }

    @Override
    public void onResume(){
        super.onResume();
        // restore default ordering
        Bundle args = new Bundle();
        args.putString("SORT", "recent");
        NewsFeedFragment nff = ((MainActivity) getActivity()).getNewsFeedFragment();
        nff.getLoaderManager().restartLoader(0, args, nff);
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
        String sortOrder = null;
        if(args != null){
            String sorting = (String) args.get("SORT");
            if(sorting.equals("popular"))
                sortOrder = Contract.Entry.COLUMN_UPVOTE_COUNT + " DESC";
        }
        if(sortOrder == null) // default is by time TODO: sort by epoch time
            sortOrder = Contract.Entry.COLUMN_SERVER_ID + " DESC";
        Log.e("Sort order: ", sortOrder);
        String selection = Contract.Entry.COLUMN_PENDINGFLAG  + " < " + 1;
        return new CursorLoader(getActivity(), Contract.Entry.CONTENT_URI,
            Report.PROJECTION, selection, null, sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        endRefresh();
        Log.e("Feed Cursor", "My count is " + cursor.getCount());
        mAdapter.changeCursor(cursor);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.changeCursor(null);
        mAdapter.notifyDataSetChanged();
    }

}
