package com.sc.mtaasafi.android;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Agree on 9/5/2014.
 */
public class FeedItem extends RelativeLayout {
    private LayoutInflater inflater;
    private TextView contentTV, userNameTV, timeSincePostTV;
    ImageView profilePic, sharedIcon, picsAttachedIcon;
    String content;
    String proPicURL;
    String mediaURL;
    String userName;
    String timeSincePost; // epoch time
    String timeStamp;
    List<String> networksShared;

    double lat, lon;

    public FeedItem(Context context){
        super(context);
        setUp();
    }

    public FeedItem(Context context, PostData pd) {
        super(context);
        setUp();

        if(pd.proPicURL != null)
            proPicURL = pd.proPicURL;
        if(pd.mediaURL == null)
            picsAttachedIcon.setVisibility(View.INVISIBLE);
        else
            mediaURL = pd.mediaURL;
        if(pd.networksShared != null)
            networksShared = pd.networksShared;
        userName = pd.userName;
        userNameTV.setText(userName);

        timeSincePost = PostData.timeSincePosted(pd.timestamp);
        timeSincePostTV.setText(timeSincePost);

        content = pd.content;
        contentTV.setText(content);

        lat = pd.latitude;
        lon = pd.longitude;

    }
    public PostData toPostData(){
        return new PostData(userName, proPicURL,
                            timeStamp, lat, lon,
                            content, mediaURL,
                            networksShared);
    }
    public void setContent(String content){
        this.content = content;
        contentTV.setText(content);
    }

    private void setUp(){
        inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.feed_item, this, true);

        userNameTV = (TextView) findViewById(R.id.userName);
        timeSincePostTV = (TextView) findViewById(R.id.timestamp);
        contentTV = (TextView) findViewById(R.id.postText);

        profilePic = (ImageView) findViewById(R.id.proPic);
        sharedIcon = (ImageView) findViewById(R.id.sharedIcon);
        picsAttachedIcon = (ImageView) findViewById(R.id.picAttachedIcon);

        Log.d(LogTags.FEEDITEM, "Created! Contents: " + content);
    }
}
