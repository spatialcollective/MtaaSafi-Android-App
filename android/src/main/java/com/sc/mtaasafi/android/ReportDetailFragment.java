package com.sc.mtaasafi.android;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;

import java.text.SimpleDateFormat;


public class ReportDetailFragment extends android.support.v4.app.Fragment {
    ImageView media;
    TextView titleTV, detailsTV, timeStampTV, userNameTV;
    ProgressBar progress;
    MainActivity mActivity;
    AQuery aq;
    Report mReport;
    
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        mActivity = (MainActivity) getActivity();
        Bundle args = getArguments();
        if (args != null) {
            mReport = new Report(args);
//            Toast.makeText(getActivity(), "Time elapsed: " + mReport.timeElapsed, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedState) {
        View view = inflater.inflate(R.layout.fragment_post_view, container, false);
        aq = new AQuery(view);
        updateView(view);
        return view;
    }

    private void updateView(View view) {
        titleTV = (TextView) view.findViewById(R.id.reportViewTitle);
        titleTV.setText(mReport.title);
        detailsTV = (TextView) view.findViewById(R.id.reportViewDetails);
        detailsTV.setText(mReport.details);
        timeStampTV = (TextView) view.findViewById(R.id.reportViewTimeStamp);
        timeStampTV.setText(getSimpleTimeStamp(mReport.timeStamp));
        timeStampTV.setVisibility(View.VISIBLE);
        userNameTV = (TextView) view.findViewById(R.id.reportViewUsername);
        userNameTV.setText(mReport.userName);
        media = (ImageView) view.findViewById(R.id.attachedPic);
        progress = (ProgressBar) view.findViewById(R.id.progressBar);
        if (mReport.mediaURL != null && !mReport.mediaURL.equals("") && !mReport.mediaURL.equals("null")) {
            aq.id(media).progress(R.id.progressBar).image(mReport.mediaURL);
        } else {
            media.setVisibility(View.INVISIBLE);
            progress.setVisibility(View.INVISIBLE);
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
        if (mReport != null)
            outState = mReport.saveState(outState);
    }

    private String getSimpleTimeStamp(String timestamp) {
        SimpleDateFormat fromFormat = new SimpleDateFormat("H:mm:ss dd-MM-yyyy");
        SimpleDateFormat displayFormat = new SimpleDateFormat("KK:mm a  dd MMM yy");
        try {
            return displayFormat.format(fromFormat.parse(timestamp));
        } catch (Exception e) {
            return timestamp;
        }
    }
}
