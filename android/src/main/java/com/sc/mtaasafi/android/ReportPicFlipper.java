package com.sc.mtaasafi.android;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ViewFlipper;

/**
 * Created by Agree on 10/8/2014.
 */
class ReportPicFlipper extends ViewFlipper {
    // Adapted from: http://stackoverflow.com/questions/5975963/swipe-listeners-in-android
    // and: http://androidtuts4u.blogspot.com/2013/03/swipe-or-onfling-event-android.html
    private Animation mInFromRight;
    private Animation mOutToLeft;
    private Animation mInFromLeft;
    private Animation mOutToRight;
    class CustomGestureListener extends GestureDetector.SimpleOnGestureListener implements OnTouchListener{
        ReportPicFlipper mFlipper;
        private static final int SWIPE_MIN_DISTANCE = 120;
        private static final int SWIPE_MAX_OFF_PATH = 250;
        private static final int SWIPE_THRESHOLD_VELOCITY = 200;
        private GestureDetector detector;
        public CustomGestureListener(ReportPicFlipper rpf) {
            mFlipper = rpf;
            detector = new GestureDetector(rpf.getContext(), this);
        }

        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                               float velocityY) {
            // TODO: add in and out animations
            if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
                return false;
            if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE
                    && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                mFlipper.showNext();
            } else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE
                    && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                mFlipper.showPrevious();
            }
            return super.onFling(e1, e2, velocityX, velocityY);
        }

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            return detector.onTouchEvent(motionEvent);
        }
    }



    private CustomGestureListener mDetector;

    public ReportPicFlipper(Context context, AttributeSet attrs) {
        super(context, attrs);
        setup();
    }

    public ReportPicFlipper(Context context) {
        super(context);
        setup();
    }
    private void setup(){
        mDetector = new CustomGestureListener(this);
        setOnTouchListener(mDetector);
    }

}