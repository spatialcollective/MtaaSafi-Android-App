package com.sc.mtaasafi.android.uploading;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

import com.androidquery.AQuery;
import com.sc.mtaasafi.android.R;
import com.sc.mtaasafi.android.Report;
import com.sc.mtaasafi.android.database.Contract;

public class ReportUploadingFragment extends ListFragment
        implements LoaderManager.LoaderCallbacks<Cursor> {

    ReportUploader uploader;
    SimpleUploadingCursorAdapter mAdapter;

    public static final int SHOW_CANCEL = 0, SHOW_RETRY = 1, HIDE_CANCEL = -1;
    private int pendingReportCount = -1, 
                mColor = R.color.mtaa_safi_blue, 
                mBtnState = SHOW_CANCEL,
                inProgressIndex = 0; // human readable index (starts @ 1)
    private String mText = "Uploading...";

    public String[] LIST_FROM_COLUMNS = new String[] {
        Contract.Entry.COLUMN_MEDIAURL1,
        Contract.Entry.COLUMN_MEDIAURL2,
        Contract.Entry.COLUMN_MEDIAURL3,
        Contract.Entry.COLUMN_CONTENT,
        Contract.Entry.COLUMN_TIMESTAMP,
        Contract.Entry.COLUMN_PENDINGFLAG,
        Contract.Entry.COLUMN_ID
    };
    private static final int[] LIST_TO_FIELDS = new int[] {
        R.id.uploadingPic1,
        R.id.uploadingPic2,
        R.id.uploadingPic3,
        R.id.uploadingContent,
        R.id.uploadingTime,
        R.id.upload_row,
        R.id.deleteReportButton
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

        mAdapter = new SimpleUploadingCursorAdapter(getActivity(), R.layout.upload_item_v2,
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

    public class ViewBinder implements SimpleCursorAdapter.ViewBinder {
        @Override
        public boolean setViewValue(View view, Cursor cursor, int i) {
            // Bug identified: index of COLUMN_ID == 0. i is never 0.
            Log.e("Binder", "Index:" + i + ". Index of ID: " + cursor.getColumnIndex(Contract.Entry.COLUMN_ID));
            if (i == cursor.getColumnIndex(Contract.Entry.COLUMN_TIMESTAMP))
                ((TextView) view).setText(Report.getElapsedTime(cursor.getString(i)));
            else if (i == cursor.getColumnIndex(Contract.Entry.COLUMN_PENDINGFLAG))
                mAdapter.updateProgressView(cursor.getInt(i), view);
            else if(i == cursor.getColumnIndex(Contract.Entry.COLUMN_MEDIAURL1)
                    || i == cursor.getColumnIndex(Contract.Entry.COLUMN_MEDIAURL2)
                    || i == cursor.getColumnIndex(Contract.Entry.COLUMN_MEDIAURL3))
                view.setTag(cursor.getString(i));
            else if(i == cursor.getColumnIndex(Contract.Entry.COLUMN_ID)){
                view.setTag(cursor.getInt(i));
                Log.e("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^Delete report tag", "Tag: " + view.getTag());
            } else
                return false;
            return true;
        }
    }

    private void beamUpFirstReport() {
        if ((uploader == null || uploader.isCancelled()) && mAdapter != null && mAdapter.getCount() > 0)
            beamUpReport(new Report((Cursor) mAdapter.getItem(0)));
        else if (mAdapter.getCount() == 0)
            exitSmoothly();
    }

    private void beamUpReport(Report pendingReport) {
        Log.e("RUF", "Beam up report has been called!");
        if (!((UploadingActivity) getActivity()).isOnline() && getView() != null) {
            changeHeader("You must be online to upload.", R.color.DarkRed, HIDE_CANCEL);
            return;
        }
        inProgressIndex++;
        if (getView() != null)
            changeHeader("Uploading " + inProgressIndex + " of " + pendingReportCount,
                    R.color.mtaa_safi_blue, SHOW_CANCEL);
        uploader = new ReportUploader(getActivity(), pendingReport, this);
        uploader.execute();
    }

    private void exitSmoothly() {
        AlphaAnimation anim = new AlphaAnimation(1.0f, 1.0f);
        anim.setDuration(1500);
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}
            @Override
            public void onAnimationEnd(Animation animation) {
                getActivity().finish();
            }
            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
        getView().findViewById(R.id.uploadingText).startAnimation(anim);
    }

    public void reportUploadSuccess() {
        changeHeader("Report uploaded successfully!", R.color.mtaa_safi_blue, HIDE_CANCEL);
        uploader = null;
        if (mAdapter.getCount() > 0)
            beamUpFirstReport();
        else if (getView() != null)
            changeHeader("Successfully uploaded " + pendingReportCount + " reports.",
                    R.color.mtaa_safi_blue, HIDE_CANCEL);
    }

    public void onReportDeleted(boolean isUploading){
        if(isUploading && uploader != null)
            uploader.deleteReport();
    }
    public void onPendingReportDeleted(){
        beamUpFirstReport();
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
        if (btnState == HIDE_CANCEL) {
            cancelBtn.setClickable(false);
            cancelBtn.setAlpha(0f);
        } else if (btnState == SHOW_CANCEL) {
            cancelBtn.setClickable(true);
            cancelBtn.setAlpha(1.0f);
            cancelBtn.setImageResource(R.drawable.cancel_upload_button);
            cancelBtn.setTag("cancel");
        } else if (btnState == SHOW_RETRY) {
            cancelBtn.setClickable(true);
            cancelBtn.setAlpha(1.0f);
            cancelBtn.setImageResource(R.drawable.restart_upload_button);
            cancelBtn.setTag("restart");
        }
    }

    private void cancelSession(View view) {
        // tell the adapter to tell the uploader to stop. Once it stops, update the view
        changeHeader("Cancelling...", R.color.DarkGray, HIDE_CANCEL);
        Log.e("cancel session", "cancelling!!");
        if(uploader != null)
            uploader.cancelSession();
        else
            onSessionCancelled();
    }

    public void onSessionCancelled() {
        Log.e("cancel session", "session was cancelled!!");
        changeHeader("Upload Cancelled", R.color.Crimson, SHOW_RETRY);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String sort = null;
        if(getArguments() != null){
            String order = getArguments().getString("ORDER");
            if(order != null && order.equals("descending")){
               sort = Contract.Entry.COLUMN_ID + " DESC";
            }
        }
        return new CursorLoader(getActivity(), Contract.Entry.CONTENT_URI,
            Report.PROJECTION, Contract.Entry.COLUMN_PENDINGFLAG + " >= 0 ", null, sort);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mAdapter.changeCursor(cursor);
        if (pendingReportCount == -1)
            pendingReportCount = mAdapter.getCount();
        boolean shouldAutoStart = uploader == null || uploader.canceller.equals(uploader.DELETE_BUTTON);
        boolean reportsLeft = pendingReportCount > 0 && pendingReportCount > inProgressIndex;
        if (reportsLeft && shouldAutoStart)
            beamUpFirstReport();
    }
    @Override
    public void onLoaderReset(Loader<Cursor> loader) { mAdapter.changeCursor(null); }
}
