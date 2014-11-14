package com.sc.mtaasafi.android.uploading;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;

import com.androidquery.AQuery;
import com.sc.mtaasafi.android.R;

/**
 * Created by Agree on 11/14/2014.
 */
public class UploadingPic extends ImageView implements Animation.AnimationListener{
    boolean uploadSuccessful, uploadStarted;
    Bitmap mThumbnail;
    AQuery aq;
    public UploadingPic(Context context, AttributeSet attrs) {
        super(context, attrs);
        uploadStarted = uploadSuccessful = false;
        aq = new AQuery(getContext());
    }

    @Override
    public void onFinishInflate(){
        Bitmap sourcePic = BitmapFactory.decodeFile((String) getTag());
        mThumbnail = getRoundedThumbnail(sourcePic);
    }
    public void startUpload(){
        uploadStarted = true;
        setImageResource(R.drawable.coralspinny);
        Animation anim = new RotateAnimation(0.0f, 1080.0f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                0.5f);
        anim.setAnimationListener(this);
        anim.setRepeatCount(15);
        anim.setDuration(1000);
        startAnimation(anim);
    }

    public void finishUpload(){
        uploadSuccessful = true;
        clearAnimation();
    }

    @Override
    public void onAnimationStart(Animation animation) {

    }
    // called by the rotate animation, not the alpha
    // fade-in a picture of the thumbnail
    @Override
    public void onAnimationEnd(Animation animation) {
        if(uploadSuccessful){
            setAlpha(0.0f);
            Animation alphaAnim = new AlphaAnimation(0.0f, 1.0f);
            alphaAnim.setDuration(500);
            setImageBitmap(mThumbnail);
            startAnimation(animation);
        }
    }

    @Override
    public void onAnimationRepeat(Animation animation) {

    }
    public Bitmap getRoundedThumbnail(Bitmap sourceBitmap) {
        int targetWidth = getWidth();
        int targetHeight = getHeight();
        Bitmap targetBitmap = Bitmap.createBitmap(targetWidth,
                targetHeight,Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(targetBitmap);
        Path path = new Path();
        path.addCircle(((float) targetWidth - 1) / 2,
                ((float) targetHeight - 1) / 2,
                (Math.min(((float) targetWidth),
                        ((float) targetHeight)) / 2),
                Path.Direction.CCW);
        canvas.clipPath(path);
        canvas.drawBitmap(sourceBitmap,
                new Rect(0, 0, sourceBitmap.getWidth(),
                        sourceBitmap.getHeight()),
                new Rect(0, 0, targetWidth, targetHeight), null);
        return targetBitmap;
    }

}
