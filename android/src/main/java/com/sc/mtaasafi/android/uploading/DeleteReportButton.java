package com.sc.mtaasafi.android.uploading;

import android.content.Context;
import android.database.Cursor;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;

import com.sc.mtaasafi.android.Report;
import com.sc.mtaasafi.android.database.ReportContract;

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
                String[] projection = new String[1];
                projection[0] = ReportContract.Entry.COLUMN_UPLOAD_IN_PROGRESS;
                UploadingActivity ua = (UploadingActivity) getContext();
                ReportUploadingFragment ruf = (ReportUploadingFragment)
                        ua.getSupportFragmentManager().findFragmentByTag(UploadingActivity.UPLOAD_TAG);
                ruf.mAdapter.notifyDataSetChanged();
                Cursor c = ua.getContentResolver().query(ReportContract.Entry.CONTENT_URI, projection, ReportContract.Entry.COLUMN_SERVER_ID + " = " + dbId, null, null);
                if(c.moveToNext()){
                    boolean isUploading =
                            c.getInt(c.getColumnIndex(ReportContract.Entry.COLUMN_UPLOAD_IN_PROGRESS)) > 0;
                    if(isUploading)
                        ruf.uploader.deleteReport();
                }
                c.close();
                ua.getContentResolver().delete(Report.uriFor(dbId), null, null);

            }
        });
    }
}
