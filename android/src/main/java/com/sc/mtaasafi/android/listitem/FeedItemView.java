package com.sc.mtaasafi.android.listitem;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sc.mtaasafi.android.Report;
import com.sc.mtaasafi.android.R;

/**
 * Created by Agree on 9/5/2014.
 */
public class FeedItemView extends RelativeLayout {
    private LayoutInflater inflater;
    public TextView detailsTV, titleTV, timeElapsedTV;
    public ImageButton upvote;
    public TextView voteCountTV;
    final Report mReport;
    public int position;

    public FeedItemView(Context context, Report report, int pos) {
        super(context);
        position = pos;
        mReport = report;
        inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.feed_item_view, this, true);
        setViewData(report);
    }
    
    public void setViewData(Report report) {
        titleTV = (TextView) findViewById(R.id.itemTitle);
        titleTV.setText(report.title);

        detailsTV = (TextView) findViewById(R.id.itemDetails);
        detailsTV.setText(briefDetails(report.details));

        timeElapsedTV = (TextView) findViewById(R.id.timeElapsed);
        timeElapsedTV.setText(report.timeElapsed);

//        upvote = (ImageButton) findViewById(R.id.upvoteCount);
//        if(report.iUpvoted)
//            upvote.setImageResource(R.drawable.button_upvote_clicked);
//        else
//            upvote.setImageResource(R.drawable.button_upvote_unclicked);
//
//        voteCountTV = (TextView) findViewById(R.id.upvoteCount);
//        voteCountTV.setText(Integer.toString(report.voteCount));
    }

    public String briefDetails(String origDetails){
        if(origDetails.length() > 140)
            return origDetails.substring(0, 139);
        else
            return origDetails;
    }
}
