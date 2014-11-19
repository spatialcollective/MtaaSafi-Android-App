package com.sc.mtaasafi.android.uploading;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
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
    Paint mPaint;
    Rect mRect;
    int image_px, gap_px;

    public SimpleUploadingCursorAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flag) {
        super(context, layout, c, from, to);
        mContext = context;
        aq = new AQuery(context);
        image_px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60, context.getResources().getDisplayMetrics());
        gap_px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, context.getResources().getDisplayMetrics());
        mRect = new Rect(0, 0, image_px, image_px);
        createPaint();
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // super.bindView(view, context, cursor);
        addDeleteListener(view, cursor);
        resetRow(view);

        boolean inProgress = cursor.getInt(cursor.getColumnIndex(Contract.Entry.COLUMN_UPLOAD_IN_PROGRESS)) == 1;
        int progress = cursor.getInt(cursor.getColumnIndex(Contract.Entry.COLUMN_PENDINGFLAG));
        if (inProgress) {
            indicateRow(view);
            addImages(view, cursor);
            for (int i = 0; i <= progress; i++)
                updateProgressView(progress, view);
        }
    }

    private void addImages(View row, Cursor cursor) {
        addThumb(row.findViewById(R.id.upload_pic_1), cursor.getString(cursor.getColumnIndex(Contract.Entry.COLUMN_MEDIAURL1)));
        addThumb(row.findViewById(R.id.upload_pic_2), cursor.getString(cursor.getColumnIndex(Contract.Entry.COLUMN_MEDIAURL2)));
        addThumb(row.findViewById(R.id.upload_pic_3), cursor.getString(cursor.getColumnIndex(Contract.Entry.COLUMN_MEDIAURL3)));
    }
    private void addThumb(View thumbView, String picPath) {
        Bitmap thumb = BitmapFactory.decodeFile(picPath);
        if (thumb != null) {
            thumb = Bitmap.createScaledBitmap(thumb, image_px, image_px, false);
            Bitmap circleThumb = createCircleThumb(thumb);
            ((ImageView) thumbView.findViewById(R.id.pic_image)).setImageBitmap(circleThumb);
        }
    }
    private Bitmap createCircleThumb(Bitmap input) {
        Bitmap output = Bitmap.createBitmap(image_px, image_px, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        mPaint.setXfermode(null);
        canvas.drawCircle(image_px/2, image_px/2, image_px/2 - gap_px, mPaint);
        mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(input, mRect, mRect, mPaint);
        return output;
    }

    public void resetRow(View row) {
        ((TextView) row.findViewById(R.id.uploadingContent))
                .setTextColor(mContext.getResources().getColor(R.color.LightGrey));
        ((TextView) row.findViewById(R.id.uploadingTime))
                .setTextColor(mContext.getResources().getColor(R.color.LightGrey));
        row.findViewById(R.id.uploading_pic_row).setVisibility(View.GONE);
    }

    public void indicateRow(View row){
        row.findViewById(R.id.uploading_pic_row).setVisibility(View.VISIBLE);
        ((TextView) row.findViewById(R.id.uploadingContent))
                .setTextColor(mContext.getResources().getColor(R.color.textDarkGray));
        ((TextView) row.findViewById(R.id.uploadingContent))
                .setTextSize(TypedValue.COMPLEX_UNIT_DIP, 25);
        ((TextView) row.findViewById(R.id.uploadingTime))
                .setTextColor(mContext.getResources().getColor(R.color.textDarkGray));
    }

    public void updateProgressView(int progress, View row){
        Log.e("Adapter", "updateProgressView. Row id == R.id.upload_row: " + (row.getId() == R.id.upload_row));
        if (row != null) {
            switch (progress) {
                case 1:
                    row.findViewById(R.id.upload_pic_1).findViewById(R.id.pic_progress).setVisibility(View.VISIBLE);
                    break;
                case 2:
                    row.findViewById(R.id.upload_pic_1).findViewById(R.id.pic_progress).setVisibility(View.INVISIBLE);
                    row.findViewById(R.id.upload_pic_2).findViewById(R.id.pic_progress).setVisibility(View.VISIBLE);
                    break;
                case 3:
                    row.findViewById(R.id.upload_pic_2).findViewById(R.id.pic_progress).setVisibility(View.INVISIBLE);
                    row.findViewById(R.id.upload_pic_3).findViewById(R.id.pic_progress).setVisibility(View.VISIBLE);
                    break;
                case -1:
                    row.findViewById(R.id.upload_pic_3).findViewById(R.id.pic_progress).setVisibility(View.INVISIBLE);
            }
        }
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

    private void createPaint() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        mPaint.setColorFilter(f);
    }
}
