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

import com.sc.mtaasafi.android.R;
import com.sc.mtaasafi.android.Report;
import com.sc.mtaasafi.android.database.ReportContract;
import com.sc.mtaasafi.android.newReport.NewReportActivity;

public class ReportUploadingFragment extends ListFragment
        implements LoaderManager.LoaderCallbacks<Cursor> {

    LinearLayout uploadInterrupted;
    ReportUploader uploader;

    NewReportActivity mActivity;

    SimpleUploadingCursorAdapter mAdapter;
    int currentUploadPosition;
    // Uri mUri; // pendingReportUri


    public final static String ACTION = "action",
                        DATA = "data";
    public final static char ACTION_SEND_NEW = 'n',
                    ACTION_SEND_ALL = 'a',
                    ACTION_VIEW = 'v';

    public String[] LIST_FROM_COLUMNS = new String[] {
        ReportContract.Entry.COLUMN_DETAILS,
        ReportContract.Entry.COLUMN_TIMESTAMP,
        ReportContract.Entry.COLUMN_PENDINGFLAG
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
        currentUploadPosition = -1;
        // mActivity = (NewReportActivity) getActivity();
//        chooseAction(getArguments());
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

    @Override
    public void onListItemClick(ListView l, View view, int position, long id) {
        super.onListItemClick(l, view, position, id);
        currentUploadPosition = position;
        Cursor c = (Cursor) mAdapter.getItem(position);
        beamUpReport(new Report(c), view);
    }

    public class ViewBinder implements SimpleCursorAdapter.ViewBinder {
        @Override
        public boolean setViewValue(View view, Cursor cursor, int i) {
            if (i == cursor.getColumnIndex(ReportContract.Entry.COLUMN_TIMESTAMP))
                ((TextView) view).setText(Report.getElapsedTime(cursor.getString(i)));
            else if (i == cursor.getColumnIndex(ReportContract.Entry.COLUMN_PENDINGFLAG))
                mAdapter.updateProgressView(cursor.getInt(i), view);
            else if (i == cursor.getColumnIndex(ReportContract.Entry.COLUMN_UPLOAD_IN_PROGRESS))
                mAdapter.indicateRow(cursor.getInt(i), view);
            else
                return false;
            return true;
        }
    }

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

    private void beamUpReport(Report pendingReport, View view) {
        uploader = new ReportUploader(getActivity(), pendingReport, this);
        uploader.execute();
        mAdapter.indicateRow(1, view);
    }
    private void beamUpPendingReports() { }
    private void viewSuccessful() {}

    public void reportUploadSuccess() {
        Log.e("Report complete", "refreshing view");
        if (currentUploadPosition != -1) {
//            View activeRow = mAdapter.getView(currentUploadPosition, null, null);
//            resetView(activeRow);
        }
        addMessage("Report uploaded successfully");
        currentUploadPosition = -1;
    }

    public void reportUploadProgress(int progress) {
        
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(), ReportContract.Entry.CONTENT_URI,
            Report.PROJECTION, ReportContract.Entry.COLUMN_PENDINGFLAG + " >= 0 ", null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mAdapter.changeCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.changeCursor(null);
    }

    private void addMessage(String message) {
        
    }





















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
    
//     public void onStart(){
//         super.onStart();
//         uploadingTV.setText("Uploading report (" + (getReportIndex(pendingReport)+1) + "/" + reportsToUpload.size() + ")");
//         uploadingList.setUp(getReportIndex(pendingReport), getReportSummaries());
//         for(int i = 0; i < progress+1; i++) // make UI reflect current status
//             updateUI(progress);
//         clearProgressBars(); // session was cancelled
//         progressBars[progress].setVisibility(View.VISIBLE);
//         if(uploader == null) // no session running, user didn't want session cancelled
//             beamUpReport(pendingReport);
//         else if(!uploader.isCancelled()){ // session was interrupted
//             uploader.setfragmentAvailable(true);
//         } else{ // there is an uploader and it was cancelled
//             clearProgressBars();
//             uploadingTV.setText("Upload cancelled");
//             uploadingTV.setTextColor(getResources().getColor(R.color.Red));
//             uploadInterrupted.setVisibility(View.VISIBLE);
//         }
//         // restore uploading interface to previous state
//     }

//     private void cancelReport(){
//         if (uploader != null)
//             uploader.cancel(true);
//         clearProgressBars();
//         uploadingTV.setText("Cancelling...");
//         uploadingTV.setTextColor(getResources().getColor(R.color.DarkGray));
//     }

//     // Called by the report uploader when it realizes the thread has been cancelled
//     public void cancelConfirmed(){
//         uploadingTV.setText("Upload canceled");
//         uploadingTV.setTextColor(getResources().getColor(R.color.Red));
//         uploadInterrupted.setVisibility(View.VISIBLE);
//     }

//     public void uploadSuccess() {
//         String toastMessage;
//         int uploadedReportIndex = getReportIndex(pendingReport);
//         uploadingList.onReportUploaded(uploadedReportIndex);
//         mActivity.uploadProgress(-1, pendingReport);
//         // if we are uploading multiple reports and there are still reports to be uploaded
//         if(reportsToUpload.size() > 1 && uploadedReportIndex < reportsToUpload.size() -1){
//             int currentReport = uploadedReportIndex+1;
//             Report nextReport = reportsToUpload.get(currentReport);
//             if(nextReport != null){ // if there are more reports to upload
//                 if(nextReport != null){
//                     uploadingTV.setText("Uploading Report (" + (currentReport+1)
//                                         + "/" + reportsToUpload.size() + ")");
//                     beamUpReport(nextReport);
//                 }
//                 return;
//             } else{
//                 toastMessage = "All saved reports uploaded";
//                 Toast.makeText(getActivity(), toastMessage, Toast.LENGTH_SHORT).show();
//             }
//         } else
//             toastMessage = "Report received!";
//         Toast.makeText(getActivity(), toastMessage, Toast.LENGTH_SHORT).show();
//         refreshInterface();
//         mActivity.finish();
//     }





//     // called by the uploader in onCancelled so that the failure is reflected in the UI thread
//     public void onFailure(String failMessage, ReportUploader uploader){
//         if(this.uploader.equals(uploader)){
//             // only update the UI if we're working with the same uploading session
//             // if the user chose to resend before the previous uploader called onFailure, don't overwrite
//             // the interface updates made by the resend event
//             uploadingTV.setText("Upload failed!" + failMessage);
//             uploadingTV.setTextColor(getResources().getColor(R.color.Red));
//             uploadInterrupted.setVisibility(View.VISIBLE);
//             clearProgressBars();
//         }
//     }
}
