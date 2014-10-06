package com.sc.mtaasafi.android.listitem;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sc.mtaasafi.android.LogTags;
import com.sc.mtaasafi.android.MainActivity;
import com.sc.mtaasafi.android.Report;
import com.sc.mtaasafi.android.R;

import java.util.List;

/**
 * Created by Agree on 9/5/2014.
 */
public class FeedItemView extends RelativeLayout {
    private LayoutInflater inflater;
    public TextView detailsTV, titleTV, timeElapsedTV;
    public ImageView picsAttachedIcon;
    final Report mReport;
    public int position;

    public FeedItemView(Context context, Report report, int pos) {
        super(context);
        position = pos;
        mReport = report;
        inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.feed_item, this, true);
        setViewData(report);
    }
    
    public void setViewData(Report report) {
        titleTV = (TextView) findViewById(R.id.itemTitle);
        titleTV.setText(report.title);
        detailsTV = (TextView) findViewById(R.id.itemDetails);
        detailsTV.setText(briefDetails(report.details));
        timeElapsedTV = (TextView) findViewById(R.id.timeElapsed);
        timeElapsedTV.setText(report.timeElapsed);
        if (report.mediaURL == null || report.mediaURL.equals("") || report.mediaURL.equals("null"))
            picsAttachedIcon.setVisibility(View.INVISIBLE);
        else{
            picsAttachedIcon.setVisibility(View.VISIBLE);
        }
    }

    public String briefDetails(String origDetails){
        if(origDetails.length() > 140)
            return origDetails.substring(0, 139);
        else
            return origDetails;
    }
}
