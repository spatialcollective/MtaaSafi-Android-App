package com.sc.mtaasafi.android.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.sc.mtaasafi.android.MainActivity;
import com.sc.mtaasafi.android.NewReportFragment;
import com.sc.mtaasafi.android.NewsFeedFragment;
import com.sc.mtaasafi.android.NonSwipePager;
import com.sc.mtaasafi.android.R;
import com.sc.mtaasafi.android.ReportDetailFragment;

/**
 * Created by Agree on 10/6/2014.
 */
public class FragmentAdapter extends FragmentStatePagerAdapter {

    MainActivity mContext;
    NonSwipePager mPager;

    NewsFeedFragment nff;
    NewReportFragment nrf;
    ReportDetailFragment rdf;

    public FragmentAdapter(MainActivity activity, NonSwipePager pager) {
        super(activity.getSupportFragmentManager());
        mContext = activity;
        mPager = pager;
        mPager.setAdapter(this);

        nff = new NewsFeedFragment();
        rdf = new ReportDetailFragment();
        nrf = new NewReportFragment();
    }

    @Override
    public Fragment getItem(int i) {
        switch (i){
            case 0:
                return nff;
            case 1:
                return nrf;
            case 2:
                return rdf;
        }
       return null;
    }

    @Override
    public int getCount() {
        return 3;
    }

    private String makeFragmentName(int viewId, int index) {
        return "android:switcher:" + viewId + ":" + index;
    }

}
