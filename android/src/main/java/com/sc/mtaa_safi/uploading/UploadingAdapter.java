package com.sc.mtaa_safi.uploading;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sc.mtaa_safi.R;
import android.support.v7.widget.RecyclerView;
import com.sc.mtaa_safi.common.RecyclerViewCursorAdapter;
import com.sc.mtaa_safi.Report;
import com.sc.mtaa_safi.SystemUtils.Utils;
import com.sc.mtaa_safi.database.Contract;

import java.util.ArrayList;

public class UploadingAdapter extends RecyclerViewCursorAdapter<UploadingAdapter.ViewHolder> {
    Context mContext;
    Paint mPaint;
    Rect mRect;
    Drawable highlight, regular;
    int image_px, gap_px;

    public UploadingAdapter(Context context, Cursor c) {
        super(context, c);
        mContext = context;
        highlight = context.getResources().getDrawable(R.drawable.border_selected);
        regular = context.getResources().getDrawable(R.drawable.border);
        image_px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60, context.getResources().getDisplayMetrics());
        gap_px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, context.getResources().getDisplayMetrics());
        mRect = new Rect(0, 0, image_px, image_px);
        createPaint();
    }

    @Override
    public UploadingAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.upload_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, Cursor c) {
        holder.mTitleView.setText(c.getString(c.getColumnIndex(Contract.Entry.COLUMN_DESCRIPTION)));
        holder.mTime.setText(Utils.getElapsedTime(c.getLong(c.getColumnIndex(Contract.Entry.COLUMN_TIMESTAMP))));
        addImages(holder, Report.getMediaList(c.getString(c.getColumnIndex(Contract.Entry.COLUMN_MEDIA))));

        final boolean isUploading = setInProgress(holder, c);
        final int dbId = c.getInt(c.getColumnIndex(Contract.Entry.COLUMN_ID));
        holder.mListener = new UploadingAdapter.ViewHolder.ViewHolderClicks() {
            public void delete(Button deleteBtn) { deleteFromDb(dbId, isUploading); }; };
    }

    private void addImages(ViewHolder holder, ArrayList<String> media) {
        for (int i = 0; i < holder.mImageRow.getChildCount(); i++) {
            holder.mImageRow.getChildAt(i).findViewById(R.id.pic_progress).setVisibility(View.INVISIBLE);
            if (media.size() >= i + 1) {
                addThumb(holder.mImageRow.getChildAt(i), media.get(i));
                holder.mImageRow.getChildAt(i).setVisibility(View.VISIBLE);
            } else
                holder.mImageRow.getChildAt(i).setVisibility(View.INVISIBLE);
        }
    }
    private void addThumb(View thumbView, String picPath) {
        Bitmap thumb = BitmapFactory.decodeFile(picPath);
        if (picPath != null && thumb != null && thumbView != null) {
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

    private boolean setInProgress(ViewHolder holder, Cursor c) {
        if (c.getInt(c.getColumnIndex(Contract.Entry.COLUMN_UPLOAD_IN_PROGRESS)) == 1) {
            holder.mRoot.setBackgroundDrawable(highlight);
            holder.mRoot.setPadding(16, 16, 16, 16);
            holder.mImageRow.getChildAt(c.getInt(c.getColumnIndex(Contract.Entry.COLUMN_PENDINGFLAG)) - 1)
                    .findViewById(R.id.pic_progress).setVisibility(View.VISIBLE);
            return true;
        }
        holder.mRoot.setBackgroundDrawable(regular);
        return false;
    }

    private void deleteFromDb(int id, boolean isUploading) {
        if (isUploading) {
            ReportUploadingFragment frag = (ReportUploadingFragment) ((UploadingActivity) mContext).getSupportFragmentManager()
                    .findFragmentByTag(UploadingActivity.UPLOAD_TAG);
            frag.cancelSession(ReportUploader.DELETE_BUTTON);
        }
        mContext.getContentResolver().delete(Report.getUri(id), null, null);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public ViewHolderClicks mListener;
        public View mRoot;
        public TextView mTitleView, mTime;
        public LinearLayout mImageRow;
        public Button mDeleteButton;

        public static interface ViewHolderClicks { public void delete(Button deleteBtn); }

        public ViewHolder(View v) {
            super(v);
            mRoot = v.findViewById(R.id.upload_row);
            mTitleView = (TextView) v.findViewById(R.id.uploadingContent);
            mTime = (TextView) v.findViewById(R.id.uploadingTime);
            mImageRow = (LinearLayout) v.findViewById(R.id.uploading_pic_row);
            mDeleteButton = (Button) v.findViewById(R.id.deleteReportButton);
            mDeleteButton.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (v instanceof Button)
                mListener.delete((Button) v);
        }
    }

    private void createPaint() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        ColorMatrix cm = new ColorMatrix();
//        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        mPaint.setColorFilter(f);
    }
}
