package com.sc.mtaasafi.android.newReport;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sc.mtaasafi.android.R;
import com.sc.mtaasafi.android.Report;
import com.sc.mtaasafi.android.SystemUtils.LogTags;
import com.sc.mtaasafi.android.newReport.NewReportActivity;
import com.sc.mtaasafi.android.newReport.ReportUploader;

import java.util.ArrayList;

/**
 * Created by Agree on 10/21/2014.
 */
public class ReportUploadingFragment extends Fragment {
    ImageView reportTextUploading, pic1Uploading, pic2Uploading, pic3Uploading;
    Button cancelButton, resendButton, sendLaterButton, abandonButton;
    LinearLayout uploadInterrupted;
    ReportUploader uploader;
    ProgressBar[] progressBars;
    TextView uploadingTV, detailText;
    Report pendingReport;
    ArrayList<Report> reportsToUpload;
    NewReportActivity mActivity;
    UploadingReportsList uploadingList;
    int progress;
    final static private String
            PROGRESS_KEY = "progress",
            PENDING_REPORT_KEY = "pendingReport",
            REPORT_SUMMARIES_KEY = "reportSummaries";

    @Override
    public void onCreate(Bundle instate){
        super.onCreate(instate);
        progressBars = new ProgressBar[4];
        mActivity = (NewReportActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedState){
        super.onCreateView(inflater, container, savedState);
        setRetainInstance(true);
        View view = inflater.inflate(R.layout.upload_interface, container, false);
        setup(view);
        return view;
    }
    private void setup(View view){
        reportTextUploading = (ImageView) view.findViewById(R.id.reportUploadingIcon);
        pic1Uploading = (ImageView) view.findViewById(R.id.pic1UploadingIcon);
        pic2Uploading = (ImageView) view.findViewById(R.id.pic2UploadingIcon);
        pic3Uploading = (ImageView) view.findViewById(R.id.pic3UploadingIcon);
        cancelButton = (Button) view.findViewById(R.id.cancelButton);
        progressBars[0] = (ProgressBar) view.findViewById(R.id.progressBarReportText);
        progressBars[1] = (ProgressBar) view.findViewById(R.id.progressBarPic1);
        progressBars[2] = (ProgressBar) view.findViewById(R.id.progressBarPic2);
        progressBars[3] = (ProgressBar) view.findViewById(R.id.progressBarPic3);
        detailText = (TextView) view.findViewById(R.id.detailText);
        uploadingTV = (TextView) view.findViewById(R.id.uploadingText);
        uploadingList = (UploadingReportsList) view.findViewById(R.id.uploadingList);
        setUploadInterruptedLayout(view);
        setListeners();
    }
    private void setUploadInterruptedLayout(View view){
        uploadInterrupted = (LinearLayout) view.findViewById(R.id.uploadInterrupted);
        resendButton = (Button) view.findViewById(R.id.resendButton);
        sendLaterButton = (Button) view.findViewById(R.id.sendLaterButton);
        abandonButton = (Button) view.findViewById(R.id.abandonReport);
    }

    private void setListeners(){
        resendButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Log.e("Resend btn", "Resend called");
                uploadingTV.setText("Resending...");
                uploadingTV.setTextColor(getResources().getColor(R.color.White));
                updatePostProgress(progress, pendingReport);
                beamUpReport(pendingReport);
            }
        });
        abandonButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                uploadingTV.setText("Abandoning report...");
                uploadingTV.setTextColor(getResources().getColor(R.color.DarkRed));
                // TODO: delete report from DB if it's there
                mActivity.deleteReport(pendingReport);
                getActivity().finish();
            }
        });
        sendLaterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View view){
                uploadingTV.setText("Saving report...");
                uploadingTV.setTextColor(getResources().getColor(R.color.White));
                // if the pendingReport was a saved one, remove the old version before saving the new one
                mActivity.saveReport(pendingReport, progress);
                Toast.makeText(mActivity, "Report saved for later!", Toast.LENGTH_SHORT).show();
                mActivity.finish();
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancelReport();
            }
        });
    }
    @Override
    public void onActivityCreated(Bundle instate){
        super.onActivityCreated(instate);
        if(getArguments() != null){
            if(getArguments().getBoolean(mActivity.UPLOAD_SAVED_REPORTS_KEY, false)){
                // fragment was called to upload saved report(s)
                if(pendingReport == null){
                    reportsToUpload = mActivity.getSavedReports();
                    pendingReport = reportsToUpload.get(0);
                }
            } else{// fragment was called to upload a single new report
                pendingReport = new Report(mActivity.REPORT_KEY, getArguments());
                reportsToUpload = new ArrayList<Report>();
                reportsToUpload.add(pendingReport);
            }
        } else { // TODO: generic error message
        }
    }

    public void onStart(){
        super.onStart();
        uploadingTV.setText("Uploading report (" + (getReportIndex(pendingReport)+1) + "/" + reportsToUpload.size() + ")");
        uploadingList.setUp(getReportIndex(pendingReport), getReportSummaries());
        for(int i = 0; i < progress+1; i++) // make UI reflect current status
            updateUI(progress);
        clearProgressBars(); // session was cancelled
        progressBars[progress].setVisibility(View.VISIBLE);
        if(uploader == null) // no session running, user didn't want session cancelled
            beamUpReport(pendingReport);
        else if(!uploader.isCancelled()){ // session was interrupted
            uploader.setfragmentAvailable(true);
        } else{ // there is an uploader and it was cancelled
            clearProgressBars();
            uploadingTV.setText("Upload cancelled");
            uploadingTV.setTextColor(getResources().getColor(R.color.Red));
            uploadInterrupted.setVisibility(View.VISIBLE);
        }
        // restore uploading interface to previous state
    }

    private void beamUpReport(Report report) {
        uploadInterrupted.setVisibility(View.INVISIBLE);
        if(report.serverId == 0)
            refreshInterface();
        Log.e(LogTags.BACKEND_W, "Beaming it up, baby! Report id: " + report.serverId);
        uploadingTV.setText("Uploading report (" + (getReportIndex(report) + 1) + "/" + reportsToUpload.size() + ")");
        uploadingTV.setTextColor(getResources().getColor(R.color.White));
        uploader = new ReportUploader(this, report);
        uploader.execute();
        uploader.setfragmentAvailable(true);
        mActivity.setUploader(uploader);
        detailText.setText(report.details);
    }

    private void refreshInterface(){
        clearProgressBars();
        progress = 0;
        reportTextUploading.setImageResource(R.drawable.report_loading);
        pic1Uploading.setImageResource(R.drawable.pic1_uploading);
        pic2Uploading.setImageResource(R.drawable.pic2_uploading);
        pic3Uploading.setImageResource(R.drawable.pic3_uploading);
        detailText.setText("");
    }
    private void cancelReport(){
        if(uploader != null){
            uploader.cancel(true);
        }
        clearProgressBars();
        uploadingTV.setText("Cancelling...");
        uploadingTV.setTextColor(getResources().getColor(R.color.DarkGray));
    }

    // Called by the report uploader when it realizes the thread has been cancelled
    public void cancelConfirmed(){
        uploadingTV.setText("Upload canceled");
        uploadingTV.setTextColor(getResources().getColor(R.color.Red));
        uploadInterrupted.setVisibility(View.VISIBLE);
    }
    public void clearProgressBars(){
        for(ProgressBar progress : progressBars)
            progress.setVisibility(View.INVISIBLE);
    }
    // progress 0 = uploading text; 1 = text uploaded, uploading pic1;
    // 2 = pic1 uploaded, uploading pic2; 3 = pic2 uploaded, uploading pic3;
    // -1 = pic3 uploaded, report fully received
    public void updatePostProgress(int progress, Report report){
        this.progress = progress;
        pendingReport = report;
        Log.e("RUF.updatePostProgress", " Progress: " + progress);
        updateUI(progress);
        mActivity.uploadProgress(progress, report);
    }
    // NOTE: Do not consolidate this f(x) and the one above
    // below f(x) deliberately lacks break statements so cases (progress --> 0) can be executed in succession
    // calling mActivity after each call of updateProgressView will cause errors in the DB
    public void updateUI(int progress){
        switch (progress) {
            case -1:
                updateProgressView(R.id.progressBarPic3, R.id.pic3UploadingIcon, 0, R.drawable.pic3_uploaded);
            case 3:
                updateProgressView(R.id.progressBarPic2, R.id.pic2UploadingIcon, R.id.progressBarPic3, R.drawable.pic2_uploaded);
            case 2:
                updateProgressView(R.id.progressBarPic1,R.id.pic1UploadingIcon, R.id.progressBarPic2, R.drawable.pic1_uploaded);
            case 1:
                updateProgressView(R.id.progressBarReportText,R.id.reportUploadingIcon, R.id.progressBarPic1, R.drawable.report_uploaded);
            case 0:
                updateProgressView(0, 0, R.id.progressBarReportText, 0);
        }
        if(progress > -1){
            for(int i = 0; i < progress; i++){
                progressBars[i].setVisibility(View.INVISIBLE);
            }
        } else
            clearProgressBars();
    }

    public void uploadSuccess() {
        String toastMessage;
        int uploadedReportIndex = getReportIndex(pendingReport);
        uploadingList.onReportUploaded(uploadedReportIndex);
        mActivity.uploadProgress(-1, pendingReport);
        // if we are uploading multiple reports and there are still reports to be uploaded
        if(reportsToUpload.size() > 1 && uploadedReportIndex < reportsToUpload.size() -1){
            int currentReport = uploadedReportIndex+1;
            Report nextReport = reportsToUpload.get(currentReport);
            if(nextReport != null){ // if there are more reports to upload
                if(nextReport != null){
                    uploadingTV.setText("Uploading Report (" + (currentReport+1)
                                        + "/" + reportsToUpload.size() + ")");
                    beamUpReport(nextReport);
                }
                return;
            } else{
                toastMessage = "All saved reports uploaded";
                Toast.makeText(getActivity(), toastMessage, Toast.LENGTH_SHORT).show();
            }
        } else
            toastMessage = "Report received!";
        Toast.makeText(getActivity(), toastMessage, Toast.LENGTH_SHORT).show();
        refreshInterface();
        mActivity.finish();
    }

    // called by the uploader if it was cancelled but still managed to upload the report
    public void stillUploaded(){
        Handler mainHandler = new Handler(getActivity().getMainLooper());
        Runnable myRunnable = new Runnable() {
            @Override
            public void run() {
                uploadSuccess();
            }
        };
        mainHandler.post(myRunnable);
    }

    // returns the current pending report
    private int getReportIndex(Report report){
        for(int i = 0; i < reportsToUpload.size(); i++){
            if(reportsToUpload.get(i).timeStamp != null && report.timeStamp != null){
                if(reportsToUpload.get(i).timeStamp.equals(report.timeStamp))
                    return i;
            } else
                Log.e("REPORT INDEX", "ERROR!!! Report "  + i + " has no fucking time stamp");
        }
        return -1;
    }
    private ArrayList<String> getReportSummaries(){
        ArrayList<String> reportSummaries = new ArrayList<String>();
        for(Report report : reportsToUpload)
            reportSummaries.add(report.details);
        return reportSummaries;
    }
    // called by the uploader if there was a 404 or some shit
    public void uploadFailure(final String failMessage){
        uploader.cancel(true);
    }

    // called by the uploader in onCancelled so that the failure is reflected in the UI thread
    public void onFailure(String failMessage, ReportUploader uploader){
        if(this.uploader.equals(uploader)){
            // only update the UI if we're working with the same uploading session
            // if the user chose to resend before the previous uploader called onFailure, don't overwrite
            // the interface updates made by the resend event
            uploadingTV.setText("Upload failed!" + failMessage);
            uploadingTV.setTextColor(getResources().getColor(R.color.Red));
            uploadInterrupted.setVisibility(View.VISIBLE);
            clearProgressBars();
        }
    }

    private void updateProgressView(int doneProgressId, int doneViewId,
                                    int workingId, int drawable) {
        if(getView()!= null){
//            if (doneProgressId != 0) {
//                ProgressBar done = (ProgressBar) getView().findViewById(doneProgressId);
//                done.setVisibility(View.INVISIBLE);
//            }
            if (doneViewId != 0 && drawable != 0) {
                ImageView doneView = (ImageView) getView().findViewById(doneViewId);
                doneView.setImageResource(drawable);
            }
            if (workingId != 0) {
                ProgressBar working = (ProgressBar) getView().findViewById(workingId);
                working.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onStop(){
        super.onStop();
        for(ProgressBar pb : progressBars)
            pb = null;
        uploadingTV = null;
        uploadingList = null;
        reportTextUploading = pic1Uploading = pic2Uploading = pic3Uploading = null;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        if(uploader != null)
            uploader.setfragmentAvailable(false);
    }
}
