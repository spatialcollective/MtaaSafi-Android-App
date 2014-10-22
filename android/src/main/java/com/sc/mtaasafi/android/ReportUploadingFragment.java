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
    ProgressBar reportTextProgress, pic1Progress, pic2Progress, pic3Progress;
    TextView uploadingTV;
    Report pendingReport;
    NewReportActivity mActivity;

    @Override
    public void onCreate(Bundle instate){
        super.onCreate(instate);
        mActivity = (NewReportActivity) getActivity();
        if(getArguments() != null)
            pendingReport = new Report(mActivity.REPORT_KEY, getArguments());

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
        reportTextProgress = (ProgressBar) view.findViewById(R.id.progressBarReportText);
        pic1Progress = (ProgressBar) view.findViewById(R.id.progressBarPic1);
        pic2Progress = (ProgressBar) view.findViewById(R.id.progressBarPic2);
        pic3Progress = (ProgressBar) view.findViewById(R.id.progressBarPic3);
        uploadingTV = (TextView) view.findViewById(R.id.uploadingText);
        setUploadInterruptedLayout(view);
        setListeners();
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
                // cancel report, clear it all out
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
        Log.e(LogTags.BACKEND_W, "Beam it up");
        uploader = new ReportUploader(this, report);
        uploader.execute();
        uploadingTV.setText("Uploading...");
        resendButton.setClickable(false);
        resendButton.setBackgroundColor(getResources().getColor((R.color.report_button_unclickable)));
    }
    private void cancelReport(){
        if(uploader != null){
            uploader.cancel(true);
        }
        uploadingTV.setText("Upload cancelled!");
        uploadInterrupted.setVisibility(View.VISIBLE);
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
        Toast.makeText(getActivity(), "Thank you for your report!", Toast.LENGTH_SHORT).show();
    }

    // called by the uploader if there was a 404 or some shit
    public void uploadFailure(final String failMessage){
        uploader.cancel(true);
    }

    // called by the uploader in onCancelled so that the failure is reflected in the UI thread
    public void onFailure(String failMessage){
        uploadingTV.setText("Upload failed!" + failMessage);
        uploadInterrupted.setVisibility(View.VISIBLE);
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
