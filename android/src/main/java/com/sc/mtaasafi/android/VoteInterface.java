package com.sc.mtaasafi.android;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by Agree on 10/10/2014.
 */
public class VoteInterface extends LinearLayout {
    TextView voteCountTV;
    ImageButton upvote;
    public VoteInterface(Context context, AttributeSet attrs) {
        super(context, attrs);
        voteCountTV = (TextView) findViewById(R.id.upvoteCount);
        upvote = (ImageButton) findViewById(R.id.upvote);
        upvote.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                // tell the freakin onVoteListener that I voted
            }
        });
    }

//    public void update(Report report){
//        if(report.iUpvoted)
//            upvote.setImageResource(R.drawable.button_upvote_clicked);
//        else
//            upvote.setImageResource(R.drawable.button_upvote_unclicked);
//    }
}
