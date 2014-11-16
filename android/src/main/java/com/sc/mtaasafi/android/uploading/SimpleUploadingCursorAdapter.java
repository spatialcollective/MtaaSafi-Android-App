package com.sc.mtaasafi.android.uploading;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.sc.mtaasafi.android.R;
import com.sc.mtaasafi.android.database.Contract;

/**
 * Created by david on 11/2/14.
 */
public class SimpleUploadingCursorAdapter extends SimpleCursorAdapter {

    Context mContext;
    AQuery aq;

    public SimpleUploadingCursorAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flag) {
        super(context, layout, c, from, to);
        aq = new AQuery(context);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        super.bindView(view, context, cursor);
        mContext = context;
        resetState(view);
        indicateRow(cursor.getInt(cursor.getColumnIndex(Contract.Entry.COLUMN_UPLOAD_IN_PROGRESS)), view);
        int progress = cursor.getInt(cursor.getColumnIndex(Contract.Entry.COLUMN_PENDINGFLAG));
        restoreProgress(progress, view);
    }
    // Restores a progress point that has already been reached
    private void restoreProgress(int progress, View row){
        int restoreTo = progress-1;
        switch(restoreTo){
            case 3:
                UploadingPic uP = (UploadingPic) row.findViewById(R.id.uploadingPic1);
                aq.id(uP).image((String)uP.getTag());
            case 2:
                UploadingPic uP2 = (UploadingPic) row.findViewById(R.id.uploadingPic2);
                aq.id(uP2).image((String)uP2.getTag());
            case 1:
                UploadingPic uP1 = (UploadingPic) row.findViewById(R.id.uploadingPic1);
                aq.id(uP1).image((String)uP1.getTag());
            case 0:
                showUploadStarted(row);
        }
        updateProgressView(progress, row);
    }
    public void resetView(View row) {
        row.setBackgroundColor(Color.WHITE);
        ((TextView) row.findViewById(R.id.uploadingContent))
                .setTextColor(mContext.getResources().getColor(R.color.LightGrey));
        ((TextView) row.findViewById(R.id.uploadingTime)).setTextColor(mContext.getResources().getColor(R.color.LightGrey));
        row.findViewById(R.id.uploading_pic_row).setVisibility(View.GONE);
    }

    public void indicateRow(int uploadInProgress, View row) {
        Log.e("Adapter", "indicateRow. Upload in progress "+ uploadInProgress);
        if (uploadInProgress == 1){
            showUploadStarted(row);
        } else
            resetView(row);
    }
    private void showUploadStarted(View row){
        row.findViewById(R.id.uploading_pic_row).setVisibility(View.VISIBLE);
        ((TextView) row.findViewById(R.id.uploadingContent))
                .setTextColor(mContext.getResources().getColor(R.color.textDarkGray));
        ((TextView) row.findViewById(R.id.uploadingContent))
                .setTextSize(TypedValue.COMPLEX_UNIT_DIP, 25);
        ((TextView) row.findViewById(R.id.uploadingTime))
                .setTextColor(mContext.getResources().getColor(R.color.textDarkGray));

    }
// view = the whole row
    public void updateProgressView(int progress, View row){
        Log.e("Adapter", "updateProgressView. Row id == R.id.upload_row: " +
                (row.getId() == R.id.upload_row));
        if(row != null){
            switch (progress) {
                case 1:
                    row.findViewById(R.id.uploading_pic_row).setVisibility(View.VISIBLE);
                    ((UploadingPic) row.findViewById(R.id.uploadingPic1)).startUpload();
                    break;
                case 2:
                    ((UploadingPic) row.findViewById(R.id.uploadingPic1)).finishUpload();
                    ((UploadingPic) row.findViewById(R.id.uploadingPic2)).startUpload();
                    break;
                case 3:
                    ((UploadingPic) row.findViewById(R.id.uploadingPic2)).finishUpload();
                    ((UploadingPic) row.findViewById(R.id.uploadingPic3)).startUpload();
                    break;
                case -1:
                    ((UploadingPic) row.findViewById(R.id.uploadingPic3)).finishUpload();
            }
        }
    }

    private void updateState(View view, int doneProgressId, int doneViewId, int workingId, int drawable) {
        if (view != null) {
            if (doneViewId != 0 && drawable != 0)
                ((ImageView) view.findViewById(doneViewId)).setImageResource(drawable);
            if (workingId != 0)
                view.findViewById(workingId).setVisibility(View.VISIBLE);
            else{
                view.findViewById(R.id.uploadSuccessText).setVisibility(View.VISIBLE);
                AlphaAnimation anim = new AlphaAnimation(1.0f, 0.0f);
                anim.setDuration(600);
                anim.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {}
                    @Override
                    public void onAnimationEnd(Animation animation) {
                        // notify data set changed?
                        // TODO: move update Database call into this class. Current arch doesn't
                        // allow this feature...
                    }
                    @Override
                    public void onAnimationRepeat(Animation animation) {}
                });
                view.startAnimation(anim);
            }
            if (doneProgressId != 0)
                view.findViewById(doneProgressId).setVisibility(View.INVISIBLE);
        }
    }

    // called on rows for report that aren't currently uploading
    private void resetState(View view) {
        // set text to light grey and hide the uploading pic row
        TextView content = (TextView) view.findViewById(R.id.uploadingContent);
        content.setTextColor(mContext.getResources().getColor(R.color.LightGrey));
        TextView time = (TextView) view.findViewById(R.id.uploadingTime);
        time.setTextColor(mContext.getResources().getColor(R.color.LightGrey));
        view.findViewById(R.id.uploading_pic_row).setVisibility(View.GONE);
    }
}
