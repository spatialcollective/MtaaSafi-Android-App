package com.sc.mtaasafi.android.feed.onboarding;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.RelativeLayout;

import com.sc.mtaasafi.android.R;
import com.sc.mtaasafi.android.feed.VoteInterface;

/**
 * Created by Agree on 11/28/2014.
 */
public class OnboardDoneFragment extends Fragment implements Animation.AnimationListener {
    Animation fadeIn;
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedState){
        super.onCreateView(inflater, container, savedState);
        View view = inflater.inflate(R.layout.fragment_onboarding_feed, container, false);
        fadeIn = new AlphaAnimation(0f, 1f);
        fadeIn.setDuration(500);
        fadeIn.setStartOffset(500);
        return view;
    }
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {}
    }

    @Override
    public void onAnimationEnd(Animation animation) {}
    @Override public void onAnimationStart(Animation animation) {}
    @Override public void onAnimationRepeat(Animation animation) {}
}
