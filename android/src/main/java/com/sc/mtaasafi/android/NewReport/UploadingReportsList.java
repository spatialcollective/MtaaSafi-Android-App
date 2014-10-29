package com.sc.mtaasafi.android.newReport;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sc.mtaasafi.android.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Agree on 10/27/2014.
 */
public class UploadingReportsList extends LinearLayout {
    ArrayList<String> reportSummaries;
    RelativeLayout uploadingRow;
    TextView uploadDeets;

    private static final String TOTAL_REPORTS_KEY = "reportSummaries.size()",
                                REPORT_SUMMARIES_KEY = "reportSummaries";
    public UploadingReportsList(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    // IMPORTANT: this class assumes that its first child in the XML will be
    // the uploading row layout. Will crash otherwise.
    public void setUp(int currentReport, ArrayList<String> reportSummaries){
        this.reportSummaries = reportSummaries;
        Log.e("UPLOADINGLIST", "Reports to send: " + reportSummaries.size());
        this.uploadingRow = (RelativeLayout) getChildAt(0);
        for(int i = 0; i < currentReport; i++){ // show the uploaded reports
            addView(getTV(reportSummaries.get(i), true));
        }
        removeViewAt(0); // show the current report being uploaded, with corresponding text
        addView(this.uploadingRow);
        uploadDeets = (TextView)uploadingRow.findViewById(R.id.detailText);
        uploadDeets.setText(reportSummaries.get(currentReport));
        for(int i = currentReport + 1; i < reportSummaries.size(); i++){ // show the reports-to-be-uploaded
            if(reportSummaries.size() > 1) // first view by default is uploading row
                addView(getTV(reportSummaries.get(i), false));
        }
        setGravity(Gravity.CENTER_HORIZONTAL);
    }

    public void startUploading(){ // set the uploading row text to the first report summary
        ((TextView) uploadingRow.findViewById(R.id.detailText)).setText(reportSummaries.get(0));
    }
    public TextView getTV(String text, boolean uploaded){
        TextView reportSumm = new TextView(getContext());
        reportSumm.setText(text);
        reportSumm.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
        LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        lp.gravity = Gravity.CENTER_HORIZONTAL;
        float density = getContext().getResources().getDisplayMetrics().density;
        int bottomPadding = (int)density * 10;
        reportSumm.setPadding(0,0,0, bottomPadding);
        if(!uploaded)
            reportSumm.setTextColor(getResources().getColor(R.color.DarkGray));
        else
            reportSumm.setTextColor(getResources().getColor(R.color.LightGreen));
        return reportSumm;
    }

    public void onReportUploaded(int reportUploaded){
        if(reportUploaded < reportSummaries.size()-1){ // if the uploaded report wasn't the last one
            incUploadRowTo(reportUploaded+1);
        } else {
            onFinalUpload();
        }
    }
    // increment Upload Row to index at toIndex
    // moves the uploading row down the list and shows that the previous report has been uploaded
    private void incUploadRowTo(int toIndex){
        // its current contents have been uploaded so get a green textview of the details to reflect this
        TextView uploadedTv = getTV(reportSummaries.get(toIndex-1), true);
        // set the text of the upload row to the text of the next report to be uploaded
        uploadDeets.setText(reportSummaries.get(toIndex));
        removeViewAt(toIndex-1);
        addView(uploadedTv, toIndex - 1);
        // take away the textView at the index to which the reportuploading row is supposed to increment
        removeViewAt(toIndex);
        if(toIndex < reportSummaries.size() - 1) // if the next report to be uploaded isn't the last report
            addView(uploadingRow, toIndex);
        else // otherwise just add the uploading row to the bottom
            addView(uploadingRow);
    }
    private void onFinalUpload(){
        // its current contents have been uploaded so get a green textview of the details to reflect this
        if(reportSummaries.size() > 1){
            TextView uploadedTv = getTV(reportSummaries.get(reportSummaries.size()-1), true);
            removeViewAt(reportSummaries.size() - 1);
            addView(uploadedTv);
        } else{
            removeViewAt(0);
            addView(getTV(reportSummaries.get(0), true));
        }
    }

    public void saveState(Bundle bundle){
        bundle.putInt(TOTAL_REPORTS_KEY, reportSummaries.size());
        bundle.putStringArrayList(REPORT_SUMMARIES_KEY, reportSummaries);
    }
}