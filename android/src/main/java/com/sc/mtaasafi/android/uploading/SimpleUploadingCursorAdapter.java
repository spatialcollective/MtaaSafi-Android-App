package com.sc.mtaasafi.android.uploading;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.sc.mtaasafi.android.R;
import com.sc.mtaasafi.android.Report;
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
        // super.bindView(view, context, cursor);
        mContext = context;
        addDeleteListener(view, cursor);
        resetState(view);
        indicateRow(cursor.getInt(cursor.getColumnIndex(Contract.Entry.COLUMN_UPLOAD_IN_PROGRESS)), view);
        int progress = cursor.getInt(cursor.getColumnIndex(Contract.Entry.COLUMN_PENDINGFLAG));
        restoreProgress(progress, view, cursor);
    }

    private void restoreProgress(int progress, View row, Cursor cursor) {
        View pic_1 = row.findViewById(R.id.upload_pic_3);
        ImageView image = (ImageView) pic_1.findViewById(R.id.pic_image);

        Bitmap thumb = BitmapFactory.decodeFile(cursor.getString(cursor.getColumnIndex(Contract.Entry.COLUMN_MEDIAURL3)));

        float image_px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60, mContext.getResources().getDisplayMetrics());
        float gap_px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, mContext.getResources().getDisplayMetrics());
        Bitmap output = Bitmap.createBitmap((int) image_px, (int) image_px, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setShader(new BitmapShader(thumb, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));
        canvas.drawCircle(image_px/2, image_px/2, image_px/2 - gap_px, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        final Rect rect = new Rect(0, 0, (int) image_px, (int) image_px);
        canvas.drawBitmap(thumb, rect, rect, paint);

        image.setImageBitmap(output);

        showUploadStarted(row);
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
                    @Override public void onAnimationStart(Animation animation) {}
                    @Override public void onAnimationEnd(Animation animation) {
                        // notify data set changed?
                        // TODO: move update Database call into this class. Current arch doesn't
                        // allow this feature...
                    }
                    @Override
                    public void onAnimationRepeat(Animation animation) {}
                });
                view.startAnimation(anim);
            }
        }
    }

    public void updateProgressView(int progress, View row){
        Log.e("Adapter", "updateProgressView. Row id == R.id.upload_row: " +
                (row.getId() == R.id.upload_row));
//         if (row != null) {
//             switch (progress) {
//                 case 1:
//                     row.findViewById(R.id.uploading_pic_row).setVisibility(View.VISIBLE);
//                     ((UploadingPic) row.findViewById(R.id.uploadingPic1)).startUpload();
//                     break;
//                 case 2:
//                     ((UploadingPic) row.findViewById(R.id.uploadingPic1)).finishUpload();
// //                    ((UploadingPic) row.findViewById(R.id.uploadingPic2)).startUpload();
//                     break;
//                 case 3:
// //                    ((UploadingPic) row.findViewById(R.id.uploadingPic2)).finishUpload();
//                     ((UploadingPic) row.findViewById(R.id.uploadingPic3)).startUpload();
//                     break;
//                 case -1:
//                     ((UploadingPic) row.findViewById(R.id.uploadingPic3)).finishUpload();
//             }
//         }
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

    private void addDeleteListener(View view, Cursor c) {
        Log.e("Simple Cursor", "adding click listener");
        final int dbId = c.getInt(c.getColumnIndex(Contract.Entry.COLUMN_ID));
        final boolean isUploading = c.getInt(c.getColumnIndex(Contract.Entry.COLUMN_UPLOAD_IN_PROGRESS)) > 0;
        ImageButton delete = (ImageButton) view.findViewById(R.id.deleteReportButton);
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e("Simple Cursor", "delete clicked");
                if (isUploading) {
                    ReportUploadingFragment frag = (ReportUploadingFragment) ((UploadingActivity) mContext).getSupportFragmentManager()
                            .findFragmentByTag(UploadingActivity.UPLOAD_TAG);
                    frag.cancelSession(ReportUploader.DELETE_BUTTON);
                }
                int rowsDeleted = mContext.getContentResolver().delete(Report.getUri(dbId), null, null);
                notifyDataSetChanged();
                Log.e("Simple Cursor", "delete finished... " + rowsDeleted );
            }
        });
    }
}
