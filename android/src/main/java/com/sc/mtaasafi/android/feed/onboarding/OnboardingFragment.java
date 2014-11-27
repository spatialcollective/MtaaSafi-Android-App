package com.sc.mtaasafi.android.feed.onboarding;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sc.mtaasafi.android.R;
import com.sc.mtaasafi.android.feed.ZoomOutPageTransformer;

/**
 * Created by Agree on 11/28/2014.
 */
public class OnboardingFragment extends Fragment implements Animation.AnimationListener {

    String[] villages = {"Village 1"," Village 2", "Village 3A" ,
            "Village 3B","Village 3C","Village 4A","Village 4B",
            "Kosovo", "Mathare Number 10","Thayu","Mabatini",
            "Mashimoni", "Huruma","New Mathare","Kiamaiko","Mathare North"};
    String[] subVillages;
    int villageMod, fadeInCount;
    Animation fadeIn, fadeOut;
    TextView villageNameTV;
    ViewPager viewPager;
    SlideAdapter adapter;
    RelativeLayout bookmarkBar;
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedState){
        View view = inflater.inflate(R.layout.fragment_onboard_launch, container, false);
        setUpAnimations();
        villageNameTV = (TextView) getView().findViewById(R.id.villageNames);
        bookmarkBar = (RelativeLayout) view.findViewById(R.id.bookMarkBar);

        subVillages = new String[4];
        villageMod = (int) (System.currentTimeMillis() % 4);
        for (int i = 0; i < subVillages.length; i++)
            subVillages[i] = villages[i * 4 + villageMod];
        fadeInCount = 0;
        adapter = new SlideAdapter(getChildFragmentManager());
        viewPager = new ViewPager(getActivity());
        viewPager.setAdapter(adapter);
        viewPager.setPageTransformer(true, new ZoomOutPageTransformer());
        return view;
    }

    private void setUpAnimations(){
        fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setStartOffset(1200);
        fadeIn.setDuration(400);
        fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setDuration(350);
        fadeOut.setInterpolator(new DecelerateInterpolator());
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser)
            startVillageNamesAnimation();
    }

    private void startVillageNamesAnimation(){
        villageNameTV.setAlpha(0);
        villageNameTV.setText(subVillages[fadeInCount]);
        villageNameTV.startAnimation(fadeIn);
        fadeInCount++;
    }

    @Override
    public void onAnimationEnd(Animation animation) {
        if(animation.getDuration() == 350 && fadeInCount < 4){
            // animation was fade out, prepare for next fade in
            villageNameTV.setText(subVillages[fadeInCount]);
            villageNameTV.startAnimation(fadeIn);
            fadeInCount++;
        } else if(animation.getDuration() == 400) // just finished a fade in, start a fade out
            villageNameTV.startAnimation(fadeOut);
        else if(animation.getDuration() == 350 && fadeInCount == 4)
            // you've done your final fade out--prepare for final fade in: MATHARE!
            finalFadeOut();
    }

    public void finalFadeOut(){
        villageNameTV.setText("Mathare");
        villageNameTV.startAnimation(fadeIn);
        getView().findViewById(R.id.tapScreenTV).setVisibility(View.VISIBLE);
        getView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fadeOut.setStartOffset(0);
                v.startAnimation(fadeOut);
                startTutorial();
            }
        });
    }

    public void startTutorial(){ viewPager.setCurrentItem(0); }

    public void setBookmarkActive(int i){
        switch(i){
            case 0:
                ((ImageButton)bookmarkBar.findViewById(R.id.feedBookmark))
                .setImageResource(R.drawable.bookmark_feed_hl);
                setBookMarkPassive(R.id.newReportBookmark);
                setBookMarkPassive(R.id.doneBookmark);
                break;
            case 1:
                ((ImageButton)bookmarkBar.findViewById(R.id.newReportBookmark))
                .setImageResource(R.drawable.bookmark_newreport_hl);
                setBookMarkPassive(R.id.feedBookmark);
                setBookMarkPassive(R.id.doneBookmark);
                break;
            case 2:
                ((ImageButton)bookmarkBar.findViewById(R.id.doneBookmark))
                .setImageResource(R.drawable.bookmark_done_hl);
                setBookMarkPassive(R.id.newReportBookmark);
                setBookMarkPassive(R.id.feedBookmark);
                break;
        }
    }

    private void setBookMarkPassive(int passiveBookMarkId){
        int passiveDrawableId = 0;
        switch(passiveBookMarkId){
            case R.id.feedBookmark: passiveDrawableId = R.drawable.bookmark_feed_gray; break;
            case R.id.newReportBookmark: passiveDrawableId = R.drawable.bookmark_newreport_gray; break;
            case R.id.doneBookmark: passiveDrawableId = R.drawable.bookmark_done_gray; break;
        }
        ((ImageButton)bookmarkBar.findViewById(passiveBookMarkId)).setImageResource(passiveDrawableId);
    }

    private class SlideAdapter extends FragmentPagerAdapter {
        public SlideAdapter(FragmentManager fm) { super(fm); }
        @Override public int getCount() { return 3; }
        @Override
        public Fragment getItem(int i) {
            setBookmarkActive(i);
            switch(i){
                case 0: return new OnboardFeedFragment();
                case 1: return new OnboardNewReportFragment();
                case 2: return new OnboardDoneFragment();
                default: return null;
            }
        }
    }

    @Override public void onAnimationStart(Animation animation) {}
    @Override public void onAnimationRepeat(Animation animation) {}
}