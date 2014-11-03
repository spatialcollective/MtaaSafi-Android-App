package com.sc.mtaasafi.android.feed;

import android.app.Activity;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.os.RemoteException;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sc.mtaasafi.android.R;
import com.sc.mtaasafi.android.Report;
import com.sc.mtaasafi.android.database.ReportContract;
import com.sc.mtaasafi.android.newReport.NewReportActivity;

import java.util.ArrayList;

/**
 * Created by Agree on 10/10/2014.
 */

public class VoteInterface extends LinearLayout {
    TextView voteCountTV;
    ImageButton upvote;
    public VoteInterface(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void onFinishInflate(){
        voteCountTV = (TextView) findViewById(R.id.upvoteCount);
        upvote = (ImageButton) findViewById(R.id.upvoteButton);
        upvote.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>();

                    // tell the reports table that you upvoted the report
                    int reportDBId = (Integer) view.getTag();
                    ContentProviderOperation.Builder reportOperation =
                            ContentProviderOperation.newUpdate(Report.uriFor(reportDBId));
                    reportOperation.withValue(ReportContract.Entry.COLUMN_USER_UPVOTED, 1);
                    batch.add(reportOperation.build());
                    // TODO: check for multiple reports entered in the db? (maybe)
                    // tell the upvote log table that you upvoted the report
                    int reportServerId = (Integer) voteCountTV.getTag();
                    ContentProviderOperation.Builder upvoteOperation =
                            ContentProviderOperation.newInsert(ReportContract.UpvoteLog.UPVOTE_URI);
                    upvoteOperation.withValue(ReportContract.UpvoteLog.COLUMN_SERVER_ID, reportServerId);
                    batch.add(upvoteOperation.build());

                    getContext().getContentResolver().applyBatch(ReportContract.CONTENT_AUTHORITY, batch);
                    getContext().getContentResolver()
                            .notifyChange(ReportContract.UpvoteLog.UPVOTE_URI, null, false);
                    voteCountTV.setTextColor(getResources().getColor(R.color.mtaa_safi_blue));
//                    Cursor upvotedReports =  getContext().getContentResolver().query(ReportContract.UpvoteLog.UPVOTE_URI, null, null,null, null);
//                    Log.e("Upvoted reports", "Upvoted reports count: " + upvotedReports.getCount());
//                    while(upvotedReports.moveToNext()){
//                        Log.e("Upvoted reports",
//                                "Upvoted report id: " + upvotedReports.getInt(upvotedReports.getColumnIndex(ReportContract.UpvoteLog.COLUMN_ID)));
//                    }
//                    upvotedReports.close();
                } catch (RemoteException e) {
                    e.printStackTrace();
                } catch (OperationApplicationException e) {
                    e.printStackTrace();
                }
                // tell the upvote log table you voted

            }
        });
    }

//    public void update(Report report){
//        if(report.iUpvoted)
//            upvote.setImageResource(R.drawable.button_upvote_clicked);
//        else
//            upvote.setImageResource(R.drawable.button_upvote_unclicked);
//    }
}
