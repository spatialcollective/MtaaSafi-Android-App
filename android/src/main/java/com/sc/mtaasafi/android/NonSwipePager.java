package com.sc.mtaasafi.android;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by Agree on 10/6/2014.
 */
public class NonSwipePager extends android.support.v4.view.ViewPager {

    public NonSwipePager(Context context) {
        super(context);
    }

    public NonSwipePager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent arg0) {
        // Never allow swiping to switch between pages
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Never allow swiping to switch between pages
        return false;
    }

}
