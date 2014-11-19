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
import com.sc.mtaasafi.android.newReport.NewReportActivity;

public class ReportUploadingFragment extends ListFragment
        implements LoaderManager.LoaderCallbacks<Cursor> {

    ReportUploader uploader;
    SimpleUploadingCursorAdapter mAdapter;

    public static final int SHOW_CANCEL = 0, SHOW_RETRY = 1, HIDE_CANCEL = -1;
    private int pendingReportCount = -1, 
                mColor = R.color.mtaa_safi_blue, 
                mBtnState = SHOW_CANCEL,
                inProgressIndex = 0;
    private String mText = "Uploading...";
    private AQuery aq;

    public String[] LIST_FROM_COLUMNS = new String[] {
        Contract.Entry.COLUMN_CONTENT,
        Contract.Entry.COLUMN_TIMESTAMP,
        Contract.Entry.COLUMN_PENDINGFLAG
    };
    private static final int[] LIST_TO_FIELDS = new int[] {
        R.id.uploadingContent,
        R.id.uploadingTime,
        R.id.upload_row,
    };
    public ReportUploadingFragment() {}

    @Override
    public void onCreate(Bundle instate){
        super.onCreate(instate);
        setRetainInstance(true);
        aq = new AQuery(getActivity());
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
                        cancelSession(ReportUploader.CANCEL_SESSION);
                    else if (tag.equals("restart"))
                        beamUpFirstReport();
                }
            }
        });
    }

    public class ViewBinder implements SimpleCursorAdapter.ViewBinder {
        @Override
        public boolean setViewValue(View view, Cursor cursor, int i) {
            if (i == cursor.getColumnIndex(Contract.Entry.COLUMN_TIMESTAMP))
                ((TextView) view).setText(Report.getElapsedTime(cursor.getString(i)));
            else if (i == cursor.getColumnIndex(Contract.Entry.COLUMN_PENDINGFLAG))
                mAdapter.updateProgressView(cursor.getInt(i), view);
            else if (i == cursor.getColumnIndex(Contract.Entry.COLUMN_UPLOAD_IN_PROGRESS))
                if (cursor.getInt(i) == 1)
                    mAdapter.indicateRow(view);
                else
                    mAdapter.resetRow(view);
            else
                return false;
            return true;
        }
    }

    public void beamUpFirstReport() {
        if ((uploader == null || uploader.isCancelled()) && mAdapter != null && mAdapter.getCount() > 0)
            beamUpReport(new Report((Cursor) mAdapter.getItem(0)));
        else if (mAdapter.getCount() == 0 && getView() != null)
            exitSmoothly();
        else if (mAdapter.getCount() == 0 && getActivity() != null)
            getActivity().finish();
        else if (mAdapter.getCount() == 0)
            setRetainInstance(false);
    }

    private void beamUpReport(Report pendingReport) {
        if (((UploadingActivity) getActivity()).isOnline() && getView() != null) {
            changeHeader("Uploading " + inProgressIndex + " of " + pendingReportCount,
                    R.color.mtaa_safi_blue, SHOW_CANCEL);
            uploader = new ReportUploader(getActivity(), pendingReport, this);
            uploader.execute();
        } else if (getView() != null)
            changeHeader("You must be online to upload.", R.color.DarkRed, HIDE_CANCEL);
    }

    private void exitSmoothly() {
        AlphaAnimation anim = new AlphaAnimation(1.0f, 1.0f);
        anim.setDuration(1500);
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
        changeHeader("Report uploaded successfully!", R.color.mtaa_safi_blue, HIDE_CANCEL);
        uploader = null;
        inProgressIndex++;
        if (mAdapter.getCount() > 0)
            beamUpFirstReport();
        else if (getView() != null) {
            changeHeader("Successfully uploaded " + pendingReportCount + " reports.",
                    R.color.mtaa_safi_blue, HIDE_CANCEL);
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

    public void cancelSession(int reason) {
        changeHeader("Cancelling...", R.color.DarkGray, HIDE_CANCEL);
        if (uploader != null)
            uploader.cancelSession(reason);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String sort = null;
        if (getArguments() != null) {
            String order = getArguments().getString("ORDER");
            if (order != null && order.equals("descending"))
               sort = Contract.Entry.COLUMN_ID + " DESC";
        }
        return new CursorLoader(getActivity(), Contract.Entry.CONTENT_URI,
            Report.PROJECTION, Contract.Entry.COLUMN_PENDINGFLAG + " >= 0 ", null, sort);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mAdapter.changeCursor(cursor);
        if (pendingReportCount == -1)
            pendingReportCount = mAdapter.getCount();
        boolean shouldAutoStart = uploader == null || uploader.canceller == uploader.DELETE_BUTTON;
        if (pendingReportCount > 0 && shouldAutoStart){
            inProgressIndex = 1; // TODO: deleting report sets inprogress index to 1 every time. Need to keep a separate successful count
            beamUpFirstReport();
        }
    }
    @Override
    public void onLoaderReset(Loader<Cursor> loader) { mAdapter.changeCursor(null); }
}
