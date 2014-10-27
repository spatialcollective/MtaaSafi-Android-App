package com.sc.mtaasafi.android.feed;

import android.database.Cursor;
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
import com.sc.mtaasafi.android.LogTags;
import com.sc.mtaasafi.android.R;
import com.sc.mtaasafi.android.database.ReportContract;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.text.SimpleDateFormat;


public class ReportDetailFragment extends android.support.v4.app.Fragment {

    private String title, details, time, user, mediaUrl1, mediaUrl2, mediaUrl3;

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
        setUpSlidingPanel(view);   
        setClickListeners(view);
        updateView(view);
        return view;
    }

    private void setUpSlidingPanel(View view){
        final ActionBar actionbar = ((ActionBarActivity)getActivity()).getSupportActionBar();
        RelativeLayout mReportText = (RelativeLayout) view.findViewById(R.id.reportDetailViewText);

        // Make sure the actionbar doesn't block the text of the Report.
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int pixels_per_dp = (int)(metrics.density + 0.5f);
        int padding_dp = 4;
        mReportText.setPadding(0, pixels_per_dp * padding_dp + actionbar.getHeight(), 0, 0);

        final SlidingUpPanelLayout mLayout = (SlidingUpPanelLayout) view.findViewById(R.id.sliding_layout);
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

    private void setClickListeners(View view) {
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

}
