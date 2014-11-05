package com.sc.mtaasafi.android.uploading;

import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.sc.mtaasafi.android.R;
import com.sc.mtaasafi.android.Report;
import com.sc.mtaasafi.android.database.Contract;
import com.sc.mtaasafi.android.newReport.NewReportActivity;

public class ReportUploadingFragment extends ListFragment
        implements LoaderManager.LoaderCallbacks<Cursor> {

    ReportUploader uploader;
    SimpleUploadingCursorAdapter mAdapter;
    private int pendingReportCount;

    public final static String ACTION = "action", DATA = "data";
    public final static char ACTION_SEND_NEW = 'n',
                    ACTION_SEND_ALL = 'a',
                    ACTION_VIEW = 'v';

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
        pendingReportCount = -1;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedState) {
        super.onCreateView(inflater, container, savedState);
        return inflater.inflate(R.layout.upload_list, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedState) {
        super.onViewCreated(view, savedState);
        mAdapter = new SimpleUploadingCursorAdapter(getActivity(), R.layout.upload_item,
                null, LIST_FROM_COLUMNS, LIST_TO_FIELDS, 0);
        mAdapter.setViewBinder(new ViewBinder());
        setListAdapter(mAdapter);
        getLoaderManager().initLoader(0, null, this);
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
            else
                return false;
            return true;
        }
    }

    private void beamUpFirstReport() {
        if ((uploader == null || uploader.isCancelled()) && mAdapter != null && mAdapter.getCount() > 0)
            beamUpReport(new Report((Cursor) mAdapter.getItem(0)));
        else if (mAdapter.getCount() == 0)
            changeHeaderMessage("Nothing to upload.", false, R.color.Coral);
    }

    private void beamUpReport(Report pendingReport) {
        if (getView() != null)
            changeHeaderMessage("Uploading...", true, R.color.mtaa_safi_blue);
        uploader = new ReportUploader(getActivity(), pendingReport, this);
        uploader.execute();
    }

    public void reportUploadSuccess() {
        changeHeaderMessage("Report uploaded successfully!", false, R.color.mtaa_safi_blue);
        uploader = null;
        if (mAdapter.getCount() > 0)
            beamUpFirstReport();
        else if (getView() != null)
            changeHeaderMessage("Successfully uploaded " + pendingReportCount + " reports.",
                    false, R.color.mtaa_safi_blue);
    }

    private void changeHeaderMessage(String message, boolean showSpinner, int color) {
        View view = getView();
        if (view == null) return;

        if (message == null || message == "") {
            view.findViewById(R.id.uploadingView).setVisibility(View.GONE);
            return;
        }
        
        view.findViewById(R.id.uploadingView).setVisibility(View.VISIBLE);
        ((TextView) view.findViewById(R.id.uploadingText)).setText(message);
        ((TextView) view.findViewById(R.id.uploadingText)).setTextColor(getResources().getColor(color));

        if (showSpinner)
            view.findViewById(R.id.headerProgressBar).setVisibility(View.VISIBLE);
        else
            view.findViewById(R.id.headerProgressBar).setVisibility(View.GONE);
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
        beamUpFirstReport();
    }
    @Override
    public void onLoaderReset(Loader<Cursor> loader) { mAdapter.changeCursor(null); }

    //    private void chooseAction(Bundle args) {
//        if (args != null) {
//            switch(args.getChar(ACTION)) {
//                case ACTION_SEND_NEW:
//                    beamUpReport(Uri.parse(args.getString(DATA)));
//                case ACTION_SEND_ALL:
//                    beamUpPendingReports();
//                    break;
//                case ACTION_VIEW:
//                    viewSuccessful();
//            }
//        }
//    }
    
//     private void setListeners(View view) {
//         view.findViewById(R.id.resendButton).setOnClickListener(new View.OnClickListener(){
//             @Override
//             public void onClick(View view){
//                 Log.e("Resend btn", "Resend called");
//                 uploadingTV.setText("Resending...");
//                 uploadingTV.setTextColor(getResources().getColor(R.color.White));
//                 updatePostProgress(progress, pendingReport);
//                 beamUpReport(pendingReport);
//             }
//         });
//         view.findViewById(R.id.abandonReport).setOnClickListener(new View.OnClickListener(){
//             @Override
//             public void onClick(View view){
//                 uploadingTV.setText("Abandoning report...");
//                 uploadingTV.setTextColor(getResources().getColor(R.color.DarkRed));
//                 // TODO: delete report from DB if it's there
//                 mActivity.deleteReport(pendingReport);
//                 getActivity().finish();
//             }
//         });
//         view.findViewById(R.id.sendLaterButton).setOnClickListener(new View.OnClickListener() {
//             @Override
//             public void onClick (View view){
//                 uploadingTV.setText("Saving report...");
//                 uploadingTV.setTextColor(getResources().getColor(R.color.White));
//                 // if the pendingReport was a saved one, remove the old version before saving the new one
//                 mActivity.saveReport(pendingReport, progress);
//                 Toast.makeText(mActivity, "Report saved for later!", Toast.LENGTH_SHORT).show();
//                 mActivity.finish();
//             }
//         });
//         view.findViewById(R.id.cancelButton).setOnClickListener(new View.OnClickListener() {
//             @Override
//             public void onClick(View view) {
//                 cancelReport();
//             }
//         });
//     }
}
