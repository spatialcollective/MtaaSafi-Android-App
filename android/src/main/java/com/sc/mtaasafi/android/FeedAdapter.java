package com.sc.mtaasafi.android;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Agree on 9/5/2014.
 */
public class FeedAdapter extends BaseAdapter {
    List<FeedItem> posts;
    Context context;
    private class PostHolder{
//        String content;
        TextView contentTV;
        PostHolder(View convertView, String content){
            contentTV = (TextView) convertView.findViewById(R.id.feedText);
            contentTV.setText(content);
        }
    }
    FeedAdapter(Context context){
        posts = new ArrayList<FeedItem>();
        this.context = context;
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
            convertView = new FeedItem(context, item.content());
            postHolder = new PostHolder(convertView, item.content());
            convertView.setTag(postHolder);
        }
        else{
            postHolder = (PostHolder) convertView.getTag();
        }
        postHolder.contentTV.setText(item.content());
        convertView.setTag(postHolder);
        return (FeedItem) convertView;
    }

    public void updateFeed(List<FeedItem> newFeed){
        posts = newFeed;
        notifyDataSetChanged();
    }
}
