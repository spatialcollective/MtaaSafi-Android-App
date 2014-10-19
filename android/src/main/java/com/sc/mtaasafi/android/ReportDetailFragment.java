package com.sc.mtaasafi.android;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

    private SlidingUpPanelLayout mLayout;
    private RelativeLayout mReportText;

    Report mReport;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null)
            mReport = new Report("some_key", args);
        else if (savedInstanceState != null && savedInstanceState.getBoolean(hadAReportKey))
            mReport = new Report(reportKey, savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedState) {
        Log.e(LogTags.FEEDITEM, "ReportDetailFrag: onCreateView called");
        View view = inflater.inflate(R.layout.fragment_report_detail, container, false);
        setUpSlidingPanel(view);

        Button previous = (Button) view.findViewById(R.id.buttonPrevious);
        final ViewFlipper flipper = (ViewFlipper) view.findViewById(R.id.viewFlipper);
        previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                flipper.showPrevious();
            }
        });
        Button next = (Button) view.findViewById(R.id.buttonNext);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                flipper.showPrevious();
            }
        });
        // if(savedState != null && savedState.getBoolean(hadAReportKey))
        //    mReport = new Report(reportKey, savedState);
        // if(mReport !=null)
        updateView(mReport, view);
        return view;
    }

    private void setUpSlidingPanel(View view){
        final ActionBar actionbar = ((ActionBarActivity)getActivity()).getSupportActionBar();
        mReportText = (RelativeLayout) view.findViewById(R.id.reportDetailViewText);

        // Make sure the actionbar doesn't block the text of the Report.
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int pixels_per_dp = (int)(metrics.density + 0.5f);
        int padding_dp = 4;
        mReportText.setPadding(0, pixels_per_dp * padding_dp + actionbar.getHeight(), 0, 0);

        mLayout = (SlidingUpPanelLayout) view.findViewById(R.id.sliding_layout);
        mLayout.setPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                if (slideOffset > 0.2 && actionbar.isShowing())
                    actionbar.hide();
                else if (!actionbar.isShowing())
                    actionbar.show();
                setActionBarTranslation(mLayout.getCurrentParalaxOffset(), actionbar.getHeight());
            }
            @Override
            public void onPanelExpanded(View panel) { Log.i(LogTags.PANEL_SLIDER, "onPanelExpanded"); }
            @Override
            public void onPanelCollapsed(View panel) { Log.i(LogTags.PANEL_SLIDER, "onPanelCollapsed"); }
            @Override
            public void onPanelAnchored(View panel) { Log.i(LogTags.PANEL_SLIDER, "onPanelAnchored"); }
            @Override
            public void onPanelHidden(View panel) { Log.i(LogTags.PANEL_SLIDER, "onPanelHidden"); }
        });
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

    public void updateView(Report report, View view) {
        TextView detailsTV = (TextView) view.findViewById(R.id.reportViewDetails);
        TextView timeStampTV = (TextView) view.findViewById(R.id.reportViewTimeStamp);
        TextView userNameTV = (TextView) view.findViewById(R.id.reportViewUsername);

        mReport = report;
        if (mReport != null) {
            Log.e(LogTags.FEEDITEM, "UpdateView's report: " + mReport.title);
            AQuery aq = new AQuery(getActivity());
            aq.id(R.id.reportViewTitle).text(mReport.title);
            aq.id(R.id.reportViewDetails).text(mReport.details);
            aq.id(R.id.reportViewTimeStamp).text(getSimpleTimeStamp(mReport.timeStamp));
            aq.id(R.id.reportViewUsername).text(mReport.userName);
            if (mReport.mediaURLs.size() > 0)
                aq.id(R.id.media1).progress(R.id.reportDetailProgress).image(mReport.mediaURLs.get(0));
            if (mReport.mediaURLs.size() > 1)
                aq.id(R.id.media2).progress(R.id.reportDetailProgress).image(mReport.mediaURLs.get(1));
            if (mReport.mediaURLs.size() > 2)
                aq.id(R.id.media3).progress(R.id.reportDetailProgress).image(mReport.mediaURLs.get(2));
            detailsTV.setText(mReport.details);
            timeStampTV.setText(getSimpleTimeStamp(mReport.timeStamp));
            userNameTV.setText(mReport.userName);
        }
    }

    @Override
    public void onResume(){
        super.onResume();
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
