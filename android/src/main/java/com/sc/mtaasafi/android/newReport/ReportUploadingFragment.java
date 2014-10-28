package com.sc.mtaasafi.android.newReport;

import android.os.Bundle;
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
    NewReportActivity mActivity;
    UploadingReportsList uploadingList;
    ArrayList<String> reportSummaries;
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
                getActivity().finish();
            }
        });
        sendLaterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View view){
                uploadingTV.setText("Saving report...");
                uploadingTV.setTextColor(getResources().getColor(R.color.White));
                // if the pendingReport was a saved one, remove the old version before saving the new one
                if(pendingReport.timeStamp.equals(mActivity.getTopSavedReport().timeStamp)){
                    mActivity.removeTopSavedReport();
                }
                mActivity.saveReport(pendingReport);
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
        if(instate != null){
            progress = instate.getInt(PROGRESS_KEY);
            pendingReport = new Report(PENDING_REPORT_KEY, instate);
            reportSummaries = instate.getStringArrayList(REPORT_SUMMARIES_KEY);
        }
        else{
            if(getArguments() != null){
                if(getArguments().getBoolean(mActivity.UPLOAD_SAVED_REPORTS_KEY, false)){
                    // fragment was called to upload saved report(s)
                    if(pendingReport == null)
                        pendingReport = mActivity.getTopSavedReport();
                    if(reportSummaries == null)
                        reportSummaries = mActivity.getSavedReportSummaries();
                    Log.e("REPORT SUMMARIES" , "Summary " + 0 + ": " + pendingReport.details);
                } else{// fragment was called to upload a single new report
                    pendingReport = new Report(mActivity.REPORT_KEY, getArguments());
                    reportSummaries = new ArrayList<String>();
                    reportSummaries.add(pendingReport.details);
                }
            }
        }
        uploadingList.setUp(getCurrentReport(pendingReport), reportSummaries);
    }
    public void onStart(){
        super.onStart();
        if(uploader == null)
            beamUpReport(pendingReport);
        else
            uploader.setfragmentAvailable(true);
        // restore uploading interface to previous state
        for(int i = 0; i < progress+1; i++){
            updatePostProgress(progress, pendingReport);
        }
    }

    private void beamUpReport(Report report) {
        uploadInterrupted.setVisibility(View.INVISIBLE);
        if(report.id == 0)
            refreshInterface();
        Log.e(LogTags.BACKEND_W, "Beaming it up, baby! Report id: " + report.id);
        uploadingTV.setText("Uploading report (" + (+1) + "/" + reportSummaries.size() + ")");
        Log.e("uploader frag", "Beaming it up, baby!");
        uploadingTV.setTextColor(getResources().getColor(R.color.White));
        uploader = new ReportUploader(this, report);
        uploader.execute();
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
        uploadingTV.setText("Upload cancelled!");
        uploadingTV.setTextColor(getResources().getColor(R.color.Red));
        uploadInterrupted.setVisibility(View.VISIBLE);
    }
    public void clearProgressBars(){
        for(ProgressBar progress : progressBars)
            progress.setVisibility(View.INVISIBLE);
    }
    public void updatePostProgress(int progress, Report report){
        this.progress = progress;
        pendingReport = report;
        Log.e(LogTags.NEWREPORT, "pending report id: " + pendingReport.id + " progress: " + progress);
        Log.e("Upload Frag", "pending report id: " + pendingReport.id);
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
    }

    public void uploadSuccess() {
        String toastMessage;
        uploadingList.onReportUploaded(getCurrentReport(pendingReport));
        mActivity.removeTopSavedReport();
        // if we are uploading multiple reports and there are still reports to be uploaded
        if(reportSummaries.size() > 1 && getCurrentReport(pendingReport) < reportSummaries.size() -1){
            Report nextReport = mActivity.getTopSavedReport();
            int currentReport = getCurrentReport(nextReport);
            if(nextReport != null){ // if there are more reports to upload
                if(nextReport != null){
                    uploadingTV.setText("Uploading Report (" + (currentReport+1)
                                        + "/" + reportSummaries.size() + ")");
                    beamUpReport(nextReport);
                }
                return;
            } else{
                toastMessage = "All saved reports uploaded";
                Toast.makeText(getActivity(), toastMessage, Toast.LENGTH_SHORT).show();
            }
        } else
            toastMessage = "Thank you for your report!";
        Toast.makeText(getActivity(), toastMessage, Toast.LENGTH_SHORT).show();
        refreshInterface();
        mActivity.finish();
    }
    // returns the 
    private int getCurrentReport(Report report){
        return reportSummaries.indexOf(report.details);
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
        if (doneProgressId != 0) {
            ProgressBar done = (ProgressBar) getView().findViewById(doneProgressId);
            done.setVisibility(View.INVISIBLE);
        }
        if (doneViewId != 0 && drawable != 0) {
            ImageView doneView = (ImageView) getView().findViewById(doneViewId);
            doneView.setImageResource(drawable);
        }
        if (workingId != 0) {
            ProgressBar working = (ProgressBar) getView().findViewById(workingId);
            working.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        if(uploader != null)
            uploader.setfragmentAvailable(false);
    }
}
