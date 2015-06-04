package com.sc.mtaa_safi.feed;

import android.content.Context;
import android.graphics.Typeface;
import android.location.Location;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.CompoundButton;

import com.sc.mtaa_safi.R;
import com.sc.mtaa_safi.SystemUtils.Utils;
import com.sc.mtaa_safi.Vote;
import com.sc.mtaa_safi.common.AsyncUploader;

import java.util.ArrayList;

public class VoteButton extends CompoundButton {
    public Uri mReportUri;
    public int mServerId;
    public boolean enabled = true;

    public VoteButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setOnCheckedChangeListener(new MyListener());
        this.setOnTouchListener(new MyTouchListener());
    }

    private class MyTouchListener implements OnTouchListener {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                Animation scaleUp = new ScaleAnimation(1.0f, 1.2f, 1.0f, 1.2f,
                        Animation.RELATIVE_TO_SELF, .5f,
                        Animation.RELATIVE_TO_SELF, .5f);
                scaleUp.setDuration(200);
                scaleUp.setInterpolator(new AccelerateInterpolator());
                v.startAnimation(scaleUp);
            } else {
                Animation scaleDown = new ScaleAnimation(1.2f, 1.0f, 1.2f, 1.0f,
                        Animation.RELATIVE_TO_SELF,.5f,
                        Animation.RELATIVE_TO_SELF, .5f);
                scaleDown.setDuration(201);
                scaleDown.setInterpolator(new AccelerateInterpolator());
                v.startAnimation(scaleDown);
            }
            return false;
        }
    }

    private class MyListener implements CompoundButton.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(CompoundButton view, boolean isChecked) {
            if (enabled && isChecked) {
                enabled = false;
                int voteCount = Integer.parseInt(getText().toString()) + 1;
                Vote vote = new Vote(getContext(), mReportUri, mServerId, voteCount);
                Location location = Utils.getLocation(getContext());
                if (location != null)
                    vote.setLocation(location);
                new AsyncUploader(getContext(), vote).execute();
                setToUp(voteCount);
            } else if (!enabled && !isChecked) {
                setChecked(true);
            }
        }
    }

    public void setCheckedState(boolean upvoted, int upvoteCount, ArrayList<Integer> upvoteList) {
        this.enabled = !upvoted;
        if (upvoteList != null && !upvoteList.isEmpty() && upvoteList.contains(this.mServerId) && this.enabled) {
            setToUp(upvoteCount + 1);
            this.enabled = false;
        } else
            this.setText(Integer.toString(upvoteCount));
        this.setChecked(!this.enabled);
    }

    public void setToUp(int count) {
        setText(count + "");
        setTextColor(getResources().getColor(R.color.mtaa_safi_blue));
        setTypeface(Typeface.DEFAULT_BOLD);
    }

    public void setToDown() {
        int voteCount = Integer.parseInt(getText().toString()) - 1;
        setText(voteCount + "");
        setTextColor(getResources().getColor(R.color.mediumGrey));
        setTypeface(Typeface.DEFAULT);
    }

    @Override
    public void onFinishInflate() {
        if (isChecked()) setToUp(Integer.parseInt(getText().toString()));
        else setToDown();
    }
}