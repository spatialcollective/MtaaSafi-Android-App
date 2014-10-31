package com.sc.mtaasafi.android.uploading;

import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleCursorTreeAdapter;
import android.widget.TextView;

import com.sc.mtaasafi.android.R;
import com.sc.mtaasafi.android.Report;
import com.sc.mtaasafi.android.database.ReportContract;
import com.sc.mtaasafi.android.newReport.NewReportActivity;

public class ReportUploadingFragment extends ListFragment
        implements LoaderManager.LoaderCallbacks<Cursor> {
    LinearLayout uploadInterrupted;
    ReportUploader uploader;
    ProgressBar[] progressBars;
    TextView uploadingTV, detailText;

    NewReportActivity mActivity;

    SimpleCursorAdapter mAdapter;
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
        progressBars = new ProgressBar[4];
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

        mAdapter = new SimpleCursorAdapter(getActivity(), R.layout.upload_item,
                null, LIST_FROM_COLUMNS, LIST_TO_FIELDS, 0);

        mAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Cursor cursor, int i) {
                if (i == cursor.getColumnIndex(ReportContract.Entry.COLUMN_TIMESTAMP))
                    ((TextView) view).setText(Report.getElapsedTime(cursor.getString(i)));
                else if (i == cursor.getColumnIndex(ReportContract.Entry.COLUMN_PENDINGFLAG))
                    updateUI(cursor.getInt(i), view);
                else
                    return false;
                return true;
            }
        });
        setListAdapter(mAdapter);
        getLoaderManager().initLoader(0, null, this);

//        progressBars[0] = (ProgressBar) view.findViewById(R.id.progressBarReportText);
//        progressBars[1] = (ProgressBar) view.findViewById(R.id.progressBarPic1);
//        progressBars[2] = (ProgressBar) view.findViewById(R.id.progressBarPic2);
//        progressBars[3] = (ProgressBar) view.findViewById(R.id.progressBarPic3);
        // detailText = (TextView) view.findViewById(R.id.detailText);
        // uploadingTV = (TextView) view.findViewById(R.id.uploadingText);
        // uploadingList = (UploadingReportsList) view.findViewById(R.id.uploadingList);
        // uploadInterrupted = (LinearLayout) view.findViewById(R.id.uploadInterrupted);
        // setListeners(view);
    }

    @Override
    public void onListItemClick(ListView l, View view, int position, long id) {
        super.onListItemClick(l, view, position, id);
        currentUploadPosition = position;

        view.setMinimumHeight(200);
        view.setBackgroundColor(getResources().getColor(R.color.mtaa_safi_blue));
        ((TextView) view.findViewById(R.id.itemDetails)).setTextColor(Color.WHITE);
        ((TextView) view.findViewById(R.id.timeElapsed)).setTextColor(Color.WHITE);
        view.findViewById(R.id.expanded_layout).setVisibility(View.VISIBLE);

        Cursor c = (Cursor) mAdapter.getItem(position);
        beamUpReport(new Report(c));
        // mCallback.goToDetailView(c, position);
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

    private void beamUpReport(Report pendingReport) {
        uploader = new ReportUploader(getActivity(), this, pendingReport);
        uploader.setfragmentAvailable(true);
        uploader.execute();
//        mActivity.setUploader(uploader);
    }
    private void beamUpPendingReports() { } // mActivity.getSavedReports();
    private void viewSuccessful() {}

    // progress 0 = uploading text; 1 = text uploaded, uploading pic1;
    // 2 = pic1 uploaded, uploading pic2; 3 = pic2 uploaded, uploading pic3;
    // -1 = pic3 uploaded, report fully received
    // public void updateUploadProgress(int progress, Report report){
    //     Log.e("RUF.updatePostProgress", " Progress: " + progress);
    //     Cursor c = (Cursor) mAdapter.getItem(position);

    //     // updateUI(progress);
    // }

    private void updateUI(int progress, View view){
        switch (progress) {
            case -1:
                updateProgressView(view, R.id.progressBarPic3, R.id.pic3UploadingIcon, 0, R.drawable.pic3_uploaded);
            case 3:
                updateProgressView(view, R.id.progressBarPic2, R.id.pic2UploadingIcon, R.id.progressBarPic3, R.drawable.pic2_uploaded);
            case 2:
                updateProgressView(view, R.id.progressBarPic1,R.id.pic1UploadingIcon, R.id.progressBarPic2, R.drawable.pic1_uploaded);
            case 1:
                updateProgressView(view, R.id.progressBarReportText,R.id.reportUploadingIcon, R.id.progressBarPic1, R.drawable.report_uploaded);
            case 0:
                updateProgressView(view, 0, 0, R.id.progressBarReportText, 0);
        }
        //     clearProgressBars();
    }

        private void updateProgressView(View view, int doneProgressId, int doneViewId, int workingId, int drawable) {
        if (view != null) {
            if (doneProgressId != 0) {
                ProgressBar done = (ProgressBar) view.findViewById(doneProgressId);
                done.setVisibility(View.INVISIBLE);
            }
            if (doneViewId != 0 && drawable != 0) {
                ImageView doneView = (ImageView) view.findViewById(doneViewId);
                doneView.setImageResource(drawable);
            }
            if (workingId != 0) {
                ProgressBar working = (ProgressBar) view.findViewById(workingId);
                working.setVisibility(View.VISIBLE);
            }
        }
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

//     private void refreshInterface() {
//         clearProgressBars();
//         progress = 0;
//         ((ImageView)getView().findViewById(R.id.reportUploadingIcon)).setImageResource(R.drawable.report_loading);
//         ((ImageView)getView().findViewById(R.id.pic1UploadingIcon)).setImageResource(R.drawable.pic1_uploading);
//         ((ImageView)getView().findViewById(R.id.pic2UploadingIcon)).setImageResource(R.drawable.pic2_uploading);
//         ((ImageView)getView().findViewById(R.id.pic3UploadingIcon)).setImageResource(R.drawable.pic3_uploading);
//         detailText.setText("");
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
//     public void clearProgressBars(){
//         for(ProgressBar progress : progressBars)
//             progress.setVisibility(View.INVISIBLE);
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

//     // called by the uploader if it was cancelled but still managed to upload the report
//     public void stillUploaded(){
//         Handler mainHandler = new Handler(getActivity().getMainLooper());
//         Runnable myRunnable = new Runnable() {
//             @Override
//             public void run() {
//                 uploadSuccess();
//             }
//         };
//         mainHandler.post(myRunnable);
//     }

//     // returns the current pending report
//     private int getReportIndex(Report report){
//         for(int i = 0; i < reportsToUpload.size(); i++){
//             if(reportsToUpload.get(i).timeStamp != null && report.timeStamp != null){
//                 if(reportsToUpload.get(i).timeStamp.equals(report.timeStamp))
//                     return i;
//             } else
//                 Log.e("REPORT INDEX", "ERROR!!! Report "  + i + " has no fucking time stamp");
//         }
//         return -1;
//     }
//     private ArrayList<String> getReportSummaries(){
//         ArrayList<String> reportSummaries = new ArrayList<String>();
//         for(Report report : reportsToUpload)
//             reportSummaries.add(report.details);
//         return reportSummaries;
//     }
//     // called by the uploader if there was a 404 or some shit
//     public void uploadFailure(final String failMessage){
//         uploader.cancel(true);
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

//     @Override
//     public void onStop(){
//         super.onStop();
//         for(ProgressBar pb : progressBars)
//             pb = null;
//         uploadingTV = null;
//         uploadingList = null;
//     }

//     @Override
//     public void onDestroy(){
//         super.onDestroy();
//         if(uploader != null)
//             uploader.setfragmentAvailable(false);
//     }
}
