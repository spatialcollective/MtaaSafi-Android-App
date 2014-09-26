package com.sc.mtaasafi.android;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.androidquery.callback.ImageOptions;

import java.util.ArrayList;


public class PostView extends android.support.v4.app.Fragment {
    TextView titleTV, detailsTV, timestampTV, userNameTV;
    ImageView imageAttachedIcon, media;
    ProgressBar progress;
    MainActivity mActivity;
    AQuery aq;
    PostData pd;
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        mActivity = (MainActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedState) {
        View view = inflater.inflate(R.layout.fragment_post_view, container, false);
        aq = new AQuery(view);
        if(pd == null && savedState != null){
            pd = new PostData  (savedState.getString("userName"),
                    savedState.getString("proPicURL"),
                    savedState.getString("timestamp"),
                    savedState.getDouble("lat"), savedState.getDouble("lon"),
                    savedState.getString("title"),
                    savedState.getString("details"),
                    savedState.getString("mediaURL"),
                    savedState.getStringArrayList("networksShared"));
        }
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
        if(mActivity.getDetailPostData() !=null){
            pd = mActivity.getDetailPostData();
        }
        titleTV.setText(pd.title);
        detailsTV.setText(pd.details);
        // TODO: get this formatted pretty-like.
        timestampTV.setText(pd.timestamp);
        ImageOptions options = new ImageOptions();
        options.round = 20;
        aq.id(R.id.proPic).image(pd.proPicURL, options);

        if(pd.mediaURL != null && !pd.mediaURL.equals("") && !pd.mediaURL.equals("null")){
            aq.id(media).progress(R.id.progressBar).image(pd.mediaURL);
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
        if(pd !=null){
            outState.putString("userName", pd.userName);
            outState.putString("proPicURL", pd.proPicURL);
            outState.putString("mediaURL", pd.mediaURL);
            outState.putString("timestamp", pd.timestamp);
            outState.putString("title", pd.title);
            outState.putString("details", pd.details);
            outState.putDouble("lat", pd.latitude);
            outState.putDouble("lon", pd.longitude);
            outState.putStringArrayList("networksShared", (ArrayList<String>) pd.networksShared);
        }
    }
}
