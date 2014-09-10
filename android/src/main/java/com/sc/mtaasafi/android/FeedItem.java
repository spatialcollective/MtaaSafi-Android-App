package com.sc.mtaasafi.android;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Created by Agree on 9/5/2014.
 */
public class FeedItem extends RelativeLayout {
    private String content;
    private LayoutInflater inflater;
    private TextView textView;

    public FeedItem(Context context){
        super(context);
        setUp();
    }

    public FeedItem(Context context, String content) {
        super(context);
        this.content = content;
    }

    public void setContent(String content){
        this.content = content;
    }
    private void setUp(){
        inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.feed_item, this, true);
        textView = (TextView) findViewById(R.id.feedText);

    }
}
