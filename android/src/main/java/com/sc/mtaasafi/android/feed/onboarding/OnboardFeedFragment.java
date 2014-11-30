package com.sc.mtaasafi.android.feed.onboarding;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sc.mtaasafi.android.R;
import com.sc.mtaasafi.android.feed.VoteInterface;


/**
 * Created by Agree on 11/27/2014.
 */
public class OnboardFeedFragment extends Fragment implements Animation.AnimationListener{
    private RelativeLayout feedItem1, feedItem2, feedItem3;
    String[] villages = {"Village 1"," Village 2", "Village 3A" ,
            "Village 3B","Village 3C","Village 4A","Village 4B",
            "Kosovo", "Mathare Number 10","Thayu","Mabatini",
            "Mashimoni", "Huruma","New Mathare","Kiamaiko","Mathare North"};
    String[] subVillages;
    int villageMod, scene;
    Animation feedFadeIn;
    AnimationSet villageFadeInFadeOut;
    TextView villageNameTV;
    boolean continueTapped;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedState){
        super.onCreateView(inflater, container, savedState);
        View view = inflater.inflate(R.layout.fragment_onboarding_feed, container, false);
        setUpAnimations();
        villageNameTV = (TextView) view.findViewById(R.id.villageNames);
        ImageView logo = (ImageView) view.findViewById(R.id.logo);
        logo.getLayoutParams().width = getActivity().getWindowManager().getDefaultDisplay().getWidth()/2;
        logo.getLayoutParams().height = getActivity().getWindowManager().getDefaultDisplay().getWidth()/2;
        logo.requestLayout();
        subVillages = new String[4];
        continueTapped = false;
        villageMod = (int) (System.currentTimeMillis() % 4);
        for (int i = 0; i < subVillages.length; i++)
            subVillages[i] = villages[i * 4 + villageMod];
        scene = 0;
        feedItem1 = (RelativeLayout) view.findViewById(R.id.feedItem1);
        feedItem2 = (RelativeLayout) view.findViewById(R.id.feedItem2);
        feedItem3 = (RelativeLayout) view.findViewById(R.id.feedItem3);
        setUpVoteInterface();
        directLaunchScreen();
        return view;
    }
    private void setUpVoteInterface(){
        VoteInterface voteInterface1 = (VoteInterface) feedItem1.findViewById(R.id.voteInterface1);
        VoteInterface voteInterface2 = (VoteInterface) feedItem2.findViewById(R.id.voteInterface2);
        VoteInterface voteInterface3 = (VoteInterface) feedItem3.findViewById(R.id.voteInterface3);
        voteInterface1.updateData(13, false, 0);
        voteInterface2.updateData(5, false, 0);
        voteInterface3.updateData(34, false, 0);
    }

    private void setUpAnimations(){
        feedFadeIn = new AlphaAnimation(0, 1);
        feedFadeIn.setDuration(500);
        feedFadeIn.setStartOffset(850);
        feedFadeIn.setAnimationListener(this);

        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setStartOffset(300);
        fadeIn.setDuration(400);
        fadeIn.setInterpolator(new DecelerateInterpolator());
        Animation fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setStartOffset(1200);
        fadeOut.setDuration(350);
        fadeOut.setInterpolator(new DecelerateInterpolator());
        villageFadeInFadeOut = new AnimationSet(false);
        villageFadeInFadeOut.addAnimation(fadeIn);
        villageFadeInFadeOut.addAnimation(fadeOut);
        villageFadeInFadeOut.setAnimationListener(this);
    }

    @Override
    public void onAnimationEnd(Animation animation) {
        Log.e("Scene just finished:", "Scene " + scene);
        if(scene < 4)
            directLaunchScreen();
        switch(scene){
                case 4: finalFadeIn(); break;
                case 5: revealTapScreenToContinue(); break;
                case 7:
                    if(continueTapped){
                        getView().findViewById(R.id.launchScreen).setVisibility(View.GONE);
                        ((OnboardingFragment)getParentFragment()).revealBookMarkBar();
                        feedItem2.startAnimation(feedFadeIn);
                        feedItem2.setVisibility(View.VISIBLE);
                    }
                    break;
                case 8:
                    feedItem2.clearAnimation();
                    feedItem3.startAnimation(feedFadeIn);
                    feedItem3.setVisibility(View.VISIBLE);
                    break;
                case 9:
                    feedItem3.clearAnimation();
                    getView().findViewById(R.id.triangleText).setVisibility(View.VISIBLE);
                    getView().findViewById(R.id.triangleText).startAnimation(feedFadeIn);
                    break;
        }
        Log.e("FItem1", "visible: " + (feedItem1.getVisibility()==View.VISIBLE));
        scene++;
    }

    private void directLaunchScreen(){
         villageNameTV.setText(subVillages[scene]);
         villageNameTV.startAnimation(villageFadeInFadeOut);
    }

    public void finalFadeIn(){
        Animation finalfadeIn = new AlphaAnimation(0, 1);
        finalfadeIn.setStartOffset(1200);
        finalfadeIn.setDuration(401);
        finalfadeIn.setAnimationListener(this);
        villageNameTV.setText("Mathare");
        villageNameTV.setTypeface(Typeface.DEFAULT_BOLD);
        villageNameTV.startAnimation(finalfadeIn);
    }

    private void revealTapScreenToContinue(){
        getView().findViewById(R.id.tapScreenTV).setVisibility(View.VISIBLE);
        Animation revealTapScreen = new AlphaAnimation(0, 1);
        revealTapScreen.setStartOffset(1200);
        revealTapScreen.setDuration(404);
        revealTapScreen.setAnimationListener(this);
        final Animation finalFadeOut = new AlphaAnimation(1, 0);
        finalFadeOut.setDuration(402);
        finalFadeOut.setAnimationListener(this);
        getView().findViewById(R.id.tapScreenTV).startAnimation(revealTapScreen);
        getView().findViewById(R.id.launchScreen).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(finalFadeOut);
                continueTapped = true;
            }
        });
    }
    @Override public void onAnimationStart(Animation animation) {}
    @Override public void onAnimationRepeat(Animation animation) {}
}
