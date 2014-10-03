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
import com.sc.mtaasafi.android.Report;
import com.sc.mtaasafi.android.listitem.FeedItemView;
import com.sc.mtaasafi.android.LogTags;
import com.sc.mtaasafi.android.MainActivity;
import com.sc.mtaasafi.android.NewsFeedFragment;
import com.sc.mtaasafi.android.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Agree on 9/5/2014.
 */
public class FeedAdapter extends BaseAdapter {
    List<FeedItemView> posts;
    AQuery aq;
    ListView mListView;
    int index;
    int top;
    NewsFeedFragment mFragment;
    Context context;
    
    public List<Report> mReports = new ArrayList<Report>();

    public FeedAdapter(Context context, NewsFeedFragment mFragment){
        this.mFragment = mFragment;
        posts = new ArrayList<FeedItemView>();
        this.context = context;
        aq = new AQuery(context);
    }

    @Override
    public int getCount() { return posts.size(); }

    @Override
    public Object getItem(int i) {
        if (posts != null)
            return posts.get(i);
        else
            return null;
    }

    @Override
    public long getItemId (int i) { return i; } // return posts.get(i).getId();

    @Override
    public FeedItemView getView(int postion, View convertView, ViewGroup viewGroup) {
        FeedItemView reportFeedView;
        Report report = mReports.get(postion);
        if (convertView == null) {
            reportFeedView = new FeedItemView(context, report, postion);
        } else {
            reportFeedView = (FeedItemView) convertView;
        }

        reportFeedView.setViewData(report);
        reportFeedView.position = postion;
        reportFeedView.setTag(report);
        return reportFeedView;
    }

    public void updateItems(List<Report> allReports) {
        List<FeedItemView> items = new ArrayList<FeedItemView>();
        for (int i = 0; i < allReports.size(); i++) {
            Report report = allReports.get(i);
            Log.d("updateFeed", "report title is " + report.title);
            FeedItemView fItemView = new FeedItemView(context, report, i);
            items.add(fItemView);
        }
        mReports = allReports;
        posts = items;
        notifyDataSetChanged();
    }
}
