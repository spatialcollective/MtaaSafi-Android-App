package com.sc.mtaasafi.android.uploading;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.Rect;
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

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Agree on 11/14/2014.
 * UploadingPic tags are mediaPath Strings
 */
public class UploadingPic extends ImageView implements Animation.AnimationListener{
    boolean uploadSuccessful, uploadStarted;
    public Bitmap mThumb;
    public UploadingPic(Context context, AttributeSet attrs) {
        super(context, attrs);
        uploadStarted = uploadSuccessful = false;
        setScaleType(ScaleType.CENTER_CROP);
    }

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
