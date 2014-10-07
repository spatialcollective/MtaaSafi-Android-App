package com.sc.mtaasafi.android.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.sc.mtaasafi.android.NewReportFragment;
import com.sc.mtaasafi.android.NewsFeedFragment;
import com.sc.mtaasafi.android.R;
import com.sc.mtaasafi.android.ReportDetailFragment;

/**
 * Created by Agree on 10/6/2014.
 */
public class FragmentAdapter extends FragmentStatePagerAdapter{

    FragmentManager fm;

    public FragmentAdapter(FragmentManager fm){
        super(fm);
        this.fm = fm;
    }

    @Override
    public Fragment getItem(int i) {
        String name = makeFragmentName(R.id.pager, i);
        Fragment f = fm.findFragmentByTag(name);
        if(f == null){
            switch (i){
                case 0:
                    return new NewsFeedFragment();
                case 1:
                    return new ReportDetailFragment();
                case 2:
                    return new NewReportFragment();
            }
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
