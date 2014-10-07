package com.sc.mtaasafi.android;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.androidquery.AQuery;

import java.text.SimpleDateFormat;


public class ReportDetailFragment extends android.support.v4.app.Fragment {
    ImageView media1, media2, media3;
    TextView titleTV, detailsTV, timeStampTV, userNameTV;
    ProgressBar progress;
    MainActivity mActivity;
    AQuery aq;
    Report mReport;
    
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        mActivity = (MainActivity) getActivity();
        if(savedInstanceState != null){
            mReport = new Report(savedInstanceState);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedState) {
        View view = inflater.inflate(R.layout.fragment_report_detail, container, false);
        aq = new AQuery(view);
        return view;
    }

    public void updateView(Report report) {
        mReport = report;
//        titleTV = (TextView) view.findViewById(R.id.reportViewTitle);
//        titleTV.setText(mReport.title);
        aq.id(R.id.reportViewTitle).text(mReport.title);
        aq.id(R.id.reportViewDetails).text(mReport.details);
        aq.id(R.id.reportViewTimeStamp).text(getSimpleTimeStamp(mReport.timeStamp));
        aq.id(R.id.reportViewUsername).text(mReport.userName);
//        aq.id(R.id.media1).progress(R.id.progressBar).image(mReport.media1URL);
//        aq.id(R.id.media2).progress(R.id.progressBar).image(mReport.media2URL);
//        aq.id(R.id.media3).progress(R.id.progressBar).image(mReport.media3URL);
//        detailsTV = (TextView) view.findViewById(R.id.reportViewDetails);
//        detailsTV.setText(mReport.details);
//        timeStampTV = (TextView) view.findViewById(R.id.reportViewTimeStamp);
//        timeStampTV.setText(getSimpleTimeStamp(mReport.timeStamp));
//        timeStampTV.setVisibility(View.VISIBLE);
//        userNameTV = (TextView) view.findViewById(R.id.reportViewUsername);
//        userNameTV.setText(mReport.userName);
//        media1 = (ImageView) view.findViewById(R.id.media1);
//        media2 = (ImageView) view.findViewById(R.id.media2);
//        media3 = (ImageView) view.findViewById(R.id.media3);
//        progress = (ProgressBar) view.findViewById(R.id.progressBar);
//        aq.id(R.id.media1).progress(R.id.progressBar).image(mReport.media1URL);
//        aq.id(R.id.media2).progress(R.id.progressBar).image(mReport.media2URL);
//        aq.id(R.id.media3).progress(R.id.progressBar).image(mReport.media3URL);
//
//        if (mReport.mediaURL != null && !mReport.mediaURL.equals("") && !mReport.mediaURL.equals("null")) {
//            aq.id(media).progress(R.id.progressBar).image(mReport.mediaURL);
//        } else {
//            media.setVisibility(View.INVISIBLE);
//            progress.setVisibility(View.INVISIBLE);
//        }
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
        if (mReport != null)
            outState = mReport.saveState(outState);
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
