package com.sc.mtaasafi.android;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.androidquery.AQuery;


public class PostView extends android.support.v4.app.Fragment {
    TextView titleTV, detailsTV, timestampTV, userNameTV;
    ImageView imageAttachedIcon, media;
    ProgressBar progress;
    MainActivity mActivity;
    AQuery aq;
    Report report;
    
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        mActivity = (MainActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedState) {
        View view = inflater.inflate(R.layout.fragment_post_view, container, false);
        aq = new AQuery(view);
        if (report == null && savedState != null)
            report = new Report(savedState);

        titleTV = (TextView) view.findViewById(R.id.reportViewTitle);
        detailsTV = (TextView) view.findViewById(R.id.reportViewDetails);
        timestampTV = (TextView) view.findViewById(R.id.reportViewTimestamp);
        userNameTV = (TextView) view.findViewById(R.id.reportViewUsername);
        imageAttachedIcon = (ImageView) view.findViewById(R.id.picAttachedIcon);
        media = (ImageView) view.findViewById(R.id.attachedPic);
        progress = (ProgressBar) view.findViewById(R.id.progressBar);
        return view;
    }

    @Override
    public void onResume(){
        super.onResume();
        if (mActivity.getReport() != null)
            report = mActivity.getReport();

        titleTV.setText(report.title);
        detailsTV.setText(report.details);
        userNameTV.setText(mActivity.mUsername);
        // TODO: get this formatted pretty-like.
//        timestampTV.setText(report.timestamp);
        if (report.mediaURL != null && !report.mediaURL.equals("") && !report.mediaURL.equals("null")){
            aq.id(media).progress(R.id.progressBar).image(report.mediaURL);
        }
        else {
            imageAttachedIcon.setVisibility(View.INVISIBLE);
            media.setVisibility(View.INVISIBLE);
            progress.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onPause(){
        super.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        if (report != null)
            outState = report.saveState(outState);
    }
}
