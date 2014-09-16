package com.sc.mtaasafi.android;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
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
        TextView userNameTV;
        TextView timeSinceTV;
        ImageView picsAttachedIcon;
        ImageView bottomLine;
        ImageView userPic;
        ImageView sharedIcon;
        PostHolder(View convertView, String content, String username,
                   boolean picsAttached, boolean shared){
            contentTV = (TextView) convertView.findViewById(R.id.postText);
            contentTV.setText(content);

            userNameTV = (TextView) convertView.findViewById(R.id.userName);
            userNameTV.setText(username);

            timeSinceTV = (TextView) convertView.findViewById(R.id.time_since_posted);
            timeSinceTV.setText("5h");

            picsAttachedIcon = (ImageView) convertView.findViewById(R.id.picAttachedIcon);
            sharedIcon = (ImageView) convertView.findViewById(R.id.shared_icon);
            if(!picsAttached){
                picsAttachedIcon.setVisibility(View.INVISIBLE);
            }
            if(!shared){
                sharedIcon.setVisibility(View.INVISIBLE);
            }
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
            postHolder = new PostHolder(convertView,
                                        item.content(),
                                        "Agree",
                                        false,
                                        false);
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
