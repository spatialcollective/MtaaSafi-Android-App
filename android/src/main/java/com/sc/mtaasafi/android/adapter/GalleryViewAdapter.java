package com.sc.mtaasafi.android.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.androidquery.AQuery;
import com.sc.mtaasafi.android.NewReportFragment;
import com.sc.mtaasafi.android.R;
import com.sc.mtaasafi.android.listitem.FeedItem;
import com.sc.mtaasafi.android.listitem.GalleryThumb;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Agree on 9/24/2014.
 */

public class GalleryViewAdapter extends BaseAdapter {
    private Context mContext;
    private NewReportFragment mFragment;
    private List selectedItems;
    private AQuery aq;
    private List<GalleryThumb> thumbnails;
    private class ThumbHolder{
        RelativeLayout view;
        ImageView thumb, selectedIcon;
        int id;
        ThumbHolder(GalleryThumb gt, int position){
            view = (RelativeLayout) gt.findViewById(R.id.feed_item);
            selectedIcon = (ImageView) gt.findViewById(R.id.selected_icon);
            thumb = (ImageView) gt.findViewById(R.id.thumb);
            id = position;
            if(selectedItems.contains(id))
                selectedIcon.setVisibility(View.INVISIBLE);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // if the item has been selected, unselect it:
                    // remove the item from the list of selected items
                    // remove the pic from the array of selected pics in the fragment
                    // make the selected icon invisible
                    if(selectedItems.contains(id)){
                        mFragment.removePic(id);
                        selectedIcon.setVisibility(View.INVISIBLE);
                        if(selectedItems.contains(id)){
                            selectedItems.remove(id);
                        }
                    }
                    // else select it:
                    // add the item to the list of selected items
                    // add the pic to the array of selected pics in the fragment
                    // make the selected icon visible
                    else{
                        mFragment.addPic(id, null);
                        selectedIcon.setVisibility(View.INVISIBLE);
                        if(!selectedItems.contains(id))
                            selectedItems.add(id);
                    }
                    // if the pic has been selected remove it from the list of attached pics
                    // else add it to the list of attached pics.
                }
            });
        }
    }

    public GalleryViewAdapter(Context c, NewReportFragment nrf) {
        mContext = c;
        mFragment = nrf;
        selectedItems = new ArrayList();
        aq = new AQuery(mFragment.getActivity());
        thumbnails = new ArrayList<GalleryThumb>();
    }

    public int getCount() {
        return mThumbIds.length;
    }

    public Object getItem(int position) {
        return thumbnails.get(position);
    }

    public long getItemId(int position) {
        return thumbnails.get(position).getId();
    }

    @Override
    // create a new ImageView for each item referenced by the Adapter
    public GalleryThumb getView(int position, View convertView, ViewGroup parent) {
        GalleryThumb gt = (GalleryThumb) convertView;
        ThumbHolder holder = new ThumbHolder((GalleryThumb) convertView,  position);
        if (convertView == null) {
            gt = new GalleryThumb(mContext);
            gt.setLayoutParams(new GridView.LayoutParams(85, 85));
            gt.thumbnail.setScaleType(ImageView.ScaleType.CENTER_CROP);
            gt.setPadding(5, 5, 5, 5);
        } else {
            holder = (ThumbHolder) convertView.getTag();
        }
        // Set the image to the corresponding part
        // set the selected icon to true or not or w/e
        // holder.thumb.setImageResource(mThumbIds[position]);
        aq.id(holder.thumb).image(R.drawable.pic_placeholder);
        if(selectedItems.contains(position))
            holder.selectedIcon.setVisibility(View.VISIBLE);
        else
            holder.selectedIcon.setVisibility(View.INVISIBLE);
        convertView.setTag(holder);

        return gt;
    }

    // references to our images
    private Integer[] mThumbIds = {
//            R.drawable.sample_2, R.drawable.sample_3,
//            R.drawable.sample_4, R.drawable.sample_5,
//            R.drawable.sample_6, R.drawable.sample_7,
//            R.drawable.sample_0, R.drawable.sample_1,
//            R.drawable.sample_2, R.drawable.sample_3,
//            R.drawable.sample_4, R.drawable.sample_5,
//            R.drawable.sample_6, R.drawable.sample_7,
//            R.drawable.sample_0, R.drawable.sample_1,
//            R.drawable.sample_2, R.drawable.sample_3,
//            R.drawable.sample_4, R.drawable.sample_5,
//            R.drawable.sample_6, R.drawable.sample_7
    };
}
