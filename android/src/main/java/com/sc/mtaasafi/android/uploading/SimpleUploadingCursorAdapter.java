package com.sc.mtaasafi.android.uploading;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.sc.mtaasafi.android.R;
import com.sc.mtaasafi.android.Report;
import com.sc.mtaasafi.android.database.ReportContract;

/**
 * Created by david on 11/2/14.
 */
public class SimpleUploadingCursorAdapter extends SimpleCursorAdapter {

    Context mContext;

    public SimpleUploadingCursorAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flag) {
        super(context, layout, c, from, to);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        super.bindView(view, context, cursor);
        mContext = context;
        indicateRow(cursor.getInt(cursor.getColumnIndex(ReportContract.Entry.COLUMN_UPLOAD_IN_PROGRESS)), view);
        updateProgressView(cursor.getInt(cursor.getColumnIndex(ReportContract.Entry.COLUMN_PENDINGFLAG)), view);
    }

    public void resetView(View row) {
        row.setMinimumHeight(0);
        row.setBackgroundColor(Color.WHITE);
        ((TextView) row.findViewById(R.id.itemDetails)).setTextColor(Color.BLACK);
        ((TextView) row.findViewById(R.id.timeElapsed)).setTextColor(Color.BLACK);
        row.findViewById(R.id.expanded_layout).setVisibility(View.GONE);
    }

    public void indicateRow(int uploadInProgress, View row) {
        if (uploadInProgress == 1) {
            row.setMinimumHeight(200);
            row.setBackgroundColor(mContext.getResources().getColor(R.color.mtaa_safi_blue));
            ((TextView) row.findViewById(R.id.itemDetails)).setTextColor(Color.WHITE);
            ((TextView) row.findViewById(R.id.timeElapsed)).setTextColor(Color.WHITE);
            row.findViewById(R.id.expanded_layout).setVisibility(View.VISIBLE);
        } else { resetView(row); };
    }

    public void updateProgressView(int progress, View view){
        switch (progress) {
            case 0:
                updateState(view, 0, 0, R.id.progressBarReportText, 0);
                break;
            case 1:
                updateState(view, R.id.progressBarReportText, R.id.reportUploadingIcon, R.id.progressBarPic1, R.drawable.report_uploaded);
                break;
            case 2:
                updateState(view, R.id.progressBarPic1, R.id.pic1UploadingIcon, R.id.progressBarPic2, R.drawable.pic1_uploaded);
                break;
            case 3:
                updateState(view, R.id.progressBarPic2, R.id.pic2UploadingIcon, R.id.progressBarPic3, R.drawable.pic2_uploaded);
                break;
            case -1:
                updateState(view, R.id.progressBarPic3, R.id.pic3UploadingIcon, 0, R.drawable.pic3_uploaded);
        }
    }

    private void updateState(View view, int doneProgressId, int doneViewId, int workingId, int drawable) {
        if (view != null) {
            if (doneViewId != 0 && drawable != 0)
                ((ImageView) view.findViewById(doneViewId)).setImageResource(drawable);
            if (workingId != 0)
                view.findViewById(workingId).setVisibility(View.VISIBLE);
            if (doneProgressId != 0)
                view.findViewById(doneProgressId).setVisibility(View.INVISIBLE);
        }
    }
}
