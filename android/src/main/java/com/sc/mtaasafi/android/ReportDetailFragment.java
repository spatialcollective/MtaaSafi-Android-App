package com.sc.mtaasafi.android;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.androidquery.AQuery;
import com.nineoldandroids.view.animation.AnimatorProxy;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.text.SimpleDateFormat;


public class ReportDetailFragment extends android.support.v4.app.Fragment {
    final String hadAReportKey = "hadareport",
                 reportKey = "report";
    Button previous, next;
    ViewFlipper flipper;
    TextView titleTV, detailsTV, timeStampTV, userNameTV;
    ImageView media1, media2, media3;
    ProgressBar progress;
    private SlidingUpPanelLayout mLayout;
    private RelativeLayout mReportText;

    MainActivity mActivity;
    AQuery aq;
    Report mReport;
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        mActivity = (MainActivity) getActivity();
        if(savedInstanceState != null && savedInstanceState.getBoolean(hadAReportKey))
            mReport = new Report(reportKey, savedInstanceState);
        if(getArguments() != null)
            mReport = new Report(MainActivity.REPORT_DETAIL_KEY, getArguments());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedState) {
        Log.e(LogTags.FEEDITEM, "ReportDetailFrag: onCreateView called");
        View view = inflater.inflate(R.layout.fragment_report_detail, container, false);
        setUpSlidingPanel(view);

        detailsTV = (TextView) view.findViewById(R.id.reportViewDetails);
        timeStampTV = (TextView) view.findViewById(R.id.reportViewTimeStamp);
        userNameTV = (TextView) view.findViewById(R.id.reportViewUsername);

        aq = new AQuery(view);
        previous = (Button) view.findViewById(R.id.buttonPrevious);

        flipper = (ViewFlipper) view.findViewById(R.id.viewFlipper);
        titleTV = (TextView) view.findViewById(R.id.reportViewTitle);
        media1 = (ImageView) view.findViewById(R.id.media1);
        media2 = (ImageView) view.findViewById(R.id.media2);
        media3 = (ImageView) view.findViewById(R.id.media3);

        previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                flipper.showPrevious();
            }
        });
        next = (Button) view.findViewById(R.id.buttonNext);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                flipper.showPrevious();
            }
        });
        if(savedState != null && savedState.getBoolean(hadAReportKey))
           mReport = new Report(reportKey, savedState);
        if(mReport !=null)
            updateView(mReport);

        return view;
    }

    private void setUpSlidingPanel(View view){
        mReportText = (RelativeLayout) view.findViewById(R.id.reportDetailViewText);

        // Make sure the actionbar doesn't block the text of the Report.
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int pixels_per_dp = (int)(metrics.density + 0.5f);
        int padding_dp = 4;
        mReportText.setPadding(0, pixels_per_dp * padding_dp + mActivity.getActionBarHeight(), 0, 0);

        mLayout = (SlidingUpPanelLayout) view.findViewById(R.id.sliding_layout);
        mLayout.setPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                if (slideOffset > 0.2 && mActivity.getSupportActionBar().isShowing())
                    mActivity.getSupportActionBar().hide();
                else if (!mActivity.getSupportActionBar().isShowing())
                    mActivity.getSupportActionBar().show();
                setActionBarTranslation(mLayout.getCurrentParalaxOffset());
            }
            @Override
            public void onPanelExpanded(View panel){Log.i(LogTags.PANEL_SLIDER, "onPanelExpanded");}
            @Override
            public void onPanelCollapsed(View panel){Log.i(LogTags.PANEL_SLIDER, "onPanelCollapsed");}
            @Override
            public void onPanelAnchored(View panel){Log.i(LogTags.PANEL_SLIDER, "onPanelAnchored");}
            @Override
            public void onPanelHidden(View panel) {Log.i(LogTags.PANEL_SLIDER, "onPanelHidden");}
        });
    }
    public void setActionBarTranslation(float y) {
        // Figure out the actionbar height
        // A hack to add the translation to the action bar
        ViewGroup content = ((ViewGroup) mActivity.findViewById(android.R.id.content).getParent());
        int children = content.getChildCount();
        for (int i = 0; i < children; i++) {
            View child = content.getChildAt(i);
            if (child.getId() != android.R.id.content) {
                if (y <= -mActivity.getActionBarHeight()) {
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

    public void updateView(Report report) {
        mReport = report;
        Log.e(LogTags.FEEDITEM, "UpdateView's report: " + mReport.title);
        aq = new AQuery(mActivity);
        aq.id(R.id.reportViewTitle).text(mReport.title);
        aq.id(R.id.reportViewDetails).text(mReport.details);
        aq.id(R.id.reportViewTimeStamp).text(getSimpleTimeStamp(mReport.timeStamp));
        aq.id(R.id.reportViewUsername).text(mReport.userName);
        if (mReport.mediaURLs.size() > 0)
            aq.id(media1).progress(R.id.reportDetailProgress).image(mReport.mediaURLs.get(0));
        if (mReport.mediaURLs.size() > 1)
            aq.id(media2).progress(R.id.reportDetailProgress).image(mReport.mediaURLs.get(1));
        if (mReport.mediaURLs.size() > 2)
            aq.id(media3).progress(R.id.reportDetailProgress).image(mReport.mediaURLs.get(2));
        detailsTV.setText(mReport.details);
        timeStampTV.setText(getSimpleTimeStamp(mReport.timeStamp));
        userNameTV.setText(mReport.userName);
    }

    @Override
    public void onResume(){
        super.onResume();
        mActivity.getReportDetailReport(this);
    }

    @Override
    public void onPause(){
        super.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        if (mReport != null){
            outState = mReport.saveState(reportKey, outState);
            outState.putBoolean(hadAReportKey, true);
        }
        else
            outState.putBoolean(hadAReportKey, false);
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

}
