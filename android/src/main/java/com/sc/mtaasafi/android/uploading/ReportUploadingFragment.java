package com.sc.mtaasafi.android.uploading;

import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageButton;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.sc.mtaasafi.android.R;
import com.sc.mtaasafi.android.Report;
import com.sc.mtaasafi.android.database.Contract;
import com.sc.mtaasafi.android.newReport.NewReportActivity;

public class ReportUploadingFragment extends ListFragment
        implements LoaderManager.LoaderCallbacks<Cursor> {

    ReportUploader uploader;
    SimpleUploadingCursorAdapter mAdapter;

    private int pendingReportCount = -1, 
                mColor = R.color.mtaa_safi_blue, 
                mBtnState = 0,
                inProgressIndex = 0;
    private String mText = "Uploading...";
    private boolean userCancelled = false;

    public String[] LIST_FROM_COLUMNS = new String[] {
        Contract.Entry.COLUMN_CONTENT,
        Contract.Entry.COLUMN_TIMESTAMP,
        Contract.Entry.COLUMN_PENDINGFLAG
    };
    private static final int[] LIST_TO_FIELDS = new int[] {
        R.id.itemDetails,
        R.id.timeElapsed,
        R.id.expanded_layout
    };
    public ReportUploadingFragment() {}

    @Override
    public void onCreate(Bundle instate){
        super.onCreate(instate);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedState) {
        super.onCreateView(inflater, container, savedState);
        return inflater.inflate(R.layout.upload_list, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedState) {
        super.onViewCreated(view, savedState);
        changeHeader(mText, mColor, mBtnState);

        mAdapter = new SimpleUploadingCursorAdapter(getActivity(), R.layout.upload_item,
                null, LIST_FROM_COLUMNS, LIST_TO_FIELDS, 0);
        mAdapter.setViewBinder(new ViewBinder());
        setListAdapter(mAdapter);
        getLoaderManager().initLoader(0, null, this);

        final ImageButton cancelButton = (ImageButton) view.findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String tag = (String) cancelButton.getTag();
                if (tag != null) {
                    if (tag.equals("cancel"))
                        cancelSession(view);
                    else if (tag.equals("restart"))
                        beamUpFirstReport();
                }
            }
        });
    }

    private void cancelSession(View view) {
        // tell the adapter to tell the uploader to stop. Once it stops, update the view
        userCancelled = true;
        changeHeader("Cancelling...", R.color.DarkGray, -1);
        Log.e("cancel session", "cancelling!!");
        if (uploader != null)
            uploader.cancelSession();
        else
            onSessionCancelled();
    }

    public void onSessionCancelled() {
        Log.e("cancel session", "session was cancelled!!");
        changeHeader("Upload Cancelled", R.color.Crimson, 1);
    }

    public class ViewBinder implements SimpleCursorAdapter.ViewBinder {
        @Override
        public boolean setViewValue(View view, Cursor cursor, int i) {
            if (i == cursor.getColumnIndex(Contract.Entry.COLUMN_TIMESTAMP))
                ((TextView) view).setText(Report.getElapsedTime(cursor.getString(i)));
            else if (i == cursor.getColumnIndex(Contract.Entry.COLUMN_PENDINGFLAG))
                mAdapter.updateProgressView(cursor.getInt(i), view);
            else if (i == cursor.getColumnIndex(Contract.Entry.COLUMN_UPLOAD_IN_PROGRESS))
                mAdapter.indicateRow(cursor.getInt(i), view);
            else if (i == cursor.getColumnIndex(Contract.Entry.COLUMN_ID)){
//                    view.setTag(cursor.getInt(i));
                if(cursor.getCount() < 2)
                    view.setVisibility(View.INVISIBLE);
            } else
                return false;
            return true;
        }
    }

    private void beamUpFirstReport() {
        if ((uploader == null || uploader.isCancelled()) && mAdapter != null && mAdapter.getCount() > 0)
            beamUpReport(new Report((Cursor) mAdapter.getItem(0)));
        else if (mAdapter.getCount() == 0) {
            changeHeader("Nothing to upload. Returning to the feed", R.color.Coral, -1);
            exitSmoothly();
        }
    }

    private void beamUpReport(Report pendingReport) {
        userCancelled = false;
        Log.e("RUF", "Beam up report has been called!");
        if (!((UploadingActivity) getActivity()).isOnline() && getView() != null) {
            changeHeader("You must be online to upload.", R.color.DarkRed, 0);
            return;
        }
        if (getView() != null)
            changeHeader("Uploading " + inProgressIndex + " of " + pendingReportCount,
                    R.color.mtaa_safi_blue, 0);
        uploader = new ReportUploader(getActivity(), pendingReport, this);
        uploader.execute();
    }

    private void exitSmoothly() {
        AlphaAnimation anim = new AlphaAnimation(1.0f, 1.0f);
        anim.setDuration(1200);
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override public void onAnimationStart(Animation animation) {}
            @Override
            public void onAnimationEnd(Animation animation) {
                getActivity().finish();
            }
            @Override public void onAnimationRepeat(Animation animation) {}
        });
        getView().findViewById(R.id.uploadingText).startAnimation(anim);
    }

    public void reportUploadSuccess() {
        changeHeader("Report uploaded successfully!", R.color.mtaa_safi_blue, 0);
        uploader = null;
        inProgressIndex++;
        if (mAdapter.getCount() > 0)
            beamUpFirstReport();
        else if (getView() != null) {
            changeHeader("Successfully uploaded " + pendingReportCount + " reports.",
                    R.color.mtaa_safi_blue, 0);
            exitSmoothly();
        }
    }

    public void changeHeader(String message, int color, int btnState) {
        mText = message;
        mColor = color;
        mBtnState = btnState;
        View view = getView();
        if (view == null) return;
        view.findViewById(R.id.uploadingView).setVisibility(View.VISIBLE);
        ((TextView) view.findViewById(R.id.uploadingText)).setText(message);
        ((TextView) view.findViewById(R.id.uploadingText)).setTextColor(getResources().getColor(color));

        ImageButton cancelBtn = (ImageButton) getView().findViewById(R.id.cancel_button);
        if (btnState == -1) {
            cancelBtn.setClickable(false);
            cancelBtn.setAlpha(0f);
        } else if (btnState == 0) {
            cancelBtn.setClickable(true);
            cancelBtn.setAlpha(1.0f);
            cancelBtn.setImageResource(R.drawable.cancel_upload_button);
            cancelBtn.setTag("cancel");
        } else if (btnState == 1) {
            cancelBtn.setClickable(true);
            cancelBtn.setAlpha(1.0f);
            cancelBtn.setImageResource(R.drawable.restart_upload_button);
            cancelBtn.setTag("restart");
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(), Contract.Entry.CONTENT_URI,
            Report.PROJECTION, Contract.Entry.COLUMN_PENDINGFLAG + " >= 0 ", null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mAdapter.changeCursor(cursor);
        if (pendingReportCount == -1)
            pendingReportCount = mAdapter.getCount();
        if (!userCancelled && pendingReportCount > 0) {
            inProgressIndex = 1;
            beamUpFirstReport();
        }
    }
    @Override
    public void onLoaderReset(Loader<Cursor> loader) { mAdapter.changeCursor(null); }
}
