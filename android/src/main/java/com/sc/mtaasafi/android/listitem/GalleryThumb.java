package com.sc.mtaasafi.android.listitem;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.sc.mtaasafi.android.R;

/**
 * Created by Agree on 9/24/2014.
 */
public class GalleryThumb extends RelativeLayout {
    public ImageView thumbnail, itemSelected;
    public GalleryThumb(Context context) {
        super(context);
        setUp();
    }
    private void setUp(){
        LayoutInflater inflater =
                (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.gallery_thumb, this, true);
        thumbnail = (ImageView) findViewById(R.id.thumb);
        itemSelected = (ImageView) findViewById(R.id.selected_icon);
        itemSelected.setVisibility(View.INVISIBLE);
        this.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(itemSelected.getVisibility() == View.INVISIBLE){
                    itemSelected.setVisibility(View.VISIBLE);
                }
                else{
                    itemSelected.setVisibility(View.INVISIBLE);
                }
            }
        });
    }
}
