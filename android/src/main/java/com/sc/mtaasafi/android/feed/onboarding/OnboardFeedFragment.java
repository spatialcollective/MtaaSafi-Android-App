package com.sc.mtaasafi.android.feed.onboarding;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.RelativeLayout;

import com.sc.mtaasafi.android.R;
import com.sc.mtaasafi.android.feed.VoteInterface;


/**
 * Created by Agree on 11/27/2014.
 */
public class OnboardFeedFragment extends Fragment implements Animation.AnimationListener{
    private RelativeLayout feedItem1, feedItem2, feedItem3;
    Animation fadeIn;
    int scene = 0;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedState){
        super.onCreateView(inflater, container, savedState);
        View view = inflater.inflate(R.layout.fragment_onboarding_feed, container, false);
        fadeIn = new AlphaAnimation(0f, 1f);
        fadeIn.setDuration(500);
        fadeIn.setStartOffset(850);
        feedItem1 = (RelativeLayout) view.findViewById(R.id.feedItem1);
        feedItem2 = (RelativeLayout) view.findViewById(R.id.feedItem2);
        feedItem3 = (RelativeLayout) view.findViewById(R.id.feedItem3);
        VoteInterface voteInterface1 = (VoteInterface) feedItem1.findViewById(R.id.voteInterface1);
        VoteInterface voteInterface2 = (VoteInterface) feedItem2.findViewById(R.id.voteInterface2);
        VoteInterface voteInterface3 = (VoteInterface) feedItem3.findViewById(R.id.voteInterface3);
        voteInterface1.updateData(13, false, 0);
        voteInterface2.updateData(5, false, 0);
        voteInterface3.updateData(34, false, 0);
        return view;
    }
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            scene = 1;
            feedItem1.startAnimation(fadeIn);
            feedItem1.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onAnimationEnd(Animation animation) {
        scene++;
        switch(scene){
            case 2:
                feedItem2.startAnimation(fadeIn);
                feedItem2.setVisibility(View.VISIBLE);
                scene++;
                break;
            case 3:
                feedItem3.startAnimation(fadeIn);
                feedItem3.setVisibility(View.VISIBLE);
                break;
        }
    }
    @Override public void onAnimationStart(Animation animation) {}
    @Override public void onAnimationRepeat(Animation animation) {}
}
