package com.sc.mtaasafi.android.uploading;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.util.Log;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxStatus;
import com.androidquery.callback.BitmapAjaxCallback;
import com.androidquery.callback.ImageOptions;
import com.sc.mtaasafi.android.R;
import com.sc.mtaasafi.android.SystemUtils.PrefUtils;
import com.sc.mtaasafi.android.database.Contract;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Agree on 11/14/2014.
 * UploadingPic tags are mediaPath Strings
 */
public class UploadingPic extends ImageView implements Animation.AnimationListener {
    boolean uploadSuccessful, uploadStarted;
    public Bitmap mThumb;

    public UploadingPic(Context context, AttributeSet attrs) {
        super(context, attrs);
        uploadStarted = uploadSuccessful = false;
    }

//    @Override
//    protected void onDraw(Canvas canvas) {
//        Log.e("on Draw", "Drawing");
//        BitmapDrawable bitmapDrawable = (BitmapDrawable) this.getDrawable();
//        if (bitmapDrawable != null)
//            mThumb = bitmapDrawable.getBitmap();
//
//        BitmapShader shader;
//        shader = new BitmapShader(mThumb, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
//
//        Paint paint = new Paint();
//        paint.setAntiAlias(true);
//        paint.setShader(shader);
//
//        canvas.drawCircle(getWidth()/2, getHeight()/2, 25, paint);
//        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
//        Log.e("on Draw", "Done");
//        final Rect rect = new Rect(0, 0, mThumb.getWidth(), mThumb.getHeight());
//
//        canvas.drawBitmap(mThumb, rect, rect, paint);
//    }

    public void startUpload(){
        uploadStarted = true;
        setImageResource(R.drawable.coralspinny);
        startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.rotate));
    }

    public void finishUpload(){
        uploadSuccessful = true;
        AQuery aq = new AQuery(getContext());
        setImageResource(R.drawable.loading_uploadthumbnail);
        aq.id(this).image((String) getTag(), true, true, 0, 0, null, AQuery.FADE_IN);
        clearAnimation();
    }

    @Override
    public void onAnimationStart(Animation animation){}

    // called by the rotate animation, not the alpha
    // fade-in a picture of the thumbnail
    @Override
    public void onAnimationEnd(Animation animation) {
//        if(uploadSuccessful){
//            Animation alphaAnim = new AlphaAnimation(0.0f, 1.0f);
//            alphaAnim.setDuration(500);
//            startAnimation(animation);
//        } else
//            Log.e("upload", "not successful");
    }
    @Override
    public void onAnimationRepeat(Animation animation) {}
}
