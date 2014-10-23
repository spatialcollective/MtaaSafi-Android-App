package com.sc.mtaasafi.android;

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

/**
 * Created by Agree on 10/21/2014.
 */
public class ReportUploadingFragment extends Fragment {
    RelativeLayout uploadingScreen;
    ImageView reportTextUploading, pic1Uploading, pic2Uploading, pic3Uploading;
    Button cancelButton, resendButton, sendLaterButton, abandonButton;
    LinearLayout uploadInterrupted;
    ReportUploader uploader;
    ProgressBar[] progressBars;
    TextView uploadingTV, detailText;
    Report pendingReport;
    NewReportActivity mActivity;
    int reportsToUpload, currentReport;
    boolean isUploadingSavedReports;

    @Override
    public void onCreate(Bundle instate){
        super.onCreate(instate);
        progressBars = new ProgressBar[4];
        reportsToUpload = 1;
        mActivity = (NewReportActivity) getActivity();
        currentReport = 1;
        if(getArguments() != null)
            if(getArguments().getBoolean(mActivity.UPLOAD_SAVED_REPORTS_KEY, false)){
                // fragment was called to upload saved reports
                reportsToUpload = mActivity.getSavedReportCount();
                isUploadingSavedReports = true;
            }
            else{
                pendingReport = new Report(getArguments());
                reportsToUpload = 1;
                // fragment was called to upload a new report
            }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedState){
        super.onCreateView(inflater, container, savedState);
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
        uploadingTV.setText("Uploading report (" + currentReport + "/" + reportsToUpload + ")");
        setUploadInterruptedLayout(view);
        setListeners();
        if(isUploadingSavedReports)
            beamUpReport(mActivity.getNextSavedReport());
        else
            beamUpReport(pendingReport);
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
                beamUpReport(pendingReport);
            }
        });
        abandonButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                mActivity.finish();
            }
        });
        sendLaterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View view){
                mActivity.saveReport(pendingReport);
                Toast.makeText(mActivity, "Report saved for later!", Toast.LENGTH_SHORT);
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

    public void beamUpReport(Report report) {
        uploadInterrupted.setVisibility(View.INVISIBLE);
        refreshInterface();
        Log.e(LogTags.BACKEND_W, "Beam it up");
        uploader = new ReportUploader(this, report);
        uploader.execute();
        resendButton.setClickable(false);
        resendButton.setBackgroundColor(getResources().getColor((R.color.report_button_unclickable)));
        if(isUploadingSavedReports)
            mActivity.removeTopSavedReport();
        detailText.setText(report.details);

    }
    private void refreshInterface(){
        clearProgressBars();
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
        uploadInterrupted.setVisibility(View.VISIBLE);
    }
    public void clearProgressBars(){
        for(ProgressBar progress : progressBars)
            progress.setVisibility(View.INVISIBLE);
    }
    public void updatePostProgress(int progress, Report report){
        pendingReport = report;
        Log.e(LogTags.NEWREPORT, "pending report id: " + pendingReport.id);
        switch (progress) {
            case 0:
                updateProgressView(0, 0, R.id.progressBarReportText, 0);
                break;
            case 1:
                updateProgressView(R.id.progressBarReportText, R.id.reportUploadingIcon, R.id.progressBarPic1, R.drawable.report_uploaded);
                break;
            case 2:
                updateProgressView(R.id.progressBarPic1, R.id.pic1UploadingIcon, R.id.progressBarPic2, R.drawable.pic1_uploaded);
                break;
            case 3:
                updateProgressView(R.id.progressBarPic2, R.id.pic2UploadingIcon, R.id.progressBarPic3, R.drawable.pic2_uploaded);
                break;
            case -1:
                updateProgressView(R.id.progressBarPic3, R.id.pic3UploadingIcon, 0, R.drawable.pic3_uploaded);
                break;
        }
    }

    public void uploadSuccess() {
        String toastMessage;
        if(isUploadingSavedReports){
            if(currentReport != reportsToUpload){
                Report nextReport = mActivity.getNextSavedReport();
                if(nextReport != null){
                    uploadingTV.setText("Uploading Report (" + currentReport + "/" + reportsToUpload + ")");
                    beamUpReport(nextReport);
                }
                toastMessage = "Report " + currentReport + " uploaded";
                currentReport++;
                Toast.makeText(getActivity(), toastMessage, Toast.LENGTH_SHORT).show();
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
    // called by the uploader if there was a 404 or some shit
    public void uploadFailure(final String failMessage){
        uploader.cancel(true);
    }

    // called by the uploader in onCancelled so that the failure is reflected in the UI thread
    public void onFailure(String failMessage){
        uploadingTV.setText("Upload failed!" + failMessage);
        uploadInterrupted.setVisibility(View.VISIBLE);
        clearProgressBars();
    }

    private void updateProgressView(int doneProgressId, int doneViewId, int workingId, int drawable) {
        if (doneProgressId != 0) {
            ProgressBar done = (ProgressBar) getView().findViewById(doneProgressId);
            done.setVisibility(View.GONE);
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


    public void clearData() {
        pendingReport = null;
    }

    public void retryUpload() {
        //     beamItUp(reportId, nextPieceKey, pendingReport);
    }

}
