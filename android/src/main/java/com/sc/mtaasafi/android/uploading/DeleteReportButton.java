package com.sc.mtaasafi.android.uploading;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;

import com.sc.mtaasafi.android.Report;
import com.sc.mtaasafi.android.database.Contract;

/**
 * Created by Agree on 11/11/2014.
 */
public class DeleteReportButton extends ImageButton {
    // NOTE: must set tag to the id of the report in the database
    public DeleteReportButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                int dbId = (Integer) getTag();
                UploadingActivity ua = (UploadingActivity) getContext();
                ua.getContentResolver().delete(Report.getUri(dbId), null, null);
                ReportUploadingFragment ruf = (ReportUploadingFragment)
                        ua.getSupportFragmentManager().findFragmentByTag(UploadingActivity.UPLOAD_TAG);
                ruf.mAdapter.notifyDataSetChanged();
            }
        });
    }
}
