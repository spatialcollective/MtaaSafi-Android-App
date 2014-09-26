package com.sc.mtaasafi.android.listitem;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sc.mtaasafi.android.LogTags;
import com.sc.mtaasafi.android.PostData;
import com.sc.mtaasafi.android.R;

import java.util.List;

/**
 * Created by Agree on 9/5/2014.
 */
public class FeedItem extends RelativeLayout {
    private LayoutInflater inflater;
    private TextView detailsTV, titleTV, timeSincePostTV;
    ImageView profilePic, picsAttachedIcon;
    public String title, details, proPicURL, mediaURL, userName, timeSincePost, timeStamp;
    public List<String> networksShared;

    double lat, lon;

    public FeedItem(Context context, PostData pd) {
        super(context);
        inflate();
        setFields(pd);
    }
    public PostData toPostData(){
        return new PostData(userName, proPicURL,
                            timeStamp, lat, lon,
                            details, "", mediaURL,
                            networksShared);
    }

    private void inflate(){
        inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.feed_item, this, true);
        titleTV = (TextView) findViewById(R.id.itemTitle);
        timeSincePostTV = (TextView) findViewById(R.id.timestamp);
        detailsTV = (TextView) findViewById(R.id.itemDetails);
        profilePic = (ImageView) findViewById(R.id.proPic);
        picsAttachedIcon = (ImageView) findViewById(R.id.picAttachedIcon);
        Log.d(LogTags.FEEDITEM, "Created! Contents: " + details);
    }
    private void setFields(PostData pd){
        if(pd.proPicURL != null)
            proPicURL = pd.proPicURL;
        if(pd.mediaURL == null)
            picsAttachedIcon.setVisibility(View.INVISIBLE);
        else
            mediaURL = pd.mediaURL;
        if(pd.networksShared != null)
            networksShared = pd.networksShared;
        title = pd.title;
        titleTV.setText(title);

        timeSincePost = PostData.timeSincePosted(pd.timestamp);
        timeSincePostTV.setText(timeSincePost);

        details = pd.details;
        detailsTV.setText(briefDetails());
        lat = pd.latitude;
        lon = pd.longitude;
    }

    public String briefDetails(){
        if(details.length() > 140)
            return details.substring(0, 139);
        else
            return details;
    }
}
