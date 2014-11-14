package com.sc.mtaasafi.android.uploading;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.sc.mtaasafi.android.R;
import com.sc.mtaasafi.android.database.Contract;

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
        resetState(view);
        indicateRow(cursor.getInt(cursor.getColumnIndex(Contract.Entry.COLUMN_UPLOAD_IN_PROGRESS)), view);
        int progress = cursor.getInt(cursor.getColumnIndex(Contract.Entry.COLUMN_PENDINGFLAG));
        for (int i = 0; i <= progress; i++)
            updateProgressView(i, view);
    }

    public void resetView(View row) {
        row.setMinimumHeight(0);
        row.setBackgroundColor(Color.WHITE);
        ((TextView) row.findViewById(R.id.itemDetails)).setTextColor(Color.BLACK);
        ((TextView) row.findViewById(R.id.timeElapsed)).setTextColor(Color.BLACK);
        row.findViewById(R.id.expanded_layout).setVisibility(View.GONE);
    }

    public void indicateRow(int uploadInProgress, View row) {
        if (uploadInProgress == 1)
            row.findViewById(R.id.uploading_pic_row).setVisibility(View.VISIBLE);
        else
            resetView(row);
    }
// view = the whole row
    public void updateProgressView(int progress, View view){
        switch (progress) {
            case 0:
                TextView content = (TextView) view.findViewById(R.id.uploadingContent);
                content.setTextColor(mContext.getResources().getColor(R.color.black));
                TextView time = (TextView) view.findViewById(R.id.uploadingTime);
                time.setTextColor(mContext.getResources().getColor(R.color.black));
                break;
            case 1:
                ((UploadingPic) view.findViewById(R.id.uploadingPic1)).startUpload();
                break;
            case 2:
                ((UploadingPic) view.findViewById(R.id.uploadingPic1)).finishUpload();
                ((UploadingPic) view.findViewById(R.id.uploadingPic2)).startUpload();
                break;
            case 3:
                ((UploadingPic) view.findViewById(R.id.uploadingPic2)).finishUpload();
                ((UploadingPic) view.findViewById(R.id.uploadingPic3)).startUpload();
                break;
            case -1:
                ((UploadingPic) view.findViewById(R.id.uploadingPic3)).finishUpload();
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
