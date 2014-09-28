package com.sc.mtaasafi.android;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.androidquery.AQuery;

import org.w3c.dom.Text;


public class ReportDetailFragment extends android.support.v4.app.Fragment {
    String mTitle, mDetails, mTimeElapsed, mUserName;
    ImageView imageAttachedIcon, media;
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
            mTitle = args.getString("title", mTitle);
            mDetails = args.getString("details", mDetails);
            mTimeElapsed = args.getString("timestamp", mTimeElapsed);
            mUserName = args.getString("user", mUserName);
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
        TextView titleTV = (TextView) view.findViewById(R.id.reportViewTitle);
        titleTV.setText(mTitle);
        TextView detailsTV = (TextView) view.findViewById(R.id.reportViewDetails);
        detailsTV.setText(mDetails);
        TextView timestampTV = (TextView) view.findViewById(R.id.reportViewTimestamp);
        timestampTV.setText(mTimeElapsed);
        TextView userNameTV = (TextView) view.findViewById(R.id.reportViewUsername);
        userNameTV.setText(mActivity.mUsername);

        imageAttachedIcon = (ImageView) view.findViewById(R.id.picAttachedIcon);
//        TextView media = (ImageView) view.findViewById(R.id.attachedPic);
        progress = (ProgressBar) view.findViewById(R.id.progressBar);

//        if (report.mediaURL != null && !report.mediaURL.equals("") && !report.mediaURL.equals("null")) {
//            aq.id(media).progress(R.id.progressBar).image(report.mediaURL);
//        } else {
//            imageAttachedIcon.setVisibility(View.INVISIBLE);
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
}
