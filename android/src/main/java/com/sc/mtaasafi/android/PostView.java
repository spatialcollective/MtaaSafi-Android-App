package com.sc.mtaasafi.android;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidquery.AQuery;


public class PostView extends android.support.v4.app.Fragment {
    TextView contentTV, timestampTV, userNameTV;
    ImageView profilePic, imageAttachedIcon, media, networkSharedIcon1, networkSharedIcon2;
    MainActivity mActivity;
    AQuery aq;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        mActivity = (MainActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_post_view, container, false);
        aq = new AQuery(view);
        PostData pd = mActivity.getDetailPostData();
        contentTV = (TextView) view.findViewById(R.id.postText);
        timestampTV = (TextView) view.findViewById(R.id.timestamp);
        userNameTV = (TextView) view.findViewById(R.id.userName);

        contentTV.setText(pd.content);
        // TODO: get this formatted pretty-like.
        timestampTV.setText(pd.timestamp);

        profilePic = (ImageView) view.findViewById(R.id.proPic);
        aq.id(R.id.proPic).image(pd.proPicURL);
        imageAttachedIcon = (ImageView) view.findViewById(R.id.picAttachedIcon);
        media = (ImageView) view.findViewById(R.id.attachedPic);
        if(pd.mediaURL != null && !pd.mediaURL.equals(""))
           aq.id(media).image(pd.mediaURL);
        else {
            imageAttachedIcon.setVisibility(View.INVISIBLE);
            media.setVisibility(View.INVISIBLE);
        }
        return view;
    }

}
