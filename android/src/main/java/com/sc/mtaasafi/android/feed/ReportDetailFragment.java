package com.sc.mtaasafi.android.feed;

import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.androidquery.AQuery;
import com.nineoldandroids.view.animation.AnimatorProxy;
import com.sc.mtaasafi.android.SystemUtils.LogTags;
import com.sc.mtaasafi.android.R;
import com.sc.mtaasafi.android.database.ReportContract;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;


public class ReportDetailFragment extends android.support.v4.app.Fragment {

    private String title, details, time, user, mediaUrl1, mediaUrl2, mediaUrl3;
    public AQuery aq;
    public ViewPager viewPager;
    RelativeLayout bottomView;
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    public void setData(Cursor c) {
        title = c.getString(c.getColumnIndex(ReportContract.Entry.COLUMN_TITLE));
        details = c.getString(c.getColumnIndex(ReportContract.Entry.COLUMN_DETAILS));
        time = getSimpleTimeStamp(c.getString(c.getColumnIndex(ReportContract.Entry.COLUMN_TIMESTAMP)));
        user = c.getString(c.getColumnIndex(ReportContract.Entry.COLUMN_USERNAME));
        mediaUrl1 = c.getString(c.getColumnIndex(ReportContract.Entry.COLUMN_MEDIAURL1));
        mediaUrl2 = c.getString(c.getColumnIndex(ReportContract.Entry.COLUMN_MEDIAURL2));
        mediaUrl3 = c.getString(c.getColumnIndex(ReportContract.Entry.COLUMN_MEDIAURL3));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedState) {
        View view = inflater.inflate(R.layout.fragment_report_detail, container, false);
        viewPager = (ViewPager) view.findViewById(R.id.viewpager);
        String[] mediaUrls ={mediaUrl1, mediaUrl2, mediaUrl3};
        viewPager.setAdapter(new ImageSlideAdapter(getChildFragmentManager(), mediaUrls));
        bottomView = (RelativeLayout) view.findViewById(R.id.report_BottomView);
        setClickListeners(view);
        updateView(view);
        return view;
    }

    private void setClickListeners(View view) {
        view.findViewById(R.id.media1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // go to 1st view in view pager
                activateViewPager(0);
            }
        });
        view.findViewById(R.id.media2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // go to 2nd view in view pager
                activateViewPager(1);

            }
        });
        view.findViewById(R.id.media3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // go to 3rd view in view pager
                activateViewPager(2);
            }
        });
        final ViewFlipper flipper = (ViewFlipper) view.findViewById(R.id.viewFlipper);
        ((Button) view.findViewById(R.id.buttonPrevious))
            .setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) { flipper.showPrevious(); }
        });
        ((Button) view.findViewById(R.id.buttonNext))
            .setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) { flipper.showPrevious(); }
        });
    }
    private void activateViewPager(int i){
        viewPager.setCurrentItem(i);
        viewPager.setVisibility(View.VISIBLE);
        bottomView.setVisibility(View.VISIBLE);
    }
    public void updateView(View view) {
        ((TextView) view.findViewById(R.id.reportViewTitle)).setText(title);
        ((TextView) view.findViewById(R.id.reportViewDetails)).setText(details);
        ((TextView) view.findViewById(R.id.reportViewTimeStamp)).setText(time);
        ((TextView) view.findViewById(R.id.reportViewUsername)).setText(user);
        AQuery aq = new AQuery(getActivity());
        aq.id(view.findViewById(R.id.media1)).image(mediaUrl1);
        aq.id(view.findViewById(R.id.media2)).image(mediaUrl2);
        aq.id(view.findViewById(R.id.media3)).image(mediaUrl3);
    }

    public void setActionBarTranslation(float y, int height) {
        // Figure out the actionbar height
        // A hack to add the translation to the action bar
        ViewGroup content = ((ViewGroup) getActivity().findViewById(android.R.id.content).getParent());
        int children = content.getChildCount();
        for (int i = 0; i < children; i++) {
            View child = content.getChildAt(i);
            if (child.getId() != android.R.id.content) {
                if (y <= -height) {
                    child.setVisibility(View.GONE);
                } else {
                    child.setVisibility(View.VISIBLE);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                        child.setTranslationY(y);
                    } else {
                        AnimatorProxy.wrap(child).setTranslationY(y);
                    }
                }
            }
        }
    }

    private String getSimpleTimeStamp(String timestamp) {
        SimpleDateFormat fromFormat = new SimpleDateFormat("H:mm:ss dd-MM-yyyy");
        SimpleDateFormat displayFormat = new SimpleDateFormat("K:mm a  d MMM yy");
        try {
            return displayFormat.format(fromFormat.parse(timestamp));
        } catch (Exception e) {
            return timestamp;
        }
    }
    private class ImageSlideAdapter extends FragmentPagerAdapter {
        String[] mediaPaths;
        public ImageSlideAdapter(FragmentManager fm, String[] mediaPaths) {
            super(fm);
            this.mediaPaths = mediaPaths;
        }

        @Override
        public int getCount() {
            return mediaPaths.length;
        }

        @Override
        public Fragment getItem(int i) {
            ImageFragment iF = new ImageFragment();
            Bundle args = new Bundle();
            args.putString("mediaPath", mediaPaths[i]);
            iF.setArguments(args);
            return iF;
        }

    }

    private class ImageFragment extends Fragment{
        String mediaPath;
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            if(getArguments() != null)
                mediaPath = getArguments().getString("mediaPath");
        }

        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            super.onCreateView(inflater, container, savedInstanceState);
            View view = inflater.inflate(R.layout.fragment_report_image, container);
            ImageView reportDetailImage = (ImageView) view.findViewById(R.id.report_detail_image);
            if(mediaPath != null)
                aq.id(reportDetailImage).image(mediaPath);
            reportDetailImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // close the view pager
                    viewPager.setVisibility(View.INVISIBLE);
                    bottomView.setVisibility(View.INVISIBLE);
                }
            });
            return view;
        }
    }
}
