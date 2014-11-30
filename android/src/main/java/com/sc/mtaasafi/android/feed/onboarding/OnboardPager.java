package com.sc.mtaasafi.android.feed.onboarding;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by Agree on 11/30/2014.
 */
public class OnboardPager extends ViewPager {
    public boolean swipeEnabled;
    public OnboardPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        swipeEnabled = false;
    }
    @Override
    public boolean onInterceptTouchEvent(MotionEvent arg0) {
        if(swipeEnabled)
            return super.onInterceptTouchEvent(arg0);
        return false;
    }
}
