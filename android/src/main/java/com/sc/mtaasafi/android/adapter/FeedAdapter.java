package com.sc.mtaasafi.android.adapter;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.androidquery.callback.ImageOptions;
import com.sc.mtaasafi.android.listitem.FeedItem;
import com.sc.mtaasafi.android.LogTags;
import com.sc.mtaasafi.android.MainActivity;
import com.sc.mtaasafi.android.NewsFeedFragment;
import com.sc.mtaasafi.android.PostData;
import com.sc.mtaasafi.android.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Agree on 9/5/2014.
 */
public class FeedAdapter extends BaseAdapter {
    List<FeedItem> posts;
    Context context;
    AQuery aq;
    ListView mListView;
    int index;
    int top;
    NewsFeedFragment mFragment;
    private class PostHolder{
//        String content;
        RelativeLayout view;
        TextView contentTV;
        TextView userNameTV;
        TextView timeSinceTV;
        ImageView picsAttachedIcon;
        ImageView bottomLine;
        ImageView proPic;
        ImageView sharedIcon;
        int position;
        PostHolder(FeedItem fi, int pos){
            position = pos;
            view = (RelativeLayout) fi.findViewById(R.id.feed_item);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    MainActivity mA = (MainActivity) context;
                    FeedItem fi = (FeedItem)getItem(position);
                    Log.e(LogTags.FEEDADAPTER, "CLICKED FEED ITEM!!!!");
                    mFragment.saveListPosition();
                    mA.goToDetailView(fi.toPostData());
                }
            });
            contentTV = (TextView) fi.findViewById(R.id.postText);
            contentTV.setText(fi.content);

            userNameTV = (TextView) fi.findViewById(R.id.userName);
            userNameTV.setText(fi.userName);

            timeSinceTV = (TextView) fi.findViewById(R.id.timestamp);
            timeSinceTV.setText(fi.timeSincePost);

            picsAttachedIcon = (ImageView) fi.findViewById(R.id.picAttachedIcon);
            sharedIcon = (ImageView) fi.findViewById(R.id.sharedIcon);
            proPic = (ImageView) fi.findViewById(R.id.proPic);
            if(fi.mediaURL == null || fi.mediaURL.equals("") || fi.mediaURL.equals("null"))
                picsAttachedIcon.setVisibility(View.INVISIBLE);

            if(fi.networksShared == null)
                sharedIcon.setVisibility(View.INVISIBLE);

            if(fi.proPicURL != null && !fi.proPicURL.equals(""))
                aq.id(proPic).image(fi.proPicURL);
        }
    }
    FeedAdapter(Context context, NewsFeedFragment mFragment){
        this.mFragment = mFragment;
        posts = new ArrayList<FeedItem>();
        this.context = context;
        aq = new AQuery(context);
    }

    @Override
    public int getCount() { return posts.size(); }

    @Override
    public Object getItem(int i) {
        if(posts !=null)
            return posts.get(i);
        else
            return null;
    }

    @Override
    public long getItemId(int i) {
        return posts.get(i).getId();
    }

    @Override
    public FeedItem getView(int i, View convertView, ViewGroup viewGroup) {
        PostHolder postHolder;
        FeedItem item = (FeedItem) getItem(i);
        if(convertView == null){
            convertView = item;
            postHolder = new PostHolder((FeedItem) convertView, i);
            convertView.setTag(postHolder);
        }
        else{
            postHolder = (PostHolder) convertView.getTag();
        }
        // set the dynamic data for each feed item.
        postHolder.contentTV.setText(item.content);
        postHolder.timeSinceTV.setText(item.timeSincePost);
        ImageOptions options = new ImageOptions();
//        options.round = 20;
        aq.id(postHolder.proPic).image(item.proPicURL);
        if(item.mediaURL == null || item.mediaURL.equals("") || item.mediaURL.equals("null"))
            postHolder.picsAttachedIcon.setVisibility(View.INVISIBLE);
        else
            postHolder.picsAttachedIcon.setVisibility(View.VISIBLE);
        postHolder.position = i;
        convertView.setTag(postHolder);
        return (FeedItem) convertView;
    }

    public void updateFeed(List<PostData> newFeed){
        List<FeedItem> items = new ArrayList<FeedItem>();
        for(PostData postData : newFeed){
            FeedItem fi = new FeedItem(context, postData);
            items.add(fi);
        }
        posts = items;
        notifyDataSetChanged();
    }
}
