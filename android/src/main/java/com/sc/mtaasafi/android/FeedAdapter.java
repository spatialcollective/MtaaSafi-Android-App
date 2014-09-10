package com.sc.mtaasafi.android;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Agree on 9/5/2014.
 */
public class FeedAdapter extends BaseAdapter {
    List<FeedItem> posts;
    Context context;
    private class PostHolder{
        String content;
    }
    FeedAdapter(Context context){
        posts = new ArrayList<FeedItem>();
        this.context = context;
    }

    @Override
    public int getCount() {
        if(posts != null)
            return posts.size();
        else
            return 0;
    }

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
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        PostHolder postHolder;
        if(convertView == null){
            convertView = new FeedItem(context);
            postHolder = new PostHolder();
            convertView.setTag(postHolder);
        }
        else{
            postHolder = (PostHolder) convertView.getTag();
        }
        postHolder.content = (String) getItem(i);
        convertView.setTag(postHolder);
        return (View) convertView;
    }

    public void updateFeed(List<FeedItem> newFeed){
        posts = newFeed;
        notifyDataSetChanged();
    }
}
