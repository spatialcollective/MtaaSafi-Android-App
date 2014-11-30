package com.sc.mtaasafi.android.feed.onboarding;

import android.graphics.Typeface;
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
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sc.mtaasafi.android.R;
import com.sc.mtaasafi.android.feed.ZoomOutPageTransformer;

/**
 * Created by Agree on 11/28/2014.
 */
public class OnboardingFragment extends Fragment {

    ViewPager viewPager;
    SlideAdapter adapter;
    RelativeLayout bookmarkBar;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedState){
        View view = inflater.inflate(R.layout.fragment_onboard, container, false);
        adapter = new SlideAdapter(getChildFragmentManager());
        bookmarkBar = (RelativeLayout) view.findViewById(R.id.bookMarkBar);

        viewPager = (ViewPager) view.findViewById(R.id.viewPager);
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(3);
        viewPager.setPageTransformer(true, new ZoomOutPageTransformer());
        return view;
    }

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
}
