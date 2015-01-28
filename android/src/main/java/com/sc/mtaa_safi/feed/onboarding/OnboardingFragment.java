package com.sc.mtaa_safi.feed.onboarding;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.sc.mtaa_safi.R;
import com.sc.mtaa_safi.feed.ZoomOutPageTransformer;

/**
 * Created by Agree on 11/28/2014.
 */
public class OnboardingFragment extends Fragment implements Animation.AnimationListener, ViewPager.OnPageChangeListener{

    OnboardPager viewPager;
    SlideAdapter adapter;
    RelativeLayout bookmarkBar;
    ImageButton feedBookMark, newReportBookMark, doneBookMark;
    Boolean continueTapped;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedState){
        View view = inflater.inflate(R.layout.fragment_onboard, container, false);
        setRetainInstance(true);
        if(savedState == null)
            continueTapped = false;
        else{
            continueTapped = savedState.getBoolean("ContinueTapped");
            Log.e("OnboardingFragment", "From bundle: " + continueTapped);
        }
        Log.e("OnboardingFragment", "CreateView: " + continueTapped);
        adapter = new SlideAdapter(getChildFragmentManager());
        bookmarkBar = (RelativeLayout) view.findViewById(R.id.bookMarkBar);
        viewPager = (OnboardPager) view.findViewById(R.id.viewPager);
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(3);
        viewPager.setPageTransformer(true, new ZoomOutPageTransformer());
        viewPager.setOnPageChangeListener(this);
        feedBookMark = (ImageButton) view.findViewById(R.id.feedBookmark);
        newReportBookMark = (ImageButton) view.findViewById(R.id.newReportBookmark);
        doneBookMark = (ImageButton) view.findViewById(R.id.doneBookmark);
        setBookMarkListeners();
        return view;
    }
    private void setBookMarkListeners(){
        feedBookMark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setBookmarkActive(0);
                viewPager.setCurrentItem(0);
            }
        });
        newReportBookMark.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                setBookmarkActive(1);
                viewPager.setCurrentItem(1);
            }
        });
        doneBookMark.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                setBookmarkActive(2);
                viewPager.setCurrentItem(2);
            }
        });
    }

    public void setBookmarkActive(int i){
        Log.e("BookMark", "Active Int val: " + i);
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
    public void revealBookMarkBar(){
        Animation slideUp = AnimationUtils.loadAnimation(getActivity(), R.anim.abc_slide_in_bottom);
        slideUp.setAnimationListener(this);
        bookmarkBar.startAnimation(slideUp);
        bookmarkBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onAnimationEnd(Animation animation) {
        Animation revealFeedBM = new ScaleAnimation(0, 1, 0, 1,
                                                    Animation.RELATIVE_TO_SELF, .5f,
                                                    Animation.RELATIVE_TO_SELF, .5f);
        revealFeedBM.setDuration(250);
        revealFeedBM.setStartOffset(150);

        Animation revealNewReportBM = new ScaleAnimation(0, 1, 0, 1,
                                                    Animation.RELATIVE_TO_SELF, .5f,
                                                    Animation.RELATIVE_TO_SELF, .5f);
        revealNewReportBM.setDuration(250);
        revealNewReportBM.setStartOffset(300);

        Animation revealDoneBM = new ScaleAnimation(0, 1, 0, 1,
                                                    Animation.RELATIVE_TO_SELF, .5f,
                                                    Animation.RELATIVE_TO_SELF, .5f);
        revealDoneBM.setDuration(250);
        revealDoneBM.setStartOffset(450);

        feedBookMark.startAnimation(revealFeedBM);
        newReportBookMark.startAnimation(revealNewReportBM);
        doneBookMark.startAnimation(revealDoneBM);
        feedBookMark.setVisibility(View.VISIBLE);
        newReportBookMark.setVisibility(View.VISIBLE);
        doneBookMark.setVisibility(View.VISIBLE);
    }
    public void continueTapped(){
        viewPager.swipeEnabled = true;
        continueTapped = true;
        Log.e("Onboard Fragment", "Continue was tapped!");
    }
    @Override
    public void onPageSelected(int i) { setBookmarkActive(i); }

    private class SlideAdapter extends FragmentPagerAdapter {
        public SlideAdapter(FragmentManager fm) { super(fm); }
        @Override
        public int getCount() { return 3; }
        @Override
        public Fragment getItem(int i) {
            switch(i){
                case 0: return new OnboardFeedFragment();
                case 1: return new OnboardNewReportFragment();
                case 2: return new OnboardDoneFragment();
                default: return null;
            }
        }
    }
    @Override
    public void onSaveInstanceState(Bundle saveState){
        super.onSaveInstanceState(saveState);
        saveState.putBoolean("ContinueTapped", continueTapped);
    }
    @Override
    public void onDestroy(){
        super.onDestroy();
        Log.e("Onboarding Fragment", "Set retain instance: " + getRetainInstance());
    }
    @Override public void onAnimationRepeat(Animation animation) {}
    @Override public void onAnimationStart(Animation animation) {}
    @Override public void onPageScrollStateChanged(int i) {}
    @Override public void onPageScrolled(int i, float v, int i2) { }
}
